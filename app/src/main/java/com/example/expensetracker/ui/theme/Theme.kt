package com.example.expensetracker.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = ElectricLime,
    onPrimary = ElectricLimeOn,
    primaryContainer = ForestCardElevatedDark,
    onPrimaryContainer = ElectricLime,
    secondary = ElectricCyan,
    onSecondary = ElectricCyanOn,
    secondaryContainer = ForestCardDark,
    onSecondaryContainer = ElectricCyan,
    tertiary = EmeraldIncome,
    onTertiary = Color.White,
    background = ForestBackgroundDark,
    onBackground = ForestTextPrimaryDark,
    surface = ForestSurfaceDark,
    onSurface = ForestTextPrimaryDark,
    surfaceVariant = ForestCardDark,
    onSurfaceVariant = ForestTextSecondaryDark,
    outline = ForestBorderDark,
    outlineVariant = ForestBorderSubtle,
    error = CrimsonExpense,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF1B4D34),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE2F0E7),
    onPrimaryContainer = Color(0xFF0F3221),
    secondary = Color(0xFF0284C7),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0F2FE),
    onSecondaryContainer = Color(0xFF0369A1),
    tertiary = Color(0xFF059669),
    onTertiary = Color.White,
    background = ForestBackgroundLight,
    onBackground = ForestTextPrimaryLight,
    surface = ForestSurfaceLight,
    onSurface = ForestTextPrimaryLight,
    surfaceVariant = ForestCardLight,
    onSurfaceVariant = ForestTextSecondaryLight,
    outline = ForestBorderLight,
    outlineVariant = Color(0x1F000000),
    error = Color(0xFFE11D48),
    onError = Color.White
)

private val DarkExtendedColors = ExtendedColors(
    isDark = true,
    electricLime = ElectricLime,
    electricCyan = ElectricCyan,
    emeraldIncome = EmeraldIncome,
    crimsonExpense = CrimsonExpense,
    cardBackground = ForestCardDark,
    cardBorder = ForestBorderDark,
    cardElevated = ForestCardElevatedDark,
    textPrimary = ForestTextPrimaryDark,
    textSecondary = ForestTextSecondaryDark,
    textMuted = ForestTextMutedDark
)

private val LightExtendedColors = ExtendedColors(
    isDark = false,
    electricLime = Color(0xFF1B4D34),
    electricCyan = Color(0xFF0284C7),
    emeraldIncome = Color(0xFF059669),
    crimsonExpense = Color(0xFFE11D48),
    cardBackground = ForestCardLight,
    cardBorder = ForestBorderLight,
    cardElevated = ForestCardElevatedLight,
    textPrimary = ForestTextPrimaryLight,
    textSecondary = ForestTextSecondaryLight,
    textMuted = ForestTextMutedLight
)

@Composable
fun ExpenseTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val extendedColors = if (darkTheme) DarkExtendedColors else LightExtendedColors

    CompositionLocalProvider(
        LocalExtendedColors provides extendedColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

object ExpenseTrackerTheme {
    val extendedColors: ExtendedColors
        @Composable
        @ReadOnlyComposable
        get() = LocalExtendedColors.current
}
