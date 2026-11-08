package com.example.expensetracker.presentation.settings

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.expensetracker.R
import com.example.expensetracker.domain.model.CurrencyModel
import com.example.expensetracker.ui.theme.ElectricLime
import com.example.expensetracker.ui.theme.ExpenseTrackerTheme

@Composable
fun CurrencyPickerDialog(
    searchQuery: String,
    currencies: List<CurrencyModel>,
    currentCurrencyCode: String,
    onSearchQueryChanged: (String) -> Unit,
    onCurrencySelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val extendedColors = ExpenseTrackerTheme.extendedColors

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            shape = RoundedCornerShape(26.dp),
            color = extendedColors.cardBackground,
            border = BorderStroke(1.dp, extendedColors.cardBorder)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Select Currency",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = extendedColors.textPrimary
                        )
                        Text(
                            text = "Choose your preferred primary currency",
                            style = MaterialTheme.typography.labelSmall,
                            color = extendedColors.textMuted
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.clip(CircleShape)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_close),
                            contentDescription = "Close",
                            tint = extendedColors.textSecondary
                        )
                    }
                }

                // Search Box
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChanged,
                    placeholder = {
                        Text(
                            "Search by name or code (e.g. USD, EUR, PKR)...",
                            color = extendedColors.textMuted
                        )
                    },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_search),
                            contentDescription = null,
                            tint = extendedColors.textSecondary
                        )
                    },
                    shape = RoundedCornerShape(18.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = extendedColors.cardElevated,
                        unfocusedContainerColor = extendedColors.cardElevated,
                        focusedBorderColor = ElectricLime,
                        unfocusedBorderColor = extendedColors.cardBorder,
                        focusedTextColor = extendedColors.textPrimary,
                        unfocusedTextColor = extendedColors.textPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = extendedColors.cardBorder, thickness = 1.dp)

                // Currencies list
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(currencies, key = { it.code }) { currency ->
                        CurrencyRow(
                            currency = currency,
                            isSelected = currency.code == currentCurrencyCode,
                            onClick = { onCurrencySelected(currency.code) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CurrencyRow(
    currency: CurrencyModel,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val extendedColors = ExpenseTrackerTheme.extendedColors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .background(if (isSelected) Color(0x1AD4F63D) else Color.Transparent)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Code badge
            Surface(
                color = if (isSelected) ElectricLime else extendedColors.cardBorder,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = currency.code,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) Color(0xFF08140E) else extendedColors.textPrimary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Column {
                Text(
                    text = currency.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = extendedColors.textPrimary
                )
                Text(
                    text = "Symbol: ${currency.symbol}",
                    style = MaterialTheme.typography.labelSmall,
                    color = extendedColors.textMuted
                )
            }
        }

        if (isSelected) {
            Icon(
                painter = painterResource(id = R.drawable.ic_check),
                contentDescription = "Selected",
                tint = ElectricLime,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Preview(name = "Currency Picker - Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CurrencyPickerDialogPreview() {
    ExpenseTrackerTheme {
        CurrencyPickerDialog(
            searchQuery = "",
            currencies = listOf(
                CurrencyModel("USD", "US Dollar", "$"),
                CurrencyModel("EUR", "Euro", "€"),
                CurrencyModel("PKR", "Pakistani Rupee", "₨")
            ),
            currentCurrencyCode = "PKR",
            onSearchQueryChanged = {},
            onCurrencySelected = {},
            onDismiss = {}
        )
    }
}
