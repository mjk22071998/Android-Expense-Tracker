package com.example.expensetracker.data.repository

import com.example.expensetracker.data.local.dao.LedgerDao
import com.example.expensetracker.data.local.dao.MonthlySnapshotDao
import com.example.expensetracker.data.local.dao.TransactionDao
import com.example.expensetracker.data.local.entity.Transaction
import com.example.expensetracker.domain.model.AppResult
import com.example.expensetracker.domain.model.safeCall
import javax.inject.Inject

class TransactionRepositoryImpl @Inject constructor(
    private val transactionDao: TransactionDao,
    private val ledgerDao: LedgerDao,
    private val snapshotDao: MonthlySnapshotDao
) : TransactionRepository {

    override fun getTransactions(ledgerId: String, startDate: Long, endDate: Long) =
        transactionDao.getTransactions(ledgerId, startDate, endDate)

    override fun getPeriodIncome(ledgerId: String, startDate: Long, endDate: Long) =
        transactionDao.getPeriodIncome(ledgerId, startDate, endDate)

    override fun getPeriodExpense(ledgerId: String, startDate: Long, endDate: Long) =
        transactionDao.getPeriodExpense(ledgerId, startDate, endDate)

    override fun getMonthlySnapshot(ledgerId: String, month: Int, year: Int) =
        snapshotDao.observeSnapshot(ledgerId, month, year)

    override suspend fun getTransactionById(id: String): AppResult<Transaction?> = safeCall {
        transactionDao.getTransactionById(id)
    }

    override suspend fun insertTransaction(transaction: Transaction): AppResult<Unit> = safeCall {
        transactionDao.insertAndUpdateBalance(transaction, ledgerDao, snapshotDao)
    }

    override suspend fun updateTransaction(
        oldTransaction: Transaction,
        newTransaction: Transaction
    ): AppResult<Unit> = safeCall {
        transactionDao.updateAndRecalculateBalance(oldTransaction, newTransaction, ledgerDao, snapshotDao)
    }

    override suspend fun softDeleteTransaction(transaction: Transaction): AppResult<Unit> = safeCall {
        transactionDao.softDeleteAndUpdateBalance(transaction, ledgerDao, snapshotDao)
    }
}