package com.obd2corolla.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.obd2corolla.data.remote.BluetoothManager
import com.obd2corolla.data.remote.BluetoothManager.ConnectionState
import com.obd2corolla.domain.model.SensorData
import com.obd2corolla.domain.usecase.GetLiveSensorDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getLiveSensorDataUseCase: GetLiveSensorDataUseCase,
    private val bluetoothManager: BluetoothManager
) : ViewModel() {

    private val _sensors = MutableStateFlow<List<SensorData>>(emptyList())
    val sensors: StateFlow<List<SensorData>> = _sensors.asStateFlow()

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _isSimulatedMode = MutableStateFlow(true)
    val isSimulatedMode: StateFlow<Boolean> = _isSimulatedMode.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _lastConnectedDevice = MutableStateFlow<BluetoothManager.BluetoothDevice?>(null)
    val lastConnectedDevice: StateFlow<BluetoothManager.BluetoothDevice?> = _lastConnectedDevice.asStateFlow()

    private var pollingJob: Job? = null
    private var reconnectJob: Job? = null
    private var reconnectAttempts = 0
    private val maxReconnectAttempts = 5
    private val baseReconnectDelayMs = 1000L

    init {
        observeConnectionState()
        loadSimulatedData()
        startSensorPolling()
    }

    private fun loadSimulatedData() {
        _isSimulatedMode.value = true
    }

    private fun observeConnectionState() {
        viewModelScope.launch {
            bluetoothManager.connectionState.collect { state ->
                _connectionState.value = state
                when (state) {
                    ConnectionState.CONNECTED -> {
                        _isSimulatedMode.value = false
                        reconnectAttempts = 0
                        _errorMessage.value = null
                    }
                    ConnectionState.DISCONNECTED -> {
                        if (reconnectAttempts > 0) {
                            _isSimulatedMode.value = true
                        }
                    }
                    ConnectionState.ERROR -> {
                        scheduleReconnect()
                    }
                    ConnectionState.CONNECTING -> {
                        // In progress
                    }
                    ConnectionState.Bluetooth -> {
                        // BLE device selected, waiting to connect
                    }
                    ConnectionState.BluetoothConnected -> {
                        _isSimulatedMode.value = false
                        reconnectAttempts = 0
                    }
                }
            }
        }
    }

    fun connectToDevice(device: BluetoothManager.BluetoothDevice) {
        viewModelScope.launch {
            _errorMessage.value = null
            val success = bluetoothManager.connect(device)
            if (success) {
                reconnectAttempts = 0
                setLastConnectedDevice(device)
                startSensorPolling()
            } else {
                _errorMessage.value = "Failed to connect to ${device.name}"
                scheduleReconnect()
            }
        }
    }

    fun disconnectFromDevice() {
        reconnectJob?.cancel()
        reconnectJob = null
        reconnectAttempts = maxReconnectAttempts
        stopSensorPolling()
        viewModelScope.launch {
            bluetoothManager.disconnect()
        }
        _isSimulatedMode.value = true
    }

    fun startSensorPolling() {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (true) {
                try {
                    val data = getLiveSensorDataUseCase()
                    _sensors.value = data
                } catch (e: Exception) {
                    _errorMessage.value = "Failed to read sensor data: ${e.message}"
                }
                delay(1000)
            }
        }
    }

    fun stopSensorPolling() {
        pollingJob?.cancel()
        pollingJob = null
    }

    private fun scheduleReconnect() {
        if (reconnectAttempts >= maxReconnectAttempts) {
            _errorMessage.value = "Max reconnection attempts reached. Please try manually."
            _isSimulatedMode.value = true
            return
        }
        reconnectJob?.cancel()
        reconnectJob = viewModelScope.launch {
            val delayMs = baseReconnectDelayMs * (1 shl reconnectAttempts)
            reconnectAttempts++
            _errorMessage.value = "Connection lost. Reconnecting in ${delayMs / 1000}s... (attempt $reconnectAttempts)"
            delay(delayMs)
            _errorMessage.value = null
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun getLastConnectedDevice(): BluetoothManager.BluetoothDevice? {
        return _lastConnectedDevice.value
    }

    private fun setLastConnectedDevice(device: BluetoothManager.BluetoothDevice) {
        _lastConnectedDevice.value = device
    }

    override fun onCleared() {
        super.onCleared()
        stopSensorPolling()
        reconnectJob?.cancel()
    }
}