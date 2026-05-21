package com.example.expensetracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category (
    @PrimaryKey
    val id: String,
    val name: String,
    val icon: String,
    val transactionType: String,
    val isDefault: Boolean = true
)