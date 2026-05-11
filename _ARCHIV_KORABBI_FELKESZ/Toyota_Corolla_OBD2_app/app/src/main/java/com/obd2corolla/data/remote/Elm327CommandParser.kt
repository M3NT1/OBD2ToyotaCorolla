package com.obd2corolla.data.remote

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Parser for ELM327 OBD2 commands and responses.
 * Handles command formatting, response parsing, and PID data extraction.
 */
@Singleton
class Elm327CommandParser @Inject constructor() {

    /**
     * Supported PIDs for Mode 01 (Current sensor data)
     */
    object PIDs {
        // Engine/RPM
        const val ENGINE_RPM = "0C"
        const val ENGINE_RPM_ALT = "1F" // Alternative RPM

        // Speed/Temperature
        const val VEHICLE_SPEED = "0D"
        const val COOLANT_TEMP = "05"
        const val INTAKE_TEMP = "0F"
        const val AMBIENT_TEMP = "46"

        // Throttle/Fuel
        const val THROTTLE_POSITION = "11"
        const val FUEL_TANK_LEVEL = "2F"
        const val FUEL_PRESSURE = "0A"
        const val FUEL_RAIL_PRESSURE = "23"
        const val FUEL_RAIL_PRESSURE_VACUUM = "22"

        // Engine/Load
        const val ENGINE_LOAD = "04"
        const val ENGINE_LOAD_ABSOLUTE = "43"

        // Timing
        const val TIMING_ADVANCE = "0E"
        const val INTake_AIR_TEMP = "0F"

        // Maf/Injection
        const val MAF_RATE = "10"
        const val THROTTLE_BATTERY_VOLTAGE = "42"

        // O2 Sensors
        const val O2_VOLTAGE = "14"
        const val O2_VOLTAGE_B1S2 = "15"
        const val O2_VOLTAGE_B2S2 = "16"

        // Short term fuel trim
        const val STFT_BANK1 = "06"
        const val STFT_BANK2 = "07"
        const val LTFT_BANK1 = "08"
        const val LTFT_BANK2 = "09"

        // Other
        const val RUNTIME = "1F"
        const val DISTANCE_WITH_MIL = "21"
        const val ABS_LOAD = "43"
        const val RELATIVE_THROTTLE = "19"

        // Intake metrics
        const val ENGINE_REF_TORQUE = "63"
        const val ENGINE_TORQUE = "64"

        // Barometric
        const val BAROMETRIC_PRESSURE = "33"

        // Control module voltage
        const val CONTROL_MODULE_VOLTAGE = "42"

        // Fuel system status
        const val FUEL_SYSTEM_STATUS = "03"

        // Calculated engine load
        const val ABS_ENGINE_LOAD = "43"

        // Equivalent ratio
        const val EQUIVALENCE_RATIO = "44"
    }

    private val validMode1Pids = setOf(
        "01", "02", "03", "04", "05", "06", "07", "08", "09", "0A", "0B", "0C", "0D", "0E", "0F",
        "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "1A", "1B", "1C", "1D", "1E", "1F",
        "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "2A", "2B", "2C", "2D", "2E", "2F",
        "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "3A", "3B", "3C", "3D", "3E", "3F",
        "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "4A", "4B", "4C", "4D", "4E", "4F"
    )

    /**
     * Clean and normalize an ELM327 command
     */
    fun parseCommand(cmd: String): String {
        val cleaned = cmd.trim()
            .uppercase()
            .replace(" ", "")
            .replace("\r", "")
            .replace("\n", "")
            .replace("AT", "")
        return cleaned.ifEmpty { cmd.trim().uppercase() }
    }

    /**
     * Format a command for sending to ELM327
     */
    fun formatCommand(mode: String, pid: String): String {
        val modeHex = mode.padStart(2, '0')
        val pidHex = pid.padStart(2, '0')
        return "${modeHex}${pidHex}"
    }

