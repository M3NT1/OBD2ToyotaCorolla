package com.obd2corolla.ui.screens.dtc

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.obd2corolla.data.local.DtcDescriptions
import com.obd2corolla.data.remote.BluetoothManager
import com.obd2corolla.data.remote.BluetoothManager.ConnectionState
import com.obd2corolla.domain.model.DtcCode
import com.obd2corolla.domain.model.DtcSeverity
import com.obd2corolla.domain.model.SensorData
import com.obd2corolla.domain.repository.ObdRepository
import com.obd2corolla.domain.usecase.ClearDtcCodesUseCase
import com.obd2corolla.domain.usecase.GetDtcCodesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DtcViewModel @Inject constructor(
    private val getDtcCodesUseCase: GetDtcCodesUseCase,
    private val clearDtcCodesUseCase: ClearDtcCodesUseCase,
    private val repository: ObdRepository,
    private val bluetoothManager: BluetoothManager
) : ViewModel() {

    private val _allDTCs = MutableStateFlow<List<DtcCode>>(emptyList())
    val allDTCs: StateFlow<List<DtcCode>> = _allDTCs.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _showClearDialog = MutableStateFlow(false)
    val showClearDialog: StateFlow<Boolean> = _showClearDialog.asStateFlow()

    private val _selectedDtc = MutableStateFlow<DtcCode?>(null)
    val selectedDtc: StateFlow<DtcCode?> = _selectedDtc.asStateFlow()

    private val _showFreezeFrame = MutableStateFlow(false)
    val showFreezeFrame: StateFlow<Boolean> = _showFreezeFrame.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _freezeFrameData = MutableStateFlow<Map<String, SensorData>>(emptyMap())
    val freezeFrameData: StateFlow<Map<String, SensorData>> = _freezeFrameData.asStateFlow()

    val connectionState: StateFlow<ConnectionState> = bluetoothManager.connectionState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ConnectionState.DISCONNECTED)

    val criticalDTCs: StateFlow<List<DtcCode>> = _allDTCs
        .combine(MutableStateFlow(DtcSeverity.CRITICAL)) { dtcs, _ ->
            dtcs.filter { it.severity == DtcSeverity.CRITICAL }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingDTCs: StateFlow<List<DtcCode>> = _allDTCs
        .combine(MutableStateFlow(DtcSeverity.INFO)) { dtcs, _ ->
            dtcs.filter { it.severity == DtcSeverity.INFO }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        observeConnectionState()
        loadStoredDTCs()
    }

    private fun observeConnectionState() {
        viewModelScope.launch {
            bluetoothManager.connectionState.collect { state ->
                when (state) {
                    ConnectionState.CONNECTED -> {
                        loadLiveDTCs()
                    }
                    ConnectionState.DISCONNECTED -> {
                        // Keep cached DTCs
                    }
                    else -> {}
                }
            }
        }
    }

    fun loadStoredDTCs() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _allDTCs.value = getDtcCodesUseCase()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load stored DTCs: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadLiveDTCs() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val dtcs = repository.getDTCs()
                _allDTCs.value = dtcs
            } catch (e: Exception) {
                _errorMessage.value = "Failed to read DTCs from vehicle: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadPendingDTCs() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val pendingDTCs = repository.readPendingDTCs()
                val currentDTCs = _allDTCs.value.toMutableList()
                pendingDTCs.forEach { pending ->
                    if (currentDTCs.none { it.code == pending.code }) {
                        currentDTCs.add(pending)
                    }
                }
                _allDTCs.value = currentDTCs
            } catch (e: Exception) {
                _errorMessage.value = "Failed to read pending DTCs: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearDTCs() {
        viewModelScope.launch {
            _showClearDialog.value = false
            _isLoading.value = true
            try {
                clearDtcCodesUseCase()
                _allDTCs.value = emptyList()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to clear DTCs: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun showClearDialog() {
        _showClearDialog.value = true
    }

    fun dismissClearDialog() {
        _showClearDialog.value = false
    }

    fun selectDtcForFreezeFrame(dtc: DtcCode) {
        _selectedDtc.value = dtc
        loadFreezeFrame(dtc.code)
        _showFreezeFrame.value = true
    }

    fun dismissFreezeFrame() {
        _showFreezeFrame.value = false
        _selectedDtc.value = null
        _freezeFrameData.value = emptyMap()
    }

    private fun loadFreezeFrame(dtcCode: String) {
        viewModelScope.launch {
            try {
                val freezeFrame = repository.readFreezeFrame(dtcCode)
                _freezeFrameData.value = freezeFrame ?: generateSimulatedFreezeFrame()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load freeze frame: ${e.message}"
                _freezeFrameData.value = generateSimulatedFreezeFrame()
            }
        }
    }

    private fun generateSimulatedFreezeFrame(): Map<String, SensorData> {
        val timestamp = System.currentTimeMillis()
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

    fun selectTab(index: Int) {
        _selectedTab.value = index
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun getDtcDescription(code: String): String {
        return DtcDescriptions.getDescription(code)
    }

    fun getDtcSeverity(code: String): DtcSeverity {
        return when {
            code.startsWith("P0") || code.startsWith("P2") -> DtcSeverity.CRITICAL
            code.startsWith("P1") || code.startsWith("P3") -> DtcSeverity.WARNING
            code.startsWith("C0") || code.startsWith("C1") -> DtcSeverity.WARNING
            code.startsWith("B0") || code.startsWith("B1") -> DtcSeverity.WARNING
            code.startsWith("U0") || code.startsWith("U1") -> DtcSeverity.INFO
            else -> DtcSeverity.WARNING
        }
    }
}