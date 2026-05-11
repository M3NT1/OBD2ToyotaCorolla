package com.toyota.obd210.ui.screens.diagnostics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.toyota.obd210.data.obd.Elm327Protocol
import com.toyota.obd210.data.obd.PidParser
import com.toyota.obd210.domain.model.DtcCode
import com.toyota.obd210.domain.model.DtcSeverity
import com.toyota.obd210.domain.model.DtcSystem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DiagnosticsUiState(
    val dtcCodes: List<DtcCode> = emptyList(),
    val storedCount: Int = 0,
    val pendingCount: Int = 0,
    val permanentCount: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class DiagnosticsViewModel @Inject constructor(
    private val elm327Protocol: Elm327Protocol
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(DiagnosticsUiState())
    val uiState: StateFlow<DiagnosticsUiState> = _uiState.asStateFlow()
    
    fun readDTCs() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val response = elm327Protocol.readDTCs()
                val codes = PidParser.parseDTCs(response)
                
                val dtcList = codes.map { code ->
                    createDtcInfo(code)
                }
                
                _uiState.update {
                    it.copy(
                        dtcCodes = dtcList,
                        storedCount = dtcList.size,
                        pendingCount = 0,
                        permanentCount = 0,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to read DTCs: ${e.message}"
                    )
                }
            }
        }
    }
    
    fun clearDTCs() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val response = elm327Protocol.clearDTCs()
                if (response.success) {
                    _uiState.update {
                        it.copy(
                            dtcCodes = emptyList(),
                            storedCount = 0,
                            pendingCount = 0,
                            permanentCount = 0,
                            isLoading = false,
                            successMessage = "DTCs cleared successfully"
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Failed to clear DTCs"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to clear DTCs: ${e.message}"
                    )
                }
            }
        }
    }
    
    private fun createDtcInfo(code: String): DtcCode {
        // Parse DTC code to determine system and severity
        val firstChar = code.firstOrNull() ?: 'P'
        val system = when (firstChar) {
            'P' -> DtcSystem.ENGINE
            'C' -> DtcSystem.BRAKE
            'B' -> DtcSystem.BODY
            'U' -> DtcSystem.UNKNOWN
            else -> DtcSystem.UNKNOWN
        }
        
        val severity = when {
            code.startsWith("P0") || code.startsWith("C0") || code.startsWith("B0") || code.startsWith("U0") -> 
                DtcSeverity.WARNING
            code.startsWith("P3") || code.startsWith("P2") -> DtcSeverity.WARNING
            code.startsWith("P1") || code.startsWith("P0") -> DtcSeverity.INFO
            else -> DtcSeverity.INFO
        }
        
        val description = getDtcDescription(code)
        
        return DtcCode(
            code = code,
            description = description,
            system = system,
            severity = severity
        )
    }
    
    private fun getDtcDescription(code: String): String {
        // Common DTC descriptions for E210
        return when (code) {
            "P0300" -> "Random/Multiple Cylinder Misfire Detected"
            "P0301" -> "Cylinder 1 Misfire Detected"
            "P0302" -> "Cylinder 2 Misfire Detected"
            "P0303" -> "Cylinder 3 Misfire Detected"
            "P0304" -> "Cylinder 4 Misfire Detected"
            "P0420" -> "Catalyst System Efficiency Below Threshold"
            "P0171" -> "System Too Lean (Bank 1)"
            "P0172" -> "System Too Rich (Bank 1)"
            "P0442" -> "Evaporative Emission System Leak Detected (small leak)"
            "P0455" -> "Evaporative Emission System Leak Detected (large leak)"
            "P0500" -> "Vehicle Speed Sensor Malfunction"
            "P0562" -> "System Voltage Low"
            "P0563" -> "System Voltage High"
            "C0200" -> "ABS Sensor Malfunction"
            "C1250" -> "Steering Angle Sensor"
            "B1500" -> "Headlamp Assembly"
            else -> "Unknown fault code - Refer to service manual"
        }
    }
}