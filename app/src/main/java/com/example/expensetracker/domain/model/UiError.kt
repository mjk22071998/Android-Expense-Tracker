package com.example.expensetracker.domain.model

sealed class UiError {
    abstract val message: String

    data class Database(
        override val message: String = "Something went wrong saving your data. Please try again."
    ) : UiError()

    data class NotFound(
        override val message: String = "The item you're looking for could not be found."
    ) : UiError()

    data class Validation(
        override val message: String
    ) : UiError()

    data class Network(
        override val message: String = "No internet connection. Changes will sync once you're back online."
    ) : UiError()

    data class Unknown(
        override val message: String = "Something unexpected happened. Please try again."
    ) : UiError()
}