package com.example.expensetracker.presentation.transaction

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.expensetracker.Constants
import com.example.expensetracker.data.local.entity.Category
import com.example.expensetracker.domain.model.categoryIconOptions
import com.example.expensetracker.domain.model.getCategoryIcon
import com.example.expensetracker.ui.theme.ElectricLime
import com.example.expensetracker.ui.theme.ElectricLimeOn
import com.example.expensetracker.ui.theme.ExpenseTrackerTheme

@Composable
fun CategoryDialog(
    transactionType: String,
    existingCategory: Category? = null,
    onDismiss: () -> Unit,
    onSave: (name: String, icon: String) -> Unit
) {
    val extendedColors = ExpenseTrackerTheme.extendedColors
    val isEditMode = existingCategory != null
    var name by remember { mutableStateOf(existingCategory?.name ?: "") }
    var selectedIcon by remember { mutableStateOf(existingCategory?.icon) }

    val typeLabel = if (transactionType == Constants.TransactionType.INCOME) "Income" else "Expense"

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(26.dp),
        containerColor = extendedColors.cardBackground,
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = if (isEditMode) "Edit Category" else "New Category",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = extendedColors.textPrimary
                )
                Text(
                    text = "Adding to $typeLabel categories",
                    style = MaterialTheme.typography.labelSmall,
                    color = extendedColors.textMuted
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Category name") },
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricLime,
                        unfocusedBorderColor = extendedColors.cardBorder,
                        focusedLabelColor = ElectricLime,
                        unfocusedLabelColor = extendedColors.textSecondary,
                        focusedTextColor = extendedColors.textPrimary,
                        unfocusedTextColor = extendedColors.textPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Select Icon",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = extendedColors.textSecondary
                )

                IconPickerGrid(
                    selectedIcon = selectedIcon,
                    onIconSelected = { selectedIcon = it }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(name.trim(), selectedIcon.orEmpty()) },
                enabled = name.isNotBlank() && selectedIcon != null,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricLime,
                    contentColor = ElectricLimeOn,
                    disabledContainerColor = extendedColors.cardBorder,
                    disabledContentColor = extendedColors.textMuted
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (isEditMode) "Save Changes" else "Create Category",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, extendedColors.cardBorder)
            ) {
                Text("Cancel", color = extendedColors.textSecondary)
            }
        }
    )
}

@Composable
private fun IconPickerGrid(
    selectedIcon: String?,
    onIconSelected: (String) -> Unit
) {
    val extendedColors = ExpenseTrackerTheme.extendedColors

    LazyVerticalGrid(
        columns = GridCells.Fixed(5),
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(categoryIconOptions) { iconName ->
            val isSelected = iconName == selectedIcon
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) Color(0x33D4F63D) else extendedColors.cardBorder)
                    .then(
                        if (isSelected) Modifier.border(2.dp, ElectricLime, CircleShape)
                        else Modifier
                    )
                    .clickable { onIconSelected(iconName) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = getCategoryIcon(iconName)),
                    contentDescription = iconName,
                    tint = if (isSelected) ElectricLime else extendedColors.textSecondary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Preview(name = "Category Dialog - Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CategoryDialogPreview() {
    ExpenseTrackerTheme {
        CategoryDialog(
            transactionType = "EXPENSE",
            onDismiss = {},
            onSave = { _, _ -> }
        )
    }
}
