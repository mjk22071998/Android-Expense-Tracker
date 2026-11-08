package com.example.expensetracker.presentation.transaction

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.expensetracker.Constants
import com.example.expensetracker.R
import com.example.expensetracker.data.local.entity.Category
import com.example.expensetracker.ui.theme.ElectricLime
import com.example.expensetracker.ui.theme.ElectricLimeOn
import com.example.expensetracker.ui.theme.ExpenseTrackerTheme
import com.example.expensetracker.ui.theme.ForestTextPrimaryDark
import com.example.expensetracker.ui.theme.ForestTextSecondaryDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionFormScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddEditTransactionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var categoryBeingEdited by remember { mutableStateOf<Category?>(null) }
    var categoryBeingDeleted by remember { mutableStateOf<Category?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val extendedColors = ExpenseTrackerTheme.extendedColors

    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error.message)
            viewModel.clearError()
        }
    }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onNavigateBack()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = if (uiState.isEditMode) "Edit Transaction" else "New Transaction",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = extendedColors.textPrimary
                    )
                },
                navigationIcon = {
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Type toggle (Expense / Income)
            TypeToggle(
                selectedType = uiState.type,
                onTypeSelected = viewModel::onTypeChanged
            )

            // Hero Amount Card with Luxury Organic Forest Gradient
            val isIncome = uiState.type == Constants.TransactionType.INCOME
            val heroGradient = Brush.linearGradient(
                colors = if (isIncome) listOf(Color(0xFF163826), Color(0xFF0F261B), Color(0xFF08140E))
                         else listOf(Color(0xFF102838), Color(0xFF0B1B26), Color(0xFF08140E)),
                start = Offset(0f, 0f),
                end = Offset(800f, 800f)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(26.dp),
                border = BorderStroke(1.dp, if (isIncome) Color(0x33D6F248) else Color(0x330099FF)),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(heroGradient)
                        .padding(22.dp)
                ) {
                    // Subtle ambient glow
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .align(Alignment.TopEnd)
                            .offset(x = 30.dp, y = (-30).dp)
                            .background(
                                color = if (isIncome) Color(0x18D6F248) else Color(0x180099FF),
                                shape = CircleShape
                            )
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = if (isIncome) "Income Amount" else "Expense Amount",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = ForestTextSecondaryDark,
                            letterSpacing = 1.sp
                        )

                        AmountInput(
                            amount = uiState.amount,
                            currencySymbol = uiState.currencySymbol,
                            onAmountChanged = viewModel::onAmountChanged,
                            textColor = ForestTextPrimaryDark
                        )
                    }
                }
            }

            // Category Selection Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = extendedColors.cardBackground),
                border = BorderStroke(1.dp, extendedColors.cardBorder)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Category",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = extendedColors.textPrimary
                        )

                        Text(
                            text = if (uiState.selectedCategoryId != null) "Selected" else "Required",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (uiState.selectedCategoryId != null) ElectricLime else extendedColors.textMuted
                        )
                    }

                    CategoryPicker(
                        categories = uiState.filteredCategories,
                        selectedCategoryId = uiState.selectedCategoryId,
                        onCategorySelected = viewModel::onCategorySelected,
                        onAddCategoryClick = { showAddCategoryDialog = true },
                        onEditCategory = { categoryBeingEdited = it },
                        onDeleteCategory = { categoryBeingDeleted = it }
                    )
                }
            }

            // Note Field with Refined Styling
            OutlinedTextField(
                value = uiState.note,
                onValueChange = viewModel::onNoteChanged,
                label = { Text("Note (optional)") },
                placeholder = {
                    Text(
                        "e.g. Weekly grocery haul, client invoice...",
                        color = extendedColors.textMuted
                    )
                },
                shape = RoundedCornerShape(18.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = extendedColors.cardBackground,
                    unfocusedContainerColor = extendedColors.cardBackground,
                    focusedBorderColor = ElectricLime,
                    unfocusedBorderColor = extendedColors.cardBorder,
                    focusedLabelColor = ElectricLime,
                    unfocusedLabelColor = extendedColors.textSecondary,
                    focusedTextColor = extendedColors.textPrimary,
                    unfocusedTextColor = extendedColors.textPrimary
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Date Selection Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = extendedColors.cardBackground),
                border = BorderStroke(1.dp, extendedColors.cardBorder)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Transaction Date",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = extendedColors.textPrimary
                    )

                    DatePickerField(
                        selectedDate = uiState.date,
                        onDateSelected = viewModel::onDateChanged
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Primary Save Action Button
            Button(
                onClick = viewModel::saveTransaction,
                enabled = uiState.isSaveEnabled && !uiState.isLoading,
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricLime,
                    contentColor = ElectricLimeOn,
                    disabledContainerColor = extendedColors.cardBorder,
                    disabledContentColor = extendedColors.textMuted
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = ElectricLimeOn,
                        strokeWidth = 2.5.dp
                    )
                } else {
                    Icon(
                        painter = painterResource(
                            id = if (uiState.isEditMode) R.drawable.ic_edit else R.drawable.ic_check
                        ),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = if (uiState.isEditMode) "Update Transaction"
                               else if (isIncome) "Save Income Deposit" else "Save Expense Outflow",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    if (showAddCategoryDialog) {
        CategoryDialog(
            transactionType = uiState.type,
            onDismiss = { showAddCategoryDialog = false },
            onSave = { name, icon ->
                viewModel.addCategory(name, icon)
                showAddCategoryDialog = false
            }
        )
    }

    categoryBeingEdited?.let { category ->
        CategoryDialog(
            transactionType = category.transactionType,
            existingCategory = category,
            onDismiss = { categoryBeingEdited = null },
            onSave = { name, icon ->
                viewModel.updateCategory(category, name, icon)
                categoryBeingEdited = null
            }
        )
    }

    categoryBeingDeleted?.let { category ->
        DeleteCategoryDialog(
            category = category,
            onConfirm = {
                viewModel.deleteCategory(category)
                categoryBeingDeleted = null
            },
            onDismiss = { categoryBeingDeleted = null }
        )
    }
}

@Preview(name = "Form - Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TransactionFormScreenPreview() {
    ExpenseTrackerTheme {
        TransactionFormScreen(onNavigateBack = {})
    }
}
