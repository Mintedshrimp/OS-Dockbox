package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = UDroidGreen,
    onPrimary = Color.White,
    primaryContainer = UDroidGreenDark,
    onPrimaryContainer = UDroidGreenLight,
    secondary = UDroidCyan,
    onSecondary = Color.White,
    background = UDroidDarkSurface,
    onBackground = Color(0xFFE2E8F0),
    surface = UDroidDarkCard,
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = Color(0xFF1E293B),
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = Color(0xFF334155)
)

private val LightColorScheme = lightColorScheme(
    primary = UDroidGreen,
    onPrimary = Color.White,
    primaryContainer = UDroidGreenLight,
    onPrimaryContainer = UDroidGreenDark,
    secondary = UDroidCyan,
    onSecondary = Color.White,
    background = UDroidBg,
    onBackground = UDroidTextPrimary,
    surface = UDroidCardSurface,
    onSurface = UDroidTextPrimary,
    surfaceVariant = Color(0xFFEFF4F1),
    onSurfaceVariant = UDroidTextSecondary,
    outline = UDroidCardBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep consistent branded theme
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
