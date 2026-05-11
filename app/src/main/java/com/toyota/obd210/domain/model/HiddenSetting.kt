package com.toyota.obd210.domain.model

data class HiddenSetting(
    val id: String,
    val register: String,
    val name: String,
    val category: SettingCategory,
    val currentValue: Int,
    val defaultValue: Int,
    val options: List<SettingOption>,
    /**
     * Támogatott jármű modellek.
     * Ha üres, akkor minden modell támogatja.
     */
    val supportedModels: Set<CarModel> = emptySet(),
    /**
     * Hardver követelmény a beállítás használatához (opcionális).
     * Pl.: "HUD", "Panoramic Roof", "JBL Audio"
     */
    val requiredHardware: String? = null,
    /**
     * A beállítás leírása, miért nem elérhető egy járműben.
     */
    val notAvailableReason: String? = null
)

data class SettingOption(
    val value: Int,
    val label: String
)

enum class SettingCategory {
    DOOR_LOCK,
    LIGHTING,
    CLIMATE,
    TPMS,
    INFOTAINMENT,
    SAFETY,
    HUD,
    WINDOWS,
    INDICATORS,
    SEATBELT
}
