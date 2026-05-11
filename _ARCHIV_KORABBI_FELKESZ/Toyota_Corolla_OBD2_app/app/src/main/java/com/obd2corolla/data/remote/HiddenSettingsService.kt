package com.obd2corolla.data.remote

import com.obd2corolla.data.local.ToyotaE210SettingsRegistry
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for reading and writing Toyota hidden/customization settings.
 * These are ECU-specific settings that control vehicle customization options
 * not accessible through standard OBD-II modes.
 * 
 * Uses Toyota-specific CAN communication via the ELM327 adapter with
 * extended addressing (ATSH 7E4 for body ECU).
 * 
 * @param bluetoothManager The Bluetooth manager for CAN communication
 */
@Singleton
class HiddenSettingsService @Inject constructor(
    private val bluetoothManager: BluetoothManager
) {
    /**
     * Read all hidden settings from the vehicle.
     * Returns map of key → current value (or null if read failed)
     */
    suspend fun readAllSettings(): Map<String, Int?> = withContext(kotlinx.coroutines.Dispatchers.IO) {
        ToyotaE210SettingsRegistry.allSettings.associate { setting ->
            setting.key to readSetting(setting.key)
        }
    }
    
    /**
     * Read a single setting's current value from the vehicle.
     * Returns the current value, or null if not supported/failed.
     */
    suspend fun readSetting(key: String): Int? = withContext(kotlinx.coroutines.Dispatchers.IO) {
        val definition = ToyotaE210SettingsRegistry.getSetting(key) ?: return@withContext null
        bluetoothManager.readToyotaRegister(
            ToyotaE210SettingsRegistry.BODY_ECU_ADDRESS, 
            definition.address
        )
    }
    
    /**
     * Write a setting value to the vehicle.
     * Returns true if successful, false otherwise.
     * @throws Elm327V15Incompatible if adapter is v1.5 (doesn't support writes)
     */
    suspend fun writeSetting(key: String, value: Int): Boolean = withContext(kotlinx.coroutines.Dispatchers.IO) {
        val definition = ToyotaE210SettingsRegistry.getSetting(key) ?: return@withContext false
        
        // Check if v1.5 — writes won't work on v1.5 adapters
        if (bluetoothManager.isV15) {
            throw Elm327V15Incompatible(
                "Ez az adapter nem támogatja az írási műveleteket. " +
                "Kérjük, használj ELM327 v2.2 vagy újabb verziót (pl. OBDLink MX+, Carista v2)."
            )
        }
        
        bluetoothManager.sendToyotaWrite(
            ToyotaE210SettingsRegistry.BODY_ECU_ADDRESS,
            definition.address,
            value
        )
        
        // Verify the write by reading back
        val readBack = readSetting(key)
        readBack == value
    }
    
    /**
     * Get all settings with their definitions (without current values from car)
     */
    fun getAllSettingDefinitions(): List<ToyotaE210SettingsRegistry.SettingDefinition> {
        return ToyotaE210SettingsRegistry.allSettings
    }
    
    /**
     * Get setting definition by key
     */
    fun getSettingDefinition(key: String): ToyotaE210SettingsRegistry.SettingDefinition? {
        return ToyotaE210SettingsRegistry.getSetting(key)
    }
}