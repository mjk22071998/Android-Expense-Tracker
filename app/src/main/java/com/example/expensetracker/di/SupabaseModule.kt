package com.example.expensetracker.di

import android.content.Context
import com.example.expensetracker.BuildConfig
import com.example.expensetracker.data.remote.session.SharedPreferencesSessionManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.CodeVerifierCache
import io.github.jan.supabase.auth.MemoryCodeVerifierCache
import io.github.jan.supabase.auth.MemorySessionManager
import io.github.jan.supabase.auth.SessionManager
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SupabaseModule {

    @Provides
    @Singleton
    fun provideSessionManager(
        @ApplicationContext context: Context
    ): SessionManager {
        return SharedPreferencesSessionManager(context)
    }

    @Provides
    @Singleton
    fun provideCodeVerifierCache(): CodeVerifierCache {
        return MemoryCodeVerifierCache()
    }

    @Provides
    @Singleton
    fun provideSupabaseClient(
        sessionManager: SessionManager = MemorySessionManager(),
        codeVerifierCache: CodeVerifierCache = MemoryCodeVerifierCache()
    ): SupabaseClient {
        return createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_ANON_KEY
        ) {
            install(Auth) {
                this.sessionManager = sessionManager
                this.codeVerifierCache = codeVerifierCache
            }
            install(Postgrest)
        }
    }
}
