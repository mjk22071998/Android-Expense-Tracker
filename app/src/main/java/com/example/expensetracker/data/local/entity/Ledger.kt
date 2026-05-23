package com.example.expensetracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

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
)