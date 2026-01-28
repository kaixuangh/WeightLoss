package com.kaixuan.weightloss

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kaixuan.weightloss.data.WeightDatabase
import com.kaixuan.weightloss.data.WeightRecord
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    private val dateKeyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    private val _records = MutableStateFlow<List<WeightRecord>>(emptyList())
    val records: StateFlow<List<WeightRecord>> = _records.asStateFlow()

    private val _selectedRange = MutableStateFlow(TimeRange.MONTH)
    val selectedRange: StateFlow<TimeRange> = _selectedRange.asStateFlow()

    init {
        loadRecords(TimeRange.MONTH)
    }

    fun addRecord(weight: Float) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val dateKey = dateKeyFormat.format(Date(now))

            // 检查当天是否已有记录
            val existingRecord = dao.getRecordByDateKey(dateKey)
            if (existingRecord != null) {
                // 更新现有记录
                dao.updateByDateKey(dateKey, weight, now)
            } else {
                // 插入新记录
                val record = WeightRecord(
                    date = now,
                    dateKey = dateKey,
                    weight = weight
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
}
