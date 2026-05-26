package com.example.expensetracker.presentation.dashboard

import com.example.expensetracker.data.local.entity.Transaction
import com.example.expensetracker.domain.model.DateRangeFilter

data class DashboardUiState(
    val balance: Double = 0.0,
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val transactions: List<Transaction> = emptyList(),
    val selectedFilter: DateRangeFilter = DateRangeFilter.Last30Days,
    val isLoading: Boolean = false,
    val error: String? = null
)
