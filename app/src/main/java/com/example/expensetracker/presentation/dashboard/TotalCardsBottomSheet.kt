package com.example.expensetracker.presentation.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.example.expensetracker.ui.theme.ForestBackgroundDark
import com.example.expensetracker.ui.theme.ForestBorderDark
import com.example.expensetracker.ui.theme.ForestCardDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TotalCardsBottomSheet(
    selectedFilter: DateRangeFilter,
    transactions: List<TransactionWithCategory>,
    allTimeBalance: Double,
    currencySymbol: String,
    onFilterSelected: (DateRangeFilter) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showPeriodSheet by remember { mutableStateOf(false) }
    var showCustomDateDialog by remember { mutableStateOf(false) }

    val periodLabel = when (selectedFilter) {
        is DateRangeFilter.AllTime -> "All Time"
        is DateRangeFilter.Today -> "Today"
        is DateRangeFilter.ThisWeek -> "This Week"
        is DateRangeFilter.ThisMonth -> "This Month"
        is DateRangeFilter.ThisYear -> "This Year"
        is DateRangeFilter.Custom -> "Custom Range"
    }

    val periodIncome = remember(transactions) {
        transactions
            .filter { it.type == Constants.TransactionType.INCOME }
            .sumOf { it.amount }
    }
    val periodExpense = remember(transactions) {
        transactions
            .filter { it.type == Constants.TransactionType.EXPENSE }
            .sumOf { it.amount }
    }
    val periodNet = periodIncome - periodExpense


    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = ForestBackgroundDark,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header: Financial Overview
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Financial Overview",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Inspection of period cashflow & ledger",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFA5C4B2)
                    )
                }

                AppTooltip(
                    text = "Close financial overview",
                    position = TooltipPosition.Below
                ) {
                    Surface(
                        onClick = onDismiss,
                        modifier = Modifier.size(36.dp),
                        shape = CircleShape,
                        color = Color(0x28FFFFFF)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_close),
                                contentDescription = "Close",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Period Selector Card (Defaults to dashboard active period, customizable here)
            AppTooltip(
                text = "Change timeframe for financial overview",
                position = TooltipPosition.Below
            ) {
                Surface(
                    onClick = { showPeriodSheet = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = ForestCardDark,
                    border = BorderStroke(1.dp, ForestBorderDark)
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
                                color = Color(0x220099FF),
                                modifier = Modifier.size(34.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_date_range),
                                        contentDescription = "Period",
                                        tint = Color(0xFF0099FF),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "Time Period",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFFA5C4B2)
                                )
                                Text(
                                    text = periodLabel,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0x22D4F63D)
                        ) {
                            Text(
                                text = "Change",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = ElectricLime,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }

            // Screen 2 Stitched Leathercraft Card
            val cardBrush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF163E24),
                    Color(0xFF0F2B19),
                    Color(0xFF08170E)
                )
            )

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
                                color = Color(0x55D4F63D),
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
                                        text = if (selectedFilter is DateRangeFilter.AllTime) "Total Income" else "Income",
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

                        // Blue Expenses Pill
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFF0099FF)
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
                                        text = if (selectedFilter is DateRangeFilter.AllTime) "Total Expenses" else "Expenses",
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
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Modal: Period Filter Selection inside Financial Overview
    if (showPeriodSheet) {
        PeriodFilterBottomSheet(
            selectedFilter = selectedFilter,
            onFilterSelected = { filter ->
                onFilterSelected(filter)
                showPeriodSheet = false
            },
            onCustomRangeClick = {
                showPeriodSheet = false
                showCustomDateDialog = true
            },
            onDismiss = { showPeriodSheet = false }
        )
    }

    // Modal: Custom Date Range Picker inside Financial Overview
    if (showCustomDateDialog) {
        CustomDateRangePickerDialog(
            onDismiss = { showCustomDateDialog = false },
            onRangeSelected = { start, end ->
                val customFilter = DateRangeFilter.Custom(start, end)
                onFilterSelected(customFilter)
                showCustomDateDialog = false
            }
        )
    }
}
