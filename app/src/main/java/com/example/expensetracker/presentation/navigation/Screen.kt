package com.example.expensetracker.presentation.navigation

import com.example.expensetracker.Constants

sealed class Screen(val route: String) {
    data object Dashboard : Screen("dashboard")
    data object AddTransaction : Screen("add_transaction")
    data object EditTransaction : Screen("edit_transaction/{${Constants.NavArgs.TRANSACTION_ID}}") {
        fun createRoute(transactionId: String) = "edit_transaction/$transactionId"
    }
    data object Settings : Screen("settings")
}
