package com.example.expensetracker.presentation.transaction

import androidx.compose.runtime.Composable

@Composable
fun AddTransactionScreen(
    onNavigateBack: () -> Unit
) {
    TransactionFormScreen(onNavigateBack = onNavigateBack)
}