package com.toyota.obd210.data.obd

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import com.toyota.obd210.data.obd.model.ConnectionState
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Bluetooth connector for OBD2 ELM327 adapter
 */
@Singleton
class BluetoothConnector @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val bluetoothManager: BluetoothManager? = 
        context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter
    
    private var socket: BluetoothSocket? = null
    
    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState
    
    // Standard SPP UUID for Bluetooth serial communication
    private val sppUuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    
    /**
     * Check if Bluetooth is available
     */
    fun isBluetoothAvailable(): Boolean = bluetoothAdapter != null
    
    /**
     * Check if Bluetooth is enabled
     */
    fun isBluetoothEnabled(): Boolean = bluetoothAdapter?.isEnabled == true
    
    /**
     * Get list of paired Bluetooth devices
     */
    @SuppressLint("MissingPermission")
    fun getPairedDevices(): Set<BluetoothDevice> {
        return bluetoothAdapter?.bondedDevices ?: emptySet()
    }
    
    /**
     * Connect to OBD2 Bluetooth adapter
     */
    @SuppressLint("MissingPermission")
    suspend fun connect(device: BluetoothDevice): Result<BluetoothSocket> = withContext(Dispatchers.IO) {
        try {
            _connectionState.value = ConnectionState.CONNECTING
            
            // Cancel discovery if running
            bluetoothAdapter?.cancelDiscovery()
            
            // Create socket and connect
            socket = device.createRfcommSocketToServiceRecord(sppUuid)
            socket?.connect()
            
            _connectionState.value = ConnectionState.CONNECTED
            Result.success(socket!!)
        } catch (e: IOException) {
            _connectionState.value = ConnectionState.ERROR
            socket?.close()
            Result.failure(e)
        } catch (e: SecurityException) {
            _connectionState.value = ConnectionState.ERROR
            Result.failure(e)
        }
    }
    
    /**
     * Disconnect from OBD2 adapter
     */
    fun disconnect() {
        try {
            socket?.close()
            socket = null
            _connectionState.value = ConnectionState.DISCONNECTED
        } catch (e: IOException) {
            // Ignore close errors
        }
    }
    
    /**
     * Get input stream
     */
    fun getInputStream() = socket?.inputStream
    
    /**
     * Get output stream
     */
    fun getOutputStream() = socket?.outputStream
    
    /**
     * Check if connected
     */
    fun isConnected(): Boolean = socket?.isConnected == true
}