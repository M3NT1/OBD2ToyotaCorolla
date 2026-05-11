package com.toyota.obd210.data.obd.model

/**
 * Connection states for OBD2 adapter
 */
enum class ConnectionState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    ERROR
}

/**
 * OBD2 Command Response
 */
data class ObdResponse(
    val success: Boolean,
    val data: String = "",
    val error: String = ""
)

/**
 * Toyota E210 PID definitions
 */
object ToyotaE210Pids {
    // Mode 01 - Current sensor data
    const val PID_ENGINE_LOAD = "0104"
    const val PID_COOLANT_TEMP = "0105"
    const val PID_FUEL_TRIM = "0106"
    const val PID_SHORT_FUEL_TRIM = "0107"
    const val PID_LONG_FUEL_TRIM = "0108"
    const val PID_INTAKE_PRESSURE = "010B"
    const val PID_RPM = "010C"
    const val PID_SPEED = "010D"
    const val PID_TIMING_ADVANCE = "010E"
    const val PID_INTAKE_TEMP = "010F"
    const val PID_MAF = "0110"
    const val PID_THROTTLE = "0111"
    const val PID_RUN_TIME = "011F"
    const val PID_FUEL_LEVEL = "012F"
    const val PID_VOLTAGE = "0142"
    const val PID_AMBIENT_TEMP = "0146"
    const val PID_GEAR = "0151"
    
    // Hybrid specific PIDs
    const val PID_HYBRID_SOC = "014D"
    const val PID_EV_MODE = "021B"
    const val PID_MOTOR_TORQUE = "0220"
    const val PID_BATTERY_CURRENT = "0223"
    const val PID_BATTERY_VOLTAGE = "0224"
}

/**
 * ELM327 Commands for Toyota E210
 */
object Elm327Commands {
    const val RESET_ADAPTER = "ATZ"
    const val ECHO_OFF = "ATE0"
    const val LINE_FEED_OFF = "ATL0"
    const val SPACES_OFF = "ATS0"
    const val PROTOCOL_AUTO = "ATSP0"
    const val HEADERS_OFF = "ATH0"
    const val SET_HEADER_TOYOTA = "ATSH7E4"
    const val FORDUMP_FORMATTED = "ATFSPH7E4"
    const val PROTOCOL_HEADER = "ATCP7E4"
    const val READ_VOLTAGE = "ATRV"
    const val VERSION = "ATI"
    const val DEVICE_ID = "AT@1"
    
    // Mode 01 commands (read current data)
    fun readPid(pid: String) = "${pid.substring(0,2)} ${pid.substring(2)}"
    
    // Mode 03 (Read DTCs)
    const val READ_STORED_DTC = "03"
    const val READ_PENDING_DTC = "07"
    const val READ_PERMANENT_DTC = "0A"
    
    // Mode 04 (Clear DTCs)
    const val CLEAR_DTC = "04"
    
    // Mode 09 (Vehicle Identification)
    const val MODE_09_READ_VIN = "09 02"  // PID 02: Vehicle Identification Number (VIN)
    const val MODE_09_READ_CAL_ID = "09 04"  // PID 04: Calibration ID
    const val MODE_09_READ_CVN = "09 06"  // PID 06: Calibration Verification Number
    const val MODE_09_READ_PIDS = "09 00"  // PID 00: Mode 09 PIDs Supported
    
    // Mode 22 (Toyota custom)
    fun readCustomRegister(register: String): String {
        val bytes = register.split(" ").map { it.toInt(16) }
        return "22 ${bytes.joinToString(" ") { String.format("%02X", it) }}"
    }
    
    fun writeCustomRegister(register: String, value: Int): String {
        val bytes = register.split(" ").map { it.toInt(16) }
        return "22 ${bytes.joinToString(" ") { String.format("%02X", it) }} ${String.format("%02X", value)}"
    }
}