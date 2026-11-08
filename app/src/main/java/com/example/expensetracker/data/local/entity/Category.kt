package com.example.expensetracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey
    val id: String,
    val name: String,
    val icon: String,
    val transactionType: String,
    val isDefault: Boolean = true
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
            isDefault: Boolean = false
        ): Category = Category(
            id = id,
            name = name,
            icon = icon,
            transactionType = transactionType,
            isDefault = isDefault
        )
    }
}
