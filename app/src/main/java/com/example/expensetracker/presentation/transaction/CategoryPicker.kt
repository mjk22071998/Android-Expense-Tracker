package com.example.expensetracker.presentation.transaction

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.expensetracker.R
import com.example.expensetracker.data.local.entity.Category
import com.example.expensetracker.ui.theme.ElectricLime
import com.example.expensetracker.ui.theme.ExpenseTrackerTheme

@Composable
fun CategoryPicker(
    categories: List<Category>,
    selectedCategoryId: String?,
    onCategorySelected: (String) -> Unit,
    onAddCategoryClick: () -> Unit,
    onEditCategory: (Category) -> Unit,
    onDeleteCategory: (Category) -> Unit,
    modifier: Modifier = Modifier
) {
    val extendedColors = ExpenseTrackerTheme.extendedColors

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        items(categories, key = { it.id }) { category ->
            CategoryChip(
                category = category,
                isSelected = selectedCategoryId == category.id,
                onClick = { onCategorySelected(category.id) },
                onEdit = { onEditCategory(category) },
                onDelete = { onDeleteCategory(category) }
            )
        }

        item {
            AssistChip(
                onClick = onAddCategoryClick,
                shape = CircleShape,
                border = BorderStroke(1.dp, ElectricLime.copy(alpha = 0.5f)),
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = extendedColors.cardBackground,
                    labelColor = ElectricLime,
                    leadingIconContentColor = ElectricLime
                ),
                label = {
                    Text(
                        text = "New Category",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_add),
                        contentDescription = "Add custom category",
                        modifier = Modifier.size(16.dp)
                    )
                }
            )
        }
    }
}

@Preview(name = "Category Picker - Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CategoryPickerPreview() {
    ExpenseTrackerTheme {
        CategoryPicker(
            categories = listOf(
                Category("1", "Food", "restaurant", "EXPENSE", true),
                Category("2", "Transport", "transport", "EXPENSE", true)
            ),
            selectedCategoryId = "1",
            onCategorySelected = {},
            onAddCategoryClick = {},
            onEditCategory = {},
            onDeleteCategory = {}
        )
    }
}
