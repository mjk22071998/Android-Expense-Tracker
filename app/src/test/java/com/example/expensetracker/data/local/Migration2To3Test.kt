package com.example.expensetracker.data.local

import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.expensetracker.data.local.database.AppDatabase
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test

class Migration2To3Test {

    @Test
    fun migration_2_3_addsSyncColumnsToLedgersCategoriesAndTransactions() {
        val db = mockk<SupportSQLiteDatabase>(relaxed = true)

        AppDatabase.MIGRATION_2_3.migrate(db)

        verify {
            db.execSQL(match { it.contains("ALTER TABLE ledgers ADD COLUMN syncStatus INTEGER NOT NULL DEFAULT 0") })
            db.execSQL(match { it.contains("ALTER TABLE ledgers ADD COLUMN serverId TEXT") })
            db.execSQL(match { it.contains("ALTER TABLE categories ADD COLUMN syncStatus INTEGER NOT NULL DEFAULT 0") })
            db.execSQL(match { it.contains("ALTER TABLE categories ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0") })
            db.execSQL(match { it.contains("ALTER TABLE categories ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0") })
            db.execSQL(match { it.contains("ALTER TABLE categories ADD COLUMN serverId TEXT") })
            db.execSQL(match { it.contains("ALTER TABLE transactions ADD COLUMN syncStatus INTEGER NOT NULL DEFAULT 0") })
            db.execSQL(match { it.contains("ALTER TABLE transactions ADD COLUMN serverId TEXT") })
        }
    }
}
