package com.example.expensetracker.data.repository

import com.example.expensetracker.data.local.entity.MonthlySnapshot
import com.example.expensetracker.data.local.entity.Transaction
import com.example.expensetracker.data.local.entity.TransactionWithCategory
import com.example.expensetracker.domain.model.AppResult
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {

    fun getTransactions(
        ledgerId: String,
        startDate: Long,
        endDate: Long
    ): Flow<List<TransactionWithCategory>>

    fun getPeriodIncome(ledgerId: String, startDate: Long, endDate: Long): Flow<Double?>
    fun getPeriodExpense(ledgerId: String, startDate: Long, endDate: Long): Flow<Double?>
    fun getMonthlySnapshot(ledgerId: String, month: Int, year: Int): Flow<MonthlySnapshot?>

    suspend fun getTransactionById(id: String): AppResult<Transaction?>
    suspend fun insertTransaction(transaction: Transaction): AppResult<Unit>
    suspend fun updateTransaction(oldTransaction: Transaction, newTransaction: Transaction): AppResult<Unit>
    suspend fun softDeleteTransaction(transaction: Transaction): AppResult<Unit>
}