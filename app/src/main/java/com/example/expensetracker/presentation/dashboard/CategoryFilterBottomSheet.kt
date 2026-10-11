package com.example.expensetracker.presentation.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.expensetracker.Constants
import com.example.expensetracker.data.local.entity.Category
import com.example.expensetracker.domain.model.getCategoryIcon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryFilterBottomSheet(
    allCategories: List<Category>,
    selectedCategoryIds: Set<String>,
    onCategoryToggled: (String) -> Unit,
    onClear: () -> Unit,
    onApply: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    val expenseCategories =
        allCategories.filter { it.transactionType == Constants.TransactionType.EXPENSE }
    val incomeCategories =
        allCategories.filter { it.transactionType == Constants.TransactionType.INCOME }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filter by Category",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onClear) {
                    Text("Clear")
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 350.dp)
            ) {
                if (expenseCategories.isNotEmpty()) {
                    item {
                        Text(
                            text = "Expense",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    items(expenseCategories, key = { it.id }) { category ->
                        CategoryCheckboxRow(
                            category = category,
                            isChecked = category.id in selectedCategoryIds,
                            onToggle = { onCategoryToggled(category.id) }
                        )
                    }
                }

                if (incomeCategories.isNotEmpty()) {
                    item {
                        Text(
                            text = "Income",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    items(incomeCategories, key = { it.id }) { category ->
                        CategoryCheckboxRow(
                            category = category,
                            isChecked = category.id in selectedCategoryIds,
                            onToggle = { onCategoryToggled(category.id) }
                        )
                    }
                }
            }

            Button(
                onClick = onApply,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                Text("Apply")
            }
        }
    }
}

@Composable
private fun CategoryCheckboxRow(
    category: Category,
    isChecked: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Checkbox(checked = isChecked, onCheckedChange = { onToggle() })
        Icon(
            painter = painterResource(id = getCategoryIcon(category.icon)),
            contentDescription = null,
            modifier = Modifier.padding(end = 4.dp)
        )
        Text(text = category.name, style = MaterialTheme.typography.bodyMedium)
    }
}