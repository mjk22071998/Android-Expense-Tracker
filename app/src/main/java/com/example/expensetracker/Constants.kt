package com.example.expensetracker

object Constants {

    // Navigation argument keys
    object NavArgs {
        const val TRANSACTION_ID = "transactionId"
        const val LEDGER_ID = "ledgerId"
    }

    // Database
    object Database {
        const val DATABASE_NAME = "expense_tracker.db"
    }

    // Transaction types
    object TransactionType {
        const val INCOME = "INCOME"
        const val EXPENSE = "EXPENSE"
    }

    // DataStore preference keys
    object Preferences {
        const val CURRENCY_CODE = "currency_code"
        const val IS_DARK_THEME = "is_dark_theme"
    }
}