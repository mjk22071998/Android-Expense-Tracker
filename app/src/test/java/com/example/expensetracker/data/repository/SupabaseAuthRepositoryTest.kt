package com.example.expensetracker.data.repository

import com.example.expensetracker.di.SupabaseModule
import com.example.expensetracker.domain.repository.AuthRepository
import com.example.expensetracker.util.MainDispatcherRule
import io.github.jan.supabase.auth.MemoryCodeVerifierCache
import io.github.jan.supabase.auth.MemorySessionManager
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SupabaseAuthRepositoryTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var authRepository: AuthRepository

    @Before
    fun setup() {
        val client = SupabaseModule.provideSupabaseClient(
            sessionManager = MemorySessionManager(),
            codeVerifierCache = MemoryCodeVerifierCache()
        )
        authRepository = SupabaseAuthRepositoryImpl(client)
    }

    @Test
    fun initialState_isNotAuthenticated() {
        assertFalse(authRepository.isAuthenticated)
        assertNull(authRepository.currentUserId)
        assertNull(authRepository.currentUserFlow.value)
    }

    @Test
    fun signInWithInvalidCredentials_returnsFailure() = runTest {
        val result = authRepository.signInWithEmail("invalid@test.com", "wrongpass123")
        assertTrue("Expected failure for invalid credentials", result.isFailure)
        assertNotNull(result.exceptionOrNull())
    }

    @Test
    fun signOut_whenNotAuthenticated_succeeds() = runTest {
        val result = authRepository.signOut()
        assertTrue(result.isSuccess)
        assertFalse(authRepository.isAuthenticated)
    }
}
