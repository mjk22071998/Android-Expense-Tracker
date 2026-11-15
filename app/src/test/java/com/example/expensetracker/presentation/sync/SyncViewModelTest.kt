package com.example.expensetracker.presentation.sync

import com.example.expensetracker.data.local.LocalDataCleaner
import com.example.expensetracker.data.local.dao.TransactionDao
import com.example.expensetracker.domain.repository.AuthRepository
import com.example.expensetracker.domain.repository.AuthUser
import com.example.expensetracker.domain.sync.SyncManager
import com.example.expensetracker.domain.sync.SyncResult
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
    private val localDataCleaner = mockk<LocalDataCleaner>(relaxed = true)

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
            transactionDao = transactionDao,
            localDataCleaner = localDataCleaner
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
    fun signOut_withoutSync_cleansLocalDataAndCancelsWorkers() = runTest {
        viewModel.signOut(syncFirst = false)

        coVerify { authRepository.signOut() }
        coVerify { localDataCleaner.resetToFreshGuest() }
        verify { syncScheduler.cancelAllSync() }
    }

    @Test
    fun signOut_withSync_whenSyncSucceeds_cleansLocalData() = runTest {
        coEvery { syncManager.sync() } returns SyncResult.Success

        viewModel.signOut(syncFirst = true)

        coVerify { syncManager.sync() }
        coVerify { authRepository.signOut() }
        coVerify { localDataCleaner.resetToFreshGuest() }
        verify { syncScheduler.cancelAllSync() }
    }

    @Test
    fun signOut_withSync_whenSyncFails_surfacesErrorWithoutWipingData() = runTest {
        coEvery { syncManager.sync() } returns SyncResult.Error(RuntimeException("No network"))

        viewModel.signOut(syncFirst = true)

        coVerify { syncManager.sync() }
        coVerify(exactly = 0) { authRepository.signOut() }
        coVerify(exactly = 0) { localDataCleaner.resetToFreshGuest() }
        verify(exactly = 0) { syncScheduler.cancelAllSync() }
        assertTrue(viewModel.uiState.value.authErrorMessage?.contains("No network") == true)
    }

    @Test
    fun sendPasswordResetEmail_whenSuccess_setsState() = runTest {
        coEvery { authRepository.sendPasswordResetEmail("user@example.com") } returns Result.success(Unit)

        viewModel.sendPasswordResetEmail("user@example.com")

        val state = viewModel.uiState.value
        assertTrue(state.passwordResetSent)
        assertEquals("Verification code sent to user@example.com", state.authSuccessMessage)
        assertEquals(null, state.authErrorMessage)
    }

    @Test
    fun sendPasswordResetEmail_whenFailure_setsErrorMessage() = runTest {
        coEvery { authRepository.sendPasswordResetEmail("bad@example.com") } returns Result.failure(
            RuntimeException("Rate limit exceeded")
        )

        viewModel.sendPasswordResetEmail("bad@example.com")

        val state = viewModel.uiState.value
        assertFalse(state.passwordResetSent)
        assertEquals("Rate limit exceeded", state.authErrorMessage)
    }

    @Test
    fun resetPasswordWithOtp_whenSuccess_setsSuccessMessageAndEnqueuesSync() = runTest {
        coEvery {
            authRepository.resetPasswordWithOtp("user@example.com", "12345678", "NewPass1!")
        } returns Result.success(Unit)

        viewModel.resetPasswordWithOtp("user@example.com", "12345678", "NewPass1!")

        val state = viewModel.uiState.value
        assertEquals("Password reset successfully. Signed in!", state.authSuccessMessage)
        verify { syncScheduler.enqueueOneTimeSync() }
    }

    @Test
    fun resetPasswordWithOtp_whenFailure_surfacesErrorMessage() = runTest {
        coEvery {
            authRepository.resetPasswordWithOtp("user@example.com", "00000000", "NewPass1!")
        } returns Result.failure(RuntimeException("Invalid OTP code"))

        viewModel.resetPasswordWithOtp("user@example.com", "00000000", "NewPass1!")

        val state = viewModel.uiState.value
        assertFalse(state.passwordUpdateSuccess)
        assertEquals("Invalid OTP code", state.authErrorMessage)
    }

    @Test
    fun updatePassword_whenSuccess_setsSuccessMessage() = runTest {
        coEvery {
            authRepository.updatePassword("OldPass1!", "NewPass1!")
        } returns Result.success(Unit)

        viewModel.updatePassword("OldPass1!", "NewPass1!")

        val state = viewModel.uiState.value
        assertEquals("Password updated successfully!", state.authSuccessMessage)
    }

    @Test
    fun updatePassword_whenFailure_setsErrorMessage() = runTest {
        coEvery {
            authRepository.updatePassword("WrongOld!", "NewPass1!")
        } returns Result.failure(RuntimeException("Current password incorrect"))

        viewModel.updatePassword("WrongOld!", "NewPass1!")

        val state = viewModel.uiState.value
        assertEquals("Current password incorrect", state.authErrorMessage)
    }

    @Test
    fun clearAuthMessages_resetsMessages() {
        viewModel.clearAuthMessages()
        val state = viewModel.uiState.value
        assertEquals(null, state.authErrorMessage)
        assertEquals(null, state.authSuccessMessage)
    }
}
