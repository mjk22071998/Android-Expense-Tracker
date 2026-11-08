package com.example.expensetracker.presentation.transaction

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.expensetracker.Constants
import com.example.expensetracker.R
import com.example.expensetracker.ui.theme.ElectricLime
import com.example.expensetracker.ui.theme.ElectricLimeOn
import com.example.expensetracker.ui.theme.ExpenseTrackerTheme

@Composable
fun TypeToggle(
    selectedType: String,
    onTypeSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val extendedColors = ExpenseTrackerTheme.extendedColors
    val isExpense = selectedType == Constants.TransactionType.EXPENSE
    val isIncome = selectedType == Constants.TransactionType.INCOME

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp),
        shape = CircleShape,
        color = extendedColors.cardBackground,
        border = BorderStroke(1.dp, extendedColors.cardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Expense Tab Pill (Calm Vivid Blue instead of danger-red)
            val expenseBg by animateColorAsState(
                targetValue = if (isExpense) Color(0xFF0099FF) else Color.Transparent,
                animationSpec = tween(200),
                label = "expenseBg"
            )
            val expenseText by animateColorAsState(
                targetValue = if (isExpense) Color.White else extendedColors.textSecondary,
                animationSpec = tween(200),
                label = "expenseText"
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(CircleShape)
                    .background(expenseBg)
                    .clickable { onTypeSelected(Constants.TransactionType.EXPENSE) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_trending_down),
                        contentDescription = null,
                        tint = expenseText,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Expense",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (isExpense) FontWeight.Bold else FontWeight.Medium,
                        color = expenseText
                    )
                }
            }

            // Income Tab Pill (Vibrant Electric Lime)
            val incomeBg by animateColorAsState(
                targetValue = if (isIncome) ElectricLime else Color.Transparent,
                animationSpec = tween(200),
                label = "incomeBg"
            )
            val incomeText by animateColorAsState(
                targetValue = if (isIncome) ElectricLimeOn else extendedColors.textSecondary,
                animationSpec = tween(200),
                label = "incomeText"
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(CircleShape)
                    .background(incomeBg)
                    .clickable { onTypeSelected(Constants.TransactionType.INCOME) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_trending_up),
                        contentDescription = null,
                        tint = incomeText,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Income",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (isIncome) FontWeight.Bold else FontWeight.Medium,
                        color = incomeText
                    )
                }
            }
        }
    }
}

@Preview(name = "Type Toggle - Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TypeTogglePreview() {
    ExpenseTrackerTheme {
        TypeToggle(
            selectedType = Constants.TransactionType.EXPENSE,
            onTypeSelected = {}
        )
    }
}
