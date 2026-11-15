package com.example.expensetracker.worker

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.example.expensetracker.domain.sync.SyncManager
import com.example.expensetracker.domain.sync.SyncResult
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class SyncWorkerTest {

    private val context = mockk<Context>(relaxed = true)
    private val workerParams = mockk<WorkerParameters>(relaxed = true)
    private val syncManager = mockk<SyncManager>()

    private lateinit var worker: SyncWorker

    @Before
    fun setup() {
        worker = SyncWorker(context, workerParams, syncManager)
    }

    @Test
    fun doWork_whenSyncSucceeds_returnsSuccess() = runTest {
        coEvery { syncManager.sync() } returns SyncResult.Success

        val result = worker.doWork()
        assertEquals(ListenableWorker.Result.success(), result)
    }

    @Test
    fun doWork_whenNotAuthenticated_returnsSuccess() = runTest {
        coEvery { syncManager.sync() } returns SyncResult.NotAuthenticated

        val result = worker.doWork()
        assertEquals(ListenableWorker.Result.success(), result)
    }

    @Test
    fun doWork_whenSyncFails_returnsRetry() = runTest {
        coEvery { syncManager.sync() } returns SyncResult.Error(RuntimeException("Network failure"))

        val result = worker.doWork()
        assertEquals(ListenableWorker.Result.retry(), result)
    }
}
