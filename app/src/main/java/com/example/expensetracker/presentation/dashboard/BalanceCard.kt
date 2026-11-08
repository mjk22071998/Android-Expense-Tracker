package com.example.expensetracker.presentation.dashboard

import android.content.res.Configuration
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expensetracker.Constants
import com.example.expensetracker.R
import com.example.expensetracker.ui.theme.ElectricCyan
import com.example.expensetracker.ui.theme.ElectricCyanOn
import com.example.expensetracker.ui.theme.ElectricLime
import com.example.expensetracker.ui.theme.ElectricLimeOn
import com.example.expensetracker.ui.theme.ExpenseTrackerTheme
import com.example.expensetracker.ui.theme.ForestBorderDark
import com.example.expensetracker.ui.theme.ForestCardDark
import com.example.expensetracker.ui.theme.ForestSurfaceDark
import com.example.expensetracker.ui.theme.ForestTextPrimaryDark
import com.example.expensetracker.ui.theme.ForestTextSecondaryDark
import java.text.NumberFormat
import java.util.Locale

@Composable
fun BalanceCard(
    modifier: Modifier = Modifier,
    balance: Double,
    currencySymbol: String,
    totalIncome: Double = 0.0,
    totalExpense: Double = 0.0,
    selectedTypeFilter: String? = null,
    onTypeFilterSelected: ((String) -> Unit)? = null
) {
    // Subtle luxury organic forest gradient
    val gradientBrush = Brush.linearGradient(
        colors = listOf(
            ForestCardDark,
            ForestSurfaceDark,
            Color(0xFF0A1A11)
        ),
        start = Offset(0f, 0f),
        end = Offset(1000f, 1000f)
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        border = BorderStroke(1.dp, ForestBorderDark),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(gradientBrush)
                // Handcrafted luxury stitched inner border inspired by leathercraft
                .drawBehind {
                    val strokeWidth = 1.2.dp.toPx()
                    val dashWidth = 5.dp.toPx()
                    val gapWidth = 4.dp.toPx()
                    val pathEffect = PathEffect.dashPathEffect(floatArrayOf(dashWidth, gapWidth), 0f)
                    val cornerRadius = 20.dp.toPx()
                    val inset = 7.dp.toPx()
                    drawRoundRect(
                        color = Color(0x44D4F63D),
                        topLeft = Offset(inset, inset),
                        size = Size(size.width - inset * 2, size.height - inset * 2),
                        cornerRadius = CornerRadius(cornerRadius, cornerRadius),
                        style = Stroke(width = strokeWidth, pathEffect = pathEffect)
                    )
                }
                .padding(20.dp)
        ) {
            // Ambient glow accent circle in background
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 40.dp, y = (-40).dp)
                    .background(
                        color = ElectricLime.copy(alpha = 0.07f),
                        shape = CircleShape
                    )
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Dual Account / Summary Pills (Yellow & Blue from Inspiration Screen 2)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Lime Income Pill
                    val isIncomeActive = selectedTypeFilter == Constants.TransactionType.INCOME
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                onTypeFilterSelected?.invoke(Constants.TransactionType.INCOME)
                            },
                        color = if (isIncomeActive) ElectricLime else ElectricLime.copy(alpha = 0.92f),
                        shape = RoundedCornerShape(16.dp),
                        shadowElevation = 2.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_trending_up),
                                    contentDescription = "Income",
                                    tint = ElectricLimeOn,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = "Inflow",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = ElectricLimeOn.copy(alpha = 0.85f),
                                    fontSize = 11.sp
                                )
                            }
                            Text(
                                text = "$currencySymbol ${formatAmount(totalIncome)}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = ElectricLimeOn,
                                fontSize = 14.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Cyan Expense Pill
                    val isExpenseActive = selectedTypeFilter == Constants.TransactionType.EXPENSE
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                onTypeFilterSelected?.invoke(Constants.TransactionType.EXPENSE)
                            },
                        color = if (isExpenseActive) ElectricCyan else ElectricCyan.copy(alpha = 0.92f),
                        shape = RoundedCornerShape(16.dp),
                        shadowElevation = 2.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_trending_down),
                                    contentDescription = "Expense",
                                    tint = ElectricCyanOn,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = "Outflow",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = ElectricCyanOn.copy(alpha = 0.85f),
                                    fontSize = 11.sp
                                )
                            }
                            Text(
                                text = "$currencySymbol ${formatAmount(totalExpense)}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = ElectricCyanOn,
                                fontSize = 14.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Balance Label & Live Status Chip
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Total Balance",
                        style = MaterialTheme.typography.labelMedium,
                        color = ForestTextSecondaryDark,
                        letterSpacing = 1.1.sp
                    )

                    Surface(
                        color = Color(0x22D4F63D),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "✦",
                                color = ElectricLime,
                                fontSize = 10.sp
                            )
                            Text(
                                text = "Live Ledger",
                                color = ElectricLime,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // Balance amount with currency symbol
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = currencySymbol,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = ElectricLime
                    )
                    Text(
                        text = formatAmount(balance),
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = ForestTextPrimaryDark,
                        letterSpacing = (-0.8).sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Humanized contextual status text with Star
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "✦",
                        color = ElectricLime,
                        fontSize = 11.sp
                    )
                    Text(
                        text = if (balance >= 0) "Healthy financial balance maintained"
                               else "Expenses exceeding income — review budget",
                        style = MaterialTheme.typography.bodySmall,
                        color = ForestTextSecondaryDark
                    )
                }
            }
        }
    }
}

private fun formatAmount(amount: Double): String {
    val formatter = NumberFormat.getNumberInstance(Locale.US)
    formatter.minimumFractionDigits = 2
    formatter.maximumFractionDigits = 2
    return formatter.format(amount)
}

private fun formatCompactAmount(amount: Double): String {
    val formatter = NumberFormat.getNumberInstance(Locale.US)
    formatter.minimumFractionDigits = 0
    formatter.maximumFractionDigits = 1
    return formatter.format(amount)
}

@Preview(name = "Balance Card - Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun BalanceCardPreview() {
    ExpenseTrackerTheme {
        BalanceCard(
            balance = 998216.58,
            currencySymbol = "$",
            totalIncome = 825912.40,
            totalExpense = 172304.18
        )
    }
}
