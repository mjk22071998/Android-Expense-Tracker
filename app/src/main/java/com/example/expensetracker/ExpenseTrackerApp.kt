package com.example.expensetracker

import android.app.Application
import com.example.expensetracker.data.local.CategorySeeder
import com.example.expensetracker.data.local.LedgerSeeder
import dagger.hilt.android.HiltAndroidApp
import jakarta.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@HiltAndroidApp
class ExpenseTrackerApp : Application() {
    @Inject
    lateinit var categorySeeder: CategorySeeder

    @Inject
    lateinit var ledgerSeeder: LedgerSeeder

    override fun onCreate() {
        super.onCreate()
        CoroutineScope(Dispatchers.IO).launch {
            categorySeeder.seedIfNeeded()
            ledgerSeeder.seedIfNeeded()
        }
    }
}