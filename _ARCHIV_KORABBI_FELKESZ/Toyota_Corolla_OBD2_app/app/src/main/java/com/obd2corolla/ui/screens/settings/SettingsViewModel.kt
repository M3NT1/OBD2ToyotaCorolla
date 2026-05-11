package com.obd2corolla.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.obd2corolla.data.remote.BluetoothManager
import com.obd2corolla.domain.model.HiddenSetting
import com.obd2corolla.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Hidden Settings screen.
 * Handles reading and writing Toyota customization settings via the OBD adapter.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val bluetoothManager: BluetoothManager
) : ViewModel() {

    private val _settings = MutableStateFlow<List<HiddenSetting>>(emptyList())
    val settings: StateFlow<List<HiddenSetting>> = _settings.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    val connectionState: StateFlow<BluetoothManager.ConnectionState> = bluetoothManager.connectionState
    
    val isV15Warning: StateFlow<Boolean> = bluetoothManager.isV15.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        false
    )

    /**
     * Group settings by their category for display in the UI.
     */
    val settingsByCategory: StateFlow<Map<String, List<HiddenSetting>>> = _settings
        .map { settingList ->
            settingList.groupBy { it.category }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyMap()
        )

    init {
        loadSettings()
    }

    /**
     * Load all settings from the vehicle.
     */
    fun loadSettings() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            try {
                val loadedSettings = settingsRepository.getHiddenSettings()
                _settings.value = loadedSettings
                
                if (loadedSettings.isEmpty()) {
                    _errorMessage.value = "Nem sikerült betölteni a beállításokat. Ellenőrizd a kapcsolatot!"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Hiba: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Request to update a setting. This is called when user selects a new value.
     * The actual update happens in confirmUpdate after user confirms.
     */
    fun updateSetting(key: String, value: Int) {
        // The confirmation is handled in the UI layer
        // This method can be used for validation or pre-checks
    }

    /**
     * Confirm and execute a setting update after user confirmation.
     */
    fun confirmUpdate(key: String, value: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            try {
                val result = settingsRepository.updateHiddenSetting(key, value.toString())
                
                result.onSuccess {
                    val settingName = _settings.value.find { it.key == key }?.name ?: key
                    _successMessage.value = "Sikeres módosítás: $settingName"
                    
                    // Reload settings to get updated values
                    viewModelScope.launch {
                        val updatedSettings = settingsRepository.getHiddenSettings()
                        _settings.value = updatedSettings
                    }
                }.onFailure { error ->
                    _errorMessage.value = "Sikertelen módosítás: ${error.message}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Hiba: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Reset all settings to factory defaults.
     */
    fun resetToDefaults() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            try {
                val result = settingsRepository.resetToDefaults()
                
                result.onSuccess {
                    _successMessage.value = "Minden beállítás visszaállítva az alapértelmezetre"
                    // Reload settings
                    viewModelScope.launch {
                        val updatedSettings = settingsRepository.getHiddenSettings()
                        _settings.value = updatedSettings
                    }
                }.onFailure { error ->
                    _errorMessage.value = "Sikertelen visszaállítás: ${error.message}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Hiba: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Clear the current error message.
     */
    fun dismissError() {
        _errorMessage.value = null
    }

    /**
     * Clear the current success message.
     */
    fun dismissSuccess() {
        _successMessage.value = null
    }

    /**
     * Get the setting by key.
     */
    fun getSetting(key: String): HiddenSetting? {
        return _settings.value.find { it.key == key }
    }

    /**
     * Get the Hungarian label for a category.
     */
    fun getCategoryDisplayName(category: String): String = category
}

