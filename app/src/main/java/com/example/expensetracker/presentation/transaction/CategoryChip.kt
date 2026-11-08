package com.example.expensetracker.presentation.transaction

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.expensetracker.R
import com.example.expensetracker.data.local.entity.Category
import com.example.expensetracker.domain.model.getCategoryIcon
import com.example.expensetracker.ui.theme.ElectricLime
import com.example.expensetracker.ui.theme.ElectricLimeOn
import com.example.expensetracker.ui.theme.ExpenseTrackerTheme

@Composable
fun CategoryChip(
    category: Category,
    isSelected: Boolean,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val extendedColors = ExpenseTrackerTheme.extendedColors
    var showMenu by remember { mutableStateOf(false) }

    FilterChip(
        selected = isSelected,
        onClick = onClick,
        shape = CircleShape,
        label = {
            Text(
                text = category.name,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        },
        leadingIcon = {
            Icon(
                painter = painterResource(id = getCategoryIcon(category.icon)),
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
        },
        trailingIcon = if (!category.isDefault) {
            {
                Box {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_more_vert),
                        contentDescription = "Options for ${category.name}",
                        modifier = Modifier
                            .size(16.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { showMenu = true }
                    )
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_edit),
                                    contentDescription = "Edit"
                                )
                            },
                            onClick = {
                                showMenu = false
                                onEdit()
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "Delete",
                                    color = MaterialTheme.colorScheme.error
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_delete),
                                    contentDescription = "Delete",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            },
                            onClick = {
                                showMenu = false
                                onDelete()
                            }
                        )
                    }
                }
            }
        } else null,
        border = BorderStroke(
            1.dp,
            if (isSelected) ElectricLime else extendedColors.cardBorder
        ),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = ElectricLime,
            selectedLabelColor = ElectricLimeOn,
            selectedLeadingIconColor = ElectricLimeOn,
            selectedTrailingIconColor = ElectricLimeOn,
            containerColor = extendedColors.cardBackground,
            labelColor = extendedColors.textSecondary,
            iconColor = extendedColors.textSecondary
        )
    )
}

@Preview(name = "Category Chip - Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CategoryChipPreview() {
    ExpenseTrackerTheme {
        CategoryChip(
            category = Category("cat_food", "Groceries", "restaurant", "EXPENSE", true),
            isSelected = true,
            onClick = {},
            onEdit = {},
            onDelete = {}
        )
    }
}
