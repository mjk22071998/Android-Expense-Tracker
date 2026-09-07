package com.example.expensetracker.domain.model

import com.example.expensetracker.R

val categoryIconOptions: List<String> = listOf(
    "restaurant", "transport", "shopping", "groceries", "health",
    "entertainment", "subscription", "travel", "fitness", "fuel",
    "utilities", "rent", "bills",
    "salary", "investment", "savings",
    "education", "gift", "pet", "family", "charity",
    "category", "other"
)

fun getCategoryIcon(iconName: String): Int {
    return when (iconName) {
        "restaurant"    -> R.drawable.ic_restaurant
        "transport"     -> R.drawable.ic_directions_car
        "shopping"      -> R.drawable.ic_shopping_cart
        "groceries"     -> R.drawable.ic_local_grocery_store
        "health"        -> R.drawable.ic_local_hospital
        "entertainment" -> R.drawable.ic_movie
        "subscription"  -> R.drawable.ic_subscriptions
        "travel"        -> R.drawable.ic_flight
        "fitness"       -> R.drawable.ic_fitness_center
        "fuel"          -> R.drawable.ic_local_gas_station
        "utilities"     -> R.drawable.ic_electric_bolt
        "rent"          -> R.drawable.ic_home
        "bills"         -> R.drawable.ic_receipt_long
        "salary"        -> R.drawable.ic_work
        "investment"    -> R.drawable.ic_trending_up
        "savings"       -> R.drawable.ic_savings
        "education"     -> R.drawable.ic_school
        "gift"          -> R.drawable.ic_redeem
        "pet"           -> R.drawable.ic_pets
        "family"        -> R.drawable.ic_child_care
        "charity"       -> R.drawable.ic_volunteer_activism
        "category"      -> R.drawable.ic_category
        else            -> R.drawable.ic_more_horiz
    }
}