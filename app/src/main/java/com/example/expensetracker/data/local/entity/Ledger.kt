package com.example.expensetracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ledgers")
data class Ledger(
    @PrimaryKey
    val id: String,
    val name: String,
    val currencyCode: String,
    val createdAt: Long,
    val isDefault: Boolean = false
)