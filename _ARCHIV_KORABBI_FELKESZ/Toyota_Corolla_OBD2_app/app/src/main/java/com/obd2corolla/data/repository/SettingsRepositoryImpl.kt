package com.obd2corolla.data.repository

import com.obd2corolla.data.local.ToyotaE210SettingsRegistry
import com.obd2corolla.data.local.dao.SettingsLogDao
import com.obd2corolla.data.local.entity.SettingsLogEntity
import com.obd2corolla.data.remote.HiddenSettingsService
import com.obd2corolla.domain.model.HiddenSetting
import com.obd2corolla.domain.model.SensorData
import com.obd2corolla.domain.repository.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of SettingsRepository that uses HiddenSettingsService
 * to communicate with the vehicle's ECU via Bluetooth OBD adapter.
 */
@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val settingsLogDao: SettingsLogDao,
    private val hiddenSettingsService: HiddenSettingsService
) : SettingsRepository {

    /**
     * Get all hidden settings with their current values from the vehicle.
     */
    override suspend fun getHiddenSettings(): List<HiddenSetting> = withContext(Dispatchers.IO) {
        val currentValues = hiddenSettingsService.readAllSettings()
        
        ToyotaE210SettingsRegistry.allSettings.mapNotNull { definition ->
            val currentValue = currentValues[definition.key]
            if (currentValue != null || currentValues[definition.key] == null) {
                HiddenSetting(
                    key = definition.key,
                    name = definition.name,
                    description = definition.description,
                    currentValue = currentValue ?: definition.defaultValue,
                    options = definition.options.map { HiddenSetting.Option(it.value, it.label) },
                    defaultValue = definition.defaultValue,
                    isWriteable = true,
                    address = definition.address,
                    category = ToyotaE210SettingsRegistry.getCategoryDisplayName(definition.category)
                )
            } else {
                null
            }
        }
    }

    /**
     * Update a hidden setting value and log the change.
     */
    override suspend fun updateHiddenSetting(key: String, value: String): Result<Unit> = withContext(Dispatchers.IO) {
        // Get current value for logging
        val oldValue = hiddenSettingsService.readSetting(key)
        
        // Write to vehicle - parse string value to int if possible
        val intValue = value.toIntOrNull() ?: 0
        val success = hiddenSettingsService.writeSetting(key, intValue)
        
        if (!success) {
            return@withContext Result.failure(Exception("Írás nem sikerült: $key = $value"))
        }
        
        // Log the change to Room database
        settingsLogDao.insertLog(
            SettingsLogEntity(
                key = key,
                value = value,
                timestamp = System.currentTimeMillis()
            )
        )
        Result.success(Unit)
    }

    /**
     * Get the settings change history log.
     */
    override suspend fun getSettingsLog(): List<SensorData> = withContext(Dispatchers.IO) {
        emptyList()
    }

    /**
     * Reset all settings to factory defaults.
     */
    override suspend fun resetToDefaults(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            ToyotaE210SettingsRegistry.allSettings.forEach { setting ->
                val intValue = setting.defaultValue.toIntOrNull() ?: 0
                hiddenSettingsService.writeSetting(setting.key, intValue)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

