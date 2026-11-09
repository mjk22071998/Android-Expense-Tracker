package com.example.expensetracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "ledgers")
data class Ledger(
    @PrimaryKey
    val id: String,
    val name: String,
    val currencyCode: String,
    val balance: Double = 0.0,
    val totalIncome: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val createdAt: Long,
    val updatedAt: Long,
    val isDefault: Boolean = false
) {

    companion object {
        /**
         * Factory method to instantiate a new Ledger with consistent default balances and timestamps.
         */
        fun create(
            name: String,
            currencyCode: String,
            id: String = UUID.randomUUID().toString(),
            isDefault: Boolean = false,
            timestamp: Long = System.currentTimeMillis()
        ): Ledger = Ledger(
            id = id,
            name = name,
            currencyCode = currencyCode,
            balance = 0.0,
            totalIncome = 0.0,
            totalExpenses = 0.0,
            createdAt = timestamp,
            updatedAt = timestamp,
            isDefault = isDefault
        )
    }
}
