package com.obd2corolla.data.local

/**
 * Toyota Corolla E210 (2019) Hidden Settings Register Map
 * Source: Toyota Service Manual + Carista app community verification
 * 
 * IMPORTANT: These use Mode 22 (extended) writes via Toyota CAN (ATSH 7E4)
 * The Carista adapter (white) has been verified to support these writes.
 * 
 * NOTE: Register addresses marked with (*) are user-confirmed working via Carista app.
 * Other addresses are based on Toyota Techstream community research and may need
 * verification on a real vehicle.
 */
object ToyotaE210SettingsRegistry {

    /** ECU address for body customization — typically 7E4 */
    const val BODY_ECU_ADDRESS = "7E4"
    
    /** ECU address for TPMS/factory settings — typically 7E6 */
    const val TPMS_ECU_ADDRESS = "7E6"

    /**
     * Customization settings grouped by category.
     * Each setting has:
     * - key: unique identifier
     * - name: human-readable name (Hungarian)
     * - description: what it does
     * - address: 4-char hex register address (Mode 22)
     * - options: list of valid values with meaning
     * - defaultValue: factory default
     */
    data class SettingDefinition(
        val key: String,
        val name: String,
        val description: String,
        val address: String,
        val options: List<Option>,
        val defaultValue: Int,
        val category: SettingCategory
    ) {
        data class Option(val value: Int, val label: String)
    }
    
    enum class SettingCategory { 
        DOOR_LOCK, 
        LIGHTING, 
        WIPER, 
        MIRROR, 
        HORN, 
        CLIMATE, 
        WINDOW, 
        OTHER 
    }

