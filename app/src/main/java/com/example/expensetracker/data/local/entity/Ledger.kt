package com.example.expensetracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.expensetracker.data.local.SyncStatus
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
    val isDefault: Boolean = false,
    val isDeleted: Boolean = false,
    val syncStatus: Int = SyncStatus.SYNCED,
    val serverId: String? = null
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
            isDeleted: Boolean = false,
            syncStatus: Int = SyncStatus.PENDING_UPLOAD,
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
            isDefault = isDefault,
            isDeleted = isDeleted,
            syncStatus = syncStatus,
            serverId = null
        )
    }
}
