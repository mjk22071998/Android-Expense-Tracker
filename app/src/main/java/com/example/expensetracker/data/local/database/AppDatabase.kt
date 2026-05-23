package com.example.expensetracker.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.expensetracker.data.local.dao.CategoryDao
import com.example.expensetracker.data.local.dao.LedgerDao
import com.example.expensetracker.data.local.dao.TransactionDao
import com.example.expensetracker.data.local.entity.Category
import com.example.expensetracker.data.local.entity.Ledger
import com.example.expensetracker.data.local.entity.Transaction

@Database(
    entities = [
        Transaction::class,
        Ledger::class,
        Category::class
    ], version = 1, exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun ledgerDao(): LedgerDao

    companion object{
        const val DATABASE_NAME="expense_tracker.db"
    }
}