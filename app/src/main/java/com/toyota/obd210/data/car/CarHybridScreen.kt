package com.toyota.obd210.data.car

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.Pane
import androidx.car.app.model.PaneBuilder
import androidx.car.app.model.Row
import androidx.car.app.model.Template

/**
 * Hybrid Monitor Screen for Android Auto
 * Displays hybrid system specific data: HV Battery, Motor Torque, EV Mode
 */
class CarHybridScreen(carContext: CarContext) : Screen(carContext) {
    
    override fun onGetTemplate(): Template {
        val paneBuilder = PaneBuilder()
        
        // HV Battery State of Charge
        paneBuilder.addRow(
            Row.Builder()
                .setTitle("HV Battery SOC")
                .setOnClickListener { }
                .build()
        )
        
        // Motor Torque
        paneBuilder.addRow(
            Row.Builder()
                .setTitle("Motor Torque")
                .setOnClickListener { }
                .build()
        )
        
        // Generator Torque
        paneBuilder.addRow(
            Row.Builder()
                .setTitle("Generator Torque")
                .setOnClickListener { }
                .build()
        )
        
        // Battery Voltage
        paneBuilder.addRow(
            Row.Builder()
                .setTitle("Battery Voltage")
                .setOnClickListener { }
                .build()
        )
        
        // Battery Current
        paneBuilder.addRow(
            Row.Builder()
                .setTitle("Battery Current")
                .setOnClickListener { }
                .build()
        )
        
        // EV Mode Status
        paneBuilder.addRow(
            Row.Builder()
                .setTitle("EV Mode Status")
                .setOnClickListener { }
                .build()
        )
        
        // Back action
        paneBuilder.addAction(
            Action.Builder()
                .setTitle("Back")
                .setOnClickListener { screenManager.pop() }
                .build()
        )
        
        return PaneTemplate.Builder(paneBuilder.build())
            .setTitle("Hybrid Monitor")
            .setHeaderAction(Action.APP_ICON)
            .build()
    }
}
