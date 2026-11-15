package com.example.expensetracker.data.remote.session

import android.content.Context
import io.github.jan.supabase.auth.SessionManager
import io.github.jan.supabase.auth.user.UserSession
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class SharedPreferencesSessionManager(
    context: Context,
    private val key: String = "supabase_session"
) : SessionManager {

    private val prefs = context.getSharedPreferences("supabase_auth_prefs", Context.MODE_PRIVATE)
    private val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    override suspend fun saveSession(session: UserSession) {
        val serialized = json.encodeToString(session)
        prefs.edit().putString(key, serialized).apply()
    }

    override suspend fun loadSession(): UserSession? {
        val serialized = prefs.getString(key, null) ?: return null
        return try {
            json.decodeFromString<UserSession>(serialized)
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun deleteSession() {
        prefs.edit().remove(key).apply()
    }
}
