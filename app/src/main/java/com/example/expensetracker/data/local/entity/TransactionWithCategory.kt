package com.example.expensetracker.data.local.entity

data class TransactionWithCategory(
    val id: String,
    val ledgerId: String,
    val categoryId: String?,
    val categoryIcon: String,
    val categoryName: String,
    val amount: Double,
    val type: String,
    val note: String,
    val date: Long,
    val createdAt: Long,
    val updatedAt: Long,
    val isDeleted: Boolean
)
