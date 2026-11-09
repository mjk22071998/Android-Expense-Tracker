package com.example.expensetracker.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "transactions",
    foreignKeys = [ForeignKey(
        entity = Ledger::class,
        parentColumns = ["id"],
        childColumns = ["ledgerId"],
        onDelete = ForeignKey.CASCADE
    ), ForeignKey(
        entity = Category::class,
        parentColumns = ["id"],
        childColumns = ["categoryId"],
        onDelete = ForeignKey.SET_NULL
    )],
    indices = [
        Index("ledgerId"),
        Index("categoryId"),
        Index("type"),
        Index("date"),
        Index("isDeleted"),
        Index(value = ["ledgerId", "type", "date", "isDeleted"])
    ]
)
data class Transaction(
    @PrimaryKey val id: String,
    val ledgerId: String,
    val categoryId: String? = null,
    val amount: Double,
    val type: String,
    val note: String,
    val date: Long,
    val createdAt: Long,
    val updatedAt: Long,
    val isDeleted: Boolean = false
) {

    /**
     * Factory helper to produce a soft-deleted clone with an updated timestamp.
     */
    fun toSoftDeleted(deletedAt: Long = System.currentTimeMillis()): Transaction = copy(
        updatedAt = deletedAt,
        isDeleted = true
    )

    companion object {
        /**
         * Factory method for creating a valid new Transaction domain entity.
         * Ensures consistent ID generation and timestamp synchronization.
         */
        fun create(
            amount: Double,
            type: String,
            categoryId: String?,
            ledgerId: String,
            note: String = "",
            date: Long = System.currentTimeMillis(),
            id: String = UUID.randomUUID().toString(),
            timestamp: Long = System.currentTimeMillis()
        ): Transaction = Transaction(
            id = id,
            ledgerId = ledgerId,
            categoryId = categoryId,
            amount = amount,
            type = type,
            note = note,
            date = date,
            createdAt = timestamp,
            updatedAt = timestamp,
            isDeleted = false
        )
    }
}
