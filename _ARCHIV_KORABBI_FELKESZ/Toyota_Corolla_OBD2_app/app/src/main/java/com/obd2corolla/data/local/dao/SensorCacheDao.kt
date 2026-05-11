package com.obd2corolla.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.obd2corolla.data.local.entity.SensorCacheEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SensorCacheDao {
    @Query("SELECT * FROM sensor_cache")
    fun getAllSensors(): Flow<List<SensorCacheEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSensor(sensor: SensorCacheEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(sensors: List<SensorCacheEntity>)

    @Query("DELETE FROM sensor_cache")
    suspend fun deleteAll()

    @Query("SELECT * FROM sensor_cache WHERE pid = :pid ORDER BY timestamp DESC LIMIT :limit")
    fun getByPid(pid: String, limit: Int): List<SensorCacheEntity>
}