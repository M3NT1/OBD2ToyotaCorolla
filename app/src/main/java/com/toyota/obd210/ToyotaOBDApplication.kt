package com.toyota.obd210

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ToyotaOBDApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        // Application initialization
    }
}