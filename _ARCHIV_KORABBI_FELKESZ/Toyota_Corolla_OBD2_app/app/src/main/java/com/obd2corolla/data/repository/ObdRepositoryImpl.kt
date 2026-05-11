package com.obd2corolla.data.repository

import android.util.Log
import com.obd2corolla.data.local.DtcDescriptions
import com.obd2corolla.data.local.dao.DtcDao
import com.obd2corolla.data.local.dao.SensorCacheDao
import com.obd2corolla.data.remote.BluetoothManager
import com.obd2corolla.data.remote.Elm327CommandParser
import com.obd2corolla.data.remote.Elm327V15Incompatible
import com.obd2corolla.domain.model.DtcCode
import com.obd2corolla.domain.model.DtcSeverity
import com.obd2corolla.domain.model.SensorData
import com.obd2corolla.domain.model.SensorType
import com.obd2corolla.domain.model.ToyotaE210PidRegistry
import com.obd2corolla.domain.model.ToyotaPid
import com.obd2corolla.domain.repository.ObdRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * OBD Repository implementation that handles real sensor reading
 * and data caching for the Toyota Corolla OBD2 app.
 */
@Singleton
class ObdRepositoryImpl @Inject constructor(
    private val bluetoothManager: BluetoothManager,
    private val sensorCacheDao: SensorCacheDao,
    private val dtcDao: DtcDao
) : ObdRepository {

    // Standard OBD2 Mode 01 PIDs for Toyota Corolla
    private val supportedPids = listOf(
        "010C" to SensorConfig("Engine RPM", "RPM", 0f, 8000f),
        "010D" to SensorConfig("Vehicle Speed", "km/h", 0f, 200f),
        "0105" to SensorConfig("Coolant Temperature", "°C", -40f, 215f),
        "010F" to SensorConfig("Intake Air Temperature", "°C", -40f, 215f),
        "0111" to SensorConfig("Throttle Position", "%", 0f, 100f),
        "012F" to SensorConfig("Fuel Tank Level", "%", 0f, 100f),
        "0104" to SensorConfig("Engine Load", "%", 0f, 100f),
        "0106" to SensorConfig("Short Term Fuel Trim B1", "%", -100f, 100f),
        "0107" to SensorConfig("Short Term Fuel Trim B2", "%", -100f, 100f),
        "0108" to SensorConfig("Long Term Fuel Trim B1", "%", -100f, 100f),
        "0109" to SensorConfig("Long Term Fuel Trim B2", "%", -100f, 100f),
        "0142" to SensorConfig("Control Module Voltage", "V", 0f, 16f),
        "010E" to SensorConfig("Timing Advance", "°", -64f, 64f),
        "0110" to SensorConfig("MAF Air Flow Rate", "g/s", 0f, 655f),
        "0114" to SensorConfig("O2 Sensor Voltage B1S1", "V", 0f, 1.275f),
        "011F" to SensorConfig("Engine Runtime", "s", 0f, 65535f),
        "0121" to SensorConfig("Distance with MIL", "km", 0f, 65535f),
        "0133" to SensorConfig("Barometric Pressure", "kPa", 0f, 255f),
        "0146" to SensorConfig("Ambient Air Temp", "°C", -40f, 215f),
        "0143" to SensorConfig("Absolute Load", "%", 0f, 100f),
        "0164" to SensorConfig("Engine Torque", "%", -125f, 125f)
    )

    // Fallback simulated data for when not connected
    private val simulatedSensorData = listOf(
        SensorData("010C", "Engine RPM", 2500f, "RPM", System.currentTimeMillis(), 0f, 8000f),
        SensorData("010D", "Vehicle Speed", 65f, "km/h", System.currentTimeMillis(), 0f, 200f),
        SensorData("0105", "Coolant Temperature", 88f, "°C", System.currentTimeMillis(), -40f, 215f),
        SensorData("010F", "Intake Air Temperature", 32f, "°C", System.currentTimeMillis(), -40f, 215f),
        SensorData("0111", "Throttle Position", 25f, "%", System.currentTimeMillis(), 0f, 100f),
        SensorData("012F", "Fuel Tank Level", 58f, "%", System.currentTimeMillis(), 0f, 100f),
        SensorData("0104", "Engine Load", 24f, "%", System.currentTimeMillis(), 0f, 100f),
        SensorData("0106", "Short Term Fuel Trim B1", 0f, "%", System.currentTimeMillis(), -100f, 100f),
        SensorData("0107", "Short Term Fuel Trim B2", 0f, "%", System.currentTimeMillis(), -100f, 100f),
        SensorData("0108", "Long Term Fuel Trim B1", 0f, "%", System.currentTimeMillis(), -100f, 100f),
        SensorData("0109", "Long Term Fuel Trim B2", 0f, "%", System.currentTimeMillis(), -100f, 100f),
        SensorData("0142", "Control Module Voltage", 13.8f, "V", System.currentTimeMillis(), 0f, 16f),
        SensorData("010E", "Timing Advance", -8f, "°", System.currentTimeMillis(), -64f, 64f),
        SensorData("0110", "MAF Air Flow Rate", 20.8f, "g/s", System.currentTimeMillis(), 0f, 655f),
        SensorData("0114", "O2 Sensor Voltage B1S1", 0.45f, "V", System.currentTimeMillis(), 0f, 1.275f),
        SensorData("011F", "Engine Runtime", 400f, "s", System.currentTimeMillis(), 0f, 65535f),
        SensorData("0121", "Distance with MIL", 0f, "km", System.currentTimeMillis(), 0f, 65535f),
        SensorData("0133", "Barometric Pressure", 101f, "kPa", System.currentTimeMillis(), 0f, 255f),
        SensorData("0146", "Ambient Air Temp", 22f, "°C", System.currentTimeMillis(), -40f, 215f),
        SensorData("0143", "Absolute Load", 24f, "%", System.currentTimeMillis(), 0f, 100f),
        SensorData("0164", "Engine Torque", 0f, "%", System.currentTimeMillis(), -125f, 125f)
    )

    /**
     * Get all sensor data from real OBD2 connection or simulated fallback
     */
    override suspend fun getSensorData(): List<SensorData> = withContext(Dispatchers.IO) {
        // Check connection state
        val connectionState = bluetoothManager.connectionState.value
        
        if (connectionState != BluetoothManager.ConnectionState.CONNECTED) {
            Log.d(TAG, "Not connected (state: $connectionState), returning simulated data")
            return@withContext getSimulatedSensorData()
        }

        try {
            readRealSensorData()
        } catch (e: Exception) {
            Log.e(TAG, "Error reading real sensors, falling back to simulated", e)
            getSimulatedSensorData()
        }
    }

    /**
     * Read real sensor data from ELM327 adapter
     */
    private suspend fun readRealSensorData(): List<SensorData> {
        val timestamp = System.currentTimeMillis()
        val sensorList = mutableListOf<SensorData>()

        // Read all PIDs and collect results
        for ((pid, config) in supportedPids) {
            try {
                val command = pid.substring(2) // Remove "01" prefix
                val response = bluetoothManager.sendCommand(pid)
                
                if (response.isNotEmpty() && !isErrorResponse(response)) {
                    val parsed = parseSensorValue(pid, response, timestamp)
                    if (parsed != null) {
                        sensorList.add(parsed)
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to read PID $pid: ${e.message}")
                // Add simulated value for this sensor
                sensorList.add(createSimulatedSensor(pid, config, timestamp))
            }
            
            // Small delay between commands for ELM327
            kotlinx.coroutines.delay(50)
        }

        // If no real data was collected, use simulated
        if (sensorList.isEmpty()) {
            Log.w(TAG, "No real data collected, using simulated")
            return getSimulatedSensorData()
        }

        return sensorList
    }

    /**
     * Parse a sensor value from OBD response
     */
    private fun parseSensorValue(pid: String, response: String, timestamp: Long): SensorData? {
        val parsed = Elm327CommandParser().parseMode01Response(response)
        
        if (parsed.isEmpty()) return null

        val (name, unit, minVal, maxVal) = supportedPids.find { it.first == pid }?.second
            ?: return null

        val rawValue = parsed.values.firstOrNull() as? Float ?: return null

        return SensorData(
            pid = pid,
            name = name,
            value = rawValue,
            unit = unit,
            timestamp = timestamp,
            minValue = minVal,
            maxValue = maxVal
        )
    }

    /**
     * Check if response indicates an error
     */
    private fun isErrorResponse(response: String): Boolean {
        val upper = response.uppercase()
        return upper.contains("ERROR") ||
               upper.contains("NODATA") ||
               upper.contains("UNABLE") ||
               upper.contains("STOPPED") ||
               upper.contains("?")
    }

    /**
     * Create a simulated sensor reading
     */
    private fun createSimulatedSensor(pid: String, config: SensorConfig, timestamp: Long): SensorData {
        return SensorData(
            pid = pid,
            name = config.name,
            value = config.defaultValue,
            unit = config.unit,
            timestamp = timestamp,
            minValue = config.minValue,
            maxValue = config.maxValue
        )
    }

    /**
     * Get simulated sensor data with updated timestamp
     */
    private fun getSimulatedSensorData(): List<SensorData> {
        return simulatedSensorData.map { it.copy(timestamp = System.currentTimeMillis()) }
    }

    /**
     * Get a single sensor value by PID
     */
    suspend fun getSensorByPid(pid: String): SensorData? = withContext(Dispatchers.IO) {
        val connectionState = bluetoothManager.connectionState.value
        
        if (connectionState != BluetoothManager.ConnectionState.CONNECTED) {
            return@withContext simulatedSensorData.find { it.pid == pid }
        }

        try {
            val response = bluetoothManager.sendCommand(pid)
            if (!isErrorResponse(response)) {
                parseSensorValue(pid, response, System.currentTimeMillis())
            } else {
                simulatedSensorData.find { it.pid == pid }
            }
        } catch (e: Exception) {
            simulatedSensorData.find { it.pid == pid }
        }
    }

    /**
     * Read DTCs from the vehicle
     */
    override suspend fun getDTCs(): List<DtcCode> = withContext(Dispatchers.IO) {
        if (bluetoothManager.connectionState.value != BluetoothManager.ConnectionState.CONNECTED) {
            Log.d(TAG, "Not connected, checking local cache")
            return@withContext getCachedDTCs()
        }

        try {
            // Mode 03: Read DTCs
            val response = bluetoothManager.sendCommand("03")
            val dtcCodes = Elm327CommandParser().parseMode03Response(response)
            
            if (dtcCodes.isEmpty()) {
                Log.d(TAG, "No DTCs found")
                return@withContext emptyList()
            }

            // Convert codes to DtcCode objects
            dtcCodes.map { code ->
                DtcCode(
                    code = code,
                    description = DtcDescriptions.getDescription(code),
                    severity = getDtcSeverity(code),
                    timestamp = System.currentTimeMillis()
                )
            }.also { dtcs ->
                Log.d(TAG, "Found ${dtcs.size} DTCs")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read DTCs", e)
            getCachedDTCs()
        }
    }

    /**
     * Get DTC description based on code
     */
    private fun getDtcDescription(code: String): String {
        return commonDtcDescriptions[code.uppercase()] ?: "Unknown fault code: $code"
    }

    /**
     * Determine DTC severity
     */
    private fun getDtcSeverity(code: String): DtcSeverity {
        return when {
            code.startsWith("P0") || code.startsWith("P2") -> DtcSeverity.CRITICAL
            code.startsWith("P1") || code.startsWith("P3") -> DtcSeverity.WARNING
            code.startsWith("C0") || code.startsWith("C1") -> DtcSeverity.WARNING
            code.startsWith("B0") || code.startsWith("B1") -> DtcSeverity.WARNING
            code.startsWith("U0") || code.startsWith("U1") -> DtcSeverity.INFO
            else -> DtcSeverity.WARNING
        }
    }

    /**
     * Clear DTCs from vehicle and local cache
     */
    override suspend fun clearDTCs() {
        if (bluetoothManager.connectionState.value == BluetoothManager.ConnectionState.CONNECTED) {
            try {
                // Mode 04: Clear DTCs
                val response = bluetoothManager.sendCommand("04")
                Log.d(TAG, "Clear DTCs response: $response")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to clear DTCs from vehicle", e)
            }
        }
        
        // Clear local cache
        dtcDao.deleteAll()
        Log.d(TAG, "Local DTC cache cleared")
    }

    /**
     * Send a custom OBD command
     */
    override suspend fun sendCustomCommand(command: String): String = withContext(Dispatchers.IO) {
        if (bluetoothManager.connectionState.value != BluetoothManager.ConnectionState.CONNECTED) {
            Log.w(TAG, "Not connected, cannot send custom command")
            return@withContext "ERROR: Not connected"
        }

        try {
            bluetoothManager.sendCommand(command)
        } catch (e: Exception) {
            Log.e(TAG, "Custom command failed: $command", e)
            "ERROR: ${e.message}"
        }
    }

    /**
     * Get cached DTCs from local database
     */
    private suspend fun getCachedDTCs(): List<DtcCode> {
        val dtcs = mutableListOf<DtcCode>()
        dtcDao.getAllDTCs().collect { entities ->
            dtcs.clear()
            dtcs.addAll(entities.map { entity ->
                DtcCode(
                    code = entity.code,
                    description = entity.description,
                    severity = DtcSeverity.valueOf(entity.severity),
                    timestamp = entity.timestamp
                )
            })
        }
        return dtcs.toList()
    }

    /**
     * Get all supported sensor PIDs
     */
    fun getSupportedPids(): List<String> = supportedPids.map { it.first }

    /**
     * Check if vehicle supports a specific PID
     */
    suspend fun isPidSupported(pid: String): Boolean = withContext(Dispatchers.IO) {
        if (bluetoothManager.connectionState.value != BluetoothManager.ConnectionState.CONNECTED) {
            return@withContext supportedPids.any { it.first == pid }
        }

        try {
            // Mode 01 PID 00 returns which PIDs are supported
            // For simplicity, assume supported if response is valid
            val response = bluetoothManager.sendCommand(pid)
            !isErrorResponse(response) && response.isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Read a Toyota-specific extended PID from a specific ECU address.
     * Uses multi-frame CAN responses (ISO 15765-2).
     */
    override suspend fun readToyotaSensor(pid: String, address: String): SensorData? = withContext(Dispatchers.IO) {
        if (bluetoothManager.connectionState.value != BluetoothManager.ConnectionState.CONNECTED) {
            Log.d(TAG, "Not connected, cannot read Toyota sensor $pid")
            return@withContext null
        }

        val pidDef = ToyotaE210PidRegistry.findByPid(pid)
        val timestamp = System.currentTimeMillis()

        try {
            val response = bluetoothManager.sendToyotaCommand(address, pid)
            
            if (isErrorResponse(response)) {
                Log.w(TAG, "Toyota sensor $pid returned error: $response")
                return@withContext createSimulatedToyotaSensor(pidDef, pid, timestamp)
            }

            // Parse the multi-frame response
            val value = parseToyotaResponse(pid, response)
            if (value != null) {
                SensorData(
                    pid = pid,
                    name = pidDef?.name ?: "Unknown Toyota Sensor",
                    value = value,
                    unit = pidDef?.unit ?: "",
                    timestamp = timestamp,
                    minValue = 0f,
                    maxValue = getMaxValueForToyotaPid(pid),
                    sensorType = getSensorTypeForPid(pid),
                    address = address,
                    isWriteable = pidDef?.isWriteable ?: false
                )
            } else {
                createSimulatedToyotaSensor(pidDef, pid, timestamp)
            }
        } catch (e: Elm327V15Incompatible) {
            Log.e(TAG, "ELM327 v1.5 incompatibility for PID $pid", e)
            createSimulatedToyotaSensor(pidDef, pid, timestamp)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read Toyota sensor $pid", e)
            createSimulatedToyotaSensor(pidDef, pid, timestamp)
        }
    }

    /**
     * Parse Toyota extended PID response value.
     * Returns null if parsing fails.
     */
    private fun parseToyotaResponse(pid: String, response: String): Float? {
        // Remove whitespace and common noise
        val clean = response.replace(" ", "").replace("\r", "").replace("\n", "").replace(">", "")
        
        // Multi-frame response parsing depends on PID
        // For now, extract hex digits and convert
        val hexDigits = clean.filter { it.isDigit() || it in 'A'..'F' || it in 'a'..'f' }
        
        if (hexDigits.length < 4) return null
        
        // Parse based on PID type
        return when {
            pid.endsWith("00") || pid.endsWith("01") -> {
                // First/second byte values (e.g., RPM, speed)
                val firstByte = hexDigits.substring(0, 2).toIntOrNull(16) ?: return null
                val secondByte = if (hexDigits.length >= 4) hexDigits.substring(2, 4).toIntOrNull(16) ?: 0 else 0
                when {
                    pid.startsWith("2201") -> (firstByte * 256 + secondByte) / 4f  // RPM
                    pid.startsWith("2200") -> (firstByte * 256 + secondByte) / 4f  // RPM (alt)
                    else -> (firstByte * 256 + secondByte).toFloat()
                }
            }
            pid.endsWith("02") -> {
                // Temperature (A - 40)
                val temp = hexDigits.substring(0, 2).toIntOrNull(16) ?: return null
                (temp - 40).toFloat()
            }
            pid.endsWith("1A") || pid.endsWith("1B") -> {
                // Throttle/Accelerator position (A * 100 / 255)
                val val8 = hexDigits.substring(0, 2).toIntOrNull(16) ?: return null
                (val8 * 100.0f / 255.0f)
            }
            pid.endsWith("20") -> {
                // HV Battery voltage (A * 256 + B) / 10
                if (hexDigits.length >= 4) {
                    val a = hexDigits.substring(0, 2).toIntOrNull(16) ?: return null
                    val b = hexDigits.substring(2, 4).toIntOrNull(16) ?: 0
                    ((a * 256 + b) / 10.0f)
                } else null
            }
            pid.endsWith("21") || pid.endsWith("22") -> {
                // Motor RPM (A * 256 + B) with sign
                if (hexDigits.length >= 4) {
                    val a = hexDigits.substring(0, 2).toIntOrNull(16) ?: return null
                    val b = hexDigits.substring(2, 4).toIntOrNull(16) ?: 0
                    ((a * 256 + b) / 2.0f)
                } else null
            }
            pid.startsWith("230001") -> {
                // HV Battery SOC (A * 100 / 255)
                val soc = hexDigits.substring(0, 2).toIntOrNull(16) ?: return null
                (soc * 100.0f / 255.0f)
            }
            pid.startsWith("230002") -> {
                // HV Current (A * 256 + B, signed)
                if (hexDigits.length >= 4) {
                    val a = hexDigits.substring(0, 2).toIntOrNull(16) ?: return null
                    val b = hexDigits.substring(2, 4).toIntOrNull(16) ?: 0
                    ((a * 256 + b) - 32768).toFloat()
                } else null
            }
            pid.startsWith("230003") -> {
                // HV Temperature (A - 40)
                val temp = hexDigits.substring(0, 2).toIntOrNull(16) ?: return null
                (temp - 40).toFloat()
            }
            pid.startsWith("2401") -> {
                // TPMS pressure (A * 2.5 in kPa, convert to PSI)
                val kpa = hexDigits.substring(0, 2).toIntOrNull(16) ?: return null
                (kpa * 2.5f * 0.145038f)  // kPa to PSI
            }
            else -> {
                // Generic parsing - take first data bytes
                val dataBytes = hexDigits.drop(2) // Remove mode/response code
                if (dataBytes.length >= 2) {
                    dataBytes.substring(0, 2).toIntOrNull(16)?.toFloat()
                } else null
            }
        }
    }

    /**
     * Determine SensorType based on PID
     */
    private fun getSensorTypeForPid(pid: String): SensorType {
        return when {
            pid.startsWith("22") -> SensorType.TOYOTA_EXTENDED
            pid.startsWith("23") -> SensorType.HYBRID
            pid.startsWith("24") -> SensorType.TPMS
            pid.startsWith("28") -> SensorType.TOYOTA_EXTENDED
            else -> SensorType.STANDARD
        }
    }

    /**
     * Get max value for a Toyota PID type
     */
    private fun getMaxValueForToyotaPid(pid: String): Float {
        return when {
            pid.endsWith("00") || pid.endsWith("01") -> 8000f  // RPM
            pid.endsWith("02") -> 215f  // Temperature
            pid.endsWith("1A") || pid.endsWith("1B") -> 100f  // Position %
            pid.endsWith("20") -> 800f  // HV Voltage
            pid.endsWith("21") || pid.endsWith("22") -> 20000f  // Motor RPM
            pid.startsWith("230001") -> 100f  // SOC %
            pid.startsWith("230002") -> 400f  // Current
            pid.startsWith("230003") -> 100f  // Temperature
            pid.startsWith("230004") || pid.startsWith("230005") || pid.startsWith("230006") -> 350f  // Torque
            pid.startsWith("2401") -> 60f  // TPMS PSI
            pid.endsWith("01") || pid.endsWith("02") -> 50f  // Various
            else -> 255f
        }
    }

    /**
     * Create simulated Toyota sensor data
     */
    private fun createSimulatedToyotaSensor(pidDef: ToyotaPid?, pid: String, timestamp: Long): SensorData {
        return SensorData(
            pid = pid,
            name = pidDef?.name ?: "Toyota Sensor",
            value = getSimulatedToyotaValue(pid),
            unit = pidDef?.unit ?: "",
            timestamp = timestamp,
            minValue = 0f,
            maxValue = getMaxValueForToyotaPid(pid),
            sensorType = getSensorTypeForPid(pid),
            address = pidDef?.address ?: "",
            isWriteable = pidDef?.isWriteable ?: false
        )
    }

    /**
     * Get simulated value for Toyota sensor
     */
    private fun getSimulatedToyotaValue(pid: String): Float {
        return when {
            pid.endsWith("00") || pid.endsWith("01") -> 2500f  // RPM ~2500
            pid.endsWith("02") -> 88f  // Coolant 88°C
            pid.endsWith("1A") -> 25f  // Throttle 25%
            pid.endsWith("1B") -> 20f  // Accelerator 20%
            pid.endsWith("20") -> 201.6f  // HV Voltage ~201.6V
            pid.endsWith("21") -> 3000f  // MG1 RPM
            pid.endsWith("22") -> 2500f  // MG2 RPM
            pid.startsWith("230001") -> 65f  // SOC ~65%
            pid.startsWith("230002") -> 0f  // Current ~0A
            pid.startsWith("230003") -> 28f  // HV Temp ~28°C
            pid.startsWith("230004") || pid.startsWith("230005") || pid.startsWith("230006") -> 0f  // Torque
            pid.startsWith("2401") -> 32f  // TPMS ~32 PSI
            else -> 0f
        }
    }

    /**
     * Get hybrid battery info (SOC, current, temperature, etc.)
     */
    override suspend fun hybridBatteryInfo(): List<SensorData> = withContext(Dispatchers.IO) {
        val timestamp = System.currentTimeMillis()
        val address = ToyotaE210PidRegistry.Address.HYBRID_7E2
        
        ToyotaE210PidRegistry.Hybrid.getAllPids().mapNotNull { pidDef ->
            readToyotaSensor(pidDef.pid, address)
        }.ifEmpty {
            // Return simulated data if real reading fails
            ToyotaE210PidRegistry.Hybrid.getAllPids().map { pidDef ->
                createSimulatedToyotaSensor(pidDef, pidDef.pid, timestamp)
            }
        }
    }

    /**
     * Get TPMS readings for all four wheels.
     */
    override suspend fun tpmsReadings(): List<SensorData> = withContext(Dispatchers.IO) {
        val timestamp = System.currentTimeMillis()
        val address = ToyotaE210PidRegistry.Address.TPMS_7E6
        
        ToyotaE210PidRegistry.TPMS.getAllPids().mapNotNull { pidDef ->
            readToyotaSensor(pidDef.pid, address)
        }.ifEmpty {
            ToyotaE210PidRegistry.TPMS.getAllPids().map { pidDef ->
                createSimulatedToyotaSensor(pidDef, pidDef.pid, timestamp)
            }
        }
    }

    /**
     * Get body ECU info (door locks, A/C status, cabin temp, ambient temp).
     */
    override suspend fun bodyInfo(): List<SensorData> = withContext(Dispatchers.IO) {
        val timestamp = System.currentTimeMillis()
        val address = ToyotaE210PidRegistry.Address.BODY_7E4
        
        ToyotaE210PidRegistry.Body.getAllPids().mapNotNull { pidDef ->
            readToyotaSensor(pidDef.pid, address)
        }.ifEmpty {
            ToyotaE210PidRegistry.Body.getAllPids().map { pidDef ->
                createSimulatedToyotaSensor(pidDef, pidDef.pid, timestamp)
            }
        }
    }

    /**
     * Read pending DTCs (Mode 07) - detected this drive cycle but not yet confirmed
     */
    override suspend fun readPendingDTCs(): List<DtcCode> = withContext(Dispatchers.IO) {
        if (bluetoothManager.connectionState.value != BluetoothManager.ConnectionState.CONNECTED) {
            Log.d(TAG, "Not connected, returning empty pending DTCs")
            return@withContext emptyList()
        }

        try {
            val response = bluetoothManager.sendCommand("07")
            val dtcCodes = Elm327CommandParser().parseMode07Response(response)
            
            if (dtcCodes.isEmpty()) {
                return@withContext emptyList()
            }

            dtcCodes.map { code ->
                DtcCode(
                    code = code,
                    description = DtcDescriptions.getDescription(code),
                    severity = DtcSeverity.INFO,
                    timestamp = System.currentTimeMillis()
                )
            }.also { dtcs ->
                Log.d(TAG, "Found ${dtcs.size} pending DTCs")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read pending DTCs", e)
            emptyList()
        }
    }

    /**
     * Read freeze frame data (Mode 02) for a specific DTC index.
     * Returns a map of PID to SensorData recorded when the fault occurred.
     */
    override suspend fun readFreezeFrame(dtcCode: String): Map<String, SensorData>? = withContext(Dispatchers.IO) {
        if (bluetoothManager.connectionState.value != BluetoothManager.ConnectionState.CONNECTED) {
            Log.d(TAG, "Not connected, cannot read freeze frame")
            return@withContext null
        }

        try {
            val timestamp = System.currentTimeMillis()
            val freezeFramePids = listOf(
                "010C" to "Engine RPM",
                "010D" to "Vehicle Speed",
                "0105" to "Coolant Temperature",
                "010F" to "Intake Air Temperature",
                "0111" to "Throttle Position",
                "0104" to "Engine Load",
                "0106" to "Short Term Fuel Trim B1",
                "0108" to "Long Term Fuel Trim B1",
                "0142" to "Control Module Voltage",
                "010E" to "Timing Advance",
                "0110" to "MAF Air Flow Rate",
                "0143" to "Absolute Load",
                "012F" to "Fuel Tank Level",
                "0123" to "Intake Manifold Pressure"
            )

            val sensors = mutableMapOf<String, SensorData>()
            
            for ((pid, name) in freezeFramePids) {
                try {
                    val mode02Pid = "02${pid.substring(2)}"
                    val response = bluetoothManager.sendCommand(mode02Pid)
                    
                    if (!isErrorResponse(response) && response.isNotEmpty()) {
                        val parsed = Elm327CommandParser().parseMode02Response(response)
                        
                        parsed.forEach { (key, value) ->
                            val sensor = createSensorFromParsedData(pid, name, key, value, timestamp)
                            if (sensor != null) {
                                sensors[pid] = sensor
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to read freeze frame PID $pid: ${e.message}")
                }
                kotlinx.coroutines.delay(30)
            }

            if (sensors.isEmpty()) {
                Log.d(TAG, "No freeze frame data retrieved, returning simulated")
                getSimulatedFreezeFrameData(timestamp)
            } else {
                sensors
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read freeze frame", e)
            null
        }
    }

    /**
     * Create a SensorData from parsed freeze frame data
     */
    private fun createSensorFromParsedData(pid: String, name: String, key: String, value: Any, timestamp: Long): SensorData? {
        val floatValue = (value as? Number)?.toFloat() ?: return null
        val (unit, minVal, maxVal) = when (key) {
            "rpm" -> Triple("RPM", 0f, 8000f)
            "speed" -> Triple("km/h", 0f, 200f)
            "coolant_temp" -> Triple("°C", -40f, 215f)
            "intake_temp" -> Triple("°C", -40f, 215f)
            "throttle" -> Triple("%", 0f, 100f)
            "engine_load" -> Triple("%", 0f, 100f)
            "stft_bank1" -> Triple("%", -100f, 100f)
            "ltft_bank1" -> Triple("%", -100f, 100f)
            "voltage" -> Triple("V", 0f, 16f)
            "timing_advance" -> Triple("°", -64f, 64f)
            "maf_rate" -> Triple("g/s", 0f, 655f)
            "fuel_level" -> Triple("%", 0f, 100f)
            "intake_manifold_pressure" -> Triple("kPa", 0f, 255f)
            else -> return null
        }
        
        return SensorData(
            pid = pid,
            name = name,
            value = floatValue,
            unit = unit,
            timestamp = timestamp,
            minValue = minVal,
            maxValue = maxVal
        )
    }

    /**
     * Get simulated freeze frame data for display purposes
     */
    private fun getSimulatedFreezeFrameData(timestamp: Long): Map<String, SensorData> {
        return mapOf(
            "010C" to SensorData("010C", "Engine RPM", 2500f, "RPM", timestamp, 0f, 8000f),
            "010D" to SensorData("010D", "Vehicle Speed", 65f, "km/h", timestamp, 0f, 200f),
            "0105" to SensorData("0105", "Coolant Temperature", 88f, "°C", timestamp, -40f, 215f),
            "010F" to SensorData("010F", "Intake Air Temperature", 32f, "°C", timestamp, -40f, 215f),
            "0111" to SensorData("0111", "Throttle Position", 25f, "%", timestamp, 0f, 100f),
            "0104" to SensorData("0104", "Engine Load", 24f, "%", timestamp, 0f, 100f),
            "0106" to SensorData("0106", "Short Term Fuel Trim B1", 0f, "%", timestamp, -100f, 100f),
            "0108" to SensorData("0108", "Long Term Fuel Trim B1", 0f, "%", timestamp, -100f, 100f),
            "0142" to SensorData("0142", "Control Module Voltage", 13.8f, "V", timestamp, 0f, 16f),
            "010E" to SensorData("010E", "Timing Advance", -8f, "°", timestamp, -64f, 64f),
            "0110" to SensorData("0110", "MAF Air Flow Rate", 20.8f, "g/s", timestamp, 0f, 655f),
            "0143" to SensorData("0143", "Absolute Load", 24f, "%", timestamp, 0f, 100f),
            "012F" to SensorData("012F", "Fuel Tank Level", 58f, "%", timestamp, 0f, 100f),
            "0123" to SensorData("0123", "Intake Manifold Pressure", 35f, "kPa", timestamp, 0f, 255f)
        )
    }

    companion object {
        private const val TAG = "ObdRepository"
    }

    /**
     * Sensor configuration data class
     */
    private data class SensorConfig(
        val name: String,
        val unit: String,
        val minValue: Float,
        val maxValue: Float,
        val defaultValue: Float = (minValue + maxValue) / 2f
    )

    /**
     * Common DTC descriptions for Toyota vehicles
     */
    private val commonDtcDescriptions = mapOf(
        // Powertrain - Fuel & Air Metering
        "P0171" to "System too lean (Bank 1)",
        "P0172" to "System too rich (Bank 1)",
        "P0174" to "System too lean (Bank 2)",
        "P0175" to "System too rich (Bank 2)",
        // Powertrain - Ignition/Misfire
        "P0300" to "Random/Multiple cylinder misfire detected",
        "P0301" to "Cylinder 1 misfire detected",
        "P0302" to "Cylinder 2 misfire detected",
        "P0303" to "Cylinder 3 misfire detected",
        "P0304" to "Cylinder 4 misfire detected",
        "P0325" to "Knock sensor 1 circuit malfunction",
        "P0335" to "Crankshaft position sensor A circuit malfunction",
        "P0340" to "Camshaft position sensor circuit malfunction",
        // Powertrain - Emission Controls
        "P0420" to "Catalyst system efficiency below threshold (Bank 1)",
        "P0430" to "Catalyst system efficiency below threshold (Bank 2)",
        "P0440" to "Evaporative emission system malfunction",
        "P0441" to "Evaporative emission system incorrect purge flow",
        "P0442" to "Evaporative emission system leak detected (small leak)",
        "P0446" to "Evaporative emission system vent control malfunction",
        "P0455" to "Evaporative emission system leak detected (large leak)",
        // Powertrain - Speed/Idle
        "P0500" to "Vehicle speed sensor malfunction",
        "P0505" to "Idle control system malfunction",
        "P0506" to "Idle control system RPM lower than expected",
        "P0507" to "Idle control system RPM higher than expected",
        // Powertrain - Transmission
        "P0700" to "Transmission control system malfunction",
        "P0715" to "Input/turbine speed sensor circuit malfunction",
        "P0720" to "Output speed sensor circuit malfunction",
        "P0730" to "Incorrect gear ratio",
        "P0741" to "Torque converter clutch solenoid A performance",
        // Additional Toyota-specific
        "P0010" to "Camshaft position actuator circuit (Bank 1)",
        "P0011" to "Camshaft position timing over-advanced (Bank 1)",
        "P0012" to "Camshaft position timing over-retarded (Bank 1)",
        "P0100" to "Mass air flow circuit malfunction",
        "P0110" to "Intake air temperature circuit malfunction",
        "P0115" to "Engine coolant temperature circuit malfunction",
        "P0120" to "Throttle position sensor circuit malfunction",
        "P0125" to "Insufficient coolant temperature for closed loop",
        "P0130" to "O2 sensor circuit malfunction (Bank 1 Sensor 1)",
        "P0133" to "O2 sensor slow response (Bank 1 Sensor 1)",
        "P0135" to "O2 sensor heater circuit malfunction (Bank 1 Sensor 1)",
        "P0141" to "O2 sensor heater circuit malfunction (Bank 1 Sensor 2)",
        "P0150" to "O2 sensor circuit malfunction (Bank 2 Sensor 1)",
        "P0155" to "O2 sensor heater circuit malfunction (Bank 2 Sensor 1)"
    )
}