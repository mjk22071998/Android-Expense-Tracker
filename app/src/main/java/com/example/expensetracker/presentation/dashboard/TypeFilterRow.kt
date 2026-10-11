package com.example.expensetracker.presentation.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.expensetracker.Constants
import com.example.expensetracker.R

@Composable
fun TypeFilterRow(
    selectedType: String?,
    onTypeSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selectedType == Constants.TransactionType.INCOME,
            onClick = { onTypeSelected(Constants.TransactionType.INCOME) },
            label = { Text("Income Only", style = MaterialTheme.typography.labelMedium) },
            leadingIcon = if (selectedType == Constants.TransactionType.INCOME) {
                {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_check),
                        contentDescription = null,
                        modifier = Modifier.size(FilterChipDefaults.IconSize)
                    )
                }
            } else null
        )
        FilterChip(
            selected = selectedType == Constants.TransactionType.EXPENSE,
            onClick = { onTypeSelected(Constants.TransactionType.EXPENSE) },
            label = { Text("Expense Only", style = MaterialTheme.typography.labelMedium) },
            leadingIcon = if (selectedType == Constants.TransactionType.EXPENSE) {
                {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_check),
                        contentDescription = null,
                        modifier = Modifier.size(FilterChipDefaults.IconSize)
                    )
                }
            } else null
        )
    }
}