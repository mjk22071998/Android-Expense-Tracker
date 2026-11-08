package com.example.expensetracker.presentation.settings

import android.content.res.Configuration
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.expensetracker.Constants
import com.example.expensetracker.R
import com.example.expensetracker.ui.theme.ElectricLime
import com.example.expensetracker.ui.theme.ElectricLimeOn
import com.example.expensetracker.ui.theme.ExpenseTrackerTheme

@Composable
fun ThemeModeSelector(
    selectedMode: String,
    onModeSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val extendedColors = ExpenseTrackerTheme.extendedColors

    val options = listOf(
        Triple(Constants.ThemeMode.SYSTEM, "System", R.drawable.ic_brightness_auto),
        Triple(Constants.ThemeMode.LIGHT, "Light", R.drawable.ic_light_mode),
        Triple(Constants.ThemeMode.DARK, "Dark", R.drawable.ic_dark_mode)
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = CircleShape,
        color = extendedColors.cardElevated,
        border = BorderStroke(1.dp, extendedColors.cardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            options.forEach { (mode, label, icon) ->
                val isSelected = selectedMode == mode

                val itemBg by animateColorAsState(
                    targetValue = if (isSelected) ElectricLime else Color.Transparent,
                    animationSpec = tween(200),
                    label = "themeItemBg"
                )
                val itemContentColor by animateColorAsState(
                    targetValue = if (isSelected) ElectricLimeOn else extendedColors.textSecondary,
                    animationSpec = tween(200),
                    label = "themeItemColor"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(CircleShape)
                        .background(itemBg)
                        .clickable { onModeSelected(mode) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = icon),
                            contentDescription = null,
                            tint = itemContentColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = itemContentColor
                        )
                    }
                }
            }
        }
    }
}

@Preview(name = "Theme Selector - Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ThemeModeSelectorPreview() {
    ExpenseTrackerTheme {
        ThemeModeSelector(
            selectedMode = Constants.ThemeMode.DARK,
            onModeSelected = {}
        )
    }
}
