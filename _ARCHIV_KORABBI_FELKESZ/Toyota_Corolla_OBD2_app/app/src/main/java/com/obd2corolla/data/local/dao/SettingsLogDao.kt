package com.obd2corolla.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.obd2corolla.data.local.entity.SettingsLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingsLogDao {
    @Query("SELECT * FROM settings_log ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<SettingsLogEntity>>

    @Insert
    suspend fun insertLog(log: SettingsLogEntity)

    @Query("DELETE FROM settings_log")
    suspend fun deleteAll()
}