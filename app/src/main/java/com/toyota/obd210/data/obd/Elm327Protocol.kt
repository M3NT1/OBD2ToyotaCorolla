package com.toyota.obd210.data.obd

import com.toyota.obd210.data.obd.model.ConnectionState
import com.toyota.obd210.data.obd.model.ObdResponse
import com.toyota.obd210.data.obd.model.Elm327Commands
import com.toyota.obd210.data.obd.model.ToyotaE210Pids
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ELM327 Protocol Handler for Toyota E210
 * Handles all OBD2 communication with the vehicle
 */
@Singleton
class Elm327Protocol @Inject constructor() {
    
    private var inputStream: InputStream? = null
    private var outputStream: OutputStream? = null
    
    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState
    
    private var adapterVersion: String = ""
    
    /**
     * Initialize connection with OBD2 adapter
     */
    suspend fun initialize(
        inputStream: InputStream,
        outputStream: OutputStream
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            this@Elm327Protocol.inputStream = inputStream
            this@Elm327Protocol.outputStream = outputStream
            
            // Reset adapter
            sendCommand(Elm327Commands.RESET_ADAPTER)
            Thread.sleep(1000)
            
            // Configure adapter settings
            sendCommand(Elm327Commands.ECHO_OFF)
            sendCommand(Elm327Commands.LINE_FEED_OFF)
            sendCommand(Elm327Commands.SPACES_OFF)
            sendCommand(Elm327Commands.PROTOCOL_AUTO)
            sendCommand(Elm327Commands.HEADERS_OFF)
            
            // Get adapter version
            val versionResponse = sendCommand(Elm327Commands.VERSION)
            adapterVersion = versionResponse.data
            
            _connectionState.value = ConnectionState.CONNECTED
            Result.success(Unit)
        } catch (e: Exception) {
            _connectionState.value = ConnectionState.ERROR
            Result.failure(e)
        }
    }
    
    /**
     * Send command to OBD2 adapter and get response
     */
    suspend fun sendCommand(command: String): ObdResponse = withContext(Dispatchers.IO) {
        try {
            outputStream?.write("$command\r".toByteArray())
            outputStream?.flush()
            
            Thread.sleep(100) // Wait for response
            
            val response = readResponse()
            ObdResponse(success = true, data = response)
        } catch (e: Exception) {
            ObdResponse(success = false, error = e.message ?: "Unknown error")
        }
    }
    
    /**
     * Read response from OBD2 adapter
     */
    private fun readResponse(): String {
        val buffer = ByteArray(1024)
        val response = StringBuilder()
        
        inputStream?.let { input ->
            while (input.available() > 0) {
                val bytesRead = input.read(buffer)
                if (bytesRead > 0) {
                    response.append(String(buffer, 0, bytesRead))
                }
            }
        }
        
        return response.toString()
            .replace("\r", "")
            .replace(">", "")
            .replace("\n", "")
            .trim()
    }
    
    /**
     * Read sensor value by PID
     */
    suspend fun readPid(pid: String): ObdResponse {
        val command = Elm327Commands.readPid(pid)
        return sendCommand(command)
    }
    
    /**
     * Read DTCs (Mode 03)
     */
    suspend fun readDTCs(): ObdResponse {
        return sendCommand(Elm327Commands.READ_STORED_DTC)
    }
    
    /**
     * Clear DTCs (Mode 04)
     */
    suspend fun clearDTCs(): ObdResponse {
        return sendCommand(Elm327Commands.CLEAR_DTC)
    }
    
    /**
     * Read Toyota custom register (Mode 22)
     */
    suspend fun readCustomRegister(register: String): ObdResponse {
        val command = Elm327Commands.readCustomRegister(register)
        return sendCommand(command)
    }
    
    /**
     * Write Toyota custom register (Mode 22)
     */
    suspend fun writeCustomRegister(register: String, value: Int): ObdResponse {
        val command = Elm327Commands.writeCustomRegister(register, value)
        return sendCommand(command)
    }
    
    /**
     * Read Vehicle Identification Number (VIN) - Mode 09 PID 02
     * This is used for automatic car model detection
     */
    suspend fun readVin(): ObdResponse {
        // For Mode 09, we need to set the proper header for Toyota
        sendCommand(Elm327Commands.SET_HEADER_TOYOTA)
        Thread.sleep(100)
        val response = sendCommand(Elm327Commands.MODE_09_READ_VIN)
        // Reset header after Mode 09 command
        sendCommand(Elm327Commands.HEADERS_OFF)
        return response
    }
    
    /**
     * Read supported Mode 09 PIDs
     */
    suspend fun readMode09Pids(): ObdResponse {
        sendCommand(Elm327Commands.SET_HEADER_TOYOTA)
        Thread.sleep(100)
        val response = sendCommand(Elm327Commands.MODE_09_READ_PIDS)
        sendCommand(Elm327Commands.HEADERS_OFF)
        return response
    }
    
    /**
     * Get adapter voltage
     */
    suspend fun getVoltage(): Float {
        val response = sendCommand(Elm327Commands.READ_VOLTAGE)
        return try {
            response.data.replace("V", "").trim().toFloat()
        } catch (e: Exception) {
            0f
        }
    }
    
    /**
     * Disconnect from OBD2 adapter
     */
    fun disconnect() {
        inputStream?.close()
        outputStream?.close()
        inputStream = null
        outputStream = null
        _connectionState.value = ConnectionState.DISCONNECTED
    }
    
    fun isConnected(): Boolean = _connectionState.value == ConnectionState.CONNECTED
}