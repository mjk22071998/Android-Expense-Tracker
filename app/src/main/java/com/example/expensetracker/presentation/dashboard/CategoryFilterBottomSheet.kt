package com.example.expensetracker.presentation.dashboard

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expensetracker.Constants
import com.example.expensetracker.data.local.entity.Category
import com.example.expensetracker.domain.model.getCategoryIcon
import com.example.expensetracker.ui.theme.ElectricLime
import com.example.expensetracker.ui.theme.ElectricLimeOn
import com.example.expensetracker.ui.theme.ExpenseTrackerTheme

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
    val extendedColors = ExpenseTrackerTheme.extendedColors
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val expenseCategories =
        allCategories.filter { it.transactionType == Constants.TransactionType.EXPENSE }
    val incomeCategories =
        allCategories.filter { it.transactionType == Constants.TransactionType.INCOME }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = extendedColors.cardBackground,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .size(width = 40.dp, height = 4.dp)
                    .clip(CircleShape)
                    .background(extendedColors.cardBorder)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: Title and Clear Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Filter by Category",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = extendedColors.textPrimary
                    )
                    Text(
                        text = "Select categories to narrow down your history",
                        style = MaterialTheme.typography.labelSmall,
                        color = extendedColors.textMuted
                    )
                }

                if (selectedCategoryIds.isNotEmpty()) {
                    TextButton(onClick = onClear) {
                        Text(
                            text = "Reset (${selectedCategoryIds.size})",
                            color = extendedColors.crimsonExpense,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Scrollable List of Categories
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 380.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (expenseCategories.isNotEmpty()) {
                    item {
                        Text(
                            text = "Expense Categories",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = extendedColors.textSecondary,
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
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
                            text = "Income Categories",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = extendedColors.textSecondary,
                            modifier = Modifier.padding(top = 14.dp, bottom = 4.dp)
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

            Spacer(modifier = Modifier.height(6.dp))

            // Primary Apply Pill Button
            Button(
                onClick = onApply,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricLime,
                    contentColor = ElectricLimeOn
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = if (selectedCategoryIds.isEmpty()) "Show All Transactions"
                           else "Apply Filter (${selectedCategoryIds.size} Selected)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun CategoryCheckboxRow(
    category: Category,
    isChecked: Boolean,
    onToggle: () -> Unit
) {
    val extendedColors = ExpenseTrackerTheme.extendedColors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onToggle)
            .background(if (isChecked) Color(0x1AD4F63D) else Color.Transparent)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Icon avatar badge
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(if (isChecked) Color(0x2FD4F63D) else extendedColors.cardBorder),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = getCategoryIcon(category.icon)),
                contentDescription = null,
                tint = if (isChecked) ElectricLime else extendedColors.textSecondary,
                modifier = Modifier.size(18.dp)
            )
        }

        Text(
            text = category.name,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isChecked) FontWeight.Bold else FontWeight.Normal,
            color = if (isChecked) extendedColors.textPrimary else extendedColors.textSecondary,
            modifier = Modifier.weight(1f)
        )

        Checkbox(
            checked = isChecked,
            onCheckedChange = { onToggle() },
            colors = CheckboxDefaults.colors(
                checkedColor = ElectricLime,
                checkmarkColor = ElectricLimeOn,
                uncheckedColor = extendedColors.textMuted
            )
        )
    }
}

@Preview(name = "Category Sheet - Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CategoryFilterBottomSheetPreview() {
    ExpenseTrackerTheme {
        CategoryFilterBottomSheet(
            allCategories = listOf(
                Category("cat_food", "Food & Dining", "restaurant", "EXPENSE", true),
                Category("cat_transport", "Transportation", "transport", "EXPENSE", true),
                Category("cat_salary", "Salary", "salary", "INCOME", true)
            ),
            selectedCategoryIds = setOf("cat_food"),
            onCategoryToggled = {},
            onClear = {},
            onApply = {},
            onDismiss = {}
        )
    }
}