    /**
     * Format an AT command for ELM327
     */
    fun formatAtCommand(command: String): String {
        return if (command.uppercase().startsWith("AT")) command.uppercase() else "AT${command.uppercase()}"
    }

    /**
     * Validate a command format
     */
    fun isValidCommand(cmd: String): Boolean {
        val cleaned = parseCommand(cmd)
        return when {
            cleaned.startsWith("AT") -> true
            cleaned.length >= 4 -> {
                val mode = cleaned.substring(0, 2)
                validMode1Pids.contains(mode) || mode in listOf("02", "03", "04", "07", "09", "0A", "0B")
            }
            else -> false
        }
    }

    /**
     * Extract PID from a response (e.g., "41 0C" -> "0C")
     */
    fun extractPidFromResponse(response: String): String? {
        val cleaned = response.replace(" ", "").replace("\r", "").replace("\n", "")
        // Response format: [mode][pid][data...] where mode is incremented by 0x40
        // So "41 0C ..." means response to "01 0C"
        if (cleaned.length >= 4 && cleaned.startsWith("41")) {
            return cleaned.substring(2, 4)
        }
        return null
    }

    /**
     * Parse a Mode 01 response and extract sensor values
     */
    fun parseMode01Response(raw: String): Map<String, Any> {
        val result = mutableMapOf<String, Any>()
        
        // Clean the response
        val data = raw
            .replace(" ", "")
            .replace("\r", "")
            .replace("\n", "")
            .replace("SEARCHING...", "")
            .replace("NODATA", "")
            .replace("UNABLE TO CONNECT", "")
            .trim()

        // Remove the "41" prefix if present (41 = 01 + 40 response code)
        val cleanData = if (data.startsWith("41")) {
            data.substring(2)
        } else if (data.length >= 2 && validMode1Pids.contains(data.substring(0, 2))) {
            data
        } else {
            return result
        }

        if (cleanData.length < 2) return result

        val pid = cleanData.substring(0, 2)
        val values = cleanData.substring(2)

        when (pid) {
            // Engine RPM (A*256 + B) / 4
            PIDs.ENGINE_RPM -> {
                if (values.length >= 4) {
                    val a = values.substring(0, 2).toIntOrNull(16) ?: 0
                    val b = values.substring(2, 4).toIntOrNull(16) ?: 0
                    val rpm = ((a * 256 + b) / 4).toFloat()
                    result["rpm"] = rpm
                }
            }

            // Vehicle Speed (A)
            PIDs.VEHICLE_SPEED -> {
                if (values.isNotEmpty()) {
                    val speed = values.substring(0, 2).toIntOrNull(16) ?: 0
                    result["speed"] = speed.toFloat()
                }
            }

            // Coolant Temperature (A - 40)
            PIDs.COOLANT_TEMP -> {
                if (values.isNotEmpty()) {
                    val temp = values.substring(0, 2).toIntOrNull(16) ?: 0
                    val celsius = temp - 40
                    result["coolant_temp"] = celsius.toFloat()
                }
            }

            // Intake Air Temperature (A - 40)
            PIDs.INTAKE_TEMP -> {
                if (values.isNotEmpty()) {
                    val temp = values.substring(0, 2).toIntOrNull(16) ?: 0
                    val celsius = temp - 40
                    result["intake_temp"] = celsius.toFloat()
                }
            }

            // Throttle Position (A * 100 / 255)
            PIDs.THROTTLE_POSITION -> {
                if (values.isNotEmpty()) {
                    val throttle = values.substring(0, 2).toIntOrNull(16) ?: 0
                    result["throttle"] = (throttle * 100.0f / 255.0f)
                }
            }

            // Fuel Tank Level (A * 100 / 255)
            PIDs.FUEL_TANK_LEVEL -> {
                if (values.isNotEmpty()) {
                    val fuel = values.substring(0, 2).toIntOrNull(16) ?: 0
                    result["fuel_level"] = (fuel * 100.0f / 255.0f)
                }
            }

            // Engine Load (A * 100 / 255)
            PIDs.ENGINE_LOAD -> {
                if (values.isNotEmpty()) {
                    val load = values.substring(0, 2).toIntOrNull(16) ?: 0
                    result["engine_load"] = (load * 100.0f / 255.0f)
                }
            }

            // Short Term Fuel Trim Bank 1 (A - 128) * 100 / 128
            PIDs.STFT_BANK1 -> {
                if (values.isNotEmpty()) {
                    val stft = values.substring(0, 2).toIntOrNull(radix = 16) ?: 0
                    val trim = (stft - 128) * 100.0f / 128.0f
                    result["stft_bank1"] = trim
                }
            }

            // Short Term Fuel Trim Bank 2
            PIDs.STFT_BANK2 -> {
                if (values.isNotEmpty()) {
                    val stft = values.substring(0, 2).toIntOrNull(radix = 16) ?: 0
                    val trim = (stft - 128) * 100.0f / 128.0f
                    result["stft_bank2"] = trim
                }
            }

            // Long Term Fuel Trim Bank 1
            PIDs.LTFT_BANK1 -> {
                if (values.isNotEmpty()) {
                    val ltft = values.substring(0, 2).toIntOrNull(radix = 16) ?: 0
                    val trim = (ltft - 128) * 100.0f / 128.0f
                    result["ltft_bank1"] = trim
                }
            }

            // Long Term Fuel Trim Bank 2
            PIDs.LTFT_BANK2 -> {
                if (values.isNotEmpty()) {
                    val ltft = values.substring(0, 2).toIntOrNull(radix = 16) ?: 0
                    val trim = (ltft - 128) * 100.0f / 128.0f
                    result["ltft_bank2"] = trim
                }
            }

            // Control Module Voltage (A*256 + B) / 1000
            PIDs.CONTROL_MODULE_VOLTAGE -> {
                if (values.length >= 4) {
                    val a = values.substring(0, 2).toIntOrNull(16) ?: 0
                    val b = values.substring(2, 4).toIntOrNull(16) ?: 0
                    val voltage = ((a * 256 + b) / 1000.0f)
                    result["voltage"] = voltage
                }
            }

            // Timing Advance (A/2 - 64)
            PIDs.TIMING_ADVANCE -> {
                if (values.isNotEmpty()) {
                    val timing = values.substring(0, 2).toIntOrNull(16) ?: 0
                    val advance = (timing / 2.0f) - 64.0f
                    result["timing_advance"] = advance
                }
            }

            // MAF Air Flow Rate (A*256 + B) / 100
            PIDs.MAF_RATE -> {
                if (values.length >= 4) {
                    val a = values.substring(0, 2).toIntOrNull(16) ?: 0
                    val b = values.substring(2, 4).toIntOrNull(16) ?: 0
                    val maf = ((a * 256 + b) / 100.0f)
                    result["maf_rate"] = maf
                }
            }

            // O2 Sensor Voltage (Bank 1, Sensor 1)
            PIDs.O2_VOLTAGE -> {
                if (values.isNotEmpty()) {
                    val voltage = values.substring(0, 2).toIntOrNull(16) ?: 0
                    result["o2_b1s1_voltage"] = (voltage / 200.0f) // 0-1.275V typical
                }
            }

            // Runtime since engine start (A*256 + B)
            PIDs.RUNTIME -> {
                if (values.length >= 4) {
                    val a = values.substring(0, 2).toIntOrNull(16) ?: 0
                    val b = values.substring(2, 4).toIntOrNull(16) ?: 0
                    val runtime = (a * 256 + b).toFloat()
                    result["runtime"] = runtime
                }
            }

            // Distance with MIL on (A*256 + B)
            PIDs.DISTANCE_WITH_MIL -> {
                if (values.length >= 4) {
                    val a = values.substring(0, 2).toIntOrNull(16) ?: 0
                    val b = values.substring(2, 4).toIntOrNull(16) ?: 0
                    val distance = (a * 256 + b).toFloat()
                    result["distance_with_mil"] = distance
                }
            }

            // Barometric Pressure (A)
            PIDs.BAROMETRIC_PRESSURE -> {
                if (values.isNotEmpty()) {
                    val pressure = values.substring(0, 2).toIntOrNull(16) ?: 0
                    result["barometric_pressure"] = pressure.toFloat()
                }
            }

            // Fuel Pressure (A * 3)
            PIDs.FUEL_PRESSURE -> {
                if (values.isNotEmpty()) {
                    val pressure = values.substring(0, 2).toIntOrNull(16) ?: 0
                    result["fuel_pressure"] = (pressure * 3).toFloat()
                }
            }

            // Intake Manifold Absolute Pressure (A)
            PIDs.ENGINE_LOAD_ABSOLUTE -> {
                if (values.isNotEmpty()) {
                    val pressure = values.substring(0, 2).toIntOrNull(16) ?: 0
                    result["intake_manifold_pressure"] = pressure.toFloat()
                }
            }

            // Relative Throttle Position
            PIDs.RELATIVE_THROTTLE -> {
                if (values.isNotEmpty()) {
                    val throttle = values.substring(0, 2).toIntOrNull(16) ?: 0
                    result["relative_throttle"] = (throttle * 100.0f / 255.0f)
                }
            }

            // Ambient Air Temperature
            PIDs.AMBIENT_TEMP -> {
                if (values.isNotEmpty()) {
                    val temp = values.substring(0, 2).toIntOrNull(16) ?: 0
                    val celsius = temp - 40
                    result["ambient_temp"] = celsius.toFloat()
                }
            }

            // Engine Torque
            PIDs.ENGINE_TORQUE -> {
                if (values.isNotEmpty()) {
                    val torque = values.substring(0, 2).toIntOrNull(16) ?: 0
                    result["engine_torque"] = (torque - 125).toFloat()
                }
            }
        }

        return result
    }

