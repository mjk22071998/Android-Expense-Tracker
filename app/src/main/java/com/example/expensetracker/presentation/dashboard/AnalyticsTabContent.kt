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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expensetracker.Constants
import com.example.expensetracker.R
import com.example.expensetracker.data.local.entity.TransactionWithCategory
import com.example.expensetracker.domain.model.CurrencyHelper.formatAmount
import com.example.expensetracker.domain.model.chart.ChartDataSet
import com.example.expensetracker.domain.model.chart.ChartMode
import com.example.expensetracker.domain.model.getCategoryIcon
import com.example.expensetracker.presentation.common.AppTooltip
import com.example.expensetracker.presentation.common.TooltipPosition
import com.example.expensetracker.presentation.dashboard.chart.FinancialChartCard
import com.example.expensetracker.ui.theme.ElectricLime
import com.example.expensetracker.ui.theme.ExpenseTrackerTheme
import com.example.expensetracker.ui.theme.ForestBorderDark
import com.example.expensetracker.ui.theme.ForestCardDark

private data class CategorySpending(
    val categoryId: String,
    val categoryName: String,
    val iconKey: String,
    val totalAmount: Double,
    val percentage: Float
)

@Composable
fun AnalyticsTabContent(
    chartDataSet: ChartDataSet,
    selectedChartMode: ChartMode,
    currencySymbol: String,
    periodLabel: String,
    transactions: List<TransactionWithCategory>,
    onChartModeSelected: (ChartMode) -> Unit,
    onPeriodClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extendedColors = ExpenseTrackerTheme.extendedColors

    // Calculate category spending breakdown for this period's expense transactions
    val categorySpendingList = remember(transactions) {
        val expenseTx = transactions.filter { !it.isDeleted && it.type == Constants.TransactionType.EXPENSE }
        val totalExpense = expenseTx.sumOf { it.amount }
        if (totalExpense <= 0.0) {
            emptyList()
        } else {
            expenseTx
                .groupBy { it.categoryId ?: "uncategorized" }
                .map { (catId, txList) ->
                    val sum = txList.sumOf { it.amount }
                    val first = txList.first()
                    CategorySpending(
                        categoryId = catId,
                        categoryName = first.categoryName,
                        iconKey = first.categoryIcon,
                        totalAmount = sum,
                        percentage = (sum / totalExpense).toFloat().coerceIn(0f, 1f)
                    )
                }
                .sortedByDescending { it.totalAmount }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Time Period Selector Card
        AppTooltip(
            text = "Change timeframe for analytics",
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
                                text = "Analytics Period",
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

        // Full Interactive Hardware-Accelerated Chart Card
        FinancialChartCard(
            chartDataSet = chartDataSet,
            selectedMode = selectedChartMode,
            currencySymbol = currencySymbol,
            periodLabel = periodLabel,
            onModeSelected = onChartModeSelected
        )

        // Category Spending Distribution Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (extendedColors.isDark) ForestCardDark else extendedColors.cardElevated
            ),
            border = BorderStroke(1.dp, if (extendedColors.isDark) ForestBorderDark else extendedColors.cardBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Spending by Category",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (extendedColors.isDark) Color.White else extendedColors.textPrimary
                    )

                    if (categorySpendingList.isNotEmpty()) {
                        Text(
                            text = "${categorySpendingList.size} categories",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (extendedColors.isDark) Color(0xFFA5C4B2) else extendedColors.textSecondary
                        )
                    }
                }

                if (categorySpendingList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No category spending recorded for this period",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (extendedColors.isDark) Color(0xFFA5C4B2) else extendedColors.textSecondary
                        )
                    }
                } else {
                    categorySpendingList.forEach { spending ->
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
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
                                            .background(if (extendedColors.isDark) Color(0x22D4F63D) else Color(0x1F1B4D34)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            painter = painterResource(id = getCategoryIcon(spending.iconKey)),
                                            contentDescription = null,
                                            tint = if (extendedColors.isDark) ElectricLime else Color(0xFF1B4D34),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    Text(
                                        text = spending.categoryName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (extendedColors.isDark) Color.White else extendedColors.textPrimary
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "$currencySymbol ${formatAmount(spending.totalAmount)}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (extendedColors.isDark) Color.White else extendedColors.textPrimary
                                    )
                                    Text(
                                        text = "(${(spending.percentage * 100).toInt()}%)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (extendedColors.isDark) Color(0xFFA5C4B2) else extendedColors.textSecondary
                                    )
                                }
                            }

                            LinearProgressIndicator(
                                progress = { spending.percentage },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = if (extendedColors.isDark) ElectricLime else Color(0xFF1B4D34),
                                trackColor = if (extendedColors.isDark) Color(0x22FFFFFF) else Color(0x1A000000),
                                strokeCap = StrokeCap.Round
                            )
                        }
                    }
                }
            }
        }

        // Bottom space so content is never hidden behind floating navigation bar
        Spacer(modifier = Modifier.height(84.dp))
    }
}
