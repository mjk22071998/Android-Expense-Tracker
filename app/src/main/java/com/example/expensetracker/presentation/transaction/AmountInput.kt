package com.example.expensetracker.presentation.transaction

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expensetracker.ui.theme.ElectricLime
import com.example.expensetracker.ui.theme.ExpenseTrackerTheme

@Composable
fun AmountInput(
    modifier: Modifier = Modifier,
    amount: String,
    currencySymbol: String,
    onAmountChanged: (String) -> Unit,
    textColor: Color = Color.White
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = currencySymbol,
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold,
            color = ElectricLime
        )

        TextField(
            value = amount,
            onValueChange = onAmountChanged,
            placeholder = {
                Text(
                    text = "0.00",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = textColor.copy(alpha = 0.35f)
                )
            },
            textStyle = MaterialTheme.typography.displayMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-1).sp,
                color = textColor
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = ElectricLime
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(name = "Amount Input - Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun AmountInputPreview() {
    ExpenseTrackerTheme {
        AmountInput(
            amount = "124.50",
            currencySymbol = "$",
            onAmountChanged = {}
        )
    }
}
