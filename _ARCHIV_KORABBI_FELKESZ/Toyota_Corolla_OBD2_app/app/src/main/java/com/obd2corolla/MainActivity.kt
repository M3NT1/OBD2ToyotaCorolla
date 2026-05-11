package com.obd2corolla

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.obd2corolla.ui.navigation.MainNavigation
import com.obd2corolla.ui.theme.OBD2CorollaTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OBD2CorollaTheme {
                MainNavigation()
            }
        }
    }
}