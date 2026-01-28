package com.kaixuan.weightloss.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WeightDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: WeightRecord)

    @Query("SELECT * FROM weight_records WHERE dateKey = :dateKey LIMIT 1")
    suspend fun getRecordByDateKey(dateKey: String): WeightRecord?

    @Query("UPDATE weight_records SET weight = :weight, date = :date WHERE dateKey = :dateKey")
    suspend fun updateByDateKey(dateKey: String, weight: Float, date: Long)

    @Query("SELECT * FROM weight_records WHERE date >= :startDate ORDER BY date ASC")
    fun getRecordsSince(startDate: Long): Flow<List<WeightRecord>>

    @Query("SELECT * FROM weight_records ORDER BY date DESC LIMIT 1")
    fun getLatestRecord(): Flow<WeightRecord?>

    @Query("DELETE FROM weight_records WHERE id = :id")
    suspend fun delete(id: Long)
}
