package com.toyota.obd210.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    data object Dashboard : Screen(
        route = "dashboard",
        title = "Dashboard",
        icon = Icons.Default.Dashboard
    )
    
    data object Diagnostics : Screen(
        route = "diagnostics",
        title = "Diagnostics",
        icon = Icons.Default.Build
    )
    
    data object HiddenSettings : Screen(
        route = "hidden_settings",
        title = "Settings",
        icon = Icons.Default.Settings
    )
}

val bottomNavItems = listOf(
    Screen.Dashboard,
    Screen.Diagnostics,
    Screen.HiddenSettings
)