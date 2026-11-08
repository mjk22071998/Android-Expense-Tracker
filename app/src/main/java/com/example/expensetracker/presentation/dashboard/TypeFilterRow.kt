package com.example.expensetracker.presentation.dashboard

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
fun TypeFilterRow(
    selectedType: String?,
    onTypeSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val extendedColors = ExpenseTrackerTheme.extendedColors
    val isIncomeSelected = selectedType == Constants.TransactionType.INCOME
    val isExpenseSelected = selectedType == Constants.TransactionType.EXPENSE

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Income Filter Pill
        FilterChip(
            selected = isIncomeSelected,
            onClick = { onTypeSelected(Constants.TransactionType.INCOME) },
            shape = CircleShape,
            label = {
                Text(
                    text = "Incomes Only",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (isIncomeSelected) FontWeight.Bold else FontWeight.Medium
                )
            },
            leadingIcon = {
                Icon(
                    painter = painterResource(
                        id = if (isIncomeSelected) R.drawable.ic_check else R.drawable.ic_trending_up
                    ),
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
            },
            border = BorderStroke(
                1.dp,
                if (isIncomeSelected) extendedColors.emeraldIncome else extendedColors.cardBorder
            ),
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = extendedColors.emeraldIncome,
                selectedLabelColor = ElectricLimeOn,
                selectedLeadingIconColor = ElectricLimeOn,
                containerColor = extendedColors.cardBackground,
                labelColor = extendedColors.textSecondary,
                iconColor = extendedColors.emeraldIncome
            )
        )

        // Expense Filter Pill
        FilterChip(
            selected = isExpenseSelected,
            onClick = { onTypeSelected(Constants.TransactionType.EXPENSE) },
            shape = CircleShape,
            label = {
                Text(
                    text = "Expenses Only",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (isExpenseSelected) FontWeight.Bold else FontWeight.Medium
                )
            },
            leadingIcon = {
                Icon(
                    painter = painterResource(
                        id = if (isExpenseSelected) R.drawable.ic_check else R.drawable.ic_trending_down
                    ),
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
            },
            border = BorderStroke(
                1.dp,
                if (isExpenseSelected) extendedColors.crimsonExpense else extendedColors.cardBorder
            ),
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = extendedColors.crimsonExpense,
                selectedLabelColor = ElectricLimeOn,
                selectedLeadingIconColor = ElectricLimeOn,
                containerColor = extendedColors.cardBackground,
                labelColor = extendedColors.textSecondary,
                iconColor = extendedColors.crimsonExpense
            )
        )
    }
}

@Preview(name = "Type Filters - Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TypeFilterRowPreview() {
    ExpenseTrackerTheme {
        TypeFilterRow(
            selectedType = Constants.TransactionType.INCOME,
            onTypeSelected = {}
        )
    }
}
