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
    private val periodIncomeFlow = MutableStateFlow<Double?>(null)
    private val periodExpenseFlow = MutableStateFlow<Double?>(null)

    // Test-only knobs — set these from inside a test to control behavior
    var errorToThrow: Throwable? = null
    var softDeleteResult: AppResult<Unit> = AppResult.Success(Unit)
    var lastSoftDeletedTransaction: Transaction? = null
    var clearAllTransactionsResult: AppResult<Unit> = AppResult.Success(Unit)
    var clearAllCalledWithLedgerId: String? = null
    var insertTransactionResult: AppResult<Unit> = AppResult.Success(Unit)
    var updateTransactionResult: AppResult<Unit> = AppResult.Success(Unit)
    var insertedTransaction: Transaction? = null
    var updatedTransaction: Transaction? = null
    var transactionToReturn: Transaction? = null

    fun emitTransactions(transactions: List<TransactionWithCategory>) {
        transactionsFlow.value = transactions
    }

    fun emitPeriodIncome(income: Double?) {
        periodIncomeFlow.value = income
    }

    fun emitPeriodExpense(expense: Double?) {
        periodExpenseFlow.value = expense
    }

    override fun getTransactions(
        ledgerId: String,
        startDate: Long,
        endDate: Long,
        searchQuery: String,
        typeFilter: String?,
        categoryIds: Set<String>
    ): Flow<List<TransactionWithCategory>> = flow {
        errorToThrow?.let { throw it }
        emitAll(transactionsFlow)
    }

    override fun getPeriodIncome(ledgerId: String, startDate: Long, endDate: Long): Flow<Double?> =
        periodIncomeFlow

    override fun getPeriodExpense(ledgerId: String, startDate: Long, endDate: Long): Flow<Double?> =
        periodExpenseFlow

    override fun getMonthlySnapshot(ledgerId: String, month: Int, year: Int): Flow<MonthlySnapshot?> =
        TODO("Not needed for current DashboardViewModel tests")

    override suspend fun getTransactionById(id: String): AppResult<Transaction?> =
        AppResult.Success(transactionToReturn)

    override suspend fun insertTransaction(transaction: Transaction): AppResult<Unit> {
        insertedTransaction = transaction
        return insertTransactionResult
    }

    override suspend fun updateTransaction(
        oldTransaction: Transaction,
        newTransaction: Transaction
    ): AppResult<Unit> {
        updatedTransaction = newTransaction
        return updateTransactionResult
    }

    override suspend fun softDeleteTransaction(transaction: Transaction): AppResult<Unit> {
        lastSoftDeletedTransaction = transaction
        return softDeleteResult
    }

    override suspend fun clearAllTransactions(ledgerId: String): AppResult<Unit> {
        clearAllCalledWithLedgerId = ledgerId
        return clearAllTransactionsResult
    }
}