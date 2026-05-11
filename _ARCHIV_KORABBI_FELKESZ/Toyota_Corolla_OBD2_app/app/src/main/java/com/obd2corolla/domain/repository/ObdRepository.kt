package com.obd2corolla.domain.repository

import com.obd2corolla.domain.model.DtcCode
import com.obd2corolla.domain.model.SensorData

interface ObdRepository {
    suspend fun getSensorData(): List<SensorData>
    suspend fun getDTCs(): List<DtcCode>
    suspend fun clearDTCs()
    suspend fun sendCustomCommand(command: String): String
    suspend fun readPendingDTCs(): List<DtcCode>
    suspend fun readFreezeFrame(dtcCode: String): Map<String, SensorData>?

    data class FreezeFrameData(val dtcCode: String, val sensors: Map<String, SensorData>, val timestamp: Long)

    /**
     * Read a Toyota-specific extended PID from a specific ECU address.
     * Uses multi-frame CAN responses.
     * @param pid Extended PID (e.g., "220100" for RPM)
     * @param address CAN address (e.g., "7E0", "7E2", "7E4")
     * @return SensorData with value, or null if failed
     */
    suspend fun readToyotaSensor(pid: String, address: String): SensorData?

    /**
     * Get hybrid battery info (SOC, current, temperature).
     * @return List of hybrid-related SensorData
     */
    suspend fun hybridBatteryInfo(): List<SensorData>

    /**
     * Get TPMS readings for all four wheels.
     * @return List of TPMS SensorData
     */
    suspend fun tpmsReadings(): List<SensorData>

    /**
     * Get body ECU info (door locks, A/C, cabin temp, ambient temp).
     * @return List of body-related SensorData
     */
    suspend fun bodyInfo(): List<SensorData>
}