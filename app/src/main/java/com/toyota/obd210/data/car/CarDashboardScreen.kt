package com.toyota.obd210.data.car

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.Pane
import androidx.car.app.model.PaneBuilder
import androidx.car.app.model.Row
import androidx.car.app.model.Template

/**
 * Dashboard Screen for Android Auto
 * Displays real-time OBD2 sensor data from the vehicle
 */
class CarDashboardScreen(carContext: CarContext) : Screen(carContext) {
    
    override fun onGetTemplate(): Template {
        val paneBuilder = PaneBuilder()
        
        // Speed
        paneBuilder.addRow(
            Row.Builder()
                .setTitle("Speed")
                .addMetaTemplate(
                    androidx.car.app.model.Metadata.Builder()
                        .setType(androidx.car.app.model.Metadata.METADATA_TYPE_CUSTOM)
                        .build()
                )
                .build()
        )
        
        // RPM
        paneBuilder.addRow(
            Row.Builder()
                .setTitle("Engine RPM")
                .setOnClickListener { }
                .build()
        )
        
        // Coolant Temperature
        paneBuilder.addRow(
            Row.Builder()
                .setTitle("Coolant Temp")
                .setOnClickListener { }
                .build()
        )
        
        // Throttle Position
        paneBuilder.addRow(
            Row.Builder()
                .setTitle("Throttle Position")
                .setOnClickListener { }
                .build()
        )
        
        // Fuel Level
        paneBuilder.addRow(
            Row.Builder()
                .setTitle("Fuel Level")
                .setOnClickListener { }
                .build()
        )
        
        // Engine Load
        paneBuilder.addRow(
            Row.Builder()
                .setTitle("Engine Load")
                .setOnClickListener { }
                .build()
        )
        
        // Add back action
        paneBuilder.addAction(
            Action.Builder()
                .setTitle("Back")
                .setOnClickListener { screenManager.pop() }
                .build()
        )
        
        return PaneTemplate.Builder(paneBuilder.build())
            .setTitle("Dashboard")
            .setHeaderAction(Action.APP_ICON)
            .build()
    }
}
