package com.example.expensetracker.presentation.dashboard

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expensetracker.Constants
import com.example.expensetracker.R
import com.example.expensetracker.data.local.entity.TransactionWithCategory
import com.example.expensetracker.domain.model.CurrencyHelper.formatAmount
import com.example.expensetracker.domain.model.getCategoryIcon
import com.example.expensetracker.ui.theme.ExpenseTrackerTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun TransactionItem(
    transaction: TransactionWithCategory,
    currencySymbol: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extendedColors = ExpenseTrackerTheme.extendedColors
    var isMenuExpanded by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val isIncome = transaction.type == Constants.TransactionType.INCOME
    val accentColor = if (isIncome) extendedColors.emeraldIncome else extendedColors.crimsonExpense
    val iconBg = if (isIncome) Color(0x2210B981) else Color(0x22F43F5E)

    if (showDeleteDialog) {
        DeleteConfirmationDialog(
            onConfirm = {
                showDeleteDialog = false
                onDelete()
            },
            onDismiss = {
                showDeleteDialog = false
            }
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = extendedColors.cardBackground
        ),
        border = BorderStroke(1.dp, extendedColors.cardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Circular Avatar Badge with Category Icon
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = getCategoryIcon(transaction.categoryIcon)),
                    contentDescription = transaction.categoryName,
                    tint = accentColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Note / Category & Humanized Timestamp
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = transaction.note.ifBlank { transaction.categoryName },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = extendedColors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = buildString {
                        append(formatTransactionDate(transaction.date))
                        if (transaction.note.isNotBlank() && transaction.categoryName.isNotBlank()) {
                            append(" • ")
                            append(transaction.categoryName)
                        }
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = extendedColors.textMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Amount & Type Pill
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "${if (isIncome) "+" else "-"} $currencySymbol ${formatAmount(transaction.amount)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = accentColor,
                    letterSpacing = (-0.3).sp
                )

                Surface(
                    color = if (isIncome) Color(0x1810B981) else Color(0x18F43F5E),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (isIncome) "Deposit" else "Expense",
                        style = MaterialTheme.typography.labelSmall,
                        color = accentColor,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // Contextual Menu
            Box {
                IconButton(
                    onClick = { isMenuExpanded = true },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_more_vert),
                        contentDescription = "More options",
                        modifier = Modifier.size(18.dp),
                        tint = extendedColors.textSecondary
                    )
                }

                DropdownMenu(
                    expanded = isMenuExpanded,
                    onDismissRequest = { isMenuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit") },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_edit),
                                contentDescription = "Edit"
                            )
                        },
                        onClick = {
                            isMenuExpanded = false
                            onEdit()
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "Delete",
                                color = MaterialTheme.colorScheme.error
                            )
                        },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_delete),
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error
                            )
                        },
                        onClick = {
                            isMenuExpanded = false
                            showDeleteDialog = true
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun DeleteConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_delete),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = { Text(text = "Delete Transaction", fontWeight = FontWeight.Bold) },
        text = { Text(text = "Are you sure you want to remove this transaction? Your balance will be recalculated automatically.") },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Delete", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

fun formatTransactionDate(timestamp: Long): String {
    val date = Date(timestamp)
    val today = Calendar.getInstance()
    val transactionCal = Calendar.getInstance().apply { time = date }

    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault()).format(date)

    return when {
        isSameDay(today, transactionCal) -> "Today, $timeFormat"
        isYesterday(transactionCal) -> "Yesterday, $timeFormat"
        else -> SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(date)
    }
}

private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

private fun isYesterday(other: Calendar): Boolean {
    val yesterday = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, -1)
    }
    return isSameDay(yesterday, other)
}

@Preview(name = "Transaction Item - Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Transaction Item - Light", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
private fun TransactionItemPreview() {
    ExpenseTrackerTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TransactionItem(
                transaction = TransactionWithCategory(
                    id = "1",
                    ledgerId = "ledger_default",
                    categoryId = "cat_food",
                    categoryIcon = "restaurant",
                    categoryName = "Food & Dining",
                    amount = 46.70,
                    type = "EXPENSE",
                    note = "Dinner with team",
                    date = System.currentTimeMillis(),
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                    isDeleted = false
                ),
                currencySymbol = "$",
                onEdit = {},
                onDelete = {},
                onClick = {}
            )
            TransactionItem(
                transaction = TransactionWithCategory(
                    id = "2",
                    ledgerId = "ledger_default",
                    categoryId = "cat_salary",
                    categoryIcon = "salary",
                    categoryName = "Salary",
                    amount = 3450.00,
                    type = "INCOME",
                    note = "Monthly Client Retainer",
                    date = System.currentTimeMillis(),
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                    isDeleted = false
                ),
                currencySymbol = "$",
                onEdit = {},
                onDelete = {},
                onClick = {}
            )
        }
    }
}
