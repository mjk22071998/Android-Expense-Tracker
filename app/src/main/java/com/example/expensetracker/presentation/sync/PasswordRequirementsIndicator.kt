package com.example.expensetracker.presentation.sync

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expensetracker.R
import com.example.expensetracker.domain.util.PasswordValidationResult
import com.example.expensetracker.ui.theme.ElectricLime
import com.example.expensetracker.ui.theme.ExpenseTrackerTheme

@Composable
fun PasswordRequirementsIndicator(
    validation: PasswordValidationResult,
    modifier: Modifier = Modifier
) {
    val extendedColors = ExpenseTrackerTheme.extendedColors
    val activeColor = if (extendedColors.isDark) ElectricLime else Color(0xFF1B4D34)
    val inactiveColor = extendedColors.textMuted

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (extendedColors.isDark) Color(0x18FFFFFF) else Color(0x08000000),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Password requirements",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = extendedColors.textSecondary
                )
                Text(
                    text = "${validation.satisfiedCount}/${validation.totalCount} satisfied",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (validation.isValid) activeColor else extendedColors.textMuted,
                    fontWeight = if (validation.isValid) FontWeight.Bold else FontWeight.Normal
                )
            }

            RequirementRow(
                satisfied = validation.hasMinLength,
                label = "At least 8 characters",
                activeColor = activeColor,
                inactiveColor = inactiveColor
            )
            RequirementRow(
                satisfied = validation.hasUppercase,
                label = "At least one uppercase letter (A-Z)",
                activeColor = activeColor,
                inactiveColor = inactiveColor
            )
            RequirementRow(
                satisfied = validation.hasLowercase,
                label = "At least one lowercase letter (a-z)",
                activeColor = activeColor,
                inactiveColor = inactiveColor
            )
            RequirementRow(
                satisfied = validation.hasDigit,
                label = "At least one number (0-9)",
                activeColor = activeColor,
                inactiveColor = inactiveColor
            )
            RequirementRow(
                satisfied = validation.hasSymbol,
                label = "At least one symbol (!@#$...)",
                activeColor = activeColor,
                inactiveColor = inactiveColor
            )
        }
    }
}

@Composable
private fun RequirementRow(
    satisfied: Boolean,
    label: String,
    activeColor: Color,
    inactiveColor: Color
) {
    val color by animateColorAsState(
        targetValue = if (satisfied) activeColor else inactiveColor,
        animationSpec = tween(durationMillis = 200),
        label = "req_color"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        if (satisfied) {
            Icon(
                painter = painterResource(id = R.drawable.ic_check),
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(14.dp)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(14.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .background(color, CircleShape)
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
            color = color,
            fontWeight = if (satisfied) FontWeight.Medium else FontWeight.Normal
        )
    }
}
