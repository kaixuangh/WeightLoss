package com.kaixuan.weightloss

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kaixuan.weightloss.data.UserSettings
import com.kaixuan.weightloss.data.UserSettingsRepository
import com.kaixuan.weightloss.data.WeightDatabase
import com.kaixuan.weightloss.data.WeightRecord
import com.kaixuan.weightloss.data.WeightUnit
import com.kaixuan.weightloss.reminder.ReminderWorker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
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
    private val dao = WeightDatabase.getDatabase(application).weightDao()
    private val settingsRepo = UserSettingsRepository(application)
    private val dateKeyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    private val _records = MutableStateFlow<List<WeightRecord>>(emptyList())
    val records: StateFlow<List<WeightRecord>> = _records.asStateFlow()

    private val _selectedRange = MutableStateFlow(TimeRange.MONTH)
    val selectedRange: StateFlow<TimeRange> = _selectedRange.asStateFlow()

    private val _settings = MutableStateFlow(UserSettings())
    val settings: StateFlow<UserSettings> = _settings.asStateFlow()

    private val _latestWeight = MutableStateFlow<Float?>(null)
    val latestWeight: StateFlow<Float?> = _latestWeight.asStateFlow()

    init {
        loadRecords(TimeRange.MONTH)
        loadSettings()
        loadLatestWeight()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            settingsRepo.settings.collect { settings ->
                _settings.value = settings
            }
        }
    }

    private fun loadLatestWeight() {
        viewModelScope.launch {
            dao.getLatestRecord().collect { record ->
                _latestWeight.value = record?.weight
            }
        }
    }

    fun addRecord(weight: Float) {
        viewModelScope.launch {
            // 根据单位转换为 kg 存储
            val weightInKg = weight / _settings.value.weightUnit.factor
            val now = System.currentTimeMillis()
            val dateKey = dateKeyFormat.format(Date(now))

            val existingRecord = dao.getRecordByDateKey(dateKey)
            if (existingRecord != null) {
                dao.updateByDateKey(dateKey, weightInKg, now)
            } else {
                val record = WeightRecord(
                    date = now,
                    dateKey = dateKey,
                    weight = weightInKg
                )
                dao.insert(record)
            }
            loadRecords(_selectedRange.value)
        }
    }

    fun selectTimeRange(range: TimeRange) {
        _selectedRange.value = range
        loadRecords(range)
    }

    private fun loadRecords(range: TimeRange) {
        viewModelScope.launch {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, -range.days)
            val startDate = calendar.timeInMillis

            dao.getRecordsSince(startDate).collect { records ->
                _records.value = records
            }
        }
    }

    fun updateTargetWeight(weight: Float) {
        viewModelScope.launch {
            // 转换为 kg 存储
            val weightInKg = weight / _settings.value.weightUnit.factor
            settingsRepo.updateTargetWeight(weightInKg)
        }
    }

    fun updateHeight(height: Float) {
        viewModelScope.launch {
            settingsRepo.updateHeight(height)
        }
    }

    fun updateWeightUnit(unit: WeightUnit) {
        viewModelScope.launch {
            settingsRepo.updateWeightUnit(unit)
        }
    }

    fun updateReminder(enabled: Boolean, hour: Int, minute: Int) {
        viewModelScope.launch {
            settingsRepo.updateReminder(enabled, hour, minute)
            if (enabled) {
                ReminderWorker.schedule(getApplication(), hour, minute)
            } else {
                ReminderWorker.cancel(getApplication())
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
