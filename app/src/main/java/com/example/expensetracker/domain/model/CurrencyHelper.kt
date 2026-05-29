package com.example.expensetracker.domain.model

import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

object  CurrencyHelper {
    val allCurrencies: List<CurrencyModel> by lazy {
        Currency.getAvailableCurrencies()
            .map { currency ->
                CurrencyModel(
                    code = currency.currencyCode,
                    symbol = currency.symbol,
                    name = currency.displayName
                )
            }
            .sortedBy { it.code }
    }

    fun search(query: String): List<CurrencyModel> {
        if (query.isBlank()) return allCurrencies
        val q = query.uppercase().trim()
        return allCurrencies.filter { currency ->
            currency.code.contains(q) ||
                    currency.name.uppercase().contains(q)
        }
    }

    fun formatAmount(amount: Double, currencyCode: String): String {
        return try {
            val currency = Currency.getInstance(currencyCode)
            val formatter = NumberFormat.getCurrencyInstance()
            formatter.currency = currency
            formatter.format(amount)
        } catch (e: Exception) {
            "$currencyCode $amount"
        }
    }

    fun getDefaultCurrencyCode(): String {
        return try {
            Currency.getInstance(Locale.getDefault()).currencyCode
        } catch (e: Exception) {
            "PKR"
        }
    }

    fun getSymbol(currencyCode: String): String {
        return try {
            Currency.getInstance(currencyCode).symbol
        } catch (e: Exception) {
            currencyCode
        }
    }
}