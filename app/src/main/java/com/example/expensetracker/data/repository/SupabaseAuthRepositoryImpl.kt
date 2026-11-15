package com.example.expensetracker.data.repository

import com.example.expensetracker.domain.repository.AuthRepository
import com.example.expensetracker.domain.repository.AuthUser
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@Singleton
class SupabaseAuthRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient
) : AuthRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override val currentUserFlow: StateFlow<AuthUser?> = supabase.auth.sessionStatus
        .map { status ->
            when (status) {
                is SessionStatus.Authenticated -> {
                    val user = status.session.user
                    user?.let { AuthUser(id = it.id, email = it.email) }
                }
                else -> null
            }
        }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = supabase.auth.currentSessionOrNull()?.user?.let {
                AuthUser(id = it.id, email = it.email)
            }
        )

    override val currentUserId: String?
        get() = currentUserFlow.value?.id ?: supabase.auth.currentSessionOrNull()?.user?.id

    override val isAuthenticated: Boolean
        get() = currentUserId != null

    override suspend fun signInWithEmail(email: String, password: String): Result<AuthUser> = runCatching {
        supabase.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
        val user = supabase.auth.currentUserOrNull()
            ?: throw IllegalStateException("Signed in successfully but user info is null")
        AuthUser(id = user.id, email = user.email)
    }

    override suspend fun signUpWithEmail(email: String, password: String): Result<AuthUser> = runCatching {
        supabase.auth.signUpWith(Email) {
            this.email = email
            this.password = password
        }
        val user = supabase.auth.currentUserOrNull()
            ?: throw IllegalStateException("Sign up submitted. Please check email if confirmation is required.")
        AuthUser(id = user.id, email = user.email)
    }

    override suspend fun signOut(): Result<Unit> = runCatching {
        supabase.auth.signOut()
    }
}
