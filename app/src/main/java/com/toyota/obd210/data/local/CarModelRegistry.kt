package com.toyota.obd210.data.local

import com.toyota.obd210.domain.model.CarModel

/**
 * Jármű modell felismerési és VIN parsing logika
 * Toyota Corolla E210 VIN struktúra alapján
 * 
 * VIN struktúra (E210):
 * Positions 1-3: WMI (World Manufacturer Identifier) - JTD = Toyota
 * Positions 4-9: VDS (Vehicle Descriptor Section) - platform és karosszéria
 * Position 10: Model Year (A=2010, B=2011... L=2020, M=2021, N=2022, P=2023, R=2024)
 * Position 11: Plant Code
 * Positions 12-17: VIS (Vehicle Identifier Section) - egyedi széria
 */
object CarModelRegistry {
    
    /**
     * Toyota VIN pozíciók
     */
    private object VinPositions {
        const val WMI_START = 0
        const val WMI_END = 3
        const val VDS_START = 3
        const val VDS_END = 9
        const val YEAR_POSITION = 9
        const val PLANT_POSITION = 10
    }
    
    /**
     * Toyota WMI kódok
     */
    private object ToyotaWmi {
        const val JTD = "JTD"  // Toyota
        const val JTE = "JTE"  // Toyota (USA)
        const val JTF = "JTF"  // Toyota (USA)
        const val JTL = "JTL"  // Toyota (USA)
    }
    
    /**
     * E210 specifikus VDS kódok
     * Position 4-6: Platform
     * Position 7-9: Karosszéria/Motor
     */
    private object E210Vds {
        // Hybrid modellek (AA3)
        const val HYBRID_AA3 = "AA3"
        const val HYBRID_PREMIUM_AA3F = "AA3F"
        
        // COMBAT/Sport modellek (AA0, AB0)
        const val COMBAT_AA0 = "AA0"
        const val COMBAT_AB0 = "AB0"
        
        // Liftback (AA4, BA4)
        const val LIFTBACK_AA4 = "AA4"
        const val LIFTBACK_BA4 = "BA4"
        
        // Sedan (AAA, BAA)
        const val SEDAN_AAA = "AAA"
        const val SEDAN_BAA = "BAA"
        const val SEDAN_BA4 = "BA4"
    }
    
    /**
     * Évszám kódok (Position 10)
     */
    private val yearCodes = mapOf(
        'L' to 2020,
        'M' to 2021,
        'N' to 2022,
        'P' to 2023,
        'R' to 2024,
        'S' to 2025,
        'T' to 2026,
        'A' to 2010, // Legacy E210 előtt
        'B' to 2011,
        'C' to 2012,
        'D' to 2013,
        'E' to 2014,
        'F' to 2015,
        'G' to 2016,
        'H' to 2017,
        'J' to 2018,
        'K' to 2019
    )
    
    /**
     * Jármű felismerése VIN-ből
     * @param vin 17 karakteres jármű azonosító szám
     * @return Felismert CarModel vagy null ha nem sikerült
     */
    fun detectFromVin(vin: String): CarModel? {
        if (vin.length != 17) {
            return null
        }
        
        val wmi = vin.substring(VinPositions.WMI_START, VinPositions.WMI_END)
        
        // Ellenőrizzük, hogy Toyota-e
        if (wmi != ToyotaWmi.JTD && !wmi.startsWith("JT")) {
            return null
        }
        
        val vds = vin.substring(VinPositions.VDS_START, VinPositions.VDS_END)
        
        return when {
            // Hybrid Premium (AA3F vagy különleges felszereltség)
            vds == E210Vds.HYBRID_PREMIUM_AA3F -> CarModel.E210_HYBRID_PREMIUM
            
            // Hybrid modellek
            vds == E210Vds.HYBRID_AA3 -> CarModel.E210_HYBRID
            
            // COMBAT / Sport
            vds == E210Vds.COMBAT_AA0 || vds == E210Vds.COMBAT_AB0 -> CarModel.E210_COMBAT
            
            // Liftback
            vds == E210Vds.LIFTBACK_AA4 || vds == E210Vds.LIFTBACK_BA4 -> CarModel.E210_LIFTBACK
            
            // Sedan
            vds == E210Vds.SEDAN_AAA || vds == E210Vds.SEDAN_BAA || vds == E210Vds.SEDAN_BA4 -> CarModel.E210_SEDAN
            
            // Ismeretlen, de Toyota - alapértelmezett E210
            else -> CarModel.E210_HYBRID
        }
    }
    
    /**
     * Évszám kiolvasása VIN-ből
     */
    fun getYearFromVin(vin: String): Int? {
        if (vin.length < 10) return null
        val yearChar = vin[VinPositions.YEAR_POSITION]
        return yearCodes[yearChar]
    }
    
    /**
     * Gyári kód kiolvasása VIN-ből (Position 11)
     */
    fun getPlantFromVin(vin: String): Char? {
        if (vin.length < 11) return null
        return vin[VinPositions.PLANT_POSITION]
    }
    
    /**
     * VIN validálása (ellenőrző karakter)
     */
    fun isValidVin(vin: String): Boolean {
        if (vin.length != 17) return false
        
        // Alfanumerikus ellenőrzés (kizárjuk az I, O, Q karaktereket VIN-ből)
        val validChars = "ABCDEFGHJKLMNPRSTUVWXYZ0123456789"
        return vin.all { it.uppercaseChar() in validChars }
    }
    
    /**
     * Manuális modell keresés display név alapján
     */
    fun findModelByDisplayName(displayName: String): CarModel? {
        return CarModel.getAllModels().find { it.displayName == displayName }
    }
    
    /**
     * Összes támogatott modell listája
     */
    fun getAllSupportedModels(): List<CarModel> = CarModel.getAllModels()
    
    /**
     * Modell specifikus VIN minta (teszteléshez)
     */
    fun generateTestVin(model: CarModel, year: Int = 2021): String {
        val yearChar = yearCodes.entries.find { it.value == year }?.key ?: 'M'
        
        val vds = when (model) {
            CarModel.E210_HYBRID -> E210Vds.HYBRID_AA3
            CarModel.E210_HYBRID_PREMIUM -> E210Vds.HYBRID_PREMIUM_AA3F
            CarModel.E210_COMBAT -> E210Vds.COMBAT_AA0
            CarModel.E210_LIFTBACK -> E210Vds.LIFTBACK_AA4
            CarModel.E210_SEDAN -> E210Vds.SEDAN_AAA
        }
        
        // JTD + VDS + Évjárat + Plant(T) + Sorozat(123456)
        return "JTD$vds${yearChar}T123456"
    }
}

/**
 * Jármű modell információk
 */
data class CarModelInfo(
    val model: CarModel,
    val vin: String?,
    val year: Int?,
    val isAutoDetected: Boolean
)