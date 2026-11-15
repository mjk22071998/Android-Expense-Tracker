package com.example.expensetracker.presentation.sync

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expensetracker.R
import com.example.expensetracker.ui.theme.ElectricLime
import com.example.expensetracker.ui.theme.ElectricLimeOn
import com.example.expensetracker.ui.theme.ExpenseTrackerTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SyncAccountCard(
    uiState: SyncUiState,
    onOpenAuth: () -> Unit,
    onSyncNow: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extendedColors = ExpenseTrackerTheme.extendedColors
    var showSignOutConfirmation by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = extendedColors.cardBackground
        ),
        border = BorderStroke(1.dp, extendedColors.cardBorder)
    ) {
        if (!uiState.isAuthenticated) {
            SignedOutCardContent(
                onOpenAuth = onOpenAuth
            )
        } else {
            SignedInCardContent(
                uiState = uiState,
                onSyncNow = onSyncNow,
                onRequestSignOut = { showSignOutConfirmation = true }
            )
        }
    }

    if (showSignOutConfirmation) {
        AlertDialog(
            onDismissRequest = { showSignOutConfirmation = false },
            title = {
                Text(
                    text = "Sign Out",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = extendedColors.textPrimary
                )
            },
            text = {
                Text(
                    text = "Your transactions and records will remain safely saved on this device. You can continue using Expense Tracker as an offline guest without any data loss.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = extendedColors.textSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSignOutConfirmation = false
                        onSignOut()
                    }
                ) {
                    Text(
                        text = "Sign Out",
                        color = extendedColors.crimsonExpense,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutConfirmation = false }) {
                    Text(
                        text = "Cancel",
                        color = extendedColors.textSecondary
                    )
                }
            },
            containerColor = extendedColors.cardBackground,
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
private fun SignedOutCardContent(
    onOpenAuth: () -> Unit
) {
    val extendedColors = ExpenseTrackerTheme.extendedColors

    Column(
        modifier = Modifier.padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (extendedColors.isDark) Color(0x22D4F63D) else Color(0x181B4D34),
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_cloud),
                            contentDescription = null,
                            tint = if (extendedColors.isDark) ElectricLime else Color(0xFF1B4D34),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Column {
                    Text(
                        text = "Cloud Backup & Sync",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = extendedColors.textPrimary
                    )
                    Text(
                        text = "Optional online sync",
                        style = MaterialTheme.typography.labelSmall,
                        color = extendedColors.textSecondary
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (extendedColors.isDark) Color(0x18FFFFFF) else Color(0x0C000000)
            ) {
                Text(
                    text = "Offline Mode",
                    style = MaterialTheme.typography.labelSmall,
                    color = extendedColors.textSecondary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }

        Text(
            text = "Back up your data securely to the cloud and keep your financial records synchronized across devices.",
            style = MaterialTheme.typography.bodySmall,
            color = extendedColors.textSecondary
        )

        Button(
            onClick = onOpenAuth,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (extendedColors.isDark) ElectricLime else Color(0xFF1B4D34),
                contentColor = if (extendedColors.isDark) ElectricLimeOn else Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_cloud),
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Set Up Cloud Backup",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun SignedInCardContent(
    uiState: SyncUiState,
    onSyncNow: () -> Unit,
    onRequestSignOut: () -> Unit
) {
    val extendedColors = ExpenseTrackerTheme.extendedColors
    val userEmail = uiState.userEmail ?: "Account Active"
    val userInitial = userEmail.firstOrNull()?.uppercase() ?: "U"

    val infiniteTransition = rememberInfiniteTransition(label = "sync_button_spin")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing)
        ),
        label = "sync_button_rotation"
    )

    Column(
        modifier = Modifier.padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // User Header Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (extendedColors.isDark) Color(0x33D4F63D) else Color(0x221B4D34),
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = userInitial,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (extendedColors.isDark) ElectricLime else Color(0xFF1B4D34)
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = userEmail,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = extendedColors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = formatRelativeSyncTime(uiState.lastSyncedTimeMillis),
                        style = MaterialTheme.typography.bodySmall,
                        color = extendedColors.textSecondary
                    )
                }
            }

            // Status Badge
            val (badgeBg, badgeFg, badgeText) = when {
                uiState.isSyncing -> Triple(
                    if (extendedColors.isDark) Color(0x2A1B4D34) else Color(0x181B4D34),
                    if (extendedColors.isDark) ElectricLime else Color(0xFF1B4D34),
                    "Syncing"
                )
                uiState.pendingUploadCount > 0 -> Triple(
                    Color(0x22FFC107),
                    Color(0xFFFFA000),
                    "${uiState.pendingUploadCount} pending"
                )
                else -> Triple(
                    if (extendedColors.isDark) Color(0x22D4F63D) else Color(0x181B4D34),
                    if (extendedColors.isDark) ElectricLime else Color(0xFF1B4D34),
                    "Backed Up"
                )
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = badgeBg
            ) {
                Text(
                    text = badgeText,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = badgeFg,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }

        // Action Buttons Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onSyncNow,
                enabled = !uiState.isSyncing,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (extendedColors.isDark) ElectricLime else Color(0xFF1B4D34),
                    contentColor = if (extendedColors.isDark) ElectricLimeOn else Color.White,
                    disabledContainerColor = extendedColors.cardBorder,
                    disabledContentColor = extendedColors.textMuted
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_sync),
                    contentDescription = null,
                    modifier = Modifier
                        .size(16.dp)
                        .then(if (uiState.isSyncing) Modifier.rotate(rotation) else Modifier)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (uiState.isSyncing) "Syncing..." else "Sync Now",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            OutlinedButton(
                onClick = onRequestSignOut,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, extendedColors.cardBorder),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = extendedColors.textSecondary
                ),
                modifier = Modifier.height(46.dp)
            ) {
                Text(
                    text = "Sign Out",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

private fun formatRelativeSyncTime(lastSyncTimeMillis: Long?): String {
    if (lastSyncTimeMillis == null || lastSyncTimeMillis <= 0L) return "Never synced"
    val diff = System.currentTimeMillis() - lastSyncTimeMillis
    if (diff < 30_000L) return "Synced just now"
    val minutes = diff / (60 * 1000)
    if (minutes < 60) return "Synced ${minutes}m ago"
    val hours = minutes / 60
    if (hours < 24) return "Synced ${hours}h ago"
    val days = hours / 24
    if (days < 7) return "Synced ${days}d ago"
    val sdf = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
    return "Synced " + sdf.format(Date(lastSyncTimeMillis))
}
