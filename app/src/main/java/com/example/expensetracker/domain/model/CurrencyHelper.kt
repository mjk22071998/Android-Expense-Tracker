package com.example.expensetracker.domain.model

import android.content.Context
import android.telephony.TelephonyManager
import java.util.Currency
import java.util.Locale

object CurrencyHelper {

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
            val simCountry = getSimCountry(context)
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

    fun getSymbol(currencyCode: String, context: Context): String {
        return try {
            val currency = Currency.getInstance(currencyCode)
            val simCountry = getSimCountry(context)

            val locale = if (simCountry.isNotEmpty()) {
                Locale.Builder().setRegion(simCountry).build()
            } else {
                Locale.getDefault()
            }

            currency.getSymbol(locale)
        } catch (e: Exception) {
            currencyCode
        }
    }

    private fun getSimCountry(context: Context): String {
        return try {
            val telephonyManager = context
                .getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            telephonyManager.simCountryIso.uppercase()
        } catch (e: Exception) {
            ""
        }
    }
}