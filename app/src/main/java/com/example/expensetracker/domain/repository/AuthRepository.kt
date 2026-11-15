package com.example.expensetracker.domain.repository

import kotlinx.coroutines.flow.StateFlow

data class AuthUser(
    val id: String,
    val email: String?
)

interface AuthRepository {
    val currentUserFlow: StateFlow<AuthUser?>
    val currentUserId: String?
    val isAuthenticated: Boolean

    suspend fun signInWithEmail(email: String, password: String): Result<AuthUser>
    suspend fun signUpWithEmail(email: String, password: String): Result<AuthUser>
    suspend fun signOut(): Result<Unit>
}
