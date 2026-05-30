package com.example.expensetracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.expensetracker.Constants
import com.example.expensetracker.data.local.entity.MonthlySnapshot
import com.example.expensetracker.data.local.entity.Transaction
import com.example.expensetracker.data.local.entity.TransactionWithCategory
import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import java.util.UUID

@Dao
interface TransactionDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(transaction: Transaction)

    @Update
    suspend fun update(transaction: Transaction)

    @Query("""
    SELECT 
        t.id,
        t.ledgerId,
        t.categoryId,
        COALESCE(c.icon, 'other') as categoryIcon,
        COALESCE(c.name, 'Uncategorized') as categoryName,
        t.amount,
        t.type,
        t.note,
        t.date,
        t.createdAt,
        t.updatedAt,
        t.isDeleted
    FROM transactions t
    LEFT JOIN categories c ON t.categoryId = c.id
    WHERE t.ledgerId = :ledgerId
    AND t.isDeleted = 0
    AND t.date BETWEEN :startDate AND :endDate
    ORDER BY t.date DESC
""")
    fun getTransactions(
        ledgerId: String,
        startDate: Long,
        endDate: Long
    ): Flow<List<TransactionWithCategory>>

    @Query(
        """
        SELECT SUM(amount) FROM transactions
        WHERE ledgerId=:ledgerId
        AND type='ICOME'
        AND isDeleted=0
        AND date BETWEEN :startDate AND :endDate
    """
    )
    fun getPeriodIncome(ledgerId: String, startDate: Long, endDate: Long): Flow<Double?>

    @Query(
        """
        SELECT SUM(amount) FROM transactions
        WHERE ledgerId=:ledgerId
        AND type='EXPENSE'
        AND isDeleted=0
        AND date BETWEEN :startDate AND :endDate
    """
    )
    fun getPeriodExpense(ledgerId: String, startDate: Long, endDate: Long): Flow<Double?>

    @Query(
        """
        UPDATE transactions
        SET isDeleted=1, updatedAt=:updatedAt
        WHERE id=:id
    """
    )
    suspend fun softDelete(id: String, updatedAt: Long)

    @androidx.room.Transaction
    suspend fun insertAndUpdateBalance(
        transaction: Transaction,
        ledgerDao: LedgerDao,
        snapshotDao: MonthlySnapshotDao
    ) {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = transaction.date
        }
        val month = calendar.get(Calendar.MONTH) + 1
        val year = calendar.get(Calendar.YEAR)
        val now = System.currentTimeMillis()

        // Ensure snapshot exists for this month
        val existingSnapshot = snapshotDao.getSnapshot(transaction.ledgerId, month, year)
        if (existingSnapshot == null) {
            val previousSnapshot = getPreviousMonthSnapshot(
                transaction.ledgerId, month, year, snapshotDao
            )
            snapshotDao.insert(
                MonthlySnapshot(
                    id = UUID.randomUUID().toString(),
                    ledgerId = transaction.ledgerId,
                    month = month,
                    year = year,
                    openingBalance = previousSnapshot?.closingBalance ?: 0.0,
                    closingBalance = previousSnapshot?.closingBalance ?: 0.0,
                    createdAt = now,
                    updatedAt = now
                )
            )
        }

        // Insert transaction
        insert(transaction)

        // Update ledger and snapshot
        if (transaction.type == Constants.TransactionType.INCOME) {
            ledgerDao.addIncome(transaction.ledgerId, transaction.amount, now)
            snapshotDao.addIncome(transaction.ledgerId, month, year, transaction.amount, now)
        } else {
            ledgerDao.addExpense(transaction.ledgerId, transaction.amount, now)
            snapshotDao.addExpense(transaction.ledgerId, month, year, transaction.amount, now)
        }
    }

    @androidx.room.Transaction
    suspend fun updateAndRecalculateBalance(
        oldTransaction: Transaction,
        newTransaction: Transaction,
        ledgerDao: LedgerDao,
        snapshotDao: MonthlySnapshotDao
    ) {
        val oldCalendar = Calendar.getInstance().apply { timeInMillis = oldTransaction.date }
        val newCalendar = Calendar.getInstance().apply { timeInMillis = newTransaction.date }
        val oldMonth = oldCalendar.get(Calendar.MONTH) + 1
        val oldYear = oldCalendar.get(Calendar.YEAR)
        val newMonth = newCalendar.get(Calendar.MONTH) + 1
        val newYear = newCalendar.get(Calendar.YEAR)
        val now = System.currentTimeMillis()

        // Reverse old transaction effect
        if (oldTransaction.type == Constants.TransactionType.INCOME) {
            ledgerDao.reverseIncome(oldTransaction.ledgerId, oldTransaction.amount, now)
            snapshotDao.reverseIncome(oldTransaction.ledgerId, oldMonth, oldYear, oldTransaction.amount, now)
        } else {
            ledgerDao.reverseExpense(oldTransaction.ledgerId, oldTransaction.amount, now)
            snapshotDao.reverseExpense(oldTransaction.ledgerId, oldMonth, oldYear, oldTransaction.amount, now)
        }

        // Apply new transaction effect
        if (newTransaction.type == Constants.TransactionType.INCOME) {
            ledgerDao.addIncome(newTransaction.ledgerId, newTransaction.amount, now)
            snapshotDao.addIncome(newTransaction.ledgerId, newMonth, newYear, newTransaction.amount, now)
        } else {
            ledgerDao.addExpense(newTransaction.ledgerId, newTransaction.amount, now)
            snapshotDao.addExpense(newTransaction.ledgerId, newMonth, newYear, newTransaction.amount, now)
        }

        update(newTransaction)
    }

    @androidx.room.Transaction
    suspend fun softDeleteAndUpdateBalance(
        transaction: Transaction,
        ledgerDao: LedgerDao,
        snapshotDao: MonthlySnapshotDao
    ) {
        val calendar = Calendar.getInstance().apply { timeInMillis = transaction.date }
        val month = calendar.get(Calendar.MONTH) + 1
        val year = calendar.get(Calendar.YEAR)
        val now = System.currentTimeMillis()

        if (transaction.type == Constants.TransactionType.INCOME) {
            ledgerDao.reverseIncome(transaction.ledgerId, transaction.amount, now)
            snapshotDao.reverseIncome(transaction.ledgerId, month, year, transaction.amount, now)
        } else {
            ledgerDao.reverseExpense(transaction.ledgerId, transaction.amount, now)
            snapshotDao.reverseExpense(transaction.ledgerId, month, year, transaction.amount, now)
        }

        softDelete(transaction.id, now)
    }

    // Helper to get previous month's snapshot for opening balance
    private suspend fun getPreviousMonthSnapshot(
        ledgerId: String,
        month: Int,
        year: Int,
        snapshotDao: MonthlySnapshotDao
    ): MonthlySnapshot? {
        val prevMonth = if (month == 1) 12 else month - 1
        val prevYear = if (month == 1) year - 1 else year
        return snapshotDao.getSnapshot(ledgerId, prevMonth, prevYear)
    }
}