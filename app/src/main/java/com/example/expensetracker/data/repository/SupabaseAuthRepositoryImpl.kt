package com.example.expensetracker.data.repository

import com.example.expensetracker.domain.repository.AuthRepository
import com.example.expensetracker.domain.repository.AuthUser
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.OtpType
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import io.ktor.client.request.header
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

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

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> = runCatching {
        supabase.auth.resetPasswordForEmail(email.trim())
    }

    override suspend fun resetPasswordWithOtp(email: String, otp: String, newPassword: String): Result<Unit> = runCatching {
        supabase.auth.verifyEmailOtp(
            type = OtpType.Email.RECOVERY,
            email = email.trim(),
            token = otp.trim()
        )
        supabase.auth.updateUser {
            this.password = newPassword.trim()
        }
        Unit
    }

    override suspend fun updatePassword(currentPassword: String, newPassword: String): Result<Unit> = runCatching {
        val email = currentUserFlow.value?.email
            ?: supabase.auth.currentUserOrNull()?.email
            ?: throw IllegalStateException("No active account found. Please sign in again.")

        // Verify current credentials
        supabase.auth.signInWith(Email) {
            this.email = email
            this.password = currentPassword
        }

        val token = supabase.auth.currentAccessTokenOrNull()
            ?: throw IllegalStateException("Authentication session expired.")

        val response = supabase.httpClient.put("${supabase.supabaseHttpUrl}/auth/v1/user") {
            header("apikey", supabase.supabaseKey)
            header("Authorization", "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(
                buildJsonObject {
                    put("password", newPassword.trim())
                    put("current_password", currentPassword)
                }.toString()
            )
        }

        if (!response.status.isSuccess()) {
            val errorText = response.bodyAsText()
            val errorMsg = parseErrorMessage(errorText)
            throw IllegalStateException(errorMsg)
        }
        Unit
    }

    private fun parseErrorMessage(errorBody: String): String {
        return try {
            val json = Json.parseToJsonElement(errorBody).jsonObject
            json["msg"]?.jsonPrimitive?.contentOrNull
                ?: json["message"]?.jsonPrimitive?.contentOrNull
                ?: json["error_description"]?.jsonPrimitive?.contentOrNull
                ?: errorBody
        } catch (_: Exception) {
            errorBody
        }
    }
}
