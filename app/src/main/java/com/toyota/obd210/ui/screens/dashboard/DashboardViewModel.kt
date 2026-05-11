package com.toyota.obd210.ui.screens.dashboard

import android.bluetooth.BluetoothDevice
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.toyota.obd210.data.obd.BluetoothConnector
import com.toyota.obd210.data.obd.Elm327Protocol
import com.toyota.obd210.data.obd.PidParser
import com.toyota.obd210.data.obd.model.ConnectionState
import com.toyota.obd210.data.obd.model.ToyotaE210Pids
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val speed: Float = 0f,
    val rpm: Float = 0f,
    val coolantTemp: Float = 0f,
    val fuelLevel: Float = 0f,
    val throttlePosition: Float = 0f,
    val engineLoad: Float = 0f,
    val intakeTemp: Float = 0f,
    val gear: Int = 0,
    val hybridBatterySoc: Float = 0f,
    val motorTorque: Float = 0f,
    val evModeActive: Boolean = false,
    val adapterVoltage: Float = 0f,
    val showDevicePicker: Boolean = false,
    val availableDevices: List<BluetoothDevice> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val bluetoothConnector: BluetoothConnector,
    private val elm327Protocol: Elm327Protocol
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()
    
    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()
    
    private var pollingJob: Job? = null
    
    init {
        viewModelScope.launch {
            bluetoothConnector.connectionState.collect { state ->
                _connectionState.value = state
                if (state == ConnectionState.CONNECTED) {
                    startPolling()
                } else {
                    stopPolling()
                }
            }
        }
    }
    
    fun showDevicePicker() {
        val devices = bluetoothConnector.getPairedDevices().toList()
        _uiState.update { it.copy(showDevicePicker = true, availableDevices = devices) }
    }
    
    fun hideDevicePicker() {
        _uiState.update { it.copy(showDevicePicker = false) }
    }
    
    fun connectToDevice(device: BluetoothDevice) {
        viewModelScope.launch {
            hideDevicePicker()
            val result = bluetoothConnector.connect(device)
            if (result.isSuccess) {
                val socket = result.getOrNull()
                socket?.let {
                    elm327Protocol.initialize(it.inputStream, it.outputStream)
                }
            }
        }
    }
    
    fun disconnect() {
        bluetoothConnector.disconnect()
        elm327Protocol.disconnect()
        _uiState.update { DashboardUiState() }
    }
    
    private fun startPolling() {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (true) {
                pollSensors()
                delay(500) // Poll every 500ms
            }
        }
    }
    
    private fun stopPolling() {
        pollingJob?.cancel()
        pollingJob = null
    }
    
    private suspend fun pollSensors() {
        if (!elm327Protocol.isConnected()) return
        
        try {
            // Read voltage
            val voltage = elm327Protocol.getVoltage()
            
            // Read main PIDs
            val speedResponse = elm327Protocol.readPid(ToyotaE210Pids.PID_SPEED)
            val rpmResponse = elm327Protocol.readPid(ToyotaE210Pids.PID_RPM)
            val coolantResponse = elm327Protocol.readPid(ToyotaE210Pids.PID_COOLANT_TEMP)
            val throttleResponse = elm327Protocol.readPid(ToyotaE210Pids.PID_THROTTLE)
            val fuelResponse = elm327Protocol.readPid(ToyotaE210Pids.PID_FUEL_LEVEL)
            val loadResponse = elm327Protocol.readPid(ToyotaE210Pids.PID_ENGINE_LOAD)
            val gearResponse = elm327Protocol.readPid(ToyotaE210Pids.PID_GEAR)
            
            _uiState.update { state ->
                state.copy(
                    speed = PidParser.parseSpeed(speedResponse),
                    rpm = PidParser.parseRpm(rpmResponse),
                    coolantTemp = PidParser.parseCoolantTemp(coolantResponse),
                    throttlePosition = PidParser.parseThrottlePosition(throttleResponse),
                    fuelLevel = PidParser.parseFuelLevel(fuelResponse),
                    engineLoad = PidParser.parseEngineLoad(loadResponse),
                    gear = PidParser.parseGear(gearResponse),
                    adapterVoltage = voltage
                )
            }
        } catch (e: Exception) {
            // Handle errors silently for now
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        disconnect()
    }
}