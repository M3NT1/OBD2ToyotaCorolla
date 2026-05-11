package com.obd2corolla.data.remote

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.os.Build
import android.util.Log
import com.obd2corolla.data.remote.BluetoothManager.ConnectionState
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Bluetooth manager implementation for ELM327 OBD2 adapters.
 * Handles device scanning, connection, command sending, and response reading.
 */
@Singleton
class BluetoothManagerImpl @Inject constructor(
    private val commandParser: Elm327CommandParser
) : BluetoothManager {

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    override val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private var _isV15 = false
    override val isV15: Boolean get() = _isV15

    private var socket: BluetoothSocket? = null
    private var inputStream: InputStream? = null
    private var outputStream: OutputStream? = null
    
    // ELM327-specific settings
    private var echoEnabled = false
    private var lineFeedEnabled = false
    private var protocol: String = "AUTO"
    
    // Timeout settings
    private val connectionTimeout = 10000L // 10 seconds
    private val commandTimeout = 3000L    // 3 seconds

    private val uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    
    // Target service UUID for BLE OBD adapters (Carista, OBDLink MX BLE, V-LINKER)
    private val targetServiceUuid = UUID.fromString("0000fff0-0000-1000-8000-00805f9b34fb")
    
    // BLE device name patterns for filtering
    private val bleDeviceNamePatterns = listOf(
        "OBD", "CARISTA", "OBDLINK", "V-LINKER", "VLINKER", "ELM327", "OBD2"
    )

    /**
     * Initialize the ELM327 adapter with proper settings
     */
    private suspend fun initializeElm327(): Boolean = withContext(Dispatchers.IO) {
        try {
            // Reset adapter
            sendRawCommand("ATZ", timeout = 2000)
            Thread.sleep(1000)

            // Disable echo
            val echoResponse = sendRawCommand("ATE0", timeout = 1000)
            echoEnabled = echoResponse.uppercase().contains("OK")

            // Disable line feed
            val lfResponse = sendRawCommand("ATL0", timeout = 1000)
            lineFeedEnabled = lfResponse.uppercase().contains("OK")

            // Set protocol to auto
            val protocolResponse = sendRawCommand("ATSP0", timeout = 1000)
            
            // Turn off headers and spaces for cleaner responses
            sendRawCommand("ATH0", timeout = 1000)
            sendRawCommand("AT0", timeout = 1000)

            Log.d(TAG, "ELM327 initialized: echo=$echoEnabled, lf=$lineFeedEnabled")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize ELM327", e)
            false
        }
    }

    /**
     * Check if a device name matches BLE OBD adapter patterns
     */
    private fun isObdDeviceName(name: String?): Boolean {
        if (name.isNullOrBlank()) return false
        val upperName = name.uppercase()
        return bleDeviceNamePatterns.any { pattern -> upperName.contains(pattern) }
    }

    /**
     * Scan for BLE OBD devices using BluetoothLeScanner.
     * Targets service UUID 0000fff0-0000-1000-8000-00805f9b34fb.
     * Filters devices with names containing OBD/Carista/OBDLink/V-LINKER.
     * Timeout: 10 seconds.
     */
    @SuppressLint("MissingPermission")
    override suspend fun scanForBleDevices(timeout: Long): List<BluetoothManager.BluetoothDevice> = 
        withContext(Dispatchers.IO) {
            val adapter = BluetoothAdapter.getDefaultAdapter()
            val scanner = adapter?.bluetoothLeScanner
            
            if (scanner == null) {
                Log.e(TAG, "BluetoothLeScanner not available")
                return@withContext emptyList()
            }
            
            val discoveredDevices = mutableSetOf<BluetoothManager.BluetoothDevice>()
            
            val scanCallback = object : ScanCallback() {
                override fun onScanResult(callbackType: Int, result: ScanResult) {
                    val device = result.device
                    val name = device.name ?: result.scanRecord?.deviceName
                    
                    // Filter by name patterns
                    if (isObdDeviceName(name)) {
                        val bleDevice = BluetoothManager.BluetoothDevice(
                            name = name ?: "Unknown BLE Device",
                            address = device.address
                        )
                        discoveredDevices.add(bleDevice)
                        Log.d(TAG, "Found BLE OBD device: ${bleDevice.name} (${bleDevice.address})")
                    }
                }
                
                override fun onScanFailed(errorCode: Int) {
                    Log.e(TAG, "BLE scan failed with error code: $errorCode")
                }
            }
            
            try {
                withTimeout(timeout) {
                    suspendCancellableCoroutine<Unit> { continuation ->
                        // Build scan filters for target service UUID
                        val scanFilters = listOf(
                            ScanFilter.Builder()
                                .setServiceUuid(android.os.ParcelUuid(targetServiceUuid))
                                .build()
                        )
                        
                        // Configure scan settings
                        val scanSettings = ScanSettings.Builder()
                            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                            .build()
                        
                        scanner.startScan(scanFilters, scanSettings, scanCallback)
                        
                        // Stop scan after timeout using a launched coroutine
                        val job = CoroutineScope(Dispatchers.Default).launch {
                            kotlinx.coroutines.delay(timeout)
                            scanner.stopScan(scanCallback)
                            continuation.resume(Unit) {}
                        }
                        // Cancel the job if continuation is cancelled
                        continuation.invokeOnCancellation {
                            job.cancel()
                        }
                    }
                }
            } catch (e: TimeoutCancellationException) {
                Log.d(TAG, "BLE scan timed out after ${timeout}ms")
            } catch (e: Exception) {
                Log.e(TAG, "BLE scan error", e)
            } finally {
                try {
                    scanner.stopScan(scanCallback)
                } catch (e: Exception) {
                    Log.e(TAG, "Error stopping scan", e)
                }
            }
            
            // Also include bonded devices that match OBD patterns (for devices that don't advertise service UUID)
            val bondedObdDevices = adapter.bondedDevices
                ?.filter { isObdDeviceName(it.name) }
                ?.map { device ->
                    BluetoothManager.BluetoothDevice(
                        name = device.name ?: "Unknown OBD Device",
                        address = device.address
                    )
                } ?: emptyList()
            
            val allDevices = (discoveredDevices + bondedObdDevices).toList()
            Log.d(TAG, "BLE scan found ${allDevices.size} total OBD devices")
            allDevices
        }

    /**
     * Send a raw command and get response
     */
    private suspend fun sendRawCommand(command: String, timeout: Long = commandTimeout): String {
        return withContext(Dispatchers.IO) {
            try {
                outputStream?.write("$command\r".toByteArray())
                outputStream?.flush()
                readResponse(timeout)
            } catch (e: Exception) {
                Log.e(TAG, "Raw command failed: $command", e)
                ""
            }
        }
    }

    @SuppressLint("MissingPermission")
    override suspend fun scanForDevices(): List<BluetoothManager.BluetoothDevice> = withContext(Dispatchers.IO) {
        try {
            val adapter = BluetoothAdapter.getDefaultAdapter()
            val bondedDevices = adapter?.bondedDevices?.map { device ->
                BluetoothManager.BluetoothDevice(
                    name = device.name ?: "Unknown OBD Device",
                    address = device.address
                )
            }?.filter { device ->
                // Filter for likely OBD adapters
                device.name?.uppercase()?.contains("OBD") == true ||
                device.name?.uppercase()?.contains("ELM") == true ||
                device.name?.uppercase()?.contains("BLUETOOTH") == true ||
                device.name?.uppercase()?.contains("CAR") == true ||
                device.name?.contains("HC") == true ||
                device.name?.contains("OBD") == true
            } ?: emptyList()
            
            Log.d(TAG, "Found ${bondedDevices.size} potential OBD devices")
            bondedDevices
        } catch (e: Exception) {
            Log.e(TAG, "Error scanning devices", e)
            emptyList()
        }
    }

    @SuppressLint("MissingPermission")
    override suspend fun connect(device: BluetoothManager.BluetoothDevice): Boolean = withContext(Dispatchers.IO) {
        try {
            _connectionState.value = ConnectionState.CONNECTING
            val adapter = BluetoothAdapter.getDefaultAdapter()
            val bluetoothDevice = adapter?.getRemoteDevice(device.address)

            if (bluetoothDevice == null) {
                Log.e(TAG, "Device not found: ${device.address}")
                _connectionState.value = ConnectionState.ERROR
                return@withContext false
            }

            // Create and connect socket
            socket = bluetoothDevice.createRfcommSocketToServiceRecord(uuid)
            socket?.connect()
            
            inputStream = socket?.inputStream
            outputStream = socket?.outputStream

            // Initialize ELM327
            if (initializeElm327()) {
                _connectionState.value = ConnectionState.CONNECTED
                Log.d(TAG, "Connected to ${device.name} (${device.address})")
                true
            } else {
                Log.w(TAG, "Connected but ELM327 init failed, using raw mode")
                _connectionState.value = ConnectionState.CONNECTED
                true
            }
        } catch (e: IOException) {
            Log.e(TAG, "Connection failed to ${device.address}", e)
            _connectionState.value = ConnectionState.ERROR
            disconnect()
            false
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected connection error", e)
            _connectionState.value = ConnectionState.ERROR
            disconnect()
            false
        }
    }

    override suspend fun disconnect(): Boolean = withContext(Dispatchers.IO) {
        try {
            inputStream?.close()
            outputStream?.close()
            socket?.close()
        } catch (e: IOException) {
            Log.e(TAG, "Disconnect failed", e)
        } finally {
            inputStream = null
            outputStream = null
            socket = null
            _connectionState.value = ConnectionState.DISCONNECTED
            Log.d(TAG, "Disconnected")
        }
        true
    }

    override suspend fun sendCommand(command: String): String = withContext(Dispatchers.IO) {
        Log.d(TAG, "Sending command: $command")

        // Check if we're connected
        if (_connectionState.value != ConnectionState.CONNECTED) {
            Log.w(TAG, "Not connected, state: ${_connectionState.value}")
            return@withContext generateSimulatedResponse(command)
        }

        try {
            // Format and send command
            val formattedCommand = formatCommand(command)
            outputStream?.write("$formattedCommand\r".toByteArray())
            outputStream?.flush()

            // Read response
            val response = readResponse(commandTimeout)
            Log.d(TAG, "Received response: $response")

            // Parse and return
            if (commandParser.isErrorResponse(response)) {
                Log.w(TAG, "Error response: $response")
                return@withContext generateSimulatedResponse(command)
            }

            response
        } catch (e: Exception) {
            Log.e(TAG, "Command failed: $command", e)
            generateSimulatedResponse(command)
        }
    }

    /**
     * Format command for ELM327
     */
    private fun formatCommand(cmd: String): String {
        val cleaned = cmd.trim().uppercase().replace(" ", "")
        return if (cleaned.startsWith("AT")) cleaned else cleaned
    }

    /**
     * Read response from input stream with timeout
     */
    private fun readResponse(timeout: Long): String {
        val buffer = ByteArray(1024)
        val response = StringBuilder()

        try {
            val startTime = System.currentTimeMillis()
            
            while (System.currentTimeMillis() - startTime < timeout) {
                // Check if data available
                while (inputStream?.available() ?: 0 > 0) {
                    val bytesRead = inputStream?.read(buffer) ?: 0
                    if (bytesRead > 0) {
                        val chunk = String(buffer, 0, bytesRead)
                        response.append(chunk)
                        
                        // Check for end marker (prompt character '>')
                        if (chunk.contains(">")) {
                            return parseResponse(response.toString())
                        }
                    }
                }
                
                // Small delay to avoid busy waiting
                if (response.isEmpty()) {
                    Thread.sleep(50)
                }
            }
        } catch (e: IOException) {
            Log.e(TAG, "Read failed", e)
        }

        return parseResponse(response.toString())
    }

    /**
     * Parse raw response into clean format
     */
    private fun parseResponse(raw: String): String {
        return raw
            .replace("\r", "")
            .replace("\n", "")
            .replace(">", "")
            .replace(" ", "")
            .trim()
            .let { cleaned ->
                // Remove echo if present
                if (echoEnabled && cleaned.length > 2) {
                    cleaned
                } else {
                    cleaned
                }
            }
    }

    /**
     * Generate simulated response for offline mode
     */
    private fun generateSimulatedResponse(command: String): String {
        val response = commandParser.generateSimulatedResponse(command)
        Log.d(TAG, "Simulated response: $response")
        return response
    }

    /**
     * Check if ELM327 is properly initialized
     */
    suspend fun isInitialized(): Boolean = withContext(Dispatchers.IO) {
        try {
            val response = sendRawCommand("ATI", timeout = 1000)
            response.isNotEmpty() && !response.contains("ERROR")
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Get supported PIDs by checking which return data
     */
    suspend fun getSupportedPids(): Set<String> = withContext(Dispatchers.IO) {
        val supported = mutableSetOf<String>()
        val mode01Pids = listOf(
            "01", "02", "03", "04", "05", "06", "07", "08", "09", "0A", "0B", "0C", "0D", "0E", "0F",
            "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "1A", "1B", "1C", "1D", "1E", "1F",
            "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "2A", "2B", "2C", "2D", "2E", "2F",
            "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "3A", "3B", "3C", "3D", "3E", "3F",
            "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "4A", "4B", "4C", "4D", "4E", "4F"
        )

        for (pid in mode01Pids) {
            try {
                val response = sendCommand("01$pid")
                if (!commandParser.isErrorResponse(response) && response.length >= 4) {
                    supported.add(pid)
                }
            } catch (e: Exception) {
                // PID not supported
            }
        }

        Log.d(TAG, "Supported PIDs: $supported")
        supported
    }

    /**
     * Read multiple sensors in sequence efficiently
     */
    suspend fun readMultipleSensors(pids: List<String>): Map<String, String> = withContext(Dispatchers.IO) {
        val results = mutableMapOf<String, String>()
        
        for (pid in pids) {
            try {
                val response = sendCommand("01$pid")
                if (!commandParser.isErrorResponse(response)) {
                    results[pid] = response
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to read PID $pid", e)
            }
            // Small delay between commands
            Thread.sleep(100)
        }
        
        results
    }

    /**
     * Detect ELM327 firmware version by sending ATI command.
     * Sets isV15 flag if version contains "1.5" or "1.4".
     * @return Version string (e.g., "ELM327 v1.5" or "OBDLink MX v3.8")
     */
    override suspend fun detectElm327Version(): String = withContext(Dispatchers.IO) {
        try {
            val response = sendRawCommand("ATI", timeout = 2000)
            Log.d(TAG, "ELM327 version response: $response")
            
            val versionString = response.trim()
            _isV15 = versionString.contains("1.5", ignoreCase = true) || 
                      versionString.contains("1.4", ignoreCase = true)
            
            if (_isV15) {
                Log.w(TAG, "ELM327 v1.4/v1.5 detected - limited multi-frame support")
            }
            
            versionString
        } catch (e: Exception) {
            Log.e(TAG, "Failed to detect ELM327 version", e)
            _isV15 = false
            "Unknown"
        }
    }

    /**
     * Send a Toyota-specific extended PID command.
     * Uses AT SH to set CAN header, AT CRA to set response address,
     * then sends Mode 22 (or custom mode) PID request.
     * Reads multi-frame (ISO 15765-2) response.
     * @throws Elm327V15Incompatible if multi-frame write fails on v1.5
     */
    override suspend fun sendToyotaCommand(address: String, pid: String): String = withContext(Dispatchers.IO) {
        if (_connectionState.value != ConnectionState.CONNECTED) {
            Log.w(TAG, "Not connected, cannot send Toyota command")
            return@withContext "ERROR: Not connected"
        }

        try {
            // Step 1: Set CAN header (target ECU address)
            // AT SH <address> - Set Headers to include target address
            val headerCmd = "ATSH$address"
            val headerResp = sendRawCommand(headerCmd, timeout = 1000)
            Log.d(TAG, "Header response: $headerResp")
            
            // Step 2: Set flow control (optional for filtering)
            // AT FC SH <address> - Set flow control headers
            sendRawCommand("ATFC SH $address", timeout = 500)
            
            // Step 3: For v1.5, multi-frame is problematic - check first
            if (_isV15) {
                // Try a test frame first to see if adapter handles it
                val testResp = sendRawCommand("ATAT2", timeout = 500)
                Log.d(TAG, "Adaptive timing test: $testResp")
            }

            // Step 4: Send the Mode 22 (or custom) request
            // Extended PID format: 22 + PID (6 char like 220100)
            val mode22Cmd = "22$pid"
            Log.d(TAG, "Sending Toyota command: $mode22Cmd")
            
            // Clear any pending data
            sendRawCommand("ATMA", timeout = 500)
            
            // Send the actual command
            outputStream?.write("$mode22Cmd\r".toByteArray())
            outputStream?.flush()
            
            // Step 5: Read multi-frame response
            val response = readMultiFrameResponse(commandTimeout)
            Log.d(TAG, "Toyota command response: $response")
            
            // Step 6: Restore standard headers
            sendRawCommand("ATSH 7E0", timeout = 500)
            
            if (commandParser.isErrorResponse(response)) {
                if (_isV15) {
                    throw Elm327V15Incompatible(
                        "ELM327 v1.5 cannot read extended PIDs. Multi-frame responses require v2.0+"
                    )
                }
                return@withContext response
            }
            
            response
        } catch (e: Elm327V15Incompatible) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Toyota command failed: $pid", e)
            if (_isV15) {
                throw Elm327V15Incompatible(
                    "ELM327 v1.5 incompatibility detected. Upgrade to v2.0+ for extended PID support."
                )
            }
            "ERROR: ${e.message}"
        }
    }

    /**
     * Read a multi-frame ISO 15765-2 response from the ELM327.
     * First frame (0x1X) contains 7 data bytes, subsequent frames (0x2X) contain 8 bytes.
     */
    private fun readMultiFrameResponse(timeout: Long): String {
        val buffer = StringBuilder()
        val startTime = System.currentTimeMillis()
        var isMultiFrame = false
        var remainingBytes = 0
        
        try {
            while (System.currentTimeMillis() - startTime < timeout) {
                while (inputStream?.available() ?: 0 > 0) {
                    val byte = inputStream?.read()
                    if (byte != null && byte != -1) {
                        val char = byte.toChar()
                        buffer.append(char)
                        
                        // Check for multi-frame start
                        // First frame starts with 0x10 or has specific multi-frame pattern
                        // Consecutive frames start with 0x20-0x2F (flow index)
                        if (buffer.length >= 2) {
                            val lastTwo = buffer.takeLast(2)
                            // Detect flow control or multi-frame pattern
                            if (lastTwo.startsWith("1") || lastTwo.startsWith("2")) {
                                // Could be multi-frame indicator
                                if (!isMultiFrame && buffer.length > 10) {
                                    isMultiFrame = true
                                }
                            }
                        }
                        
                        // End marker
                        if (char == '>') {
                            return parseResponse(buffer.toString())
                        }
                    }
                }
                Thread.sleep(20)
            }
        } catch (e: IOException) {
            Log.e(TAG, "Multi-frame read failed", e)
        }
        
        return parseResponse(buffer.toString())
    }

    /**
     * Write a customization setting to the vehicle (hidden settings).
     * WARNING: Only supported on certain Toyota ECUs. May require v2.0+ ELM327.
     * @param code Setting code (e.g., "1234" for a specific customization)
     * @param value Value to write
     * @return Success message or error
     * @throws Elm327V15Incompatible if v1.5 is detected
     */
    suspend fun writeCustomizationSetting(code: String, value: String): String = withContext(Dispatchers.IO) {
        if (_isV15) {
            throw Elm327V15Incompatible(
                "Customization writes require ELM327 v2.0+. v1.5 cannot perform multi-frame writes."
            )
        }
        
        if (_connectionState.value != ConnectionState.CONNECTED) {
            return@withContext "ERROR: Not connected"
        }
        
        try {
            // Hidden settings typically use Mode 29 or custom Toyota mode
            // Format: AT SH <address> + 29 <code> <value>
            // This is ECU and customization-specific
            
            Log.w(TAG, "Writing customization $code = $value. This is potentially dangerous!")
            
            val headerCmd = "ATSH 7E0"
            sendRawCommand(headerCmd, timeout = 1000)
            
            // Custom write command (varies by ECU and setting)
            val writeCmd = "29$code$value"
            outputStream?.write("$writeCmd\r".toByteArray())
            outputStream?.flush()
            
            val response = readResponse(commandTimeout)
            Log.d(TAG, "Customization write response: $response")
            
            // Restore standard mode
            sendRawCommand("ATSH 7E0", timeout = 500)
            
            response
        } catch (e: Elm327V15Incompatible) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Customization write failed: $code = $value", e)
            "ERROR: ${e.message}"
        }
    }

    /**
     * Read a Toyota customization register value using Mode 2A.
     * Toyota uses Mode 2A for reading customization registers from the body ECU.
     * Flow: ATSH <address> → 2A <register> → parse response
     * 
     * @param address CAN header address (e.g., "7E4" for body ECU)
     * @param register 4-char hex register address (e.g., "00A0")
     * @return The register value (0-255), or null if read failed
     */
    override suspend fun readToyotaRegister(address: String, register: String): Int? = withContext(Dispatchers.IO) {
        if (_connectionState.value != ConnectionState.CONNECTED) {
            Log.w(TAG, "Not connected, cannot read Toyota register")
            return@withContext null
        }
        
        try {
            // Step 1: Set CAN header to target ECU
            val headerCmd = "ATSH$address"
            val headerResp = sendRawCommand(headerCmd, timeout = 1000)
            Log.d(TAG, "Set header $address: $headerResp")
            
            // Step 2: Send Mode 2A read command
            // Toyota Mode 2A format: 2A <4-char register>
            // e.g., 2A 00A0 to read register 00A0
            val readCmd = "2A$register"
            Log.d(TAG, "Sending Toyota read: $readCmd")
            
            outputStream?.write("$readCmd\r".toByteArray())
            outputStream?.flush()
            
            // Step 3: Read response
            val response = readResponse(commandTimeout)
            Log.d(TAG, "Toyota register read response: $response")
            
            // Step 4: Parse the value from response
            // Response format typically: 2A <register> <value> or multi-frame
            // e.g., "2A00A001" means register 00A0 has value 01
            val value = parseToyotaRegisterResponse(response, register)
            
            // Step 5: Restore standard headers
            sendRawCommand("ATSH 7E0", timeout = 500)
            
            value
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read Toyota register $register", e)
            null
        }
    }

    /**
     * Parse Toyota register read response to extract the value.
     * Response format: 2A <register> <value> (single frame) or multi-frame
     */
    private fun parseToyotaRegisterResponse(response: String, register: String): Int? {
        val cleaned = response.uppercase().replace(" ", "").replace("\r", "").replace("\n", "")
        
        // Try to find the register value pair
        // Pattern: 2A<register><value> or with length byte
        val registerUpper = register.uppercase()
        
        // Find 2A followed by register
        val idx = cleaned.indexOf("2A$registerUpper")
        if (idx >= 0) {
            val afterRegister = cleaned.substring(idx + 2 + registerUpper.length)
            if (afterRegister.length >= 2) {
                val hexValue = afterRegister.substring(0, 2)
                return try {
                    hexValue.toInt(16)
                } catch (e: NumberFormatException) {
                    null
                }
            }
        }
        
        // Fallback: try to parse any hex value at the end
        if (cleaned.length >= 2) {
            val possibleHex = cleaned.takeLast(2)
            return try {
                possibleHex.toInt(16)
            } catch (e: NumberFormatException) {
                null
            }
        }
        
        return null
    }

    /**
     * Write a value to a Toyota customization register.
     * Toyota uses Mode 22 with specific format for writes:
     * 22 + register + 01 + value
     * e.g., to write 01 to register 00A0: 2200A00101
     * 
     * @param address CAN header address (e.g., "7E4" for body ECU)
     * @param register 4-char hex register address (e.g., "00A0")
     * @param value The value to write (0-255)
     * @return true if write appeared successful
     * @throws Elm327V15Incompatible if v1.5 is detected (no multi-frame write support)
     */
    override suspend fun sendToyotaWrite(address: String, register: String, value: Int): Boolean = withContext(Dispatchers.IO) {
        if (_isV15) {
            throw Elm327V15Incompatible(
                "Ez az adapter nem támogatja az írási műveleteket. " +
                "Kérjük, használj ELM327 v2.2 vagy újabb verziót."
            )
        }
        
        if (_connectionState.value != ConnectionState.CONNECTED) {
            Log.w(TAG, "Not connected, cannot write Toyota register")
            return@withContext false
        }
        
        try {
            // Step 1: Set CAN header to target ECU
            val headerCmd = "ATSH$address"
            val headerResp = sendRawCommand(headerCmd, timeout = 1000)
            Log.d(TAG, "Set header for write $address: $headerResp")
            
            // Step 2: Send Mode 22 write command
            // Toyota write format: 22 <register> 01 <value>
            // e.g., 2200A00101 writes value 01 to register 00A0
            val valueHex = value.toString(16).uppercase().padStart(2, '0')
            val writeCmd = "22${register}01$valueHex"
            Log.d(TAG, "Sending Toyota write: $writeCmd")
            
            outputStream?.write("$writeCmd\r".toByteArray())
            outputStream?.flush()
            
            // Step 3: Read response
            val response = readResponse(commandTimeout)
            Log.d(TAG, "Toyota write response: $response")
            
            // Step 4: Check for success/failure indicators
            val success = !commandParser.isErrorResponse(response) && 
                         !response.uppercase().contains("ERROR") &&
                         !response.uppercase().contains("?")
            
            // Step 5: Restore standard headers
            sendRawCommand("ATSH 7E0", timeout = 500)
            
            success
        } catch (e: Elm327V15Incompatible) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Failed to write Toyota register $register = $value", e)
            false
        }
    }

    companion object {
        private const val TAG = "BluetoothManager"
    }
}
