package com.example.expensetracker.domain.util

data class PasswordValidationResult(
    val hasMinLength: Boolean,
    val hasUppercase: Boolean,
    val hasLowercase: Boolean,
    val hasDigit: Boolean,
    val hasSymbol: Boolean
) {
    val isValid: Boolean
        get() = hasMinLength && hasUppercase && hasLowercase && hasDigit && hasSymbol

    val satisfiedCount: Int
        get() = listOf(hasMinLength, hasUppercase, hasLowercase, hasDigit, hasSymbol).count { it }

    val totalCount: Int
        get() = 5
}

object PasswordValidator {
    const val MIN_LENGTH = 8

    fun validate(password: String): PasswordValidationResult {
        return PasswordValidationResult(
            hasMinLength = password.length >= MIN_LENGTH,
            hasUppercase = password.any { it.isUpperCase() },
            hasLowercase = password.any { it.isLowerCase() },
            hasDigit = password.any { it.isDigit() },
            hasSymbol = password.any { !it.isLetterOrDigit() && !it.isWhitespace() }
        )
    }
}
