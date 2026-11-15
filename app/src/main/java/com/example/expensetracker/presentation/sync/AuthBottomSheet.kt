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
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.expensetracker.domain.util.PasswordValidator
import com.example.expensetracker.ui.theme.ElectricLime
import com.example.expensetracker.ui.theme.ElectricLimeOn
import com.example.expensetracker.ui.theme.ExpenseTrackerTheme

enum class AuthMode {
    SIGN_IN,
    SIGN_UP,
    FORGOT_PASSWORD_REQUEST,
    FORGOT_PASSWORD_VERIFY
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthBottomSheet(
    isLoading: Boolean,
    errorMessage: String?,
    successMessage: String? = null,
    isResetCodeSent: Boolean = false,
    onSignIn: (String, String) -> Unit,
    onSignUp: (String, String) -> Unit,
    onSendResetCode: (String) -> Unit = {},
    onResetPassword: (email: String, otp: String, newPassword: String) -> Unit = { _, _, _ -> },
    onClearError: () -> Unit,
    onDismiss: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    val extendedColors = ExpenseTrackerTheme.extendedColors
    var authMode by remember { mutableStateOf(AuthMode.SIGN_IN) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }

    var passwordVisible by remember { mutableStateOf(false) }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    val signUpPasswordValidation = remember(password) {
        PasswordValidator.validate(password)
    }

    val resetPasswordValidation = remember(newPassword) {
        PasswordValidator.validate(newPassword)
    }

    LaunchedEffect(isResetCodeSent) {
        if (isResetCodeSent && authMode == AuthMode.FORGOT_PASSWORD_REQUEST) {
            authMode = AuthMode.FORGOT_PASSWORD_VERIFY
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
                        text = when (authMode) {
                            AuthMode.SIGN_IN -> "Sign in to Cloud"
                            AuthMode.SIGN_UP -> "Create Cloud Account"
                            AuthMode.FORGOT_PASSWORD_REQUEST -> "Reset Password"
                            AuthMode.FORGOT_PASSWORD_VERIFY -> "Enter Verification Code"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = extendedColors.textPrimary
                    )
                    Text(
                        text = when (authMode) {
                            AuthMode.SIGN_IN -> "Safely back up your financial history and sync across devices."
                            AuthMode.SIGN_UP -> "Register with email to enable cloud sync and multi-device access."
                            AuthMode.FORGOT_PASSWORD_REQUEST -> "Enter your email to receive an 8-digit verification code."
                            AuthMode.FORGOT_PASSWORD_VERIFY -> "Check your email for the 8-digit code and choose a new password."
                        },
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
                            painter = painterResource(
                                id = if (authMode == AuthMode.FORGOT_PASSWORD_REQUEST || authMode == AuthMode.FORGOT_PASSWORD_VERIFY) {
                                    R.drawable.ic_lock
                                } else {
                                    R.drawable.ic_cloud
                                }
                            ),
                            contentDescription = null,
                            tint = if (extendedColors.isDark) ElectricLime else Color(0xFF1B4D34),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // Segmented Mode Selector (only when in Sign In / Sign Up)
            if (authMode == AuthMode.SIGN_IN || authMode == AuthMode.SIGN_UP) {
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

            // Mode-specific content
            when (authMode) {
                AuthMode.SIGN_IN, AuthMode.SIGN_UP -> {
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
                                if (authMode == AuthMode.SIGN_IN) {
                                    if (trimmedEmail.isNotBlank() && trimmedPassword.isNotBlank() && !isLoading) {
                                        onSignIn(trimmedEmail, trimmedPassword)
                                    }
                                } else {
                                    if (trimmedEmail.isNotBlank() && signUpPasswordValidation.isValid && !isLoading) {
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

                    // Password requirements in Sign Up mode
                    if (authMode == AuthMode.SIGN_UP) {
                        PasswordRequirementsIndicator(
                            validation = signUpPasswordValidation,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Forgot Password link in Sign In mode
                    if (authMode == AuthMode.SIGN_IN) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Text(
                                text = "Forgot password?",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = if (extendedColors.isDark) ElectricLime else Color(0xFF1B4D34),
                                modifier = Modifier
                                    .clickable {
                                        authMode = AuthMode.FORGOT_PASSWORD_REQUEST
                                        onClearError()
                                    }
                                    .padding(vertical = 4.dp, horizontal = 2.dp)
                            )
                        }
                    }

                    // Submit Button
                    val isFormValid = if (authMode == AuthMode.SIGN_IN) {
                        email.trim().isNotBlank() && password.trim().isNotBlank()
                    } else {
                        email.trim().isNotBlank() && signUpPasswordValidation.isValid
                    }

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
                }

                AuthMode.FORGOT_PASSWORD_REQUEST -> {
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
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (email.trim().isNotBlank() && !isLoading) {
                                    onSendResetCode(email.trim())
                                }
                            }
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

                    Button(
                        onClick = {
                            onSendResetCode(email.trim())
                        },
                        enabled = email.trim().isNotBlank() && !isLoading,
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
                                text = "Send Verification Code",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    TextButton(
                        onClick = {
                            authMode = AuthMode.SIGN_IN
                            onClearError()
                        }
                    ) {
                        Text(
                            text = "Back to Sign In",
                            color = extendedColors.textSecondary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                AuthMode.FORGOT_PASSWORD_VERIFY -> {
                    OutlinedTextField(
                        value = otpCode,
                        onValueChange = {
                            // Supabase Email OTP length is 8 digits
                            if (it.length <= 8) {
                                otpCode = it
                                if (errorMessage != null) onClearError()
                            }
                        },
                        label = { Text("8-digit verification code") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
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
                        value = newPassword,
                        onValueChange = {
                            newPassword = it
                            if (errorMessage != null) onClearError()
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

                    PasswordRequirementsIndicator(
                        validation = resetPasswordValidation,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            if (errorMessage != null) onClearError()
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
                                val trimmedEmail = email.trim()
                                val trimmedOtp = otpCode.trim()
                                val trimmedNewPassword = newPassword.trim()
                                if (trimmedOtp.length >= 6 && resetPasswordValidation.isValid && trimmedNewPassword == confirmPassword && !isLoading) {
                                    onResetPassword(trimmedEmail, trimmedOtp, trimmedNewPassword)
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
                        supportingText = if (confirmPassword.isNotEmpty() && confirmPassword != newPassword) {
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

                    val isResetValid = otpCode.trim().length >= 6 &&
                        resetPasswordValidation.isValid &&
                        newPassword == confirmPassword

                    Button(
                        onClick = {
                            onResetPassword(email.trim(), otpCode.trim(), newPassword.trim())
                        },
                        enabled = isResetValid && !isLoading,
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
                                text = "Reset Password & Sign In",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { onSendResetCode(email.trim()) }, enabled = !isLoading) {
                            Text("Resend Code", color = if (extendedColors.isDark) ElectricLime else Color(0xFF1B4D34))
                        }
                        TextButton(
                            onClick = {
                                authMode = AuthMode.SIGN_IN
                                onClearError()
                            }
                        ) {
                            Text("Back to Sign In", color = extendedColors.textSecondary)
                        }
                    }
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
