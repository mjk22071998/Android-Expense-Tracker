package com.example.expensetracker.data.remote.mapper

import com.example.expensetracker.data.local.SyncStatus
import com.example.expensetracker.data.local.entity.Category
import com.example.expensetracker.data.local.entity.Ledger
import com.example.expensetracker.data.local.entity.Transaction
import com.example.expensetracker.data.remote.dto.CategoryDto
import com.example.expensetracker.data.remote.dto.LedgerDto
import com.example.expensetracker.data.remote.dto.TransactionDto
import java.time.Instant

fun Long.toIso8601(): String = Instant.ofEpochMilli(this).toString()

fun String?.toEpochMillis(fallback: Long = System.currentTimeMillis()): Long {
    if (this == null) return fallback
    return try {
        Instant.parse(this).toEpochMilli()
    } catch (_: Exception) {
        fallback
    }
}

// Transaction mappings
fun Transaction.toDto(userId: String): TransactionDto = TransactionDto(
    id = serverId ?: id,
    userId = userId,
    ledgerId = ledgerId,
    categoryId = categoryId,
    amount = amount,
    type = type,
    date = date,
    note = note,
    createdAt = createdAt.toIso8601(),
    updatedAt = updatedAt.toIso8601(),
    isDeleted = isDeleted
)

fun TransactionDto.toEntity(): Transaction = Transaction(
    id = id,
    ledgerId = ledgerId,
    categoryId = categoryId,
    amount = amount,
    type = type,
    note = note,
    date = date,
    createdAt = createdAt.toEpochMillis(),
    updatedAt = updatedAt.toEpochMillis(),
    isDeleted = isDeleted,
    syncStatus = SyncStatus.SYNCED,
    serverId = id
)

// Category mappings
fun Category.toDto(userId: String): CategoryDto = CategoryDto(
    id = serverId ?: id,
    userId = userId,
    name = name,
    type = transactionType,
    icon = icon,
    isDefault = isDefault,
    updatedAt = updatedAt.toIso8601(),
    isDeleted = isDeleted
)

fun CategoryDto.toEntity(): Category = Category(
    id = id,
    name = name,
    icon = icon,
    transactionType = type,
    isDefault = isDefault,
    syncStatus = SyncStatus.SYNCED,
    updatedAt = updatedAt.toEpochMillis(),
    isDeleted = isDeleted,
    serverId = id
)

// Ledger mappings
fun Ledger.toDto(userId: String): LedgerDto = LedgerDto(
    id = serverId ?: id,
    userId = userId,
    name = name,
    currencyCode = currencyCode,
    isDefault = isDefault,
    createdAt = createdAt.toIso8601(),
    updatedAt = updatedAt.toIso8601(),
    isDeleted = false
)

fun LedgerDto.toEntity(): Ledger = Ledger(
    id = id,
    name = name,
    currencyCode = currencyCode,
    balance = 0.0,
    totalIncome = 0.0,
    totalExpenses = 0.0,
    createdAt = createdAt.toEpochMillis(),
    updatedAt = updatedAt.toEpochMillis(),
    isDefault = isDefault,
    syncStatus = SyncStatus.SYNCED,
    serverId = id
)
