package com.example.expensetracker.presentation.navigation

import com.example.expensetracker.Constants

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object AddTransaction : Screen("add_transaction")
    object EditTransaction : Screen("edit_transaction/{${Constants.NavArgs.TRANSACTION_ID}}") {
        fun createRoute(transactionId: String) = "edit_transaction/$transactionId"
    }
    object Settings : Screen("settings")
}