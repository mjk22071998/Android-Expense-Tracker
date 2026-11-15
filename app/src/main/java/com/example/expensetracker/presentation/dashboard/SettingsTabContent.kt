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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.expensetracker.R
import com.example.expensetracker.presentation.settings.ClearAllRecordsDialog
import com.example.expensetracker.presentation.settings.CurrencyPickerDialog
import com.example.expensetracker.presentation.settings.SettingsViewModel
import com.example.expensetracker.presentation.settings.ThemeModeSelector
import com.example.expensetracker.presentation.sync.AuthBottomSheet
import com.example.expensetracker.presentation.sync.ChangePasswordBottomSheet
import com.example.expensetracker.presentation.sync.SyncAccountCard
import com.example.expensetracker.presentation.sync.SyncViewModel
import com.example.expensetracker.ui.theme.ElectricLime
import com.example.expensetracker.ui.theme.ExpenseTrackerTheme
import com.example.expensetracker.ui.theme.ForestBorderDark
import com.example.expensetracker.ui.theme.ForestCardDark
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsTabContent(
    onExportClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
    syncViewModel: SyncViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val syncUiState by syncViewModel.uiState.collectAsStateWithLifecycle()
    val extendedColors = ExpenseTrackerTheme.extendedColors
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val appVersion = remember(context) {
        try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            "Version ${pInfo.versionName ?: "0.3"}"
        } catch (_: Exception) {
            "Version 0.3"
        }
    }
    var showClearRecordsDialog by remember { mutableStateOf(false) }
    var showAuthBottomSheet by remember { mutableStateOf(false) }
    var showChangePasswordSheet by remember { mutableStateOf(false) }

    LaunchedEffect(syncUiState.isAuthenticated) {
        if (syncUiState.isAuthenticated) {
            showAuthBottomSheet = false
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 0. Cloud Backup & Sync Section Card
        SyncAccountCard(
            uiState = syncUiState,
            onOpenAuth = { showAuthBottomSheet = true },
            onSyncNow = syncViewModel::syncNow,
            onSignOut = syncViewModel::signOut,
            onOpenChangePassword = { showChangePasswordSheet = true }
        )

        // 1. Currency Section Card
        SettingsTabSectionCard(title = "Currency") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .clickable {
                        viewModel.onCurrencyPickerVisibilityChanged(true)
                    }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(if (extendedColors.isDark) Color(0x22D4F63D) else Color(0x1F1B4D34)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = uiState.currencySymbol,
                            fontWeight = FontWeight.Bold,
                            color = if (extendedColors.isDark) ElectricLime else Color(0xFF1B4D34)
                        )
                    }

                    Column {
                        Text(
                            text = uiState.currencyCode,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = if (extendedColors.isDark) Color.White else extendedColors.textPrimary
                        )
                        Text(
                            text = "Tap to change active currency",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (extendedColors.isDark) Color(0xFFA5C4B2) else extendedColors.textSecondary
                        )
                    }
                }

                Icon(
                    painter = painterResource(id = R.drawable.ic_chevron_right),
                    contentDescription = "Select Currency",
                    tint = if (extendedColors.isDark) Color(0xFFA5C4B2) else extendedColors.textSecondary
                )
            }
        }

        // 2. Appearance & Theme Mode Section Card
        SettingsTabSectionCard(title = "Appearance") {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                ThemeModeSelector(
                    selectedMode = uiState.themeMode,
                    onModeSelected = viewModel::setThemeMode
                )
            }
        }

        // 3. Data Management Section Card
        SettingsTabSectionCard(title = "Data Management") {
            // PDF Export Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .clickable(onClick = onExportClick)
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(if (extendedColors.isDark) Color(0x22D4F63D) else Color(0x1F1B4D34)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_picture_as_pdf),
                            contentDescription = "Export PDF",
                            tint = if (extendedColors.isDark) ElectricLime else Color(0xFF1B4D34),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Export Statement (PDF)",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (extendedColors.isDark) Color.White else extendedColors.textPrimary
                        )
                        Text(
                            text = "Generate and share PDF statements",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (extendedColors.isDark) Color(0xFFA5C4B2) else extendedColors.textSecondary
                        )
                    }
                }

                Icon(
                    painter = painterResource(id = R.drawable.ic_chevron_right),
                    contentDescription = "Export PDF",
                    tint = if (extendedColors.isDark) Color(0xFFA5C4B2) else extendedColors.textSecondary
                )
            }

            // Clear Records Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .clickable {
                        showClearRecordsDialog = true
                    }
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0x22FF5252)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_delete),
                            contentDescription = "Clear Records",
                            tint = Color(0xFFFF5252),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Clear All Records",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = extendedColors.crimsonExpense
                        )
                        Text(
                            text = "Soft-deletes all entries and resets balances",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (extendedColors.isDark) Color(0xFFA5C4B2) else extendedColors.textSecondary
                        )
                    }
                }

                Icon(
                    painter = painterResource(id = R.drawable.ic_chevron_right),
                    contentDescription = "Clear",
                    tint = extendedColors.crimsonExpense
                )
            }
        }

        // 4. About App Section Card
        SettingsTabSectionCard(title = "About Expense Tracker") {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "100% Offline & Private Financial Ledger",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (extendedColors.isDark) Color.White else extendedColors.textPrimary
                )
                Text(
                    text = "Zero tracking, zero cloud data transfer. All calculations, charts, and PDF reports are generated locally on your device.",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (extendedColors.isDark) Color(0xFFA5C4B2) else extendedColors.textSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$appVersion • Hardware-Accelerated Native Canvas",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = if (extendedColors.isDark) Color(0x88A5C4B2) else extendedColors.textSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(84.dp))
    }

    if (showClearRecordsDialog) {
        ClearAllRecordsDialog(
            currencySymbol = uiState.currencySymbol,
            onConfirm = {
                showClearRecordsDialog = false
                viewModel.clearAllTransactions {
                    // Handled safely
                }
            },
            onDismiss = { showClearRecordsDialog = false }
        )
    }

    if (uiState.isCurrencyPickerVisible) {
        CurrencyPickerDialog(
            searchQuery = uiState.currencySearchQuery,
            currencies = uiState.filteredCurrencies,
            currentCurrencyCode = uiState.currencyCode,
            onSearchQueryChanged = viewModel::onCurrencySearchQueryChanged,
            onCurrencySelected = viewModel::onCurrencySelected,
            onDismiss = { viewModel.onCurrencyPickerVisibilityChanged(false) }
        )
    }

    if (showAuthBottomSheet) {
        AuthBottomSheet(
            isLoading = syncUiState.isAuthLoading,
            errorMessage = syncUiState.authErrorMessage,
            successMessage = syncUiState.authSuccessMessage,
            isResetCodeSent = syncUiState.passwordResetSent,
            onSignIn = syncViewModel::signIn,
            onSignUp = syncViewModel::signUp,
            onSendResetCode = syncViewModel::sendPasswordResetEmail,
            onResetPassword = syncViewModel::resetPasswordWithOtp,
            onClearError = syncViewModel::clearAuthMessages,
            onDismiss = {
                showAuthBottomSheet = false
                syncViewModel.resetPasswordFlowState()
            }
        )
    }

    if (showChangePasswordSheet) {
        ChangePasswordBottomSheet(
            isLoading = syncUiState.isAuthLoading,
            errorMessage = syncUiState.authErrorMessage,
            successMessage = syncUiState.authSuccessMessage,
            onUpdatePassword = syncViewModel::updatePassword,
            onClearMessages = syncViewModel::clearAuthMessages,
            onDismiss = {
                showChangePasswordSheet = false
                syncViewModel.resetPasswordFlowState()
            }
        )
    }
}

@Composable
private fun SettingsTabSectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    val extendedColors = ExpenseTrackerTheme.extendedColors

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (extendedColors.isDark) ForestCardDark else extendedColors.cardElevated
        ),
        border = BorderStroke(1.dp, if (extendedColors.isDark) ForestBorderDark else extendedColors.cardBorder)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (extendedColors.isDark) Color.White else extendedColors.textPrimary
            )
            content()
        }
    }
}
