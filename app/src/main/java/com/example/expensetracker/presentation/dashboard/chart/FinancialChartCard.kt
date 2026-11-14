package com.example.expensetracker.presentation.dashboard.chart

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expensetracker.R
import com.example.expensetracker.domain.model.chart.ChartDataSet
import com.example.expensetracker.domain.model.chart.ChartMode
import com.example.expensetracker.ui.theme.LocalExtendedColors
import kotlin.math.roundToInt

@Composable
fun FinancialChartCard(
    chartDataSet: ChartDataSet,
    selectedMode: ChartMode,
    currencySymbol: String,
    periodLabel: String,
    onModeSelected: (ChartMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val extendedColors = LocalExtendedColors.current

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = extendedColors.cardBackground,
        border = BorderStroke(1.dp, extendedColors.cardBorder),
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // ---------- 1. Header: Title + Period Badge ----------
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(
                                if (extendedColors.isDark) Color(0x33489B5F) else Color(0x33A3D9B5)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_trending_up),
                            contentDescription = null,
                            tint = extendedColors.electricLime,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Text(
                        text = "Cashflow Analytics",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = extendedColors.textPrimary,
                        letterSpacing = (-0.2).sp
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = extendedColors.cardElevated,
                    border = BorderStroke(0.8.dp, extendedColors.cardBorder)
                ) {
                    Text(
                        text = periodLabel,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = extendedColors.textSecondary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ---------- 2. 3-Way Mode Toggle Pill ----------
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(extendedColors.cardElevated)
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                ChartModeTab(
                    title = "Expenses",
                    isSelected = selectedMode == ChartMode.EXPENSES,
                    activeColor = extendedColors.crimsonExpense,
                    activeTextColor = Color.White,
                    onClick = { onModeSelected(ChartMode.EXPENSES) },
                    modifier = Modifier.weight(1f)
                )

                ChartModeTab(
                    title = "Income",
                    isSelected = selectedMode == ChartMode.INCOME,
                    activeColor = extendedColors.emeraldIncome,
                    activeTextColor = Color.White,
                    onClick = { onModeSelected(ChartMode.INCOME) },
                    modifier = Modifier.weight(1f)
                )

                ChartModeTab(
                    title = "Balance",
                    isSelected = selectedMode == ChartMode.BALANCE,
                    activeColor = extendedColors.electricLime,
                    activeTextColor = Color(0xFF08140E),
                    onClick = { onModeSelected(ChartMode.BALANCE) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // ---------- 3. Metric Summary Context Strip ----------
            val summaryText = when (selectedMode) {
                ChartMode.EXPENSES -> {
                    "Total Spent: $currencySymbol ${formatAmount(chartDataSet.totalExpense)} • Peak: $currencySymbol ${formatAmount(chartDataSet.maxExpense)}"
                }
                ChartMode.INCOME -> {
                    "Total Inflow: $currencySymbol ${formatAmount(chartDataSet.totalIncome)} • Peak: $currencySymbol ${formatAmount(chartDataSet.maxIncome)}"
                }
                ChartMode.BALANCE -> {
                    val closingBalance = chartDataSet.points.lastOrNull()?.runningBalance ?: 0.0
                    "Closing Balance: $currencySymbol ${formatAmount(closingBalance)}"
                }
            }

            Text(
                text = summaryText,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = extendedColors.textSecondary,
                letterSpacing = 0.2.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ---------- 4. Chart or Empty State ----------
            if (chartDataSet.isEmpty) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(extendedColors.cardElevated.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_receipt_long),
                            contentDescription = null,
                            tint = extendedColors.textMuted,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "No transactions recorded for this period",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = extendedColors.textSecondary
                        )
                        Text(
                            text = "Log transactions to reveal cashflow and balance trends",
                            style = MaterialTheme.typography.labelSmall,
                            color = extendedColors.textMuted
                        )
                    }
                }
            } else {
                FinancialCanvasChart(
                    dataSet = chartDataSet,
                    mode = selectedMode,
                    currencySymbol = currencySymbol
                )

                // Drag scrubbing hint footnote
                Text(
                    text = "Tip: drag finger across chart to scrub daily values",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = extendedColors.textMuted,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

@Composable
private fun ChartModeTab(
    title: String,
    isSelected: Boolean,
    activeColor: Color,
    activeTextColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extendedColors = LocalExtendedColors.current
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) activeColor else Color.Transparent,
        animationSpec = tween(durationMillis = 200),
        label = "tabBgColor"
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) activeTextColor else extendedColors.textSecondary,
        animationSpec = tween(durationMillis = 200),
        label = "tabTextColor"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(9.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = textColor
        )
    }
}

private fun formatAmount(amount: Double): String {
    val rounded = (amount * 100.0).roundToInt() / 100.0
    return if (rounded % 1.0 == 0.0) {
        String.format(java.util.Locale.getDefault(), "%,d", rounded.toLong())
    } else {
        String.format(java.util.Locale.getDefault(), "%,.2f", rounded)
    }
}
