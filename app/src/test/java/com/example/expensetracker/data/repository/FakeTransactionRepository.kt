package com.example.expensetracker.data.repository

import com.example.expensetracker.data.local.entity.MonthlySnapshot
import com.example.expensetracker.data.local.entity.Transaction
import com.example.expensetracker.data.local.entity.TransactionWithCategory
import com.example.expensetracker.domain.model.AppResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow

class FakeTransactionRepository : TransactionRepository {

    private val transactionsFlow = MutableStateFlow<List<TransactionWithCategory>>(emptyList())

    // Test-only knobs — set these from inside a test to control behavior
    var errorToThrow: Throwable? = null
    var softDeleteResult: AppResult<Unit> = AppResult.Success(Unit)
    var lastSoftDeletedTransaction: Transaction? = null

    fun emitTransactions(transactions: List<TransactionWithCategory>) {
        transactionsFlow.value = transactions
    }

    override fun getTransactions(
        ledgerId: String,
        startDate: Long,
        endDate: Long,
        searchQuery: String
    ): Flow<List<TransactionWithCategory>> = flow {
        errorToThrow?.let { throw it }
        emitAll(transactionsFlow)
    }

    override fun getPeriodIncome(ledgerId: String, startDate: Long, endDate: Long): Flow<Double?> =
        TODO("Not needed for current DashboardViewModel tests")

    override fun getPeriodExpense(ledgerId: String, startDate: Long, endDate: Long): Flow<Double?> =
        TODO("Not needed for current DashboardViewModel tests")

    override fun getMonthlySnapshot(ledgerId: String, month: Int, year: Int): Flow<MonthlySnapshot?> =
        TODO("Not needed for current DashboardViewModel tests")

    override suspend fun getTransactionById(id: String): AppResult<Transaction?> =
        TODO("Not needed for current DashboardViewModel tests")

    override suspend fun insertTransaction(transaction: Transaction): AppResult<Unit> =
        TODO("Not needed for current DashboardViewModel tests")

    override suspend fun updateTransaction(
        oldTransaction: Transaction,
        newTransaction: Transaction
    ): AppResult<Unit> = TODO("Not needed for current DashboardViewModel tests")

    override suspend fun softDeleteTransaction(transaction: Transaction): AppResult<Unit> {
        lastSoftDeletedTransaction = transaction
        return softDeleteResult
    }
}