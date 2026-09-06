package com.example.expensetracker.data.repository

import com.example.expensetracker.data.local.entity.MonthlySnapshot
import com.example.expensetracker.data.local.entity.Transaction
import com.example.expensetracker.data.local.entity.TransactionWithCategory
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    fun getTransactions(
        ledgerId: String,
        startDate: Long,
        endDate: Long
    ): Flow<List<TransactionWithCategory>>
    fun getPeriodIncome(ledgerId: String, startDate: Long, endDate: Long):Flow<Double?>
    fun getPeriodExpense(ledgerId: String, startDate: Long, endDate: Long):Flow<Double?>
    fun getMonthlySnapshot(
        ledgerId: String,
        month: Int,
        year: Int
    ): Flow<MonthlySnapshot?>
    suspend fun insertTransaction(transaction: Transaction)
    suspend fun updateTransaction(oldTransaction: Transaction, newTransaction: Transaction)
    suspend fun softDeleteTransaction(transaction: Transaction)
    suspend fun getTransactionById(id: String): Transaction?
}