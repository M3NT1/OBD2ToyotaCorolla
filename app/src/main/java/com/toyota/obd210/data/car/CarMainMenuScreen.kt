package com.toyota.obd210.data.car

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.CarIcon
import androidx.car.app.model.Row
import androidx.car.app.model.Template
import androidx.car.app.navigation.model.NavigationTemplate
import androidx.core.graphics.drawable.IconCompat
import com.toyota.obd210.R

/**
 * Main Menu Screen for Android Auto
 * Provides navigation to Dashboard, Diagnostics, and Settings screens
 */
class CarMainMenuScreen(carContext: CarContext) : Screen(carContext) {
    
    override fun onGetTemplate(): Template {
        return NavigationTemplate.Builder()
            .setTitle("Toyota Corolla E210")
            .setHeaderAction(HeaderAction.APP_ICON)
            .addRow(
                Row.Builder()
                    .setTitle("📊 Dashboard")
                    .setSummary("Real-time sensor data")
                    .setOnClickListener {
                        screenManager.push(CarDashboardScreen(carContext))
                    }
                    .build()
            )
            .addRow(
                Row.Builder()
                    .setTitle("🔧 Diagnostics")
                    .setSummary("DTC codes and troubleshooting")
                    .setOnClickListener {
                        screenManager.push(CarDiagnosticsScreen(carContext))
                    }
                    .build()
            )
            .addRow(
                Row.Builder()
                    .setTitle("⚙️ Settings")
                    .setSummary("Hidden settings & customization")
                    .setOnClickListener {
                        screenManager.push(CarSettingsScreen(carContext))
                    }
                    .build()
            )
            .addRow(
                Row.Builder()
                    .setTitle("🔋 Hybrid Monitor")
                    .setSummary("Battery and EV status")
                    .setOnClickListener {
                        screenManager.push(CarHybridScreen(carContext))
                    }
                    .build()
            )
            .build()
    }
}
