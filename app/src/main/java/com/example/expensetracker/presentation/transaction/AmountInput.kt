package com.example.expensetracker.presentation.transaction

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AmountInput(
    modifier: Modifier = Modifier,
    amount: String,
    currencySymbol: String,
    onAmountChanged: (String) -> Unit,
    textColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = currencySymbol,
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = textColor.copy(alpha = 0.8f),
            modifier = Modifier.padding(end = 8.dp)
        )

        TextField(
            value = amount,
            onValueChange = onAmountChanged,
            placeholder = {
                Text(
                    text = "0.00",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = textColor.copy(alpha = 0.4f)
                )
            },
            textStyle = MaterialTheme.typography.displaySmall.copy(
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-1).sp,
                color = textColor
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = textColor.copy(alpha = 0.6f),
                unfocusedIndicatorColor = textColor.copy(alpha = 0.3f),
                cursorColor = textColor
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
    }
}