package com.example.ticket_scanner.validation.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ticket_scanner.validation.model.ValidationResult
import com.example.ticket_scanner.validation.model.ValidationStatus

@Composable
fun UnavailableResultScreen(
    result: ValidationResult,
    onScanNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    val color = ValidationColors.Orange
    val title = if (result.status == ValidationStatus.SESSION_EXPIRED) {
        "SIGN-IN REQUIRED"
    } else {
        "UNABLE TO VERIFY"
    }

    ValidationCard(
        color = color,
        buttonText = if (result.status == ValidationStatus.SESSION_EXPIRED) "Return to Sign In" else "Scan Next",
        onButtonClick = onScanNext,
        modifier = modifier
    ) {
        StatusHeader(Icons.Filled.WifiOff, title, color)
        Spacer(Modifier.height(22.dp))
        DetailRows(listOf("Ticket Code" to result.ticketCode))
        Spacer(Modifier.height(18.dp))
        StatusMessage(
            result.reason ?: "The backend could not verify this ticket. Do not treat it as valid.",
            color
        )
    }
}
