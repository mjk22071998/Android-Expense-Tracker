package com.example.expensetracker.presentation.sync

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expensetracker.R
import com.example.expensetracker.ui.theme.ElectricLime
import com.example.expensetracker.ui.theme.ElectricLimeOn
import com.example.expensetracker.ui.theme.ExpenseTrackerTheme

enum class AuthMode {
    SIGN_IN,
    SIGN_UP
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthBottomSheet(
    isLoading: Boolean,
    errorMessage: String?,
    onSignIn: (String, String) -> Unit,
    onSignUp: (String, String) -> Unit,
    onClearError: () -> Unit,
    onDismiss: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    val extendedColors = ExpenseTrackerTheme.extendedColors
    var authMode by remember { mutableStateOf(AuthMode.SIGN_IN) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

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
                        text = if (authMode == AuthMode.SIGN_IN) "Sign in to Cloud" else "Create Cloud Account",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = extendedColors.textPrimary
                    )
                    Text(
                        text = "Safely back up your financial history and sync across devices.",
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
                            painter = painterResource(id = R.drawable.ic_cloud),
                            contentDescription = null,
                            tint = if (extendedColors.isDark) ElectricLime else Color(0xFF1B4D34),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // Segmented Mode Selector
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (extendedColors.isDark) Color(0x33000000) else Color(0x0C000000),
                border = BorderStroke(1.dp, extendedColors.cardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                ) {
                    val isSignIn = authMode == AuthMode.SIGN_IN
                    val activeBg = if (extendedColors.isDark) ElectricLime else Color(0xFF1B4D34)
                    val activeFg = if (extendedColors.isDark) ElectricLimeOn else Color.White
                    val inactiveFg = extendedColors.textSecondary

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSignIn) activeBg else Color.Transparent)
                            .clickable {
                                authMode = AuthMode.SIGN_IN
                                onClearError()
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Sign In",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (isSignIn) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSignIn) activeFg else inactiveFg
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (!isSignIn) activeBg else Color.Transparent)
                            .clickable {
                                authMode = AuthMode.SIGN_UP
                                onClearError()
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Create Account",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (!isSignIn) FontWeight.Bold else FontWeight.Medium,
                            color = if (!isSignIn) activeFg else inactiveFg
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
                                onClick = onClearError,
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

            // Input Fields
            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    if (errorMessage != null) onClearError()
                },
                label = { Text("Email address") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
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

            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    if (errorMessage != null) onClearError()
                },
                label = { Text("Password") },
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        val trimmedEmail = email.trim()
                        val trimmedPassword = password.trim()
                        if (trimmedEmail.isNotBlank() && trimmedPassword.length >= 6 && !isLoading) {
                            if (authMode == AuthMode.SIGN_IN) {
                                onSignIn(trimmedEmail, trimmedPassword)
                            } else {
                                onSignUp(trimmedEmail, trimmedPassword)
                            }
                        }
                    }
                ),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            painter = painterResource(
                                id = if (passwordVisible) R.drawable.ic_visibility else R.drawable.ic_visibility_off
                            ),
                            contentDescription = if (passwordVisible) "Hide password" else "Show password",
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

            if (authMode == AuthMode.SIGN_UP && password.isNotEmpty() && password.length < 6) {
                Text(
                    text = "Password must be at least 6 characters.",
                    style = MaterialTheme.typography.labelSmall,
                    color = extendedColors.textMuted,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Submit Button
            val isFormValid = email.trim().isNotBlank() && password.trim().length >= 6
            Button(
                onClick = {
                    val trimmedEmail = email.trim()
                    val trimmedPassword = password.trim()
                    if (authMode == AuthMode.SIGN_IN) {
                        onSignIn(trimmedEmail, trimmedPassword)
                    } else {
                        onSignUp(trimmedEmail, trimmedPassword)
                    }
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
                        text = if (authMode == AuthMode.SIGN_IN) "Sign In" else "Create Account & Sync",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Subtle Privacy Footnote
            Text(
                text = "Protected with Row Level Security. Offline guest access remains 100% free and private.",
                style = MaterialTheme.typography.labelSmall,
                color = extendedColors.textMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
            )
        }
    }
}
