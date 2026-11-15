package com.example.expensetracker.data.sync

import com.example.expensetracker.data.local.UserPreferences
import com.example.expensetracker.data.local.dao.CategoryDao
import com.example.expensetracker.data.local.dao.LedgerDao
import com.example.expensetracker.data.local.dao.MonthlySnapshotDao
import com.example.expensetracker.data.local.dao.TransactionDao
import com.example.expensetracker.domain.repository.AuthRepository
import com.example.expensetracker.domain.sync.SyncResult
import com.example.expensetracker.util.MainDispatcherRule
import io.github.jan.supabase.SupabaseClient
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SyncManagerTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val supabase = mockk<SupabaseClient>(relaxed = true)
    private val authRepository = mockk<AuthRepository>(relaxed = true)
    private val ledgerDao = mockk<LedgerDao>(relaxed = true)
    private val categoryDao = mockk<CategoryDao>(relaxed = true)
    private val transactionDao = mockk<TransactionDao>(relaxed = true)
    private val monthlySnapshotDao = mockk<MonthlySnapshotDao>(relaxed = true)
    private val userPreferences = mockk<UserPreferences>(relaxed = true)

    private lateinit var syncManager: SyncManagerImpl

    @Before
    fun setup() {
        every { userPreferences.lastSyncTime } returns flowOf(null)
        syncManager = SyncManagerImpl(
            supabase = supabase,
            authRepository = authRepository,
            ledgerDao = ledgerDao,
            categoryDao = categoryDao,
            transactionDao = transactionDao,
            monthlySnapshotDao = monthlySnapshotDao,
            userPreferences = userPreferences
        )
    }

    @Test
    fun sync_whenNotAuthenticated_returnsNotAuthenticated() = runTest {
        every { authRepository.isAuthenticated } returns false
        every { authRepository.currentUserId } returns null

        val result = syncManager.sync()
        assertEquals(SyncResult.NotAuthenticated, result)
    }

    @Test
    fun sync_whenExceptionOccurs_catchesAndReturnsError() = runTest {
        every { authRepository.isAuthenticated } returns true
        every { authRepository.currentUserId } returns "user-123"
        coEvery { ledgerDao.getPendingSyncLedgers() } throws RuntimeException("DB Disk Error")

        val result = syncManager.sync()
        assertTrue("Expected SyncResult.Error on exception", result is SyncResult.Error)
    }
}
