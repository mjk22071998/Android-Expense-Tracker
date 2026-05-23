package com.example.expensetracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.expensetracker.data.local.entity.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(transaction: Transaction)

    @Update
    suspend fun update(transaction: Transaction)

    @Query(
        """
        SELECT * FROM transactions
        WHERE ledgerId=:ledgerId
        AND isDeleted=0
        AND date BETWEEN :startDate AND :endDate
        ORDER BY date DESC
    """
    )
    fun getTransactions(ledgerId: String, startDate: Long, endDate: Long): Flow<List<Transaction>>

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
    suspend fun insertAndUpdateBalance(transaction: Transaction, ledgerDao: LedgerDao) {
        insert(transaction)
        val updatedAt = System.currentTimeMillis()
        if (transaction.type == "INCOME") {
            ledgerDao.addIncome(transaction.ledgerId, transaction.amount, updatedAt)
        } else {
            ledgerDao.addExpense(transaction.ledgerId, transaction.amount, updatedAt)
        }
    }

    @androidx.room.Transaction
    suspend fun updateAndRecalculateBalance(
        oldTransaction: Transaction,
        newTransaction: Transaction,
        ledgerDao: LedgerDao
    ) {
        // Step 1 — reverse old transaction effect
        if (oldTransaction.type == "EXPENSE") {
            ledgerDao.reverseExpense(oldTransaction.ledgerId, oldTransaction.amount, System.currentTimeMillis())
        } else {
            ledgerDao.reverseIncome(oldTransaction.ledgerId, oldTransaction.amount, System.currentTimeMillis())
        }

        // Step 2 — apply new transaction effect
        if (newTransaction.type == "EXPENSE") {
            ledgerDao.addExpense(newTransaction.ledgerId, newTransaction.amount, System.currentTimeMillis())
        } else {
            ledgerDao.addIncome(newTransaction.ledgerId, newTransaction.amount, System.currentTimeMillis())
        }

        // Step 3 — update the transaction itself
        update(newTransaction)
    }

    @androidx.room.Transaction
    suspend fun softDeleteAndUpdateBalance(
        transaction: Transaction,
        ledgerDao: LedgerDao
    ) {
        // Reverse the effect
        if (transaction.type == "EXPENSE") {
            ledgerDao.reverseExpense(transaction.ledgerId, transaction.amount, System.currentTimeMillis())
        } else {
            ledgerDao.reverseIncome(transaction.ledgerId, transaction.amount, System.currentTimeMillis())
        }

        // Soft delete
        softDelete(transaction.id, System.currentTimeMillis())
    }
}