    /**
     * Parse Mode 02 response (freeze frame data)
     * Mode 02 PID XX returns freeze frame data for DTC stored at index XX
     * Response format: "42 XX [DTC bytes] [sensor data...]"
     * First 2 bytes of data encode which DTC the freeze frame belongs to
     * Remaining bytes are same format as Mode 01 PIDs
     */
    fun parseMode02Response(raw: String): Map<String, Any> {
        val result = mutableMapOf<String, Any>()
        
        // Clean the response
        val data = raw
            .replace(" ", "")
            .replace("\r", "")
            .replace("\n", "")
            .replace("SEARCHING...", "")
            .replace("NODATA", "")
            .replace("UNABLE TO CONNECT", "")
            .trim()

        // Remove "42" prefix if present (42 = 02 + 40 response code)
        val cleanData = if (data.startsWith("42")) {
            data.substring(2)
        } else if (data.length >= 2 && validMode1Pids.contains(data.substring(0, 2))) {
            data
        } else {
            return result
        }

        if (cleanData.length < 2) return result

        val pid = cleanData.substring(0, 2)
        val values = cleanData.substring(2)

        // Skip first 2 bytes (DTC that caused freeze frame)
        // Then parse sensor data like Mode 01
        if (values.length > 4) {
            val sensorData = values.substring(4)
            val sensorResult = parseSensorBytes(pid, sensorData)
            result.putAll(sensorResult)
        }

        return result
    }

