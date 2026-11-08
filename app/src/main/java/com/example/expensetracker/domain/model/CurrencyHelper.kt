package com.example.expensetracker.domain.model

import android.content.Context
import android.telephony.TelephonyManager
import java.util.Currency
import java.util.Locale

object CurrencyHelper {

    // Colloquial local symbols where standard Java returns 3-letter codes or cumbersome prefixes
    private val OVERRIDE_SYMBOLS = mapOf(
        "PKR" to "Rs",
        "INR" to "₹",
        "USD" to "$",
        "CAD" to "$",
        "AUD" to "$",
        "NZD" to "$",
        "SGD" to "$",
        "HKD" to "$",
        "EUR" to "€",
        "GBP" to "£",
        "JPY" to "¥",
        "CNY" to "¥",
        "BDT" to "৳",
        "LKR" to "Rs",
        "NPR" to "Rs",
        "RUB" to "₽",
        "TRY" to "₺",
        "BRL" to "R$",
        "PHP" to "₱",
        "KRW" to "₩",
        "VND" to "₫",
        "THB" to "฿",
        "MYR" to "RM",
        "IDR" to "Rp",
        "ZAR" to "R",
        "NGN" to "₦"
    )

    val allCurrencies: List<CurrencyModel> by lazy {
        Currency.getAvailableCurrencies()
            .map { currency ->
                CurrencyModel(
                    code = currency.currencyCode,
                    symbol = getSymbol(currency.currencyCode),
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
                    currency.name.uppercase().contains(q) ||
                    currency.symbol.uppercase().contains(q)
        }
    }

    fun formatAmount(amount: Double): String {
        return String.format(Locale.getDefault(), "%,.2f", amount)
    }

    fun formatCleanAmount(amount: Double): String {
        return if (amount % 1.0 == 0.0) {
            String.format(Locale.getDefault(), "%,d", amount.toLong())
        } else {
            String.format(Locale.getDefault(), "%,.2f", amount)
        }
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

    fun getSymbol(currencyCode: String, context: Context? = null): String {
        val upperCode = currencyCode.uppercase().trim()
        OVERRIDE_SYMBOLS[upperCode]?.let { return it }

        return try {
            val currency = Currency.getInstance(upperCode)
            val simCountry = context?.let { getSimCountry(it) }.orEmpty()
            val locale = if (simCountry.isNotEmpty()) {
                Locale.Builder().setRegion(simCountry).build()
            } else {
                Locale.getDefault()
            }

            val symbol = currency.getSymbol(locale)
            if (symbol == upperCode) {
                currency.symbol
            } else {
                symbol
            }
        } catch (e: Exception) {
            upperCode
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
