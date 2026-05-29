package com.example.expensetracker.presentation.settings

import com.example.expensetracker.domain.model.CurrencyModel

data class SettingsUiState(
    val currencyCode: String = "PKR",
    val currencySymbol: String = "₨",
    val isDarkTheme: Boolean = false,
    val currencySearchQuery: String = "",
    val filteredCurrencies: List<CurrencyModel> = emptyList(),
    val isCurrencyPickerVisible: Boolean = false
)