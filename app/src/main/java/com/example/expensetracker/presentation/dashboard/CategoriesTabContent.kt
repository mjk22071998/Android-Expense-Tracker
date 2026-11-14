package com.example.expensetracker.presentation.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expensetracker.Constants
import com.example.expensetracker.R
import com.example.expensetracker.data.local.entity.Category
import com.example.expensetracker.domain.model.CurrencyHelper.formatAmount
import com.example.expensetracker.domain.model.getCategoryIcon
import com.example.expensetracker.ui.theme.ElectricLime
import com.example.expensetracker.ui.theme.ElectricLimeOn
import com.example.expensetracker.ui.theme.ExpenseTrackerTheme
import com.example.expensetracker.ui.theme.ForestBorderDark
import com.example.expensetracker.ui.theme.ForestCardDark

private enum class CategoryFilterTab {
    ALL,
    EXPENSES,
    INCOME
}

private fun formatRelativeTime(dateMillis: Long, now: Long = System.currentTimeMillis()): String {
    if (dateMillis <= 0L) return "never"
    val diff = (now - dateMillis).coerceAtLeast(0L)
    val days = (diff / (24L * 60 * 60 * 1000)).toInt()
    return when {
        days == 0 -> "today"
        days == 1 -> "yesterday"
        days < 7 -> "$days days ago"
        days < 14 -> "1 week ago"
        days < 30 -> "${days / 7} weeks ago"
        days < 60 -> "1 month ago"
        days < 365 -> "${days / 30} months ago"
        else -> "over a year ago"
    }
}

@Composable
fun CategoriesTabContent(
    allCategories: List<Category>,
    selectedCategoryIds: Set<String>,
    categoryUsageMap: Map<String, CategoryUsage>,
    currencySymbol: String,
    onCategoryToggled: (String) -> Unit,
    onClearFilters: () -> Unit,
    onApplyFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extendedColors = ExpenseTrackerTheme.extendedColors
    var selectedTab by remember { mutableStateOf(CategoryFilterTab.ALL) }

    val filteredCategories = remember(allCategories, selectedTab) {
        when (selectedTab) {
            CategoryFilterTab.ALL -> allCategories
            CategoryFilterTab.EXPENSES -> allCategories.filter { it.transactionType == Constants.TransactionType.EXPENSE }
            CategoryFilterTab.INCOME -> allCategories.filter { it.transactionType == Constants.TransactionType.INCOME }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Segmented Tab Filter: All | Expenses | Income
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = if (extendedColors.isDark) ForestCardDark else extendedColors.cardElevated,
            border = BorderStroke(1.dp, if (extendedColors.isDark) ForestBorderDark else extendedColors.cardBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                CategoryFilterTab.values().forEach { tab ->
                    val isSelected = selectedTab == tab
                    val label = when (tab) {
                        CategoryFilterTab.ALL -> "All (${allCategories.size})"
                        CategoryFilterTab.EXPENSES -> "Expenses"
                        CategoryFilterTab.INCOME -> "Income"
                    }
                    val bg = if (isSelected) {
                        if (extendedColors.isDark) ElectricLime else Color(0xFF1B4D34)
                    } else Color.Transparent
                    val textColor = if (isSelected) {
                        if (extendedColors.isDark) ElectricLimeOn else Color.White
                    } else {
                        if (extendedColors.isDark) Color(0xFFA5C4B2) else extendedColors.textSecondary
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(bg)
                            .clickable { selectedTab = tab },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = textColor
                        )
                    }
                }
            }
        }

        // Active filter notification and reset banner
        if (selectedCategoryIds.isNotEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = if (extendedColors.isDark) Color(0x22D4F63D) else Color(0x1F1B4D34),
                border = BorderStroke(1.dp, if (extendedColors.isDark) ElectricLime else Color(0xFF1B4D34))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Filtered by ${selectedCategoryIds.size} categories",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = if (extendedColors.isDark) ElectricLime else Color(0xFF1B4D34)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = onClearFilters) {
                            Text(
                                text = "Clear",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = extendedColors.crimsonExpense
                            )
                        }
                        Button(
                            onClick = onApplyFilters,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (extendedColors.isDark) ElectricLime else Color(0xFF1B4D34),
                                contentColor = if (extendedColors.isDark) ElectricLimeOn else Color.White
                            )
                        ) {
                            Text(
                                text = "Apply",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Category List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredCategories, key = { it.id }) { category ->
                val isChecked = category.id in selectedCategoryIds
                val usage = categoryUsageMap[category.id] ?: CategoryUsage()
                val count = usage.transactionCount
                val totalAmount = usage.totalAmount
                val lastDate = usage.lastTransactionDate
                val isExpense = category.transactionType == Constants.TransactionType.EXPENSE

                val subtitleText = if (count == 0 || lastDate == null) {
                    "Never used"
                } else {
                    val relativeTime = formatRelativeTime(lastDate)
                    val txPlural = if (count == 1) "1 transaction" else "$count transactions"
                    "$txPlural • Last $relativeTime • $currencySymbol ${formatAmount(totalAmount)}"
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onCategoryToggled(category.id) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isChecked) {
                            if (extendedColors.isDark) Color(0x26D4F63D) else Color(0x1F1B4D34)
                        } else {
                            if (extendedColors.isDark) ForestCardDark else extendedColors.cardElevated
                        }
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (isChecked) {
                            if (extendedColors.isDark) ElectricLime else Color(0xFF1B4D34)
                        } else {
                            if (extendedColors.isDark) ForestBorderDark else extendedColors.cardBorder
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Category Icon Avatar
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isExpense) Color(0x22FF5252)
                                    else (if (extendedColors.isDark) Color(0x22D4F63D) else Color(0x1F1B4D34))
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = getCategoryIcon(category.icon)),
                                contentDescription = null,
                                tint = if (isExpense) Color(0xFFFF5252)
                                       else (if (extendedColors.isDark) ElectricLime else Color(0xFF1B4D34)),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Category Details
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = category.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (extendedColors.isDark) Color.White else extendedColors.textPrimary
                                )

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isExpense) Color(0x22FF5252) else Color(0x22D4F63D)
                                ) {
                                    Text(
                                        text = if (isExpense) "Expense" else "Income",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 10.sp,
                                        color = if (isExpense) Color(0xFFFF5252) else ElectricLime,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = subtitleText,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (extendedColors.isDark) Color(0xFFA5C4B2) else extendedColors.textSecondary
                            )
                        }

                        // Checkbox to toggle category filter
                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = { onCategoryToggled(category.id) },
                            colors = CheckboxDefaults.colors(
                                checkedColor = if (extendedColors.isDark) ElectricLime else Color(0xFF1B4D34),
                                checkmarkColor = if (extendedColors.isDark) ElectricLimeOn else Color.White,
                                uncheckedColor = if (extendedColors.isDark) Color(0x66A5C4B2) else extendedColors.textSecondary
                            )
                        )
                    }
                }
            }

            // Bottom space for floating bottom bar
            item {
                Spacer(modifier = Modifier.height(84.dp))
            }
        }
    }
}
