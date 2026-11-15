package com.example.expensetracker.presentation.sync

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.expensetracker.R
import com.example.expensetracker.domain.util.PasswordValidator
import com.example.expensetracker.ui.theme.ElectricLime
import com.example.expensetracker.ui.theme.ElectricLimeOn
import com.example.expensetracker.ui.theme.ExpenseTrackerTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordBottomSheet(
    isLoading: Boolean,
    errorMessage: String?,
    successMessage: String?,
    onUpdatePassword: (currentPassword: String, newPassword: String) -> Unit,
    onClearMessages: () -> Unit,
    onDismiss: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    val extendedColors = ExpenseTrackerTheme.extendedColors
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var currentPasswordVisible by remember { mutableStateOf(false) }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    val passwordValidation = remember(newPassword) {
        PasswordValidator.validate(newPassword)
    }

    val passwordsMatch = newPassword.isNotEmpty() && newPassword == confirmPassword
    val isFormValid = currentPassword.isNotBlank() && passwordValidation.isValid && passwordsMatch

    LaunchedEffect(successMessage) {
        if (successMessage != null) {
            kotlinx.coroutines.delay(1200)
            onDismiss()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = extendedColors.cardBackground,
        dragHandle = {
            Surface(
                modifier = Modifier.padding(top = 12.dp, bottom = 8.dp),
                color = extendedColors.cardBorder,
                shape = CircleShape
            ) {
                Box(modifier = Modifier.size(width = 36.dp, height = 4.dp))
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
                .verticalScroll(rememberScrollState())
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Change Password",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = extendedColors.textPrimary
                    )
                    Text(
                        text = "Update your cloud account password to keep your backups secure.",
                        style = MaterialTheme.typography.bodySmall,
                        color = extendedColors.textSecondary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = if (extendedColors.isDark) Color(0x22D4F63D) else Color(0x181B4D34),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_lock),
                            contentDescription = null,
                            tint = if (extendedColors.isDark) ElectricLime else Color(0xFF1B4D34),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            // Error Banner (if any)
            AnimatedVisibility(
                visible = errorMessage != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                errorMessage?.let { error ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0x22FF5252),
                        border = BorderStroke(1.dp, Color(0x44FF5252)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = error,
                                style = MaterialTheme.typography.bodySmall,
                                color = extendedColors.crimsonExpense,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = onClearMessages,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_close),
                                    contentDescription = "Dismiss error",
                                    tint = extendedColors.crimsonExpense,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Success Banner (if any)
            AnimatedVisibility(
                visible = successMessage != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                successMessage?.let { success ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0x224CAF50),
                        border = BorderStroke(1.dp, Color(0x444CAF50)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_check),
                                contentDescription = null,
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.size(8.dp))
                            Text(
                                text = success,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF4CAF50),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // 1. Current Password Field
            OutlinedTextField(
                value = currentPassword,
                onValueChange = {
                    currentPassword = it
                    if (errorMessage != null) onClearMessages()
                },
                label = { Text("Current password") },
                singleLine = true,
                visualTransformation = if (currentPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                trailingIcon = {
                    IconButton(onClick = { currentPasswordVisible = !currentPasswordVisible }) {
                        Icon(
                            painter = painterResource(
                                id = if (currentPasswordVisible) R.drawable.ic_visibility else R.drawable.ic_visibility_off
                            ),
                            contentDescription = if (currentPasswordVisible) "Hide password" else "Show password",
                            tint = extendedColors.textSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (extendedColors.isDark) ElectricLime else Color(0xFF1B4D34),
                    unfocusedBorderColor = extendedColors.cardBorder,
                    focusedLabelColor = if (extendedColors.isDark) ElectricLime else Color(0xFF1B4D34),
                    unfocusedLabelColor = extendedColors.textSecondary,
                    cursorColor = if (extendedColors.isDark) ElectricLime else Color(0xFF1B4D34)
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // 2. New Password Field
            OutlinedTextField(
                value = newPassword,
                onValueChange = {
                    newPassword = it
                    if (errorMessage != null) onClearMessages()
                },
                label = { Text("New password") },
                singleLine = true,
                visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                trailingIcon = {
                    IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
                        Icon(
                            painter = painterResource(
                                id = if (newPasswordVisible) R.drawable.ic_visibility else R.drawable.ic_visibility_off
                            ),
                            contentDescription = if (newPasswordVisible) "Hide password" else "Show password",
                            tint = extendedColors.textSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (extendedColors.isDark) ElectricLime else Color(0xFF1B4D34),
                    unfocusedBorderColor = extendedColors.cardBorder,
                    focusedLabelColor = if (extendedColors.isDark) ElectricLime else Color(0xFF1B4D34),
                    unfocusedLabelColor = extendedColors.textSecondary,
                    cursorColor = if (extendedColors.isDark) ElectricLime else Color(0xFF1B4D34)
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // Live Password Requirements Feedback
            PasswordRequirementsIndicator(
                validation = passwordValidation,
                modifier = Modifier.fillMaxWidth()
            )

            // 3. Confirm Password Field
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    if (errorMessage != null) onClearMessages()
                },
                label = { Text("Confirm new password") },
                singleLine = true,
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (isFormValid && !isLoading) {
                            onUpdatePassword(currentPassword.trim(), newPassword.trim())
                        }
                    }
                ),
                trailingIcon = {
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(
                            painter = painterResource(
                                id = if (confirmPasswordVisible) R.drawable.ic_visibility else R.drawable.ic_visibility_off
                            ),
                            contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password",
                            tint = extendedColors.textSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                supportingText = if (confirmPassword.isNotEmpty() && !passwordsMatch) {
                    { Text("Passwords do not match", color = extendedColors.crimsonExpense) }
                } else null,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (extendedColors.isDark) ElectricLime else Color(0xFF1B4D34),
                    unfocusedBorderColor = extendedColors.cardBorder,
                    focusedLabelColor = if (extendedColors.isDark) ElectricLime else Color(0xFF1B4D34),
                    unfocusedLabelColor = extendedColors.textSecondary,
                    cursorColor = if (extendedColors.isDark) ElectricLime else Color(0xFF1B4D34)
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // Submit Button
            Button(
                onClick = {
                    onUpdatePassword(currentPassword.trim(), newPassword.trim())
                },
                enabled = isFormValid && !isLoading,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (extendedColors.isDark) ElectricLime else Color(0xFF1B4D34),
                    contentColor = if (extendedColors.isDark) ElectricLimeOn else Color.White,
                    disabledContainerColor = extendedColors.cardBorder,
                    disabledContentColor = extendedColors.textMuted
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = if (extendedColors.isDark) ElectricLimeOn else Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Update Password",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
