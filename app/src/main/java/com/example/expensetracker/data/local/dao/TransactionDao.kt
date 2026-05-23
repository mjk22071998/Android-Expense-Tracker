package com.example.expensetracker.data.local.dao

import android.R
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
    suspend fun getTransactions(ledgerId: String, startDate: Long, endDate: Long)

    @Query(
        """
        SELECT SUM(amount) FROM transactions
        WHERE ledgerId=:ledgerId
        AND type='ICOME'
        AND isDeleted=0
        AND date BETWEEN :startDate AND :endDate
    """
    )
    suspend fun getTotalIncome(ledgerId: String, startDate: Long, endDate: Long): Flow<Double?>

    @Query(
        """
        SELECT SUM(amount) FROM transactions
        WHERE ledgerId=:ledgerId
        AND type='EXPENSE'
        AND isDeleted=0
        AND date BETWEEN :startDate AND :endDate
    """
    )
    suspend fun getTotalExpenses(ledgerId: String, startDate: Long, endDate: Long): Flow<Double?>

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
}