package com.example.expensetracker.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6650A4),
    secondary = Color(0xFF2E7D32),
    tertiary = Color(0xFF9C27B0),
    background = Color(0xFFF3F0FA),      // soft lavender tint instead of white
    surface = Color(0xFFFAF8FF),          // slightly off-white for cards
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFCFBCFF),
    secondary = Color(0xFF81C784),
    tertiary = Color(0xFFE1BEE7),
    background = Color(0xFF141218),       // deep purple-black instead of pure black
    surface = Color(0xFF1D1B20),
    onBackground = Color(0xFFE6E0E9),
    onSurface = Color(0xFFE6E0E9)
)

@Composable
fun ExpenseTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}