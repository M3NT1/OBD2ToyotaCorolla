package com.toyota.obd210.data.car

import android.content.Intent
import androidx.car.app.CarAppService
import androidx.car.app.Screen
import androidx.car.app.Session
import androidx.car.app.validation.HostValidator

/**
 * Toyota Corolla E210 OBD2 Android Auto CarAppService
 * 
 * Provides the main entry point for Android Auto integration.
 * Handles screen navigation and session management for car head unit displays.
 */
class ToyotaCarAppService : CarAppService() {
    
    override fun createHostValidator(): HostValidator {
        return HostValidator.Builder(applicationContext)
            .addAllowedHosts(HostValidator.LIST_TYPE_ALL_WIDGETS)
            .build()
    }
    
    override fun onCreateSession(): Session {
        return ToyotaCarSession()
    }
}

/**
 * CarSession for Toyota OBD2 App
 * Manages the lifecycle of the car app and provides access to screens
 */
class ToyotaCarSession : Session() {
    
    override fun onCreateScreen(intent: Intent): Screen {
        // Start with the Main Menu screen
        return CarMainMenuScreen(carContext)
    }
}
