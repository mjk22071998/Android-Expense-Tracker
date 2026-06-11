package com.example.expensetracker.domain.model

import android.content.Context
import android.telephony.TelephonyManager
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

    fun formatAmount(amount: Double): String {
        return String.format(Locale.getDefault(), "%,.2f", amount)
    }

    fun getDefaultCurrencyCode(context: Context): String {
        return try {
            val telephonyManager = context
                .getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val simCountry = telephonyManager.simCountryIso.uppercase()
            if (simCountry.isNotEmpty()) {
                val locale = Locale.Builder()
                    .setRegion(simCountry)
                    .build()
                Currency.getInstance(locale).currencyCode
            } else {
                Currency.getInstance(Locale.getDefault()).currencyCode
            }
        } catch (e: Exception) {
            "PKR"
        }
    }

    fun getSymbol(currencyCode: String): String {
        return try {
            val currency = Currency.getInstance(currencyCode)
            // Build locale using SIM country for accurate symbol
            val simLocale = Locale.Builder()
                .setRegion(currencyCode.take(2)) // "PK" from "PKR"
                .build()
            val symbol = currency.getSymbol(simLocale)
            // If symbol still equals code, it means locale didn't help
            if (symbol == currencyCode) currency.symbol else symbol
        } catch (e: Exception) {
            currencyCode
        }
    }
}