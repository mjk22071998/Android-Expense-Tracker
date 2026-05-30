package com.example.expensetracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.expensetracker.data.local.entity.MonthlySnapshot
import kotlinx.coroutines.flow.Flow

@Dao
interface MonthlySnapshotDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(snapshot: MonthlySnapshot)

    @Query("""
        SELECT * FROM monthly_snapshots 
        WHERE ledgerId = :ledgerId 
        AND month = :month 
        AND year = :year 
        LIMIT 1
    """)
    suspend fun getSnapshot(ledgerId: String, month: Int, year: Int): MonthlySnapshot?

    @Query("""
        SELECT * FROM monthly_snapshots 
        WHERE ledgerId = :ledgerId 
        AND month = :month 
        AND year = :year 
        LIMIT 1
    """)
    fun observeSnapshot(ledgerId: String, month: Int, year: Int): Flow<MonthlySnapshot?>

    @Query("""
        UPDATE monthly_snapshots
        SET closingBalance = closingBalance + :amount,
            totalIncome = totalIncome + :amount,
            updatedAt = :updatedAt
        WHERE ledgerId = :ledgerId
        AND month = :month
        AND year = :year
    """)
    suspend fun addIncome(
        ledgerId: String,
        month: Int,
        year: Int,
        amount: Double,
        updatedAt: Long
    )

    @Query("""
        UPDATE monthly_snapshots
        SET closingBalance = closingBalance - :amount,
            totalExpense = totalExpense + :amount,
            updatedAt = :updatedAt
        WHERE ledgerId = :ledgerId
        AND month = :month
        AND year = :year
    """)
    suspend fun addExpense(
        ledgerId: String,
        month: Int,
        year: Int,
        amount: Double,
        updatedAt: Long
    )

    @Query("""
        UPDATE monthly_snapshots
        SET closingBalance = closingBalance - :amount,
            totalIncome = totalIncome - :amount,
            updatedAt = :updatedAt
        WHERE ledgerId = :ledgerId
        AND month = :month
        AND year = :year
    """)
    suspend fun reverseIncome(
        ledgerId: String,
        month: Int,
        year: Int,
        amount: Double,
        updatedAt: Long
    )

    @Query("""
        UPDATE monthly_snapshots
        SET closingBalance = closingBalance + :amount,
            totalExpense = totalExpense - :amount,
            updatedAt = :updatedAt
        WHERE ledgerId = :ledgerId
        AND month = :month
        AND year = :year
    """)
    suspend fun reverseExpense(
        ledgerId: String,
        month: Int,
        year: Int,
        amount: Double,
        updatedAt: Long
    )

    @Query("""
        SELECT * FROM monthly_snapshots
        WHERE ledgerId = :ledgerId
        ORDER BY year DESC, month DESC
    """)
    fun getAllSnapshots(ledgerId: String): Flow<List<MonthlySnapshot>>
}