    /**
     * Parse sensor bytes for freeze frame (same encoding as Mode 01)
     */
    private fun parseSensorBytes(pid: String, bytes: String): Map<String, Any> {
        val result = mutableMapOf<String, Any>()
        
        if (bytes.length < 4) return result

        val a = bytes.substring(0, 2).toIntOrNull(16) ?: 0
        val b = bytes.substring(2, 4).toIntOrNull(16) ?: 0

        when (pid) {
            PIDs.ENGINE_RPM -> {
                val rpm = ((a * 256 + b) / 4).toFloat()
                result["rpm"] = rpm
            }
            PIDs.VEHICLE_SPEED -> {
                result["speed"] = a.toFloat()
            }
            PIDs.COOLANT_TEMP -> {
                result["coolant_temp"] = (a - 40).toFloat()
            }
            PIDs.INTAKE_TEMP -> {
                result["intake_temp"] = (a - 40).toFloat()
            }
            PIDs.THROTTLE_POSITION -> {
                result["throttle"] = (a * 100.0f / 255.0f)
            }
            PIDs.FUEL_TANK_LEVEL -> {
                result["fuel_level"] = (a * 100.0f / 255.0f)
            }
            PIDs.ENGINE_LOAD -> {
                result["engine_load"] = (a * 100.0f / 255.0f)
            }
            PIDs.STFT_BANK1 -> {
                result["stft_bank1"] = ((a - 128) * 100.0f / 128.0f)
            }
            PIDs.STFT_BANK2 -> {
                result["stft_bank2"] = ((a - 128) * 100.0f / 128.0f)
            }
            PIDs.LTFT_BANK1 -> {
                result["ltft_bank1"] = ((a - 128) * 100.0f / 128.0f)
            }
            PIDs.LTFT_BANK2 -> {
                result["ltft_bank2"] = ((a - 128) * 100.0f / 128.0f)
            }
            PIDs.CONTROL_MODULE_VOLTAGE -> {
                result["voltage"] = ((a * 256 + b) / 1000.0f)
            }
            PIDs.TIMING_ADVANCE -> {
                result["timing_advance"] = (a / 2.0f) - 64.0f
            }
            PIDs.MAF_RATE -> {
                result["maf_rate"] = ((a * 256 + b) / 100.0f)
            }
            PIDs.ENGINE_LOAD_ABSOLUTE -> {
                result["intake_manifold_pressure"] = a.toFloat()
            }
        }

        return result
    }

