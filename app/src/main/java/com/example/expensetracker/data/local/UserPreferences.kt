package com.example.expensetracker.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.expensetracker.Constants
import com.example.expensetracker.domain.model.CurrencyHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore by preferencesDataStore(name = "user_preferences")

@Singleton
class UserPreferences @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    companion object {
        private val CURRENCY_CODE = stringPreferencesKey(Constants.Preferences.CURRENCY_CODE)
        private val THEME_MODE = stringPreferencesKey("theme_mode")
        private val LAST_SYNC_TIME = longPreferencesKey("last_sync_time")
    }

    val lastSyncTime: Flow<Long?> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[LAST_SYNC_TIME]
        }

    suspend fun setLastSyncTime(timeMillis: Long) {
        context.dataStore.edit { preferences ->
            preferences[LAST_SYNC_TIME] = timeMillis
        }
    }

    suspend fun clearLastSyncTime() {
        context.dataStore.edit { preferences ->
            preferences.remove(LAST_SYNC_TIME)
        }
    }

    val currencyCode: Flow<String> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[CURRENCY_CODE] ?: CurrencyHelper.getDefaultCurrencyCode(context)
        }

    val themeMode: Flow<String> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[THEME_MODE] ?: Constants.ThemeMode.DARK
        }

    suspend fun setCurrencyCode(code: String) {
        context.dataStore.edit { preferences ->
            preferences[CURRENCY_CODE] = code
        }
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[THEME_MODE] = mode
        }
    }
}
