package com.example.expensetracker.presentation.settings

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.expensetracker.R
import com.example.expensetracker.presentation.common.AppTooltip
import com.example.expensetracker.presentation.common.TooltipPosition
import com.example.expensetracker.ui.theme.ElectricLime
import com.example.expensetracker.ui.theme.ExpenseTrackerTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val extendedColors = ExpenseTrackerTheme.extendedColors
    val context = LocalContext.current
    val appVersion = remember(context) {
        try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            "Version ${pInfo.versionName ?: "0.2"}"
        } catch (_: Exception) {
            "Version 0.2"
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error.message)
            viewModel.clearError()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = extendedColors.textPrimary
                    )
                },
                navigationIcon = {
                    AppTooltip(
                        text = "Back to Dashboard",
                        position = TooltipPosition.Below
                    ) {
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .clip(CircleShape)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_arrow_back),
                                contentDescription = "Back",
                                tint = extendedColors.textPrimary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Currency Preferences Section
            SettingsSectionCard(
                title = "Currency & Locale",
                subtitle = "Configure how monetary figures are displayed"
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable {
                            viewModel.onCurrencyPickerVisibilityChanged(true)
                        }
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = uiState.currencyCode,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = extendedColors.textPrimary
                            )
                            Surface(
                                color = Color(0x22D4F63D),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = uiState.currencySymbol,
                                    color = ElectricLime,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Tap to choose from 150+ international currencies",
                            style = MaterialTheme.typography.labelSmall,
                            color = extendedColors.textMuted
                        )
                    }

                    Icon(
                        painter = painterResource(id = R.drawable.ic_chevron_right),
                        contentDescription = "Change currency",
                        tint = extendedColors.textSecondary
                    )
                }
            }

            // Appearance & Theme Mode Section
            SettingsSectionCard(
                title = "Appearance",
                subtitle = "Choose light, dark, or system matching theme"
            ) {
                ThemeModeSelector(
                    selectedMode = uiState.themeMode,
                    onModeSelected = viewModel::setThemeMode
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Footer Information
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = appVersion,
                    style = MaterialTheme.typography.labelSmall,
                    color = extendedColors.textMuted
                )
            }
        }
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
}

@Composable
private fun SettingsSectionCard(
    title: String,
    subtitle: String,
    content: @Composable () -> Unit
) {
    val extendedColors = ExpenseTrackerTheme.extendedColors

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = extendedColors.cardBackground
        ),
        border = BorderStroke(1.dp, extendedColors.cardBorder)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = extendedColors.textPrimary
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = extendedColors.textMuted
                )
            }
            content()
        }
    }
}

@Preview(name = "Settings - Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SettingsScreenPreview() {
    ExpenseTrackerTheme {
        SettingsScreen(onNavigateBack = {})
    }
}
