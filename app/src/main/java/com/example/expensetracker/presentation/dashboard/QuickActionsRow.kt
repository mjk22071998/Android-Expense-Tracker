package com.example.expensetracker.presentation.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expensetracker.R
import com.example.expensetracker.presentation.common.AppTooltip
import com.example.expensetracker.presentation.common.TooltipPosition
import com.example.expensetracker.ui.theme.ElectricLime
import com.example.expensetracker.ui.theme.ElectricLimeOn
import com.example.expensetracker.ui.theme.ExpenseTrackerTheme

@Composable
fun QuickActionsRow(
    onAddTransaction: () -> Unit,
    onFilterPeriodClick: () -> Unit,
    periodLabel: String = "This Month",
    modifier: Modifier = Modifier
) {
    val extendedColors = ExpenseTrackerTheme.extendedColors
    val addContainerColor = if (extendedColors.isDark) ElectricLime else Color(0xFF1B4D34)
    val addContentColor = if (extendedColors.isDark) ElectricLimeOn else Color.White
    val periodContainerColor = if (extendedColors.isDark) Color(0xFF0099FF) else Color(0xFF0284C7)

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Primary Action Pill (Add Entry)
        Box(modifier = Modifier.weight(1f)) {
            AppTooltip(
                text = "Record new expense or income",
                position = TooltipPosition.Below,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    onClick = onAddTransaction,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = addContainerColor
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_add),
                            contentDescription = "Add Entry",
                            tint = addContentColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Add Entry",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = addContentColor,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(start = 6.dp)
                        )
                    }
                }
            }
        }

        // 2. Secondary Filter Pill (Period Filter)
        Box(modifier = Modifier.weight(1f)) {
            AppTooltip(
                text = "Filter transactions by date range",
                position = TooltipPosition.Below,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    onClick = onFilterPeriodClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = periodContainerColor
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_date_range),
                            contentDescription = "Period",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = periodLabel,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(start = 6.dp)
                        )
                    }
                }
            }
        }
    }
}
