package com.example.expensetracker

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.example.expensetracker.data.local.CategorySeeder
import com.example.expensetracker.data.local.LedgerSeeder
import com.example.expensetracker.worker.SyncScheduler
import dagger.hilt.android.HiltAndroidApp
import jakarta.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@HiltAndroidApp
class ExpenseTrackerApp : Application(), Configuration.Provider {
    @Inject
    lateinit var categorySeeder: CategorySeeder

    @Inject
    lateinit var ledgerSeeder: LedgerSeeder

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var syncScheduler: SyncScheduler

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        CoroutineScope(Dispatchers.IO).launch {
            categorySeeder.seedIfNeeded()
            ledgerSeeder.seedIfNeeded()
            syncScheduler.schedulePeriodicSync()
        }
    }
}
