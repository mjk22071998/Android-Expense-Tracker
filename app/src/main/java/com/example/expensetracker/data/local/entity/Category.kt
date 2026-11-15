package com.example.expensetracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.expensetracker.data.local.SyncStatus
import java.util.UUID

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey
    val id: String,
    val name: String,
    val icon: String,
    val transactionType: String,
    val isDefault: Boolean = true,
    val syncStatus: Int = SyncStatus.SYNCED,
    val updatedAt: Long = 0L,
    val isDeleted: Boolean = false,
    val serverId: String? = null
) {
    companion object {
        /**
         * Factory method to create a custom user category.
         */
        fun create(
            name: String,
            icon: String,
            transactionType: String,
            id: String = UUID.randomUUID().toString(),
            isDefault: Boolean = false,
            syncStatus: Int = SyncStatus.PENDING_UPLOAD,
            timestamp: Long = System.currentTimeMillis()
        ): Category = Category(
            id = id,
            name = name,
            icon = icon,
            transactionType = transactionType,
            isDefault = isDefault,
            syncStatus = syncStatus,
            updatedAt = timestamp,
            isDeleted = false,
            serverId = null
        )
    }
}