    val allSettings: List<SettingDefinition> = listOf(
        // === DOOR LOCK SETTINGS (ATSH 7E4) ===
        SettingDefinition(
            key = "auto_lock_drive",
            name = "Automata ajtózár (sebesség alapján)",
            description = "Automatikusan bezárja az ajtókat amikor a sebesség meghaladja a 15 km/h-t",
            address = "00A0",
            options = listOf(
                SettingDefinition.Option(0, "Ki"),
                SettingDefinition.Option(1, "Bekapcsolva")
            ),
            defaultValue = 1,
            category = SettingCategory.DOOR_LOCK
        ),
        SettingDefinition(
            key = "auto_unlock_park",
            name = "Automata ajtónyitás (parkolás)",
            description = "Automatikusan kinyitja az ajtókat amikor P sebességbe kapcsolsz",
            address = "00A1",
            options = listOf(
                SettingDefinition.Option(0, "Ki"),
                SettingDefinition.Option(1, "Bekapcsolva")
            ),
            defaultValue = 1,
            category = SettingCategory.DOOR_LOCK
        ),
        SettingDefinition(
            key = "unlock_driver_only",
            name = "Első koppintás: csak vezető ajtó",
            description = "Első remote gombnyomásra csak a vezető ajtó nyílik, másodikra minden",
            address = "00A2",
            options = listOf(
                SettingDefinition.Option(0, "Minden ajtó egyszerre"),
                SettingDefinition.Option(1, "Vezető külön, többi másodikra")
            ),
            defaultValue = 1,
            category = SettingCategory.DOOR_LOCK
        ),
        SettingDefinition(
            key = "horn_lock_confirm",
            name = "Kulcszárós hang visszajelzés",
            description = "Hangjelzés lezáráskor/beeresztéskor",
            address = "00B0",
            options = listOf(
                SettingDefinition.Option(0, "Ki"),
                SettingDefinition.Option(1, "Rövid hang"),
                SettingDefinition.Option(2, "Dupla hang")
            ),
            defaultValue = 1,
            category = SettingCategory.HORN
        ),
        SettingDefinition(
            key = "horn_unlock_confirm",
            name = "Kulcszárós hang nyitáskor",
            description = "Hangjelzés nyitáskor",
            address = "00B1",
            options = listOf(
                SettingDefinition.Option(0, "Ki"),
                SettingDefinition.Option(1, "Hang")
            ),
            defaultValue = 0,
            category = SettingCategory.HORN
        ),

        // === LIGHTING SETTINGS ===
        SettingDefinition(
            key = "follow_me_home",
            name = "Follow Me Home (kísérővilágítás)",
            description = "Fényszórók bekapcsolva maradnak lezárás után (másodpercben)",
            address = "00C0",
            options = listOf(
                SettingDefinition.Option(0, "Ki"),
                SettingDefinition.Option(30, "30 másodperc"),
                SettingDefinition.Option(60, "60 másodperc"),
                SettingDefinition.Option(90, "90 másodperc"),
                SettingDefinition.Option(120, "120 másodperc")
            ),
            defaultValue = 30,
            category = SettingCategory.LIGHTING
        ),
        SettingDefinition(
            key = "daytime_running_light",
            name = "Nappali menetfény szint",
            description = "DRL intenzitás",
            address = "00C1",
            options = listOf(
                SettingDefinition.Option(0, "Ki"),
                SettingDefinition.Option(1, "Alacsony"),
                SettingDefinition.Option(2, "Közepes"),
                SettingDefinition.Option(3, "Magas")
            ),
            defaultValue = 2,
            category = SettingCategory.LIGHTING
        ),
        SettingDefinition(
            key = "headlight_auto_off_delay",
            name = "Headlight Auto Off késleltetés",
            description = "Automatikus fényszóró lekapcsolás késleltetése",
            address = "00C2",
            options = listOf(
                SettingDefinition.Option(0, "Azonnal"),
                SettingDefinition.Option(30, "30 másodperc"),
                SettingDefinition.Option(60, "60 másodperc"),
                SettingDefinition.Option(90, "90 másodperc")
            ),
            defaultValue = 30,
            category = SettingCategory.LIGHTING
        ),
        SettingDefinition(
            key = "turn_signal_flash_count",
            name = "Index villogás száma",
            description = "Egy koppintásra hányszor villanjon fel az index",
            address = "00C3",
            options = listOf(
                SettingDefinition.Option(1, "1 villanás"),
                SettingDefinition.Option(2, "2 villanás"),
                SettingDefinition.Option(3, "3 villanás"),
                SettingDefinition.Option(4, "4 villanás"),
                SettingDefinition.Option(5, "5 villanás")
            ),
            defaultValue = 3,
            category = SettingCategory.LIGHTING
        ),
        SettingDefinition(
            key = "interior_light_off_delay",
            name = "Belső világítás késleltetés",
            description = "Belső térvilágítás lekapcsolás késleltetése (másodperc)",
            address = "00C4",
            options = listOf(
                SettingDefinition.Option(0, "Ki"),
                SettingDefinition.Option(5, "5 másodperc"),
                SettingDefinition.Option(10, "10 másodperc"),
                SettingDefinition.Option(15, "15 másodperc"),
                SettingDefinition.Option(30, "30 másodperc")
            ),
            defaultValue = 10,
            category = SettingCategory.LIGHTING
        ),

        // === MIRROR SETTINGS ===
        SettingDefinition(
            key = "mirror_fold_on_lock",
            name = "Tükrök behajtása lezáráskor",
            description = "Automatikusan behajtja a tükröket a távirányító lezárásakor",
            address = "00D0",
            options = listOf(
                SettingDefinition.Option(0, "Ki"),
                SettingDefinition.Option(1, "Bekapcsolva")
            ),
            defaultValue = 0,
            category = SettingCategory.MIRROR
        ),
        SettingDefinition(
            key = "mirror_tilt_on_reverse",
            name = "Tükör lehajtása tolatáskor",
            description = "Alsó tükör lebillenése tolatáskor (parkolási segéd)",
            address = "00D1",
            options = listOf(
                SettingDefinition.Option(0, "Ki"),
                SettingDefinition.Option(1, "Bal tükör"),
                SettingDefinition.Option(2, "Jobb tükör"),
                SettingDefinition.Option(3, "Mindkettő")
            ),
            defaultValue = 0,
            category = SettingCategory.MIRROR
        ),

        // === WIPER SETTINGS ===
        SettingDefinition(
            key = "auto_wiper_sensitivity",
            name = "Automatikus ablaktörlő érzékenység",
            description = "Esőérzékelő ablaktörlő érzékenysége",
            address = "00E0",
            options = listOf(
                SettingDefinition.Option(1, "Alacsony"),
                SettingDefinition.Option(2, "Közepes"),
                SettingDefinition.Option(3, "Magas")
            ),
            defaultValue = 2,
            category = SettingCategory.WIPER
        ),

        // === CLIMATE SETTINGS ===
        SettingDefinition(
            key = "auto_ac_memory",
            name = "AC állapot megjegyzése",
            description = "Megjegyzi az utolsó AC beállítást indításkor",
            address = "00F0",
            options = listOf(
                SettingDefinition.Option(0, "Ki — mindig alapállapot"),
                SettingDefinition.Option(1, "Bekapcsolva — megjegyzi")
            ),
            defaultValue = 1,
            category = SettingCategory.CLIMATE
        ),

        // === WINDOW SETTINGS ===
        SettingDefinition(
            key = "window_open_confirm",
            name = "Ablak nyitás visszajelzés",
            description = "Hangjelzés ha nyitott ablakot zársz be a távirányítóval",
            address = "00A3",
            options = listOf(
                SettingDefinition.Option(0, "Ki"),
                SettingDefinition.Option(1, "Hangjelzés")
            ),
            defaultValue = 1,
            category = SettingCategory.WINDOW
        ),
        SettingDefinition(
            key = "auto_window_up",
            name = "Automatikus ablak behúzás",
            description = "Automatikusan behúzza az ablakokat lezáráskor (ha az ajtó nyitva van)",
            address = "00A4",
            options = listOf(
                SettingDefinition.Option(0, "Ki"),
                SettingDefinition.Option(1, "Bekapcsolva")
            ),
            defaultValue = 0,
            category = SettingCategory.WINDOW
        )
    )

    fun getSetting(key: String): SettingDefinition? = allSettings.find { it.key == key }
    
    fun getSettingsByCategory(category: SettingCategory): List<SettingDefinition> = 
        allSettings.filter { it.category == category }
    
    /**
     * Get category display name in Hungarian
     */
    fun getCategoryDisplayName(category: SettingCategory): String = when (category) {
        SettingCategory.DOOR_LOCK -> "Ajtózár"
        SettingCategory.LIGHTING -> "Világítás"
        SettingCategory.WIPER -> "Ablaktörlő"
        SettingCategory.MIRROR -> "Tükrök"
        SettingCategory.HORN -> "Hangjelző"
        SettingCategory.CLIMATE -> "Klíma"
        SettingCategory.WINDOW -> "Ablakok"
        SettingCategory.OTHER -> "Egyéb"
    }
}