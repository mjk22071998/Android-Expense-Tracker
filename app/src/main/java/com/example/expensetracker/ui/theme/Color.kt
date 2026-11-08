package com.example.expensetracker.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ============================================================================
// Organic Forest & Electric Lime Brand Palette (Fintech Luxury)
// Inspired by humanized organic financial design
// ============================================================================

// Core Dark Surfaces (Forest Pine)
val ForestBackgroundDark = Color(0xFF08140E)
val ForestSurfaceDark = Color(0xFF0E2017)
val ForestCardDark = Color(0xFF142B1F)
val ForestCardElevatedDark = Color(0xFF1A3527)
val ForestBorderDark = Color(0xFF214431)
val ForestBorderSubtle = Color(0x2EFFFFFF)

// Core Light Surfaces (Mint Sage)
val ForestBackgroundLight = Color(0xFFF2F7F4)
val ForestSurfaceLight = Color(0xFFFFFFFF)
val ForestCardLight = Color(0xFFF8FAF9)
val ForestCardElevatedLight = Color(0xFFFFFFFF)
val ForestBorderLight = Color(0xFFD6E3DC)

// Accent & Action Colors
val ElectricLime = Color(0xFFD4F63D)        // Primary CTA, highlight stars, transfer pill
val ElectricLimePressed = Color(0xFFBEDF30)
val ElectricLimeOn = Color(0xFF08140E)

val ElectricCyan = Color(0xFF00A3FF)        // Secondary action, targets, spend pills
val ElectricCyanPressed = Color(0xFF008EDF)
val ElectricCyanOn = Color(0xFFFFFFFF)

// Financial Semantic Indicators
val EmeraldIncome = Color(0xFF10B981)       // Deposits, income, positive deltas
val CrimsonExpense = Color(0xFFF43F5E)      // Expenses, deductions, alerts
val AmberWarning = Color(0xFFF59E0B)

// Typography & Text Shades
val ForestTextPrimaryDark = Color(0xFFF2F7F4)
val ForestTextSecondaryDark = Color(0xFF91AB9D)
val ForestTextMutedDark = Color(0xFF5A7869)

val ForestTextPrimaryLight = Color(0xFF09160F)
val ForestTextSecondaryLight = Color(0xFF4C6657)
val ForestTextMutedLight = Color(0xFF7D9688)

// Semantic extended colors bundle for easy access in Composables
@Immutable
data class ExtendedColors(
    val isDark: Boolean = true,
    val electricLime: Color,
    val electricCyan: Color,
    val emeraldIncome: Color,
    val crimsonExpense: Color,
    val cardBackground: Color,
    val cardBorder: Color,
    val cardElevated: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color
)

val LocalExtendedColors = staticCompositionLocalOf {
    ExtendedColors(
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
}
