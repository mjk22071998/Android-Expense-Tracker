package com.example.expensetracker.domain.model

import android.content.Context
import android.telephony.TelephonyManager
import org.joda.money.CurrencyUnit
import org.joda.money.IllegalCurrencyException
import org.joda.money.Money
import java.math.BigDecimal
import java.math.RoundingMode
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
        CurrencyUnit.registeredCurrencies()
            .map { unit ->
                val displayName = try {
                    Currency.getInstance(unit.code).displayName
                } catch (e: Exception) {
                    unit.code
                }
                CurrencyModel(
                    code = unit.code,
                    symbol = getSymbol(unit.code),
                    name = displayName
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

    /**
     * Converts an amount and currency code into an industry-tested, immutable [Money] object.
     * Uses [RoundingMode.HALF_UP] to match the ISO-4217 standard decimal places.
     */
    fun toMoney(amount: Double, currencyCode: String): Money {
        val unit = getCurrencyUnit(currencyCode)
        val bd = BigDecimal.valueOf(amount).setScale(unit.decimalPlaces, RoundingMode.HALF_UP)
        return Money.of(unit, bd)
    }

    /**
     * Returns the ISO-4217 [CurrencyUnit] for a given 3-letter currency code,
     * falling back to USD if the code is invalid or unparseable.
     */
    fun getCurrencyUnit(currencyCode: String): CurrencyUnit {
        return try {
            CurrencyUnit.of(currencyCode.uppercase().trim())
        } catch (e: Exception) {
            CurrencyUnit.USD
        }
    }

    /**
     * Checks if a currency code is a valid registered ISO-4217 currency.
     */
    fun isValidCurrency(currencyCode: String): Boolean {
        return try {
            CurrencyUnit.of(currencyCode.uppercase().trim())
            true
        } catch (e: IllegalCurrencyException) {
            false
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Returns the standard ISO-4217 decimal places for a currency (e.g. 2 for USD, 0 for JPY, 3 for KWD).
     */
    fun getDecimalPlaces(currencyCode: String): Int {
        return getCurrencyUnit(currencyCode).decimalPlaces
    }

    fun formatAmount(amount: Double): String {
        return String.format(Locale.getDefault(), "%,.2f", amount)
    }

    fun formatCleanAmount(amount: Double): String {
        // Mathematically exact integer check via BigDecimal to prevent IEEE 754 float drift
        val bd = BigDecimal.valueOf(amount).setScale(2, RoundingMode.HALF_UP)
        val hasNoCents = bd.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) == 0
        return if (hasNoCents) {
            String.format(Locale.getDefault(), "%,d", bd.toLong())
        } else {
            String.format(Locale.getDefault(), "%,.2f", bd.toDouble())
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
        if (upperCode.isBlank()) return "$"
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
            try {
                CurrencyUnit.of(upperCode).symbol
            } catch (ignored: Exception) {
                upperCode
            }
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
