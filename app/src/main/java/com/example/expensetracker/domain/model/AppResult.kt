package com.example.expensetracker.domain.model

import kotlinx.coroutines.CancellationException

sealed class AppResult<out T> {
    data class Success<out T>(val data: T) : AppResult<T>()
    data class Error(val error: UiError) : AppResult<Nothing>()

    inline fun onSuccess(action: (T) -> Unit): AppResult<T> {
        if (this is Success) action(data)
        return this
    }

    inline fun onError(action: (UiError) -> Unit): AppResult<T> {
        if (this is Error) action(error)
        return this
    }
}

fun Throwable.toUiError(): UiError {
    val simpleName = this::class.simpleName ?: ""
    return when {
        simpleName.contains("SQLiteConstraint") ->
            UiError.Database("This action conflicts with existing data and could not be completed.")
        simpleName.contains("SQLite") ->
            UiError.Database()
        else ->
            UiError.Unknown(message ?: "Something unexpected happened. Please try again.")
    }
}

suspend fun <T> safeCall(block: suspend () -> T): AppResult<T> {
    return try {
        AppResult.Success(block())
    } catch (e: CancellationException) {
        throw e // never swallow cancellation — see note below
    } catch (e: Exception) {
        AppResult.Error(e.toUiError())
    }
}