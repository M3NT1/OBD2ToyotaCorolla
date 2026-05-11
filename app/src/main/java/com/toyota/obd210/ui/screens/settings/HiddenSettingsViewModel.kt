package com.toyota.obd210.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.toyota.obd210.data.local.CarModelRegistry
import com.toyota.obd210.data.local.ToyotaE210SettingsRegistry
import com.toyota.obd210.data.obd.Elm327Protocol
import com.toyota.obd210.data.repository.CarModelRepository
import com.toyota.obd210.domain.model.CarModel
import com.toyota.obd210.domain.model.HiddenSetting
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HiddenSettingsUiState(
    val settings: List<HiddenSetting> = emptyList(),
    val currentCarModel: CarModel = CarModel.getDefault(),
    val detectedVin: String? = null,
    val isAutoDetected: Boolean = false,
    val isLoading: Boolean = false,
    val message: String? = null,
    val isError: Boolean = false
)

@HiltViewModel
class HiddenSettingsViewModel @Inject constructor(
    private val elm327Protocol: Elm327Protocol,
    private val carModelRepository: CarModelRepository
) : ViewModel() {
    
    private val _detectedCarModel = MutableStateFlow<CarModel?>(null)
    private val _detectedVin = MutableStateFlow<String?>(null)
    
    private val _uiState = MutableStateFlow(HiddenSettingsUiState())
    val uiState: StateFlow<HiddenSettingsUiState> = _uiState.asStateFlow()
    
    init {
        // Kombináljuk az automatikus és manuális jármű kiválasztást
        viewModelScope.launch {
            combine(
                carModelRepository.selectedCarModel,
                _detectedCarModel
            ) { manualModel, detectedModel ->
                manualModel ?: detectedModel ?: CarModel.getDefault()
            }.collect { effectiveModel ->
                _uiState.update { it.copy(currentCarModel = effectiveModel) }
            }
        }
        
        // VIN observer
        viewModelScope.launch {
            carModelRepository.detectedVin.collect { vin ->
                _uiState.update { it.copy(detectedVin = vin) }
            }
        }
        
        // Inicializáljuk a beállításokat az alapértelmezett modellel
        _uiState.update {
            it.copy(settings = filterSettingsForModel(ToyotaE210SettingsRegistry.allSettings, it.currentCarModel))
        }
    }
    
    /**
     * Jármű modell automatikus felismerése VIN-ből
     */
    fun detectCarModel() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, message = null) }
            
            try {
                // VIN olvasása Mode 09 PID 02-ből
                val vinResponse = elm327Protocol.readVin()
                
                if (vinResponse.success && vinResponse.data.isNotEmpty()) {
                    // VIN parsing
                    val vin = extractVinFromResponse(vinResponse.data)
                    val detectedModel = CarModelRegistry.detectFromVin(vin)
                    
                    if (detectedModel != null) {
                        _detectedCarModel.value = detectedModel
                        _detectedVin.value = vin
                        carModelRepository.setDetectedVin(vin)
                        
                        // Beállítások szűrése az új modellhez
                        val filteredSettings = filterSettingsForModel(
                            ToyotaE210SettingsRegistry.allSettings,
                            detectedModel
                        )
                        
                        _uiState.update {
                            it.copy(
                                currentCarModel = detectedModel,
                                detectedVin = vin,
                                isAutoDetected = true,
                                settings = filteredSettings,
                                isLoading = false,
                                message = "Vehicle detected: ${detectedModel.displayName}",
                                isError = false
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                message = "Could not determine vehicle model from VIN",
                                isError = true
                            )
                        }
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            message = "Failed to read VIN",
                            isError = true
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        message = "Detection failed: ${e.message}",
                        isError = true
                    )
                }
            }
        }
    }
    
    /**
     * Manuális jármű modell felülírás
     */
    fun overrideCarModel(model: CarModel) {
        viewModelScope.launch {
            carModelRepository.setCarModel(model)
            
            // Beállítások szűrése az új modellhez
            val filteredSettings = filterSettingsForModel(
                ToyotaE210SettingsRegistry.allSettings,
                model
            )
            
            _uiState.update {
                it.copy(
                    currentCarModel = model,
                    settings = filteredSettings,
                    message = "Vehicle changed to: ${model.displayName}",
                    isError = false
                )
            }
        }
    }
    
    /**
     * Manuális kiválasztás törlése (vissza az automatikusra)
     */
    fun resetToAutoDetection() {
        viewModelScope.launch {
            carModelRepository.clearManualSelection()
            
            val detectedModel = _detectedCarModel.value ?: CarModel.getDefault()
            val filteredSettings = filterSettingsForModel(
                ToyotaE210SettingsRegistry.allSettings,
                detectedModel
            )
            
            _uiState.update {
                it.copy(
                    currentCarModel = detectedModel,
                    settings = filteredSettings,
                    isAutoDetected = _detectedVin.value != null,
                    message = "Reset to auto-detection",
                    isError = false
                )
            }
        }
    }
    
    /**
     * Beállítás szűrése a jármű modell alapján
     */
    private fun filterSettingsForModel(settings: List<HiddenSetting>, model: CarModel): List<HiddenSetting> {
        return settings.map { setting ->
            val isSupported = setting.supportedModels.isEmpty() || 
                              setting.supportedModels.contains(model) ||
                              model.supportedSettingIds.contains(setting.id)
            
            setting.copy(
                notAvailableReason = if (isSupported) null else "Not available for ${model.displayName}"
            )
        }
    }
    
    /**
     * Ellenőrzi, hogy egy beállítás támogatott-e az aktuális járműben
     */
    fun isSettingSupported(setting: HiddenSetting): Boolean {
        val model = _uiState.value.currentCarModel
        return setting.supportedModels.isEmpty() || 
               setting.supportedModels.contains(model) ||
               model.supportedSettingIds.contains(setting.id)
    }
    
    fun readAllSettings() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, message = null) }
            
            try {
                val updatedSettings = _uiState.value.settings.map { setting ->
                    // Csak támogatott beállításokat olvassunk
                    if (isSettingSupported(setting)) {
                        val response = elm327Protocol.readCustomRegister(setting.register)
                        if (response.success) {
                            val value = parseRegisterValue(response.data)
                            setting.copy(currentValue = value)
                        } else {
                            setting
                        }
                    } else {
                        setting
                    }
                }
                
                _uiState.update {
                    it.copy(
                        settings = updatedSettings,
                        isLoading = false,
                        message = "Settings read successfully",
                        isError = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        message = "Failed to read settings: ${e.message}",
                        isError = true
                    )
                }
            }
        }
    }
    
    fun updateSetting(setting: HiddenSetting, newValue: Int) {
        // Ellenőrizzük, hogy támogatott-e
        if (!isSettingSupported(setting)) {
            _uiState.update {
                it.copy(
                    message = "${setting.name} is not available for this vehicle",
                    isError = true
                )
            }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, message = null) }
            
            try {
                val response = elm327Protocol.writeCustomRegister(setting.register, newValue)
                
                if (response.success) {
                    // Update local state
                    val updatedSettings = _uiState.value.settings.map {
                        if (it.id == setting.id) {
                            it.copy(currentValue = newValue)
                        } else {
                            it
                        }
                    }
                    
                    _uiState.update {
                        it.copy(
                            settings = updatedSettings,
                            isLoading = false,
                            message = "${setting.name} updated successfully",
                            isError = false
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            message = "Failed to update ${setting.name}",
                            isError = true
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        message = "Error: ${e.message}",
                        isError = true
                    )
                }
            }
        }
    }
    
    private fun extractVinFromResponse(response: String): String {
        // VIN formátum: 49 02 01 4A 54 44 41 41 33 4D 54 31 32 33 34 35 36
        // Vagy sima szöveg: JTDAA3MT1234567
        return response
            .replace(" ", "")
            .replace("49", "")
            .replace("02", "")
            .filter { it.isLetterOrDigit() }
            .take(17)
    }
    
    private fun parseRegisterValue(responseData: String): Int {
        val hexPattern = Regex("[0-9A-Fa-f]{2}")
        val matches = hexPattern.findAll(responseData).toList()
        
        return if (matches.isNotEmpty()) {
            try {
                matches.last().value.toInt(16)
            } catch (e: Exception) {
                0
            }
        } else {
            0
        }
    }
}