package com.kaixuan.weightloss

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kaixuan.weightloss.api.ApiClient
import com.kaixuan.weightloss.api.ApiRepository
import com.kaixuan.weightloss.api.ApiResult
import com.kaixuan.weightloss.api.WeightRecordData
import com.kaixuan.weightloss.data.UserSettings
import com.kaixuan.weightloss.data.WeightUnit
import com.kaixuan.weightloss.reminder.ReminderWorker
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class TimeRange(val days: Int, val label: String) {
    WEEK(7, "一周"),
    MONTH(30, "一月"),
    QUARTER(90, "一季度"),
    HALF_YEAR(180, "半年"),
    YEAR(365, "一年")
}

class WeightViewModel(application: Application) : AndroidViewModel(application) {
    private val apiRepository = ApiRepository()
    private val dateKeyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    private val _records = MutableStateFlow<List<WeightRecordData>>(emptyList())
    val records: StateFlow<List<WeightRecordData>> = _records.asStateFlow()

    private val _selectedRange = MutableStateFlow(TimeRange.MONTH)
    val selectedRange: StateFlow<TimeRange> = _selectedRange.asStateFlow()

    private val _settings = MutableStateFlow(UserSettings())
    val settings: StateFlow<UserSettings> = _settings.asStateFlow()

    private val _latestWeight = MutableStateFlow<Float?>(null)
    val latestWeight: StateFlow<Float?> = _latestWeight.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _loginSuccess = MutableSharedFlow<Unit>()
    val loginSuccess = _loginSuccess.asSharedFlow()

    init {
        ApiClient.init(application)
        checkLoginStatus()
    }

    private fun checkLoginStatus() {
        viewModelScope.launch {
            _isLoggedIn.value = ApiClient.isLoggedIn()
            if (_isLoggedIn.value) {
                loadData()
            }
        }
    }

    fun loadData() {
        loadRecords(TimeRange.MONTH)
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            when (val result = apiRepository.getSettings()) {
                is ApiResult.Success -> {
                    val data = result.data
                    _settings.value = UserSettings(
                        targetWeight = data.targetWeight ?: 0f,
                        height = data.height ?: 0f,
                        weightUnit = when (data.weightUnit) {
                            "JIN" -> WeightUnit.JIN
                            else -> WeightUnit.KG
                        },
                        reminderEnabled = data.reminderEnabled ?: false,
                        reminderHour = data.reminderTime?.split(":")?.getOrNull(0)?.toIntOrNull() ?: 8,
                        reminderMinute = data.reminderTime?.split(":")?.getOrNull(1)?.toIntOrNull() ?: 0
                    )
                }
                is ApiResult.Error -> {
                    // 使用默认设置
                }
            }
        }
    }

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            when (val result = apiRepository.login(username, password)) {
                is ApiResult.Success -> {
                    _isLoggedIn.value = true
                    loadData()
                    _loginSuccess.emit(Unit)
                }
                is ApiResult.Error -> {
                    _errorMessage.value = result.message
                }
            }
            _isLoading.value = false
        }
    }

    fun register(username: String, password: String, confirmPassword: String) {
        viewModelScope.launch {
            if (password != confirmPassword) {
                _errorMessage.value = "两次密码不一致"
                return@launch
            }

            _isLoading.value = true
            _errorMessage.value = null

            when (val result = apiRepository.register(username, password, confirmPassword)) {
                is ApiResult.Success -> {
                    _isLoggedIn.value = true
                    loadData()
                    _loginSuccess.emit(Unit)
                }
                is ApiResult.Error -> {
                    _errorMessage.value = result.message
                }
            }
            _isLoading.value = false
        }
    }

    fun logout() {
        viewModelScope.launch {
            apiRepository.logout()
            _isLoggedIn.value = false
            _records.value = emptyList()
            _settings.value = UserSettings()
            _latestWeight.value = null
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun addRecord(weight: Float) {
        viewModelScope.launch {
            // 根据单位转换为 kg 存储
            val weightInKg = weight / _settings.value.weightUnit.factor
            val dateKey = dateKeyFormat.format(Date())

            when (val result = apiRepository.addWeightRecord(dateKey, weightInKg)) {
                is ApiResult.Success -> {
                    loadRecords(_selectedRange.value)
                }
                is ApiResult.Error -> {
                    _errorMessage.value = result.message
                }
            }
        }
    }

    fun selectTimeRange(range: TimeRange) {
        _selectedRange.value = range
        loadRecords(range)
    }

    private fun loadRecords(range: TimeRange) {
        viewModelScope.launch {
            when (val result = apiRepository.getWeightRecords(days = range.days)) {
                is ApiResult.Success -> {
                    _records.value = result.data.records
                    _latestWeight.value = result.data.statistics?.latestWeight
                }
                is ApiResult.Error -> {
                    // 保持现有数据
                }
            }
        }
    }

    fun updateTargetWeight(weight: Float) {
        viewModelScope.launch {
            // 转换为 kg 存储
            val weightInKg = weight / _settings.value.weightUnit.factor
            when (apiRepository.updateSettings(targetWeight = weightInKg)) {
                is ApiResult.Success -> {
                    _settings.value = _settings.value.copy(targetWeight = weightInKg)
                }
                is ApiResult.Error -> {}
            }
        }
    }

    fun updateHeight(height: Float) {
        viewModelScope.launch {
            when (apiRepository.updateSettings(height = height)) {
                is ApiResult.Success -> {
                    _settings.value = _settings.value.copy(height = height)
                }
                is ApiResult.Error -> {}
            }
        }
    }

    fun updateWeightUnit(unit: WeightUnit) {
        viewModelScope.launch {
            when (apiRepository.updateSettings(weightUnit = unit.name)) {
                is ApiResult.Success -> {
                    _settings.value = _settings.value.copy(weightUnit = unit)
                }
                is ApiResult.Error -> {}
            }
        }
    }

    fun updateReminder(enabled: Boolean, hour: Int, minute: Int) {
        viewModelScope.launch {
            val reminderTime = String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
            when (apiRepository.updateSettings(reminderEnabled = enabled, reminderTime = reminderTime)) {
                is ApiResult.Success -> {
                    _settings.value = _settings.value.copy(
                        reminderEnabled = enabled,
                        reminderHour = hour,
                        reminderMinute = minute
                    )
                    if (enabled) {
                        ReminderWorker.schedule(getApplication(), hour, minute)
                    } else {
                        ReminderWorker.cancel(getApplication())
                    }
                }
                is ApiResult.Error -> {}
            }
        }
    }

    // 计算 BMI
    fun calculateBMI(weightKg: Float, heightCm: Float): Float {
        if (heightCm <= 0) return 0f
        val heightM = heightCm / 100
        return weightKg / (heightM * heightM)
    }

    fun getBMIStatus(bmi: Float): String {
        return when {
            bmi <= 0 -> ""
            bmi < 18.5 -> "偏瘦"
            bmi < 24 -> "正常"
            bmi < 28 -> "偏胖"
            else -> "肥胖"
        }
    }

    // 转换体重显示
    fun convertWeight(weightKg: Float, unit: WeightUnit): Float {
        return weightKg * unit.factor
    }
}
