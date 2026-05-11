package com.toyota.obd210.domain.model

/**
 * Toyota Corolla E210 modellek enumerációja
 * Minden modellhez hozzá vannak rendelve a támogatott rejtett beállítás ID-k
 */
enum class CarModel(
    val displayName: String,
    val supportedSettingIds: Set<String>,
    val requiresHardware: Set<String> = emptySet()
) {
    /**
     * Corolla E210 Hybrid - 2019-2023
     * Teljes támogatás a hibrid specifikus funkciókkal
     */
    E210_HYBRID(
        displayName = "Corolla E210 Hybrid (2019-2023)",
        supportedSettingIds = setOf(
            // Door Lock
            "unlock_method", "auto_lock_speed", "auto_unlock", "horn_lock", "horn_unlock",
            // Lighting
            "drl_enable", "drl_type", "welcome_light", "follow_me_home", "interior_light",
            // Climate
            "auto_recirc", "defog_auto", "remote_start",
            // TPMS
            "tpms_warning", "tpms_unit",
            // Infotainment
            "beep_volume", "camera_guide", "park_assist_volume", "start_sound",
            // Safety
            "pcs_enable", "pcs_sensitivity", "lka_enable", "lda_alert", "acc_distance", "rsa_enable",
            // Windows
            "one_touch_up_driver", "one_touch_up_passenger", "one_touch_up_rear", "anti_trap_sensitivity",
            // Indicators
            "turn_signal_flashes", "lane_change_flash", "hazard_auto_off",
            // Seatbelt
            "seatbelt_minder", "seatbelt_minder_chime",
            // Hybrid specific
            "hybrid_battery_display", "ev_mode_priority"
        )
    ),

    /**
     * Corolla E210 COMBAT / Lifestyle - 2020-2023
     * Sportosabb felszereltség specifikus beállításokkal
     */
    E210_COMBAT(
        displayName = "Corolla E210 COMBAT (2020-2023)",
        supportedSettingIds = setOf(
            // Door Lock
            "unlock_method", "auto_lock_speed", "auto_unlock", "horn_lock", "horn_unlock",
            // Lighting
            "drl_enable", "drl_type", "welcome_light", "follow_me_home", "interior_light",
            // Climate
            "auto_recirc", "defog_auto",
            // TPMS
            "tpms_warning", "tpms_unit",
            // Infotainment
            "beep_volume", "camera_guide", "park_assist_volume", "start_sound",
            // Safety
            "pcs_enable", "pcs_sensitivity", "lka_enable", "lda_alert", "acc_distance", "rsa_enable",
            // Windows
            "one_touch_up_driver", "one_touch_up_passenger", "one_touch_up_rear", "anti_trap_sensitivity",
            // Indicators
            "turn_signal_flashes", "lane_change_flash", "hazard_auto_off",
            // Seatbelt
            "seatbelt_minder", "seatbelt_minder_chime",
            // COMBAT specific
            "sport_mode_default", "exhaust_sound"
        )
    ),

    /**
     * Corolla E210 Liftback - 2019-2023
     * Liftback specifikus beállítások
     */
    E210_LIFTBACK(
        displayName = "Corolla E210 Liftback (2019-2023)",
        supportedSettingIds = setOf(
            // Door Lock
            "unlock_method", "auto_lock_speed", "auto_unlock", "horn_lock", "horn_unlock",
            // Lighting
            "drl_enable", "drl_type", "welcome_light", "follow_me_home", "interior_light",
            // Climate
            "auto_recirc", "defog_auto", "remote_start",
            // TPMS
            "tpms_warning", "tpms_unit",
            // Infotainment
            "beep_volume", "camera_guide", "park_assist_volume", "start_sound",
            // Safety
            "pcs_enable", "pcs_sensitivity", "lka_enable", "lda_alert", "acc_distance", "rsa_enable",
            // Windows
            "one_touch_up_driver", "one_touch_up_passenger", "one_touch_up_rear", "anti_trap_sensitivity",
            // Indicators
            "turn_signal_flashes", "lane_change_flash", "hazard_auto_off",
            // Seatbelt
            "seatbelt_minder", "seatbelt_minder_chime",
            // Liftback specific
            "tailgate_auto_close"
        )
    ),

    /**
     * Corolla E210 Sedan - 2019-2023
     * Alap modell beállítások
     */
    E210_SEDAN(
        displayName = "Corolla E210 Sedan (2019-2023)",
        supportedSettingIds = setOf(
            // Door Lock
            "unlock_method", "auto_lock_speed", "auto_unlock", "horn_lock", "horn_unlock",
            // Lighting
            "drl_enable", "drl_type", "welcome_light", "follow_me_home", "interior_light",
            // Climate
            "auto_recirc", "defog_auto",
            // TPMS
            "tpms_warning", "tpms_unit",
            // Infotainment
            "beep_volume", "camera_guide", "park_assist_volume",
            // Safety
            "pcs_enable", "pcs_sensitivity", "lka_enable", "lda_alert", "acc_distance", "rsa_enable",
            // Windows
            "one_touch_up_driver", "one_touch_up_passenger", "anti_trap_sensitivity",
            // Indicators
            "turn_signal_flashes", "lane_change_flash", "hazard_auto_off",
            // Seatbelt
            "seatbelt_minder", "seatbelt_minder_chime"
        )
    ),

    /**
     * Corolla E210 Hybrid Premium (HUD-val felszerelt)
     * Teljes funkcionalitás + HUD specifikus beállítások
     */
    E210_HYBRID_PREMIUM(
        displayName = "Corolla E210 Hybrid Premium (HUD)",
        supportedSettingIds = setOf(
            // Minden E210_HYBRID beállítás
            "unlock_method", "auto_lock_speed", "auto_unlock", "horn_lock", "horn_unlock",
            "drl_enable", "drl_type", "welcome_light", "follow_me_home", "interior_light",
            "auto_recirc", "defog_auto", "remote_start",
            "tpms_warning", "tpms_unit",
            "beep_volume", "camera_guide", "park_assist_volume", "start_sound",
            "pcs_enable", "pcs_sensitivity", "lka_enable", "lda_alert", "acc_distance", "rsa_enable",
            "one_touch_up_driver", "one_touch_up_passenger", "one_touch_up_rear", "anti_trap_sensitivity",
            "turn_signal_flashes", "lane_change_flash", "hazard_auto_off",
            "seatbelt_minder", "seatbelt_minder_chime",
            "hybrid_battery_display", "ev_mode_priority",
            // Premium/HUD specifikus
            "hud_brightness", "hud_content", "hud_color"
        ),
        requiresHardware = setOf("HUD", "JBL_AUDIO")
    );

    companion object {
        /**
         * Összes jármű modell listája
         */
        fun getAllModels(): List<CarModel> = entries.toList()

        /**
         * Alapértelmezett modell
         */
        fun getDefault(): CarModel = E210_HYBRID

        /**
         * Modell keresése név alapján
         */
        fun fromName(name: String): CarModel? {
            return entries.find { it.name == name }
        }
    }
}

/**
 * Hardware komponens kategóriák a jármű felszereltség ellenőrzéséhez
 */
enum class HardwareComponent(val displayName: String) {
    HUD("Head-Up Display"),
    JBL_AUDIO("JBL Premium Audio"),
    PANORAMIC_ROOF("Panoramic Sunroof"),
    ADAPTIVE_CRUISE("Adaptive Cruise Control"),
    WIRELESS_CHARGER("Wireless Phone Charger"),
    POWER_TAILGATE("Power Tailgate")
}