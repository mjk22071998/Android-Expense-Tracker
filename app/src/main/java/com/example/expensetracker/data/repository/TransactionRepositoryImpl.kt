package com.example.expensetracker.data.repository

import com.example.expensetracker.data.local.dao.LedgerDao
import com.example.expensetracker.data.local.dao.MonthlySnapshotDao
import com.example.expensetracker.data.local.dao.TransactionDao
import com.example.expensetracker.data.local.entity.MonthlySnapshot
import com.example.expensetracker.data.local.entity.Transaction
import com.example.expensetracker.data.local.entity.TransactionWithCategory
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow

class TransactionRepositoryImpl @Inject constructor(
    private val transactionDao: TransactionDao,
    private val ledgerDao: LedgerDao,
    private val snapshotDao: MonthlySnapshotDao
): TransactionRepository {
    override fun getTransactions(
        ledgerId: String,
        startDate: Long,
        endDate: Long
    ): Flow<List<TransactionWithCategory>> { // ← update return type
        return transactionDao.getTransactions(ledgerId, startDate, endDate)
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
        transactionDao.insertAndUpdateBalance(transaction, ledgerDao, snapshotDao)
    }

    override suspend fun updateTransaction(
        oldTransaction: Transaction,
        newTransaction: Transaction
    ) {
        transactionDao.updateAndRecalculateBalance(
            oldTransaction, newTransaction, ledgerDao, snapshotDao
        )
    }

    override suspend fun softDeleteTransaction(transaction: Transaction) {
        transactionDao.softDeleteAndUpdateBalance(transaction, ledgerDao, snapshotDao)
    }

    override fun getMonthlySnapshot(
        ledgerId: String,
        month: Int,
        year: Int
    ): Flow<MonthlySnapshot?> {
        return snapshotDao.observeSnapshot(ledgerId, month, year)
    }

    override suspend fun getTransactionById(id: String): Transaction? {
        return transactionDao.getTransactionById(id)
    }
}