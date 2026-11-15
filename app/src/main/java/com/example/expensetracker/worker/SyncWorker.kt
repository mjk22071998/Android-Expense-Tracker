package com.example.expensetracker.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.expensetracker.domain.sync.SyncManager
import com.example.expensetracker.domain.sync.SyncResult
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val syncManager: SyncManager
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return when (val syncResult = syncManager.sync()) {
            is SyncResult.Success -> Result.success()
            is SyncResult.NotAuthenticated -> Result.success()
            is SyncResult.Error -> {
                if (runAttemptCount < MAX_RETRIES) {
                    Result.retry()
                } else {
                    Result.failure()
                }
            }
        }
    }

    companion object {
        const val WORK_NAME_ONE_TIME = "SyncWorker_OneTime"
        const val WORK_NAME_PERIODIC = "SyncWorker_Periodic"
        private const val MAX_RETRIES = 3
    }
}
