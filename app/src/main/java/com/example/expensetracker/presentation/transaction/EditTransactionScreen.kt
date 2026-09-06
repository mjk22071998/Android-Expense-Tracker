package com.example.expensetracker.presentation.transaction

import androidx.compose.runtime.Composable

@Composable
fun EditTransactionScreen(
    onNavigateBack: () -> Unit
) {
    TransactionFormScreen(onNavigateBack = onNavigateBack)
}