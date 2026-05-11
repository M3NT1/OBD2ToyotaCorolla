package com.obd2corolla.domain.model

/**
 * Represents a hidden/customization setting on the Toyota vehicle.
 * These are ECU-specific settings that control vehicle customization options
 * not accessible through standard OBD-II modes or the infotainment system.
 *
 * @param key Unique identifier for the setting (e.g., "turn_signal_flash_count")
 * @param name Human-readable name in Hungarian
 * @param description Description of what the setting controls
 * @param currentValue The current value read from the vehicle (Int)
 * @param options Available options for this setting
 * @param defaultValue Factory default value
 * @param isWriteable Whether this setting can be written (false for read-only info)
 * @param address The ECU register address (e.g., "00C3"), null if not applicable
 * @param category The category for grouping (e.g., "Világítás", "Ajtózár")
 */
data class HiddenSetting(
    val key: String,
    val name: String,
    val description: String,
    val currentValue: Int,
    val options: List<Option>,
    val defaultValue: Int,
    val isWriteable: Boolean = true,
    val address: String? = null,
    val category: String
) {
    /**
     * Represents a single option for a hidden setting.
     * @param value The numeric value stored in the ECU
     * @param label Human-readable label in Hungarian
     */
    data class Option(
        val value: Int,
        val label: String
    )
    
    /**
     * Get the label for the current value
     */
    fun currentValueLabel(): String {
        return options.find { it.value == currentValue }?.label ?: currentValue.toString()
    }
    
    /**
     * Check if the current value is the default
     */
    fun isDefault(): Boolean = currentValue == defaultValue
}