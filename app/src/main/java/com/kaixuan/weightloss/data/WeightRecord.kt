package com.kaixuan.weightloss.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "weight_records",
    indices = [Index(value = ["dateKey"], unique = true)]
)
data class WeightRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: Long, // 使用时间戳存储日期
    val dateKey: String, // 日期键，格式为 yyyy-MM-dd，用于保证每天只有一条记录
    val weight: Float // 体重（kg）
)
