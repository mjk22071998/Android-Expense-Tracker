package com.example.expensetracker.data.local

import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.expensetracker.data.local.database.AppDatabase
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test

class Migration3To4Test {

    @Test
    fun migration_3_4_addsIsDeletedColumnToLedgers() {
        val db = mockk<SupportSQLiteDatabase>(relaxed = true)

        AppDatabase.MIGRATION_3_4.migrate(db)

        verify {
            db.execSQL(match { it.contains("ALTER TABLE ledgers ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0") })
        }
    }
}
