package com.toyota.obd210.data.car

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.Pane
import androidx.car.app.model.PaneBuilder
import androidx.car.app.model.Row
import androidx.car.app.model.Template

/**
 * Diagnostics Screen for Android Auto
 * Displays DTC codes and allows clearing them
 */
class CarDiagnosticsScreen(carContext: CarContext) : Screen(carContext) {
    
    override fun onGetTemplate(): Template {
        val paneBuilder = PaneBuilder()
        
        // DTC Status
        paneBuilder.addRow(
            Row.Builder()
                .setTitle("Stored DTCs")
                .setOnClickListener {
                    screenManager.push(CarDtcStoredScreen(carContext))
                }
                .build()
        )
        
        paneBuilder.addRow(
            Row.Builder()
                .setTitle("Pending DTCs")
                .setOnClickListener {
                    screenManager.push(CarDtcPendingScreen(carContext))
                }
                .build()
        )
        
        paneBuilder.addRow(
            Row.Builder()
                .setTitle("Permanent DTCs")
                .setOnClickListener {
                    screenManager.push(CarDtcPermanentScreen(carContext))
                }
                .build()
        )
        
        // Clear DTCs action
        paneBuilder.addAction(
            Action.Builder()
                .setTitle("Clear All DTCs")
                .setOnClickListener {
                    screenManager.push(CarDtcClearConfirmScreen(carContext))
                }
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
            .setTitle("Diagnostics")
            .setHeaderAction(Action.APP_ICON)
            .build()
    }
}

/**
 * Stored DTCs Screen
 */
class CarDtcStoredScreen(carContext: CarContext) : Screen(carContext) {
    override fun onGetTemplate(): Template {
        val paneBuilder = PaneBuilder()
        
        paneBuilder.addRow(
            Row.Builder()
                .setTitle("No Stored DTCs")
                .setOnClickListener { }
                .build()
        )
        
        paneBuilder.addAction(
            Action.Builder()
                .setTitle("Back")
                .setOnClickListener { screenManager.pop() }
                .build()
        )
        
        return PaneTemplate.Builder(paneBuilder.build())
            .setTitle("Stored DTCs")
            .setHeaderAction(Action.APP_ICON)
            .build()
    }
}

/**
 * Pending DTCs Screen
 */
class CarDtcPendingScreen(carContext: CarContext) : Screen(carContext) {
    override fun onGetTemplate(): Template {
        val paneBuilder = PaneBuilder()
        
        paneBuilder.addRow(
            Row.Builder()
                .setTitle("No Pending DTCs")
                .setOnClickListener { }
                .build()
        )
        
        paneBuilder.addAction(
            Action.Builder()
                .setTitle("Back")
                .setOnClickListener { screenManager.pop() }
                .build()
        )
        
        return PaneTemplate.Builder(paneBuilder.build())
            .setTitle("Pending DTCs")
            .setHeaderAction(Action.APP_ICON)
            .build()
    }
}

/**
 * Permanent DTCs Screen
 */
class CarDtcPermanentScreen(carContext: CarContext) : Screen(carContext) {
    override fun onGetTemplate(): Template {
        val paneBuilder = PaneBuilder()
        
        paneBuilder.addRow(
            Row.Builder()
                .setTitle("No Permanent DTCs")
                .setOnClickListener { }
                .build()
        )
        
        paneBuilder.addAction(
            Action.Builder()
                .setTitle("Back")
                .setOnClickListener { screenManager.pop() }
                .build()
        )
        
        return PaneTemplate.Builder(paneBuilder.build())
            .setTitle("Permanent DTCs")
            .setHeaderAction(Action.APP_ICON)
            .build()
    }
}

/**
 * DTC Clear Confirmation Screen
 */
class CarDtcClearConfirmScreen(carContext: CarContext) : Screen(carContext) {
    override fun onGetTemplate(): Template {
        val paneBuilder = PaneBuilder()
        
        paneBuilder.addRow(
            Row.Builder()
                .setTitle("⚠️ Warning")
                .setSummary("Clearing DTCs will remove all diagnostic trouble codes from the vehicle.")
                .setOnClickListener { }
                .build()
        )
        
        paneBuilder.addAction(
            Action.Builder()
                .setTitle("Clear DTCs")
                .setOnClickListener {
                    // TODO: Implement DTC clearing logic
                    screenManager.pop()
                }
                .build()
        )
        
        paneBuilder.addAction(
            Action.Builder()
                .setTitle("Cancel")
                .setOnClickListener { screenManager.pop() }
                .build()
        )
        
        return PaneTemplate.Builder(paneBuilder.build())
            .setTitle("Clear DTCs?")
            .setHeaderAction(Action.APP_ICON)
            .build()
    }
}
