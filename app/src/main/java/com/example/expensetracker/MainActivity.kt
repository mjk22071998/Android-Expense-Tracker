package com.example.expensetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.example.expensetracker.data.local.UserPreferences
import com.example.expensetracker.presentation.navigation.NavGraph
import com.example.expensetracker.ui.theme.ExpenseTrackerTheme
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var userPreferences: UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen() // ← must be called BEFORE super.onCreate()
        super.onCreate(savedInstanceState)

        var isReady = false

        // Keep splash visible until our first data load completes
        splashScreen.setKeepOnScreenCondition { !isReady }

        setContent {
            val themeMode by userPreferences.themeMode.collectAsStateWithLifecycle(
                initialValue = Constants.ThemeMode.SYSTEM
            )

            val darkTheme = when (themeMode) {
                Constants.ThemeMode.LIGHT -> false
                Constants.ThemeMode.DARK -> true
                else -> isSystemInDarkTheme()
            }

            // Mark ready once we've read the theme preference at least once
            isReady = true

            ExpenseTrackerTheme(darkTheme = darkTheme) {
                val navController = rememberNavController()
                NavGraph(navController = navController)
            }
        }
    }
}