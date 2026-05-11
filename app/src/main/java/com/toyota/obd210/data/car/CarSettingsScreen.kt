package com.toyota.obd210.data.car

import androidx.car.app.AlertDialog
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.Pane
import androidx.car.app.model.PaneBuilder
import androidx.car.app.model.Row
import androidx.car.app.model.Template
import androidx.car.app.model.Toggle

/**
 * Settings Screen for Android Auto
 * Displays and allows modification of hidden vehicle settings
 */
class CarSettingsScreen(carContext: CarContext) : Screen(carContext) {
    
    override fun onGetTemplate(): Template {
        val paneBuilder = PaneBuilder()
        
        // Door Lock Settings
        paneBuilder.addRow(
            Row.Builder()
                .setTitle("Door Lock Customization")
                .setOnClickListener {
                    screenManager.push(CarSettingsDoorLocksScreen(carContext))
                }
                .build()
        )
        
        // Lighting Settings
        paneBuilder.addRow(
            Row.Builder()
                .setTitle("Lighting Settings")
                .setOnClickListener {
                    screenManager.push(CarSettingsLightingScreen(carContext))
                }
                .build()
        )
        
        // Climate Settings
        paneBuilder.addRow(
            Row.Builder()
                .setTitle("Climate Control")
                .setOnClickListener {
                    screenManager.push(CarSettingsClimateScreen(carContext))
                }
                .build()
        )
        
        // Multimedia Settings
        paneBuilder.addRow(
            Row.Builder()
                .setTitle("Multimedia & Display")
                .setOnClickListener {
                    screenManager.push(CarSettingsMultimediaScreen(carContext))
                }
                .build()
        )
        
        // Toyota Safety Sense
        paneBuilder.addRow(
            Row.Builder()
                .setTitle("Toyota Safety Sense (TSS)")
                .setOnClickListener {
                    screenManager.push(CarSettingsTssScreen(carContext))
                }
                .build()
        )
        
        // Seatbelt Settings
        paneBuilder.addRow(
            Row.Builder()
                .setTitle("Seatbelt Minder")
                .setOnClickListener {
                    screenManager.push(CarSettingsSeatbeltScreen(carContext))
                }
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
            .setTitle("Vehicle Settings")
            .setHeaderAction(Action.APP_ICON)
            .build()
    }
    
    /**
     * Shows confirmation dialog before modifying settings
     */
    private fun showConfirmation(settingName: String) {
        val dialog = AlertDialog.Builder(carContext)
            .setTitle("Confirm Change")
            .setMessage("Are you sure you want to modify $settingName?")
            .setPositiveButton("Yes") { }
            .setNegativeButton("No") { }
            .build()
        
        carContext.startCarApp(dialog)
    }
}

/**
 * Door Lock Settings Sub-screen
 */
class CarSettingsDoorLocksScreen(carContext: CarContext) : Screen(carContext) {
    override fun onGetTemplate(): Template = buildSettingsPane(
        title = "Door Lock Settings",
        rows = listOf(
            "Auto Lock" to true,
            "Auto Unlock" to true,
            "Unlock All Doors" to false,
            "Horn Lock Confirm" to true,
            "Horn Unlock Confirm" to true
        )
    )
}

/**
 * Lighting Settings Sub-screen
 */
class CarSettingsLightingScreen(carContext: CarContext) : Screen(carContext) {
    override fun onGetTemplate(): Template = buildSettingsPane(
        title = "Lighting Settings",
        rows = listOf(
            "Daytime Running Lights" to true,
            "Follow Me Home" to true,
            "Welcome Light" to true,
            "Interior Light Auto" to true
        )
    )
}

/**
 * Climate Control Settings Sub-screen
 */
class CarSettingsClimateScreen(carContext: CarContext) : Screen(carContext) {
    override fun onGetTemplate(): Template = buildSettingsPane(
        title = "Climate Control",
        rows = listOf(
            "Auto Recirculation" to true,
            "Remote Start" to true
        )
    )
}

/**
 * Multimedia Settings Sub-screen
 */
class CarSettingsMultimediaScreen(carContext: CarContext) : Screen(carContext) {
    override fun onGetTemplate(): Template = buildSettingsPane(
        title = "Multimedia & Display",
        rows = listOf(
            "Beep Sound" to true,
            "Camera Guidelines" to true,
            "Park Assist" to true,
            "Start Sound" to true
        )
    )
}

/**
 * Toyota Safety Sense Settings Sub-screen
 */
class CarSettingsTssScreen(carContext: CarContext) : Screen(carContext) {
    override fun onGetTemplate(): Template = buildSettingsPane(
        title = "Toyota Safety Sense",
        rows = listOf(
            "Pre-Collision System" to true,
            "Lane Departure Alert" to true,
            "Adaptive Cruise Control" to true,
            "Road Sign Assist" to true
        )
    )
}

/**
 * Seatbelt Minder Settings Sub-screen
 */
class CarSettingsSeatbeltScreen(carContext: CarContext) : Screen(carContext) {
    override fun onGetTemplate(): Template = buildSettingsPane(
        title = "Seatbelt Minder",
        rows = listOf(
            "Seatbelt Minder" to true,
            "Minder Chime" to true
        )
    )
}

/**
 * Helper function to build settings pane with toggle rows
 */
private fun buildSettingsPane(title: String, rows: List<Pair<String, Boolean>>): Template {
    // This is a simplified version - actual implementation would need
    // proper PaneBuilder initialization
    val paneBuilder = PaneBuilder()
    
    rows.forEach { (settingName, isEnabled) ->
        paneBuilder.addRow(
            Row.Builder()
                .setTitle(settingName)
                .setToggle(
                    Toggle.Builder { }
                        .setChecked(isEnabled)
                        .build()
                )
                .build()
        )
    }
    
    paneBuilder.addAction(
        Action.Builder()
            .setTitle("Back")
            .setOnClickListener { /* Pop handled by screen manager */ }
            .build()
    )
    
    return PaneTemplate.Builder(paneBuilder.build())
        .setTitle(title)
        .setHeaderAction(Action.APP_ICON)
        .build()
}
