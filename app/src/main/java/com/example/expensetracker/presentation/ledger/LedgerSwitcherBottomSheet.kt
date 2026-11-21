package com.example.expensetracker.presentation.ledger

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import com.example.expensetracker.R
import com.example.expensetracker.data.local.entity.Ledger
import com.example.expensetracker.domain.model.CurrencyHelper
import com.example.expensetracker.ui.theme.ElectricLime
import com.example.expensetracker.ui.theme.ElectricLimeOn
import com.example.expensetracker.ui.theme.ExpenseTrackerTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LedgerSwitcherBottomSheet(
    activeLedger: Ledger?,
    allLedgers: List<Ledger>,
    onSelectLedger: (String) -> Unit,
    onCreateLedgerClick: () -> Unit,
    onEditLedgerClick: (Ledger) -> Unit,
    onDeleteLedgerClick: (Ledger) -> Unit,
    onDismiss: () -> Unit
) {
    val extendedColors = ExpenseTrackerTheme.extendedColors
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = extendedColors.cardBackground,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .size(width = 40.dp, height = 4.dp)
                    .clip(CircleShape)
                    .background(extendedColors.cardBorder)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Account Ledgers",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = extendedColors.textPrimary
                    )
                    Text(
                        text = "Switch active book or manage separate finances",
                        style = MaterialTheme.typography.labelSmall,
                        color = extendedColors.textMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Scrollable List of Ledgers
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 380.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(allLedgers, key = { it.id }) { ledger ->
                    val isActive = ledger.id == activeLedger?.id

                    LedgerItemCard(
                        ledger = ledger,
                        isActive = isActive,
                        onSelect = {
                            onSelectLedger(ledger.id)
                            onDismiss()
                        },
                        onEdit = { onEditLedgerClick(ledger) },
                        onDelete = { onDeleteLedgerClick(ledger) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Action Button: + Create New Ledger
            Button(
                onClick = {
                    onDismiss()
                    onCreateLedgerClick()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricLime,
                    contentColor = ElectricLimeOn
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_add),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Create New Ledger",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(18.dp))
        }
    }
}

@Composable
private fun LedgerItemCard(
    ledger: Ledger,
    isActive: Boolean,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val extendedColors = ExpenseTrackerTheme.extendedColors
    var showMenu by remember { mutableStateOf(false) }

    Surface(
        onClick = onSelect,
        shape = RoundedCornerShape(18.dp),
        color = if (isActive) {
            if (extendedColors.isDark) Color(0x33489B5F) else Color(0x1A489B5F)
        } else {
            extendedColors.cardBackground
        },
        border = BorderStroke(
            width = if (isActive) 1.5.dp else 1.dp,
            color = if (isActive) extendedColors.emeraldIncome else extendedColors.cardBorder
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Leading Wallet Icon Badge
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(
                        if (isActive) extendedColors.emeraldIncome.copy(alpha = 0.2f)
                        else if (extendedColors.isDark) Color(0x1AFFFFFF)
                        else Color(0x0D000000)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_account_balance_wallet),
                    contentDescription = null,
                    tint = if (isActive) extendedColors.emeraldIncome
                    else if (extendedColors.isDark) Color.White
                    else extendedColors.textSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Center details (Name, Default tag, Currency, Balance)
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = ledger.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.SemiBold,
                        color = extendedColors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (ledger.isDefault) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (extendedColors.isDark) Color(0x28FFFFFF) else Color(0x14000000),
                            modifier = Modifier.padding(bottom = 1.dp)
                        ) {
                            Text(
                                text = "Default",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                fontWeight = FontWeight.Medium,
                                color = extendedColors.textSecondary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "• ${ledger.currencyCode}",
                        style = MaterialTheme.typography.labelSmall,
                        color = extendedColors.textMuted
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                val symbol = CurrencyHelper.getSymbol(ledger.currencyCode)
                val formattedBalance = "$symbol ${CurrencyHelper.formatAmount(ledger.balance)}"
                Text(
                    text = "Balance: $formattedBalance",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (ledger.balance >= 0) extendedColors.emeraldIncome
                    else extendedColors.crimsonExpense,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Right side: Active Checkmark or Radio Indicator
            if (isActive) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(ElectricLime),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_check),
                        contentDescription = "Active",
                        tint = ElectricLimeOn,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Overflow Options Menu (Edit / Delete)
            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_more_vert),
                        contentDescription = "Ledger Options",
                        tint = extendedColors.textMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit Ledger", color = extendedColors.textPrimary) },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_edit),
                                contentDescription = null,
                                tint = extendedColors.textSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        onClick = {
                            showMenu = false
                            onEdit()
                        }
                    )

                    if (!ledger.isDefault) {
                        DropdownMenuItem(
                            text = { Text("Delete Ledger", color = extendedColors.crimsonExpense) },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_delete),
                                    contentDescription = null,
                                    tint = extendedColors.crimsonExpense,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            onClick = {
                                showMenu = false
                                onDelete()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Preview(name = "Ledger Switcher Bottom Sheet - Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun LedgerSwitcherBottomSheetPreview() {
    val sampleLedgers = listOf(
        Ledger(
            id = "ledger_1",
            name = "Personal",
            currencyCode = "PKR",
            balance = 45000.0,
            isDefault = true,
            createdAt = 0L,
            updatedAt = 0L
        ),
        Ledger(
            id = "ledger_2",
            name = "Business",
            currencyCode = "USD",
            balance = 1250.0,
            isDefault = false,
            createdAt = 1L,
            updatedAt = 1L
        )
    )

    ExpenseTrackerTheme {
        LedgerSwitcherBottomSheet(
            activeLedger = sampleLedgers[0],
            allLedgers = sampleLedgers,
            onSelectLedger = {},
            onCreateLedgerClick = {},
            onEditLedgerClick = {},
            onDeleteLedgerClick = {},
            onDismiss = {}
        )
    }
}
