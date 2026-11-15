package com.example.expensetracker.presentation.sync

import com.example.expensetracker.data.local.dao.TransactionDao
import com.example.expensetracker.domain.repository.AuthRepository
import com.example.expensetracker.domain.repository.AuthUser
import com.example.expensetracker.domain.sync.SyncManager
import com.example.expensetracker.util.MainDispatcherRule
import com.example.expensetracker.worker.SyncScheduler
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SyncViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val authRepository = mockk<AuthRepository>(relaxed = true)
    private val syncManager = mockk<SyncManager>(relaxed = true)
    private val syncScheduler = mockk<SyncScheduler>(relaxed = true)
    private val transactionDao = mockk<TransactionDao>(relaxed = true)

    private val currentUserFlow = MutableStateFlow<AuthUser?>(null)
    private val isSyncingFlow = MutableStateFlow(false)
    private val lastSyncTimeFlow = MutableStateFlow<Long?>(null)
    private val pendingCountFlow = MutableStateFlow(0)

    private lateinit var viewModel: SyncViewModel

    @Before
    fun setup() {
        every { authRepository.currentUserFlow } returns currentUserFlow
        every { syncManager.isSyncing } returns isSyncingFlow
        every { syncManager.lastSyncTime } returns lastSyncTimeFlow
        every { transactionDao.getPendingSyncCountFlow() } returns pendingCountFlow

        viewModel = SyncViewModel(
            authRepository = authRepository,
            syncManager = syncManager,
            syncScheduler = syncScheduler,
            transactionDao = transactionDao
        )
    }

    @Test
    fun initialState_reflectsDefaultFlowValues() {
        val state = viewModel.uiState.value
        assertFalse(state.isAuthenticated)
        assertEquals(null, state.userEmail)
        assertFalse(state.isSyncing)
        assertEquals(0, state.pendingUploadCount)
    }

    @Test
    fun signIn_whenSuccessful_triggersSync() = runTest {
        coEvery { authRepository.signInWithEmail("test@example.com", "pass123") } returns Result.success(
            AuthUser("uid-1", "test@example.com")
        )

        viewModel.signIn("test@example.com", "pass123")

        verify { syncScheduler.enqueueOneTimeSync() }
        assertEquals(null, viewModel.uiState.value.authErrorMessage)
    }

    @Test
    fun signIn_whenFailed_surfacesErrorMessage() = runTest {
        coEvery { authRepository.signInWithEmail("test@example.com", "wrong") } returns Result.failure(
            RuntimeException("Invalid login credentials")
        )

        viewModel.signIn("test@example.com", "wrong")

        assertEquals("Invalid login credentials", viewModel.uiState.value.authErrorMessage)
    }

    @Test
    fun syncNow_triggersScheduler() {
        viewModel.syncNow()
        verify { syncScheduler.enqueueOneTimeSync() }
    }

    @Test
    fun signOut_delegatesToRepository() = runTest {
        viewModel.signOut()
        coVerify { authRepository.signOut() }
    }
}
