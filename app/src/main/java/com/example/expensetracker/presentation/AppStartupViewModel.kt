package com.example.expensetracker.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.data.local.CategorySeeder
import com.example.expensetracker.data.local.LedgerSeeder
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class AppStartupViewModel @Inject constructor(
    private val categorySeeder: CategorySeeder,
    private val ledgerSeeder: LedgerSeeder
) : ViewModel() {

    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    init {
        viewModelScope.launch {
            coroutineScope {
                val minDisplayTime = async { delay(600.milliseconds) } // avoids a jarring flash
                val seeding = async {
                    categorySeeder.seedIfNeeded()
                    ledgerSeeder.seedIfNeeded()
                }
                seeding.await()
                minDisplayTime.await()
            }
            _isReady.value = true
        }
    }
}