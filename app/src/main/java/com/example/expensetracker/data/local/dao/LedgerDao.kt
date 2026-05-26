package com.example.expensetracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.expensetracker.data.local.entity.Ledger
import kotlinx.coroutines.flow.Flow

@Dao
interface LedgerDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(ledger: Ledger)

    @Update
    suspend fun update(ledger: Ledger)

    @Query("SELECT * FROM ledgers")
    fun getAllLedgers(): Flow<List<Ledger>>

    @Query("SELECT * FROM ledgers WHERE id=:id")
    fun getLedgerById(id: String): Ledger?

    @Query("SELECT * FROM ledgers WHERE isDefault=1 LIMIT 1")
    fun getDefaultLedger(): Flow<Ledger?>

    @Query(
        """
        UPDATE ledgers
        SET balance=balance+:amount, 
            totalIncome=totalIncome+:amount, 
            updatedAt=:updatedAt
        WHERE id=:ledgerId
    """
    )
    suspend fun addIncome(ledgerId: String, amount: Double, updatedAt: Long)

    @Query(
        """
        UPDATE ledgers
        SET balance=balance-:amount, 
            totalExpenses=totalExpenses+:amount, 
            updatedAt=:updatedAt
        WHERE id=:ledgerId
    """
    )
    suspend fun addExpense(ledgerId: String, amount: Double, updatedAt: Long)

    @Query(
        """
            UPDATE ledgers 
            SET balance = balance - :amount,
                totalIncome = totalIncome - :amount,
                updatedAt = :updatedAt
            WHERE id = :ledgerId
        """
    )
    suspend fun reverseIncome(ledgerId: String, amount: Double, updatedAt: Long)

    @Query(
        """
        UPDATE ledgers 
        SET balance = balance + :amount,
            totalExpenses = totalExpenses - :amount,
            updatedAt = :updatedAt
        WHERE id = :ledgerId
        """
    )
    suspend fun reverseExpense(ledgerId: String, amount: Double, updatedAt: Long)

    @Query("DELETE FROM ledgers WHERE id=:id AND isDefault=0")
    suspend fun delete(id: String)
}