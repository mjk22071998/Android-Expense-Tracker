package com.example.expensetracker.presentation.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.domain.model.UiError
import com.example.expensetracker.domain.model.toUiError
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

abstract class BaseViewModel : ViewModel() {

    protected fun launchSafely(
        onError: (UiError) -> Unit = {},
        block: suspend CoroutineScope.() -> Unit
    ): Job {
        val handler = CoroutineExceptionHandler { _, throwable ->
            onError(throwable.toUiError())
        }
        return viewModelScope.launch(handler) {
            block()
        }
    }
}