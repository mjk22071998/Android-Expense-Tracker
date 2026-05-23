package com.example.expensetracker.data.repository

import com.example.expensetracker.data.local.entity.Transaction
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    fun getTransactions(ledgerId: String, startDate: Long, endDate: Long): Flow<List<Transaction>>
    fun getPeriodIncome(ledgerId: String, startDate: Long, endDate: Long):Flow<Double?>
    fun getPeriodExpense(ledgerId: String, startDate: Long, endDate: Long):Flow<Double?>

    suspend fun insertTransaction(transaction: Transaction)
    suspend fun updateTransaction(oldTransaction: Transaction, newTransaction: Transaction)
    suspend fun softDeleteTransaction(transaction: Transaction)
}