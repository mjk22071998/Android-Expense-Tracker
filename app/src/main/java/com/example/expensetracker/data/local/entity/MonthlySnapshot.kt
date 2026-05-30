package com.example.expensetracker.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "monthly_snapshots",
    foreignKeys = [
        ForeignKey(
            entity = Ledger::class,
            parentColumns = ["id"],
            childColumns = ["ledgerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("ledgerId"),
        Index(value = ["ledgerId", "month", "year"], unique = true)
    ]
)
data class MonthlySnapshot(
    @PrimaryKey
    val id: String,
    val ledgerId: String,
    val month: Int,
    val year: Int,
    val openingBalance: Double = 0.0,
    val closingBalance: Double = 0.0,
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val createdAt: Long,
    val updatedAt: Long
)