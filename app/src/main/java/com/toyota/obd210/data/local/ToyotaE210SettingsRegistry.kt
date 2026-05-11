package com.toyota.obd210.data.local

import com.toyota.obd210.domain.model.HiddenSetting
import com.toyota.obd210.domain.model.SettingCategory
import com.toyota.obd210.domain.model.SettingOption

/**
 * Toyota Corolla E210 Hidden Settings Registry
 * Contains all customization register addresses and their options
 */
object ToyotaE210SettingsRegistry {
    
    val allSettings: List<HiddenSetting> = listOf(
        // Door Lock Settings (0x0B00 - 0x0B04)
        HiddenSetting(
            id = "unlock_method",
            register = "0B 00",
            name = "Unlock Method",
            category = SettingCategory.DOOR_LOCK,
            currentValue = 1,
            defaultValue = 1,
            options = listOf(
                SettingOption(0, "All Doors"),
                SettingOption(1, "Driver Door Only"),
                SettingOption(2, "Driver + 2x Press")
            )
        ),
        HiddenSetting(
            id = "auto_lock_speed",
            register = "0B 01",
            name = "Auto Lock Speed",
            category = SettingCategory.DOOR_LOCK,
            currentValue = 20,
            defaultValue = 20,
            options = listOf(
                SettingOption(0, "Off"),
                SettingOption(10, "10 km/h"),
                SettingOption(20, "20 km/h"),
                SettingOption(30, "30 km/h")
            )
        ),
        HiddenSetting(
            id = "auto_unlock",
            register = "0B 02",
            name = "Auto Unlock",
            category = SettingCategory.DOOR_LOCK,
            currentValue = 1,
            defaultValue = 1,
            options = listOf(
                SettingOption(0, "Off"),
                SettingOption(1, "In Park"),
                SettingOption(2, "Ignition Off")
            )
        ),
        HiddenSetting(
            id = "horn_lock",
            register = "0B 03",
            name = "Horn Lock Confirm",
            category = SettingCategory.DOOR_LOCK,
            currentValue = 1,
            defaultValue = 1,
            options = listOf(
                SettingOption(0, "Off"),
                SettingOption(1, "Short Beep"),
                SettingOption(2, "Long Beep")
            )
        ),
        HiddenSetting(
            id = "horn_unlock",
            register = "0B 04",
            name = "Horn Unlock Confirm",
            category = SettingCategory.DOOR_LOCK,
            currentValue = 2,
            defaultValue = 2,
            options = listOf(
                SettingOption(0, "Off"),
                SettingOption(1, "Short Beep"),
                SettingOption(2, "Double Beep")
            )
        ),
        
        // Lighting Settings (0x0C00 - 0x0C04)
        HiddenSetting(
            id = "drl_enable",
            register = "0C 00",
            name = "Daytime Running Lights",
            category = SettingCategory.LIGHTING,
            currentValue = 1,
            defaultValue = 1,
            options = listOf(
                SettingOption(0, "Off"),
                SettingOption(1, "On")
            )
        ),
        HiddenSetting(
            id = "drl_type",
            register = "0C 01",
            name = "DRL Type",
            category = SettingCategory.LIGHTING,
            currentValue = 2,
            defaultValue = 2,
            options = listOf(
                SettingOption(0, "Full"),
                SettingOption(1, "Partial"),
                SettingOption(2, "Auto")
            )
        ),
        HiddenSetting(
            id = "welcome_light",
            register = "0C 02",
            name = "Welcome Light Duration",
            category = SettingCategory.LIGHTING,
            currentValue = 30,
            defaultValue = 30,
            options = listOf(
                SettingOption(0, "Off"),
                SettingOption(15, "15 sec"),
                SettingOption(30, "30 sec"),
                SettingOption(60, "60 sec")
            )
        ),
        HiddenSetting(
            id = "follow_me_home",
            register = "0C 03",
            name = "Follow Me Home",
            category = SettingCategory.LIGHTING,
            currentValue = 30,
            defaultValue = 30,
            options = listOf(
                SettingOption(0, "Off"),
                SettingOption(30, "30 sec"),
                SettingOption(60, "60 sec"),
                SettingOption(90, "90 sec"),
                SettingOption(120, "120 sec")
            )
        ),
        HiddenSetting(
            id = "interior_light",
            register = "0C 04",
            name = "Interior Light Auto",
            category = SettingCategory.LIGHTING,
            currentValue = 1,
            defaultValue = 1,
            options = listOf(
                SettingOption(0, "Off"),
                SettingOption(1, "Auto")
            )
        ),
        
        // Climate Settings (0x0D00 - 0x0D02)
        HiddenSetting(
            id = "auto_recirc",
            register = "0D 00",
            name = "Auto Recirculation",
            category = SettingCategory.CLIMATE,
            currentValue = 1,
            defaultValue = 1,
            options = listOf(
                SettingOption(0, "Manual"),
                SettingOption(1, "Auto")
            )
        ),
        HiddenSetting(
            id = "defog_auto",
            register = "0D 01",
            name = "Auto Defog Start",
            category = SettingCategory.CLIMATE,
            currentValue = 15,
            defaultValue = 15,
            options = listOf(
                SettingOption(0, "Off"),
                SettingOption(10, "10°C"),
                SettingOption(15, "15°C"),
                SettingOption(20, "20°C")
            )
        ),
        HiddenSetting(
            id = "remote_start",
            register = "0D 02",
            name = "Remote Start Duration",
            category = SettingCategory.CLIMATE,
            currentValue = 10,
            defaultValue = 10,
            options = listOf(
                SettingOption(0, "Off"),
                SettingOption(5, "5 min"),
                SettingOption(10, "10 min"),
                SettingOption(15, "15 min"),
                SettingOption(20, "20 min")
            )
        ),
        
        // TPMS Settings (0x0E00 - 0x0E01)
        HiddenSetting(
            id = "tpms_warning",
            register = "0E 00",
            name = "TPMS Warning Mode",
            category = SettingCategory.TPMS,
            currentValue = 1,
            defaultValue = 1,
            options = listOf(
                SettingOption(0, "Indicator Only"),
                SettingOption(1, "Message Display")
            )
        ),
        HiddenSetting(
            id = "tpms_unit",
            register = "0E 01",
            name = "TPMS Pressure Unit",
            category = SettingCategory.TPMS,
            currentValue = 0,
            defaultValue = 0,
            options = listOf(
                SettingOption(0, "kPa"),
                SettingOption(1, "PSI"),
                SettingOption(2, "Bar")
            )
        ),
        
        // Infotainment Settings (0x0F00 - 0x0F03)
        HiddenSetting(
            id = "beep_volume",
            register = "0F 00",
            name = "Beep Volume",
            category = SettingCategory.INFOTAINMENT,
            currentValue = 1,
            defaultValue = 1,
            options = listOf(
                SettingOption(0, "Off"),
                SettingOption(1, "Low"),
                SettingOption(2, "High")
            )
        ),
        HiddenSetting(
            id = "camera_guide",
            register = "0F 01",
            name = "Camera Guidelines",
            category = SettingCategory.INFOTAINMENT,
            currentValue = 1,
            defaultValue = 1,
            options = listOf(
                SettingOption(0, "Off"),
                SettingOption(1, "On")
            )
        ),
        HiddenSetting(
            id = "park_assist_volume",
            register = "0F 02",
            name = "Park Assist Volume",
            category = SettingCategory.INFOTAINMENT,
            currentValue = 1,
            defaultValue = 1,
            options = listOf(
                SettingOption(0, "Off"),
                SettingOption(1, "Low"),
                SettingOption(2, "High")
            )
        ),
        HiddenSetting(
            id = "start_sound",
            register = "0F 03",
            name = "Start Sound",
            category = SettingCategory.INFOTAINMENT,
            currentValue = 1,
            defaultValue = 1,
            options = listOf(
                SettingOption(0, "Off"),
                SettingOption(1, "Standard"),
                SettingOption(2, "Premium")
            )
        ),
        
        // Safety Settings (0x1000 - 0x1005)
        HiddenSetting(
            id = "pcs_enable",
            register = "10 00",
            name = "Pre-Collision System",
            category = SettingCategory.SAFETY,
            currentValue = 1,
            defaultValue = 1,
            options = listOf(
                SettingOption(0, "Off"),
                SettingOption(1, "On")
            )
        ),
        HiddenSetting(
            id = "pcs_sensitivity",
            register = "10 01",
            name = "PCS Sensitivity",
            category = SettingCategory.SAFETY,
            currentValue = 1,
            defaultValue = 1,
            options = listOf(
                SettingOption(0, "Early"),
                SettingOption(1, "Normal"),
                SettingOption(2, "Late")
            )
        ),
        HiddenSetting(
            id = "lka_enable",
            register = "10 02",
            name = "Lane Keep Assist",
            category = SettingCategory.SAFETY,
            currentValue = 1,
            defaultValue = 1,
            options = listOf(
                SettingOption(0, "Off"),
                SettingOption(1, "On")
            )
        ),
        HiddenSetting(
            id = "lda_alert",
            register = "10 03",
            name = "LDA Alert Type",
            category = SettingCategory.SAFETY,
            currentValue = 2,
            defaultValue = 2,
            options = listOf(
                SettingOption(0, "Sound"),
                SettingOption(1, "Vibration"),
                SettingOption(2, "Both")
            )
        ),
        HiddenSetting(
            id = "acc_distance",
            register = "10 04",
            name = "ACC Follow Distance",
            category = SettingCategory.SAFETY,
            currentValue = 3,
            defaultValue = 3,
            options = listOf(
                SettingOption(1, "Very Close"),
                SettingOption(2, "Close"),
                SettingOption(3, "Normal"),
                SettingOption(4, "Far"),
                SettingOption(5, "Very Far")
            )
        ),
        HiddenSetting(
            id = "rsa_enable",
            register = "10 05",
            name = "Road Sign Assist",
            category = SettingCategory.SAFETY,
            currentValue = 1,
            defaultValue = 1,
            options = listOf(
                SettingOption(0, "Off"),
                SettingOption(1, "On")
            )
        ),
        
        // Window Settings (0x1400 - 0x1402) - Common Toyota customization
        HiddenSetting(
            id = "one_touch_up_driver",
            register = "14 00",
            name = "One-Touch Up Driver",
            category = SettingCategory.WINDOWS,
            currentValue = 1,
            defaultValue = 1,
            options = listOf(
                SettingOption(0, "Off"),
                SettingOption(1, "On")
            )
        ),
        HiddenSetting(
            id = "one_touch_up_passenger",
            register = "14 01",
            name = "One-Touch Up Passenger",
            category = SettingCategory.WINDOWS,
            currentValue = 1,
            defaultValue = 1,
            options = listOf(
                SettingOption(0, "Off"),
                SettingOption(1, "On")
            )
        ),
        HiddenSetting(
            id = "one_touch_up_rear",
            register = "14 02",
            name = "One-Touch Up Rear",
            category = SettingCategory.WINDOWS,
            currentValue = 1,
            defaultValue = 1,
            options = listOf(
                SettingOption(0, "Off"),
                SettingOption(1, "On")
            )
        ),
        HiddenSetting(
            id = "anti_trap_sensitivity",
            register = "14 03",
            name = "Anti-Trap Sensitivity",
            category = SettingCategory.WINDOWS,
            currentValue = 1,
            defaultValue = 1,
            options = listOf(
                SettingOption(0, "Low"),
                SettingOption(1, "Medium"),
                SettingOption(2, "High")
            )
        ),
        
        // Indicator/Blinker Settings (0x1500 - 0x1502)
        HiddenSetting(
            id = "turn_signal_flashes",
            register = "15 00",
            name = "Turn Signal Flashes",
            category = SettingCategory.INDICATORS,
            currentValue = 3,
            defaultValue = 3,
            options = listOf(
                SettingOption(1, "1 Flash"),
                SettingOption(2, "2 Flashes"),
                SettingOption(3, "3 Flashes"),
                SettingOption(4, "4 Flashes"),
                SettingOption(5, "5 Flashes")
            )
        ),
        HiddenSetting(
            id = "lane_change_flash",
            register = "15 01",
            name = "Lane Change Flash",
            category = SettingCategory.INDICATORS,
            currentValue = 1,
            defaultValue = 1,
            options = listOf(
                SettingOption(0, "Off"),
                SettingOption(1, "1 Flash"),
                SettingOption(2, "2 Flashes")
            )
        ),
        HiddenSetting(
            id = "hazard_auto_off",
            register = "15 02",
            name = "Hazard Auto Off",
            category = SettingCategory.INDICATORS,
            currentValue = 1,
            defaultValue = 1,
            options = listOf(
                SettingOption(0, "Off"),
                SettingOption(1, "On")
            )
        ),
        
        // Seatbelt Minder Settings (0x1600)
        HiddenSetting(
            id = "seatbelt_minder",
            register = "16 00",
            name = "Seatbelt Minder",
            category = SettingCategory.SEATBELT,
            currentValue = 1,
            defaultValue = 1,
            options = listOf(
                SettingOption(0, "Off"),
                SettingOption(1, "On"),
                SettingOption(2, "Continuous")
            )
        ),
        HiddenSetting(
            id = "seatbelt_minder_chime",
            register = "16 01",
            name = "Minder Chime Volume",
            category = SettingCategory.SEATBELT,
            currentValue = 1,
            defaultValue = 1,
            options = listOf(
                SettingOption(0, "Off"),
                SettingOption(1, "Low"),
                SettingOption(2, "High")
            )
        )
    )
    
    fun getSettingsByCategory(category: SettingCategory): List<HiddenSetting> {
        return allSettings.filter { it.category == category }
    }
    
    fun getSettingById(id: String): HiddenSetting? {
        return allSettings.find { it.id == id }
    }
}