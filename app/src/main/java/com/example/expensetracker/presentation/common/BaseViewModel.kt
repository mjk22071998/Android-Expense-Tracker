package com.example.expensetracker.presentation.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.domain.model.UiError
import com.example.expensetracker.domain.model.toUiError
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

abstract class BaseViewModel : ViewModel() {

    /**
     * Repository calls already return AppResult and handle their own exceptions.
     * This handler is the last line of defense for anything that slips past that —
     * e.g. an exception thrown directly during Flow collection, or a bug in the
     * ViewModel's own transformation logic.
     */
    protected fun launchSafely(
        onError: (UiError) -> Unit = {},
        block: suspend CoroutineScope.() -> Unit
    ) {
        val handler = CoroutineExceptionHandler { _, throwable ->
            onError(throwable.toUiError())
        }
        viewModelScope.launch(handler) {
            block()
        }
    }
}