    /**
     * Parse Mode 03 response (DTCs)
     */
    fun parseMode03Response(raw: String): List<String> {
        val dtcs = mutableListOf<String>()
        val data = raw.replace(" ", "").replace("\r", "").replace("\n", "").replace("43", "")

        // DTCs are 2 bytes each: first 2 bits = type, remaining 14 bits = code
        var i = 0
        while (i + 3 < data.length) {
            val firstByte = data.substring(i, i + 2).toIntOrNull(16) ?: break
            val secondByte = data.substring(i + 2, i + 4).toIntOrNull(16) ?: break

            val dtcHigh = (firstByte shr 6) and 0x03
            val dtcLow = firstByte and 0x3F
            val dtcCode = ((secondByte shr 6) and 0x03) or ((secondByte and 0x3F) shl 2)

            if (dtcHigh != 0 || dtcCode != 0) {
                val prefix = when (dtcHigh) {
                    0 -> "P"
                    1 -> "C"
                    2 -> "B"
                    3 -> "U"
                    else -> "P"
                }
                val codeNum = String.format("%02X%02X", dtcLow, dtcCode)
                dtcs.add("$prefix$codeNum")
            }
            i += 4
        }
        return dtcs
    }

    /**
     * Parse Mode 04 response (clear DTCs)
     */
    fun parseMode04Response(raw: String): Boolean {
        return raw.contains("44") || raw.contains("CLEAR")
    }

    /**
     * Parse Mode 07 response (pending DTCs)
     */
    fun parseMode07Response(raw: String): List<String> {
        return parseMode03Response(raw)
    }

