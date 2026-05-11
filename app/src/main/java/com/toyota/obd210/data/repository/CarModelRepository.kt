package com.toyota.obd210.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.toyota.obd210.domain.model.CarModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// DataStore extension
private val Context.carModelDataStore: DataStore<Preferences> by preferencesDataStore(name = "car_model_preferences")

/**
 * Repository a jármű modell kiválasztás perzisztálásához
 * DataStore-ban tárolja a felhasználó által kiválasztott jármű modellt
 */
@Singleton
class CarModelRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    companion object {
        private val SELECTED_CAR_MODEL_KEY = stringPreferencesKey("selected_car_model")
        private val DETECTED_VIN_KEY = stringPreferencesKey("detected_vin")
        private val LAST_DETECTION_TIMESTAMP_KEY = stringPreferencesKey("last_detection_timestamp")
    }
    
    /**
     * Flow a jelenleg kiválasztott jármű modellhez
     * Ha nincs manuálisan kiválasztva, null-t ad vissza
     */
    val selectedCarModel: Flow<CarModel?> = context.carModelDataStore.data
        .map { preferences ->
            preferences[SELECTED_CAR_MODEL_KEY]?.let { modelName ->
                try {
                    CarModel.valueOf(modelName)
                } catch (e: IllegalArgumentException) {
                    null
                }
            }
        }
    
    /**
     * Flow a automatikusan felismert VIN-hez
     */
    val detectedVin: Flow<String?> = context.carModelDataStore.data
        .map { preferences ->
            preferences[DETECTED_VIN_KEY]
        }
    
    /**
     * Legutóbbi detektálás időbélyegzője
     */
    val lastDetectionTimestamp: Flow<String?> = context.carModelDataStore.data
        .map { preferences ->
            preferences[LAST_DETECTION_TIMESTAMP_KEY]
        }
    
    /**
     * Manuális jármű modell beállítása
     * Ez felülírja az automatikus felismerést
     */
    suspend fun setCarModel(model: CarModel) {
        context.carModelDataStore.edit { preferences ->
            preferences[SELECTED_CAR_MODEL_KEY] = model.name
        }
    }
    
    /**
     * Automatikusan felismert VIN beállítása
     */
    suspend fun setDetectedVin(vin: String) {
        context.carModelDataStore.edit { preferences ->
            preferences[DETECTED_VIN_KEY] = vin
            preferences[LAST_DETECTION_TIMESTAMP_KEY] = System.currentTimeMillis().toString()
        }
    }
    
    /**
     * Manuális kiválasztás törlése
     * Ez visszaállítja az automatikus felismerés használatát
     */
    suspend fun clearManualSelection() {
        context.carModelDataStore.edit { preferences ->
            preferences.remove(SELECTED_CAR_MODEL_KEY)
        }
    }
    
    /**
     * Összes beállítás törlése
     */
    suspend fun clearAll() {
        context.carModelDataStore.edit { preferences ->
            preferences.clear()
        }
    }
    
    /**
     * Ellenőrzi, hogy van-e manuális kiválasztás
     */
    fun hasManualSelection(): Flow<Boolean> = context.carModelDataStore.data
        .map { preferences ->
            preferences[SELECTED_CAR_MODEL_KEY] != null
        }
}