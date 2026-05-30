package com.example.expensetracker.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.expensetracker.Constants
import com.example.expensetracker.data.local.dao.CategoryDao
import com.example.expensetracker.data.local.dao.LedgerDao
import com.example.expensetracker.data.local.dao.MonthlySnapshotDao
import com.example.expensetracker.data.local.dao.TransactionDao
import com.example.expensetracker.data.local.entity.Category
import com.example.expensetracker.data.local.entity.Ledger
import com.example.expensetracker.data.local.entity.MonthlySnapshot
import com.example.expensetracker.data.local.entity.Transaction

@Database(
    entities = [
        Transaction::class,
        Category::class,
        Ledger::class,
        MonthlySnapshot::class
    ],
    version = 2,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun ledgerDao(): LedgerDao
    abstract fun monthlySnapshotDao(): MonthlySnapshotDao

    companion object {
        const val DATABASE_NAME = Constants.Database.DATABASE_NAME

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                CREATE TABLE IF NOT EXISTS monthly_snapshots (
                    id TEXT NOT NULL PRIMARY KEY,
                    ledgerId TEXT NOT NULL,
                    month INTEGER NOT NULL,
                    year INTEGER NOT NULL,
                    openingBalance REAL NOT NULL DEFAULT 0.0,
                    closingBalance REAL NOT NULL DEFAULT 0.0,
                    totalIncome REAL NOT NULL DEFAULT 0.0,
                    totalExpense REAL NOT NULL DEFAULT 0.0,
                    createdAt INTEGER NOT NULL,
                    updatedAt INTEGER NOT NULL,
                    FOREIGN KEY(ledgerId) REFERENCES ledgers(id) ON DELETE CASCADE
                )
            """)
                db.execSQL("""
                CREATE UNIQUE INDEX IF NOT EXISTS index_monthly_snapshots_ledgerId_month_year
                ON monthly_snapshots(ledgerId, month, year)
            """)
            }
        }
    }
}