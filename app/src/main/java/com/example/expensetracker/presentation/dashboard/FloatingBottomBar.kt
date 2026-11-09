package com.example.expensetracker.presentation.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.expensetracker.R
import com.example.expensetracker.presentation.common.AppTooltip
import com.example.expensetracker.ui.theme.ElectricLime
import com.example.expensetracker.ui.theme.ElectricLimeOn
import com.example.expensetracker.ui.theme.ExpenseTrackerTheme
import com.example.expensetracker.ui.theme.ForestBorderDark
import com.example.expensetracker.ui.theme.ForestTextSecondaryDark

enum class BottomNavDestination {
    HOME,
    LEDGER,
    CATEGORIES,
    SETTINGS
}

@Composable
fun FloatingBottomBar(
    currentDestination: BottomNavDestination = BottomNavDestination.HOME,
    onHomeClick: () -> Unit = {},
    onLedgerClick: () -> Unit = {},
    onCategoriesClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val extendedColors = ExpenseTrackerTheme.extendedColors
    val barColor = if (extendedColors.isDark) Color(0xF00B1A12) else Color(0xF8FFFFFF)
    val barBorder = if (extendedColors.isDark) ForestBorderDark else extendedColors.cardBorder
    val selectedBg = if (extendedColors.isDark) ElectricLime else Color(0xFF1B4D34)
    val selectedTint = if (extendedColors.isDark) ElectricLimeOn else Color.White
    val unselectedTint = if (extendedColors.isDark) ForestTextSecondaryDark else extendedColors.textSecondary

    Surface(
        modifier = modifier
            .height(64.dp)
            .clip(RoundedCornerShape(32.dp)),
        shape = RoundedCornerShape(32.dp),
        color = barColor,
        border = BorderStroke(1.dp, barBorder),
        shadowElevation = if (extendedColors.isDark) 10.dp else 8.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Home
            val isHome = currentDestination == BottomNavDestination.HOME
            AppTooltip(text = "Dashboard") {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(if (isHome) selectedBg else Color.Transparent)
                        .clickable(onClick = onHomeClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_home),
                        contentDescription = "Home",
                        tint = if (isHome) selectedTint else unselectedTint,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // 2. Ledger / Accounts
            val isLedger = currentDestination == BottomNavDestination.LEDGER
            AppTooltip(text = "Financial Overview") {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(if (isLedger) selectedBg else Color.Transparent)
                        .clickable(onClick = onLedgerClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_savings),
                        contentDescription = "Ledger",
                        tint = if (isLedger) selectedTint else unselectedTint,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // 3. Categories / Analytics
            val isCategories = currentDestination == BottomNavDestination.CATEGORIES
            AppTooltip(text = "Filter by Category") {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(if (isCategories) selectedBg else Color.Transparent)
                        .clickable(onClick = onCategoriesClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_category),
                        contentDescription = "Categories",
                        tint = if (isCategories) selectedTint else unselectedTint,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // 4. Settings
            val isSettings = currentDestination == BottomNavDestination.SETTINGS
            AppTooltip(text = "Settings & Preferences") {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(if (isSettings) selectedBg else Color.Transparent)
                        .clickable(onClick = onSettingsClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_settings),
                        contentDescription = "Settings",
                        tint = if (isSettings) selectedTint else unselectedTint,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}
