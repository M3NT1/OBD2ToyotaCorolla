package com.obd2corolla.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.obd2corolla.ui.screens.dashboard.DashboardScreen
import com.obd2corolla.ui.screens.dtc.DtcScreen
import com.obd2corolla.ui.screens.settings.SettingsScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route,
        modifier = modifier
    ) {
        composable(Screen.Dashboard.route) {
            DashboardScreen()
        }
        composable(Screen.Dtc.route) {
            DtcScreen()
        }
        composable(Screen.Settings.route) {
            SettingsScreen()
        }
    }
}