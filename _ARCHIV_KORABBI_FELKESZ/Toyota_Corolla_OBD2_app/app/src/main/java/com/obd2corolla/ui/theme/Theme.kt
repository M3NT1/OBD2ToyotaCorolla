package com.obd2corolla.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = DeepBlueLight,
    onPrimary = DarkBackground,
    primaryContainer = DeepBlue,
    onPrimaryContainer = LightSurface,
    secondary = CaristaBlue,
    onSecondary = DarkBackground,
    secondaryContainer = DeepBlueDark,
    onSecondaryContainer = LightSurface,
    tertiary = SuccessGreen,
    onTertiary = DarkBackground,
    background = DarkBackground,
    onBackground = LightSurface,
    surface = DarkSurface,
    onSurface = LightSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = LightSurfaceVariant,
    error = ErrorRed,
    onError = DarkBackground
)

private val LightColorScheme = lightColorScheme(
    primary = DeepBlue,
    onPrimary = LightSurface,
    primaryContainer = DeepBlueLight,
    onPrimaryContainer = DarkBackground,
    secondary = CaristaBlue,
    onSecondary = LightSurface,
    secondaryContainer = DeepBlueLight,
    onSecondaryContainer = DarkBackground,
    tertiary = SuccessGreen,
    onTertiary = LightSurface,
    background = LightBackground,
    onBackground = DarkBackground,
    surface = LightSurface,
    onSurface = DarkBackground,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = DarkSurfaceVariant,
    error = ErrorRed,
    onError = LightSurface
)

@Composable
fun OBD2CorollaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}