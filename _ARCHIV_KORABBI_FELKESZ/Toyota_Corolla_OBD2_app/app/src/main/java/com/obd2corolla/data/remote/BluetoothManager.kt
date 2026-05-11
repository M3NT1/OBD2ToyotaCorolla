package com.obd2corolla.data.remote

import kotlinx.coroutines.flow.StateFlow

interface BluetoothManager {
    val connectionState: StateFlow<ConnectionState>

    /**
     * True if ELM327 v1.4 or v1.5 is detected (limited functionality)
     */
    val isV15: Boolean

    /**
     * Scan for BLE OBD devices using BluetoothLeScanner.
     * Filters for devices with names containing OBD/Carista/OBDLink/V-LINKER.
     * Uses target service UUID 0000fff0-0000-1000-8000-00805f9b34fb.
     * @param timeout Scan duration in milliseconds (default 10000ms = 10s)
     * @return List of discovered BLE devices
     */
    suspend fun scanForBleDevices(timeout: Long = 10000L): List<BluetoothDevice>

    /**
     * Scan for paired classic Bluetooth devices (legacy method).
     */
    suspend fun scanForDevices(): List<BluetoothDevice>
    
    suspend fun connect(device: BluetoothDevice): Boolean
    suspend fun disconnect(): Boolean
    suspend fun sendCommand(command: String): String

    /**
     * Detect and return ELM327 version string.
     * Sets isV15 flag if v1.4 or v1.5 detected.
     */
    suspend fun detectElm327Version(): String

    /**
     * Send a Toyota-specific extended PID command with CAN header.
     * @param address CAN address (e.g., "7E0", "7E2", "7E4")
     * @param pid PID to read (e.g., "220100" for RPM)
     * @return Response string or error
     * @throws Elm327V15Incompatible if multi-frame write fails on v1.5
     */
    suspend fun sendToyotaCommand(address: String, pid: String): String

    /**
     * Read a Toyota customization register value.
     * Uses Mode 2A (Toyota custom read) with CAN header set to target ECU.
     * @param address CAN header address (e.g., "7E4" for body ECU)
     * @param register 4-char hex register address (e.g., "00A0")
     * @return The register value, or null if read failed
     */
    suspend fun readToyotaRegister(address: String, register: String): Int?

    /**
     * Write a value to a Toyota customization register.
     * Uses Toyota custom write format: 22 + register + 01 + value
     * @param address CAN header address (e.g., "7E4" for body ECU)
     * @param register 4-char hex register address (e.g., "00A0")
     * @param value The value to write (0-255)
     * @return true if write appeared successful
     * @throws Elm327V15Incompatible if adapter doesn't support multi-frame writes
     */
    suspend fun sendToyotaWrite(address: String, register: String, value: Int): Boolean

    data class BluetoothDevice(
        val name: String,
        val address: String
    )

    enum class ConnectionState {
        DISCONNECTED,
        Bluetooth,
        BluetoothConnected,
        CONNECTING,
        CONNECTED,
        ERROR
    }
}

/**
 * Exception thrown when ELM327 v1.5 cannot perform required multi-frame operations
 */
class Elm327V15Incompatible(message: String = "ELM327 v1.5 does not support multi-frame writes") : Exception(message)
