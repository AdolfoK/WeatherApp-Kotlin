
package com.example.weatherapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val WeatherColors = darkColorScheme(
    primary = Color(0xFFB9D9FF),
    onPrimary = Color(0xFF102B4A),
    secondary = Color(0xFFCCDFFF),
    background = Color(0xFF09162D),
    onBackground = Color.White,
    surface = Color(0xFF172B49),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF263D5D),
    onSurfaceVariant = Color(0xFFD7E4F5),
    error = Color(0xFFFFB4AB)
)

@Composable
fun WeatherTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = WeatherColors,
        content = content
    )
}
