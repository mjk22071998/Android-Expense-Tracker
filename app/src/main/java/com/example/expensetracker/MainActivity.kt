package com.example.expensetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.example.expensetracker.data.local.UserPreferences
import com.example.expensetracker.presentation.AppStartupViewModel
import com.example.expensetracker.presentation.SplashOverlay
import com.example.expensetracker.presentation.navigation.NavGraph
import com.example.expensetracker.ui.theme.ExpenseTrackerTheme
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var userPreferences: UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            val startupViewModel: AppStartupViewModel = hiltViewModel()
            val isReady by startupViewModel.isReady.collectAsStateWithLifecycle()

            val themeMode by userPreferences.themeMode.collectAsStateWithLifecycle(
                initialValue = Constants.ThemeMode.DARK
            )
            val darkTheme = when (themeMode) {
                Constants.ThemeMode.LIGHT -> false
                Constants.ThemeMode.DARK -> true
                else -> true // default to dark Organic Forest theme
            }

            DisposableEffect(darkTheme) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(
                        android.graphics.Color.TRANSPARENT,
                        android.graphics.Color.TRANSPARENT
                    ) { darkTheme },
                    navigationBarStyle = SystemBarStyle.auto(
                        android.graphics.Color.TRANSPARENT,
                        android.graphics.Color.TRANSPARENT
                    ) { darkTheme }
                )
                onDispose {}
            }

            ExpenseTrackerTheme(darkTheme = darkTheme) {
                if (isReady) {
                    val navController = rememberNavController()
                    NavGraph(navController = navController)
                } else {
                    SplashOverlay()
                }
            }
        }
    }
}