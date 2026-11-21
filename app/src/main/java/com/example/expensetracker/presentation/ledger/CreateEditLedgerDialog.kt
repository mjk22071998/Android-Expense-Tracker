package com.example.expensetracker.presentation.ledger

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expensetracker.R
import com.example.expensetracker.data.local.entity.Ledger
import com.example.expensetracker.domain.model.CurrencyHelper
import com.example.expensetracker.presentation.settings.CurrencyPickerDialog
import com.example.expensetracker.ui.theme.ElectricLime
import com.example.expensetracker.ui.theme.ElectricLimeOn
import com.example.expensetracker.ui.theme.ExpenseTrackerTheme

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreateEditLedgerDialog(
    ledgerToEdit: Ledger? = null,
    defaultCurrencyCode: String = "PKR",
    onDismiss: () -> Unit,
    onConfirm: (name: String, currencyCode: String) -> Unit
) {
    val extendedColors = ExpenseTrackerTheme.extendedColors
    var name by remember { mutableStateOf(ledgerToEdit?.name.orEmpty()) }
    var selectedCurrency by remember {
        mutableStateOf(ledgerToEdit?.currencyCode ?: defaultCurrencyCode)
    }
    var showFullCurrencyPicker by remember { mutableStateOf(false) }
    var currencySearchQuery by remember { mutableStateOf("") }

    val commonCurrencies = remember {
        listOf("PKR", "USD", "EUR", "GBP", "AED", "SAR", "INR", "CAD")
    }

    val isEditing = ledgerToEdit != null
    val isNameValid = name.trim().isNotEmpty()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = extendedColors.cardBackground,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                text = if (isEditing) "Edit Ledger" else "New Ledger",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = extendedColors.textPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = if (isEditing) "Update the ledger details below."
                    else "Create a dedicated ledger to keep personal, business, or project finances separate.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = extendedColors.textSecondary
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { if (it.length <= 30) name = it },
                    label = { Text("Ledger Name") },
                    placeholder = { Text("e.g., Business, Savings, Trip") },
                    singleLine = true,
                    isError = name.isNotEmpty() && name.isBlank(),
                    supportingText = {
                        Text(
                            text = "${name.length}/30",
                            style = MaterialTheme.typography.labelSmall,
                            color = extendedColors.textMuted
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = extendedColors.emeraldIncome,
                        unfocusedBorderColor = extendedColors.cardBorder,
                        focusedLabelColor = extendedColors.emeraldIncome,
                        unfocusedLabelColor = extendedColors.textMuted
                    )
                )

                // Currency Section
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Currency",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = extendedColors.textSecondary
                    )

                    // Popular currency chips
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        commonCurrencies.forEach { code ->
                            val isSelected = selectedCurrency.equals(code, ignoreCase = true)
                            val symbol = CurrencyHelper.getSymbol(code)

                            Surface(
                                onClick = { selectedCurrency = code },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) {
                                    if (extendedColors.isDark) Color(0x33489B5F) else Color(0x1A489B5F)
                                } else {
                                    if (extendedColors.isDark) Color(0x1AFFFFFF) else Color(0x0D000000)
                                },
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) extendedColors.emeraldIncome else extendedColors.cardBorder
                                )
                            ) {
                                Text(
                                    text = "$code ($symbol)",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) extendedColors.emeraldIncome else extendedColors.textPrimary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }

                        // More Currencies chip
                        val isCustomSelected = !commonCurrencies.any { it.equals(selectedCurrency, ignoreCase = true) }
                        Surface(
                            onClick = { showFullCurrencyPicker = true },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isCustomSelected) {
                                if (extendedColors.isDark) Color(0x33489B5F) else Color(0x1A489B5F)
                            } else {
                                if (extendedColors.isDark) Color(0x14FFFFFF) else Color(0x08000000)
                            },
                            border = BorderStroke(
                                1.dp,
                                if (isCustomSelected) extendedColors.emeraldIncome else extendedColors.cardBorder
                            )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = if (isCustomSelected) "$selectedCurrency (${CurrencyHelper.getSymbol(selectedCurrency)})" else "Other...",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (isCustomSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isCustomSelected) extendedColors.emeraldIncome else extendedColors.textMuted
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isNameValid) {
                        onConfirm(name.trim(), selectedCurrency.uppercase().trim())
                    }
                },
                enabled = isNameValid,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricLime,
                    contentColor = ElectricLimeOn,
                    disabledContainerColor = if (extendedColors.isDark) Color(0x28FFFFFF) else Color(0x1A000000),
                    disabledContentColor = extendedColors.textMuted
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (isEditing) "Save Changes" else "Create",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Cancel",
                    color = extendedColors.textSecondary
                )
            }
        }
    )

    if (showFullCurrencyPicker) {
        val filtered = remember(currencySearchQuery) {
            CurrencyHelper.search(currencySearchQuery)
        }
        CurrencyPickerDialog(
            searchQuery = currencySearchQuery,
            currencies = filtered,
            currentCurrencyCode = selectedCurrency,
            onSearchQueryChanged = { currencySearchQuery = it },
            onCurrencySelected = { code ->
                selectedCurrency = code
                showFullCurrencyPicker = false
                currencySearchQuery = ""
            },
            onDismiss = {
                showFullCurrencyPicker = false
                currencySearchQuery = ""
            }
        )
    }
}

@Preview(name = "Create Ledger Dialog", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CreateEditLedgerDialogPreview() {
    ExpenseTrackerTheme {
        CreateEditLedgerDialog(
            onDismiss = {},
            onConfirm = { _, _ -> }
        )
    }
}
