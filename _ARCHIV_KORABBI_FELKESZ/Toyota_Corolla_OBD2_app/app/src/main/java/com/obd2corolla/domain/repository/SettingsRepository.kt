package com.obd2corolla.domain.repository

import com.obd2corolla.domain.model.HiddenSetting
import com.obd2corolla.domain.model.SensorData

interface SettingsRepository {
    suspend fun getHiddenSettings(): List<HiddenSetting>
    suspend fun updateHiddenSetting(key: String, value: String): Result<Unit>
    suspend fun getSettingsLog(): List<SensorData>
    suspend fun resetToDefaults(): Result<Unit>
}

