package com.example.expensetracker.presentation.dashboard

import android.content.res.Configuration
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expensetracker.R
import com.example.expensetracker.domain.model.CurrencyHelper.formatAmount
import com.example.expensetracker.ui.theme.ExpenseTrackerTheme

@Composable
fun SummaryCard(
    title: String,
    amount: Double,
    currencySymbol: String,
    isIncome: Boolean,
    modifier: Modifier = Modifier
) {
    val extendedColors = ExpenseTrackerTheme.extendedColors
    val accentColor = if (isIncome) extendedColors.emeraldIncome else extendedColors.crimsonExpense
    val pillBg = if (isIncome) Color(0x2410B981) else Color(0x24F43F5E)

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = extendedColors.cardBackground
        ),
        border = BorderStroke(1.dp, extendedColors.cardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: Circular pill icon & title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(pillBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(
                            id = if (isIncome) R.drawable.ic_trending_up
                            else R.drawable.ic_trending_down
                        ),
                        contentDescription = title,
                        tint = accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = extendedColors.textSecondary,
                    fontWeight = FontWeight.Medium
                )
            }

            // Amount Display
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "$currencySymbol ${formatAmount(amount)}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = extendedColors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = if (isIncome) "Total Inflow" else "Total Outflow",
                    style = MaterialTheme.typography.labelSmall,
                    color = extendedColors.textMuted
                )
            }
        }
    }
}

@Preview(name = "Summary Cards - Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Summary Cards - Light", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
private fun SummaryCardsPreview() {
    ExpenseTrackerTheme {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SummaryCard(
                title = "Income",
                amount = 85200.0,
                currencySymbol = "₨",
                isIncome = true,
                modifier = Modifier.weight(1f)
            )
            SummaryCard(
                title = "Expense",
                amount = 34150.0,
                currencySymbol = "₨",
                isIncome = false,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
