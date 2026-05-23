package com.example.expensetracker.data.repository

import com.example.expensetracker.data.local.dao.LedgerDao
import com.example.expensetracker.data.local.dao.TransactionDao
import com.example.expensetracker.data.local.entity.Transaction
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow

class TransactionRepositoryImpl @Inject constructor(
    private val transactionDao: TransactionDao,
    private val ledgerDao: LedgerDao
): TransactionRepository {
    override fun getTransactions(
        ledgerId: String,
        startDate: Long,
        endDate: Long
    ): Flow<List<Transaction>> {
        return transactionDao.getTransactions(ledgerId,startDate,endDate)
    }

    override fun getPeriodIncome(
        ledgerId: String,
        startDate: Long,
        endDate: Long
    ): Flow<Double?> {
        return transactionDao.getPeriodIncome(ledgerId,startDate,endDate)
    }

    override fun getPeriodExpense(
        ledgerId: String,
        startDate: Long,
        endDate: Long
    ): Flow<Double?> {
        return transactionDao.getPeriodExpense(ledgerId,startDate,endDate)
    }

    override suspend fun insertTransaction(transaction: Transaction) {
        transactionDao.insertAndUpdateBalance(transaction,ledgerDao)
    }

    override suspend fun updateTransaction(
        oldTransaction: Transaction,
        newTransaction: Transaction
    ) {
        transactionDao.updateAndRecalculateBalance(oldTransaction, newTransaction, ledgerDao)
    }

    override suspend fun softDeleteTransaction(transaction: Transaction) {
        transactionDao.softDeleteAndUpdateBalance(transaction, ledgerDao)
    }
}