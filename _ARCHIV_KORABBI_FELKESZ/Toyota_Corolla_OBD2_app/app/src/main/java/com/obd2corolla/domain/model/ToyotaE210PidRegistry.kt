package com.obd2corolla.domain.model

/**
 * Toyota E210 (Corolla 2019+ Hybrid 2.0L) specific CAN extended PID registry.
 * These PIDs use non-standard addressing and require multi-frame responses.
 */
object ToyotaE210PidRegistry {

    /**
     * Toyota CAN addresses for different ECUs
     */
    object Address {
        const val ENGINE_7E0 = "7E0"
        const val ENGINE_7E1 = "7E1"
        const val HYBRID_7E2 = "7E2"
        const val HYBRID_7E3 = "7E3"
        const val BODY_7E4 = "7E4"
        const val BODY_7E5 = "7E5"
        const val TPMS_7E6 = "7E6"
        const val TPMS_7E7 = "7E7"
    }

    /**
     * Engine ECU (7E0/7E1) PIDs - Mode 22 (custom)
     */
    object Engine {
        const val PID_RPM = "220100"
        const val PID_SPEED = "220101"
        const val PID_COOLANT = "220102"
        const val PID_THROTTLE = "22011A"
        const val PID_ACCELERATOR = "22011B"
        const val PID_HV_VOLTAGE = "220120"
        const val PID_MG1_RPM = "220121"
        const val PID_MG2_RPM = "220122"

        fun getAllPids(): List<ToyotaPid> = listOf(
            ToyotaPid(PID_RPM, "Engine RPM", "RPM", Address.ENGINE_7E0, true, false),
            ToyotaPid(PID_SPEED, "Vehicle Speed", "km/h", Address.ENGINE_7E0, true, false),
            ToyotaPid(PID_COOLANT, "Coolant Temperature", "°C", Address.ENGINE_7E0, true, false),
            ToyotaPid(PID_THROTTLE, "Throttle Position", "%", Address.ENGINE_7E0, true, false),
            ToyotaPid(PID_ACCELERATOR, "Accelerator Position", "%", Address.ENGINE_7E0, true, false),
            ToyotaPid(PID_HV_VOLTAGE, "HV Battery Voltage", "V", Address.ENGINE_7E0, true, false),
            ToyotaPid(PID_MG1_RPM, "MG1 Motor RPM", "RPM", Address.ENGINE_7E0, true, false),
            ToyotaPid(PID_MG2_RPM, "MG2 Motor RPM", "RPM", Address.ENGINE_7E0, true, false)
        )
    }

    /**
     * Body ECU (7E4/7E5) PIDs - Mode 28 (custom)
     */
    object Body {
        const val PID_DOOR_LOCKS = "280102"
        const val PID_AC_STATUS = "280200"
        const val PID_CABIN_TEMP = "280201"
        const val PID_AMBIENT_TEMP = "280202"

        fun getAllPids(): List<ToyotaPid> = listOf(
            ToyotaPid(PID_DOOR_LOCKS, "Door Lock Status", "", Address.BODY_7E4, true, false),
            ToyotaPid(PID_AC_STATUS, "A/C Status", "", Address.BODY_7E4, true, false),
            ToyotaPid(PID_CABIN_TEMP, "Cabin Temperature", "°C", Address.BODY_7E4, true, false),
            ToyotaPid(PID_AMBIENT_TEMP, "Ambient Temperature", "°C", Address.BODY_7E4, true, false)
        )
    }

    /**
     * Hybrid ECU (7E2/7E3) PIDs - Mode 23 (custom)
     */
    object Hybrid {
        const val PID_HV_BATTERY_SOC = "230001"
        const val PID_HV_CURRENT = "230002"
        const val PID_HV_TEMP = "230003"
        const val PID_TORQUE_1 = "230004"
        const val PID_TORQUE_2 = "230005"
        const val PID_TORQUE_3 = "230006"

        fun getAllPids(): List<ToyotaPid> = listOf(
            ToyotaPid(PID_HV_BATTERY_SOC, "HV Battery SOC", "%", Address.HYBRID_7E2, true, false),
            ToyotaPid(PID_HV_CURRENT, "HV Battery Current", "A", Address.HYBRID_7E2, true, false),
            ToyotaPid(PID_HV_TEMP, "HV Battery Temperature", "°C", Address.HYBRID_7E2, true, false),
            ToyotaPid(PID_TORQUE_1, "Motor Torque 1", "Nm", Address.HYBRID_7E2, true, false),
            ToyotaPid(PID_TORQUE_2, "Motor Torque 2", "Nm", Address.HYBRID_7E2, true, false),
            ToyotaPid(PID_TORQUE_3, "Motor Torque 3", "Nm", Address.HYBRID_7E2, true, false)
        )
    }

    /**
     * TPMS ECU (7E6/7E7) PIDs - Mode 24 (custom)
     */
    object TPMS {
        const val PID_WHEEL_FL = "240100"
        const val PID_WHEEL_FR = "240101"
        const val PID_WHEEL_RL = "240102"
        const val PID_WHEEL_RR = "240103"

        fun getAllPids(): List<ToyotaPid> = listOf(
            ToyotaPid(PID_WHEEL_FL, "Front Left Tire Pressure", "PSI", Address.TPMS_7E6, true, false),
            ToyotaPid(PID_WHEEL_FR, "Front Right Tire Pressure", "PSI", Address.TPMS_7E6, true, false),
            ToyotaPid(PID_WHEEL_RL, "Rear Left Tire Pressure", "PSI", Address.TPMS_7E6, true, false),
            ToyotaPid(PID_WHEEL_RR, "Rear Right Tire Pressure", "PSI", Address.TPMS_7E6, true, false)
        )
    }

    /**
     * Get all Toyota-specific PIDs from all ECUs
     */
    fun getAllPids(): List<ToyotaPid> {
        return Engine.getAllPids() + Body.getAllPids() + Hybrid.getAllPids() + TPMS.getAllPids()
    }

    /**
     * Find a PID definition by its hex code
     */
    fun findByPid(pid: String): ToyotaPid? {
        return getAllPids().find { it.pid.equals(pid, ignoreCase = true) }
    }

    /**
     * Find PIDs by address
     */
    fun findByAddress(address: String): List<ToyotaPid> {
        return getAllPids().filter { it.address.equals(address, ignoreCase = true) }
    }
}

/**
 * Represents a Toyota-specific OBD PID
 */
data class ToyotaPid(
    val pid: String,
    val name: String,
    val unit: String,
    val address: String,
    val isReadable: Boolean,
    val isWriteable: Boolean
)