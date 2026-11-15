package com.example.expensetracker.di

import com.example.expensetracker.util.MainDispatcherRule
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.MemoryCodeVerifierCache
import io.github.jan.supabase.auth.MemorySessionManager
import io.github.jan.supabase.postgrest.Postgrest
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test

class SupabaseModuleTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun provideSupabaseClient_installsAuthAndPostgrestPlugins() {
        val client = SupabaseModule.provideSupabaseClient(
            sessionManager = MemorySessionManager(),
            codeVerifierCache = MemoryCodeVerifierCache()
        )
        assertNotNull("SupabaseClient must not be null", client)
        assertNotNull("Auth plugin must be installed", client.pluginManager.getPluginOrNull(Auth))
        assertNotNull("Postgrest plugin must be installed", client.pluginManager.getPluginOrNull(Postgrest))
    }
}