    /**
     * Parse Mode 09 response (vehicle info)
     */
    fun parseMode09Response(raw: String): Map<String, String> {
        val result = mutableMapOf<String, String>()
        val data = raw.replace(" ", "").replace("\r", "").replace("\n", "")

        if (data.startsWith("49")) {
            val infoType = data.substring(2, 4)
            val infoData = data.substring(4)

            when (infoType) {
                "02" -> result["vin"] = infoData.take(17)
                "04" -> result["calibration_id"] = infoData
                "06" -> result["cvn"] = infoData
            }
        }
        return result
    }

    /**
     * Check if response indicates an error
     */
    fun isErrorResponse(response: String): Boolean {
        val upper = response.uppercase()
        return upper.contains("ERROR") ||
               upper.contains("UNABLE") ||
               upper.contains("NODATA") ||
               upper.contains("STOPPED") ||
               upper.contains("?")
    }

    /**
     * Generate a simulated response for testing
     */
    fun generateSimulatedResponse(command: String): String {
        val cmd = parseCommand(command).uppercase()

        return when {
            cmd.startsWith("010C") -> "41 0C 1A F0"  // RPM ~1700
            cmd.startsWith("010D") -> "41 0D 55"      // Speed 85 km/h
            cmd.startsWith("0105") -> "41 05 6A"     // Coolant 90°C
            cmd.startsWith("010F") -> "41 0F 35"     // Intake 35°C
            cmd.startsWith("0111") -> "41 11 50"     // Throttle 31%
            cmd.startsWith("012F") -> "41 2F 3D"     // Fuel 24%
            cmd.startsWith("0104") -> "41 04 3C"     // Engine Load 24%
            cmd.startsWith("0106") -> "41 06 80"     // STFT Bank1 0%
            cmd.startsWith("0107") -> "41 07 80"     // STFT Bank2 0%
            cmd.startsWith("0108") -> "41 08 80"     // LTFT Bank1 0%
            cmd.startsWith("0109") -> "41 09 80"     // LTFT Bank2 0%
            cmd.startsWith("0142") -> "41 42 0D A8"  // Voltage 13.8V
            cmd.startsWith("010E") -> "41 0E C8"    // Timing Advance -8°
            cmd.startsWith("0110") -> "41 10 00 D0"  // MAF 20.8 g/s
            cmd.startsWith("0114") -> "41 14 80 00"  // O2 B1S1 0.5V
            cmd.startsWith("011F") -> "41 1F 01 90"  // Runtime 400 seconds
            cmd.startsWith("0121") -> "41 21 00 00"  // Distance with MIL 0
            cmd.startsWith("0133") -> "41 33 65"     // Baro 101 kPa
            cmd.startsWith("0146") -> "41 46 22"     // Ambient 22°C
            cmd.startsWith("010A") -> "41 0A 30"     // Fuel Pressure 48 kPa
            cmd.startsWith("0123") -> "41 23 00 00"  // Fuel Rail Pressure 0 kPa
            cmd.startsWith("0143") -> "41 43 00 00"  // Absolute Load 0%
            cmd.startsWith("0144") -> "41 44 80 00"  // Equivalence Ratio 1.0
            cmd.startsWith("0163") -> "41 63 00 00"  // Engine Ref Torque 0 Nm
            cmd.startsWith("0164") -> "41 64 00 00"  // Engine Torque 0%
            cmd.startsWith("0103") -> "43 00 00 00 00 00 00 00"  // Fuel system status
            cmd.startsWith("01") -> "41 ${cmd.substring(2, 4)} 00 00"
            cmd.startsWith("02") -> "42 00 00 00 00 00 00 00"
            cmd.startsWith("03") -> "43 00 00 00 00 00 00 00"
            cmd.startsWith("04") -> "44"
            cmd.startsWith("07") -> "47"
            cmd.startsWith("09") -> "49 00 00 00 00 00 00 00"
            cmd.startsWith("AT") -> "OK"
            else -> "NODATA"
        }
    }

    companion object {
        private const val TAG = "Elm327CommandParser"
    }
}