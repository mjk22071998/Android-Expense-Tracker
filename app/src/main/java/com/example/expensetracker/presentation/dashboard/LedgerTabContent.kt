package com.example.expensetracker.presentation.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expensetracker.Constants
import com.example.expensetracker.R
import com.example.expensetracker.data.local.entity.TransactionWithCategory
import com.example.expensetracker.domain.model.CurrencyHelper.formatAmount
import com.example.expensetracker.domain.model.DateRangeFilter
import com.example.expensetracker.presentation.common.AppTooltip
import com.example.expensetracker.presentation.common.TooltipPosition
import com.example.expensetracker.ui.theme.ElectricLime
import com.example.expensetracker.ui.theme.ElectricLimeOn
import com.example.expensetracker.ui.theme.ExpenseTrackerTheme
import com.example.expensetracker.ui.theme.ForestBorderDark
import com.example.expensetracker.ui.theme.ForestCardDark

@Composable
fun LedgerTabContent(
    selectedFilter: DateRangeFilter,
    transactions: List<TransactionWithCategory>,
    allTimeBalance: Double,
    monthlyTrendDelta: Double?,
    currencySymbol: String,
    onPeriodClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extendedColors = ExpenseTrackerTheme.extendedColors

    val periodLabel = when (selectedFilter) {
        is DateRangeFilter.AllTime -> "All Time"
        is DateRangeFilter.Today -> "Today"
        is DateRangeFilter.ThisWeek -> "This Week"
        is DateRangeFilter.ThisMonth -> "This Month"
        is DateRangeFilter.ThisYear -> "This Year"
        is DateRangeFilter.Custom -> "Custom Range"
    }

    val activeTx = remember(transactions) { transactions.filter { !it.isDeleted } }
    val incomeList = remember(activeTx) { activeTx.filter { it.type == Constants.TransactionType.INCOME } }
    val expenseList = remember(activeTx) { activeTx.filter { it.type == Constants.TransactionType.EXPENSE } }

    val periodIncome = remember(incomeList) { incomeList.sumOf { it.amount } }
    val periodExpense = remember(expenseList) { expenseList.sumOf { it.amount } }
    val periodNet = periodIncome - periodExpense

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Time Period Selector Card
        AppTooltip(
            text = "Change timeframe for financial overview",
            position = TooltipPosition.Below
        ) {
            Surface(
                onClick = onPeriodClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = if (extendedColors.isDark) ForestCardDark else extendedColors.cardElevated,
                border = BorderStroke(1.dp, if (extendedColors.isDark) ForestBorderDark else extendedColors.cardBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (extendedColors.isDark) Color(0x22D4F63D) else Color(0x1F1B4D34),
                            modifier = Modifier.size(34.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_date_range),
                                    contentDescription = "Period",
                                    tint = if (extendedColors.isDark) ElectricLime else Color(0xFF1B4D34),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "Overview Period",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (extendedColors.isDark) Color(0xFFA5C4B2) else extendedColors.textSecondary
                            )
                            Text(
                                text = periodLabel,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (extendedColors.isDark) Color.White else extendedColors.textPrimary
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (extendedColors.isDark) Color(0x22D4F63D) else Color(0x1F1B4D34)
                    ) {
                        Text(
                            text = "Change",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (extendedColors.isDark) ElectricLime else Color(0xFF1B4D34),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }
        }

        // Stitched Leathercraft Card
        val cardBrush = if (extendedColors.isDark) {
            Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF163E24),
                    Color(0xFF0F2B19),
                    Color(0xFF08170E)
                )
            )
        } else {
            Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF1E5238),
                    Color(0xFF153F2B),
                    Color(0xFF0E2C1E)
                )
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(cardBrush)
                    .drawBehind {
                        val strokeWidth = 1.2.dp.toPx()
                        val dashWidth = 5.dp.toPx()
                        val gapWidth = 4.dp.toPx()
                        val pathEffect = PathEffect.dashPathEffect(floatArrayOf(dashWidth, gapWidth), 0f)
                        val cornerRadius = 22.dp.toPx()
                        val inset = 6.dp.toPx()
                        drawRoundRect(
                            color = if (extendedColors.isDark) Color(0x55D4F63D) else Color(0x55A3D9B5),
                            topLeft = Offset(inset, inset),
                            size = Size(size.width - inset * 2, size.height - inset * 2),
                            cornerRadius = CornerRadius(cornerRadius, cornerRadius),
                            style = Stroke(width = strokeWidth, pathEffect = pathEffect)
                        )
                    }
                    .padding(20.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Electric Lime Income Pill
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = ElectricLime
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f, fill = false),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_savings),
                                    contentDescription = "Income",
                                    tint = ElectricLimeOn,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = if (selectedFilter is DateRangeFilter.AllTime) "Total Income" else "Income (${incomeList.size})",
                                    fontWeight = FontWeight.Bold,
                                    color = ElectricLimeOn,
                                    fontSize = 13.sp,
                                    maxLines = 1
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "$currencySymbol ${formatAmount(periodIncome)}",
                                fontWeight = FontWeight.ExtraBold,
                                color = ElectricLimeOn,
                                fontSize = 14.sp
                            )
                        }
                    }

                    // Crimson Expenses Pill
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = extendedColors.crimsonExpense
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f, fill = false),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_shopping_cart),
                                    contentDescription = "Expenses",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = if (selectedFilter is DateRangeFilter.AllTime) "Total Expenses" else "Expenses (${expenseList.size})",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    maxLines = 1
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "$currencySymbol ${formatAmount(periodExpense)}",
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Period Net Balance Section
                    Text(
                        text = if (selectedFilter is DateRangeFilter.AllTime) "Total Balance" else "Net Cashflow ($periodLabel)",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFFA5C4B2)
                    )

                    val netSign = if (periodNet < 0) "− " else ""
                    Text(
                        text = "$netSign$currencySymbol ${formatAmount(kotlin.math.abs(periodNet))}",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        letterSpacing = (-0.8).sp
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(text = "✦", color = ElectricLime, fontSize = 12.sp)
                        Text(
                            text = if (periodNet >= 0) "Healthy financial balance maintained"
                            else "Expenses exceeding income — review budget",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFA5C4B2)
                        )
                    }

                    if (selectedFilter !is DateRangeFilter.AllTime) {
                        Text(
                            text = "All-Time Account Balance: $currencySymbol ${formatAmount(allTimeBalance)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0x88A5C4B2),
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // Cashflow Metric Cards (Side-by-side or stacked)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Inflow Summary Card
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (extendedColors.isDark) ForestCardDark else extendedColors.cardElevated
                ),
                border = BorderStroke(1.dp, if (extendedColors.isDark) ForestBorderDark else extendedColors.cardBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(if (extendedColors.isDark) Color(0x22D4F63D) else Color(0x1F1B4D34)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_savings),
                            contentDescription = null,
                            tint = if (extendedColors.isDark) ElectricLime else Color(0xFF1B4D34),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = "Total Inflow",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (extendedColors.isDark) Color(0xFFA5C4B2) else extendedColors.textSecondary
                    )
                    Text(
                        text = "$currencySymbol ${formatAmount(periodIncome)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (extendedColors.isDark) ElectricLime else Color(0xFF1B4D34)
                    )
                    Text(
                        text = "${incomeList.size} entries",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (extendedColors.isDark) Color(0x88A5C4B2) else extendedColors.textSecondary
                    )
                }
            }

            // Outflow Summary Card
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (extendedColors.isDark) ForestCardDark else extendedColors.cardElevated
                ),
                border = BorderStroke(1.dp, if (extendedColors.isDark) ForestBorderDark else extendedColors.cardBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0x22FF5252)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_shopping_cart),
                            contentDescription = null,
                            tint = Color(0xFFFF5252),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = "Total Outflow",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (extendedColors.isDark) Color(0xFFA5C4B2) else extendedColors.textSecondary
                    )
                    Text(
                        text = "$currencySymbol ${formatAmount(periodExpense)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF5252)
                    )
                    Text(
                        text = "${expenseList.size} entries",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (extendedColors.isDark) Color(0x88A5C4B2) else extendedColors.textSecondary
                    )
                }
            }
        }

        // Month-over-Month Trend Card (if available)
        monthlyTrendDelta?.let { delta ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (extendedColors.isDark) ForestCardDark else extendedColors.cardElevated
                ),
                border = BorderStroke(1.dp, if (extendedColors.isDark) ForestBorderDark else extendedColors.cardBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (delta >= 0) Color(0x22D4F63D) else Color(0x22FF5252),
                            modifier = Modifier.size(34.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    painter = painterResource(
                                        id = if (delta >= 0) R.drawable.ic_trending_up else R.drawable.ic_trending_down
                                    ),
                                    contentDescription = "Trend",
                                    tint = if (delta >= 0) ElectricLime else Color(0xFFFF5252),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "Month-over-Month Net",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (extendedColors.isDark) Color(0xFFA5C4B2) else extendedColors.textSecondary
                            )
                            val prefix = if (delta >= 0) "+" else ""
                            Text(
                                text = "$prefix$currencySymbol ${formatAmount(delta)}",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (delta >= 0) ElectricLime else Color(0xFFFF5252)
                            )
                        }
                    }

                    Text(
                        text = if (delta >= 0) "Better than last month" else "Lower than last month",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (extendedColors.isDark) Color(0xFFA5C4B2) else extendedColors.textSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(84.dp))
    }
}
