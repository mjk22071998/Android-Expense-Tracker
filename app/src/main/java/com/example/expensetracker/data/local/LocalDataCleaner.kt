package com.example.expensetracker.data.local

import com.example.expensetracker.data.local.database.AppDatabase
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class LocalDataCleaner @Inject constructor(
    private val appDatabase: AppDatabase,
    private val categorySeeder: CategorySeeder,
    private val ledgerSeeder: LedgerSeeder,
    private val userPreferences: UserPreferences
) {
    suspend fun resetToFreshGuest() = withContext(Dispatchers.IO) {
        appDatabase.clearAllTables()
        categorySeeder.seedIfNeeded()
        ledgerSeeder.seedIfNeeded()
        userPreferences.clearLastSyncTime()
    }
}
