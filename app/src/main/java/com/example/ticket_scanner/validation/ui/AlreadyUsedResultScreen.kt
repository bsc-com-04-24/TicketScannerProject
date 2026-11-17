package com.example.ticket_scanner.validation.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.ticket_scanner.validation.model.ValidationResult
import com.example.ticket_scanner.validation.model.ValidationStatus

@Composable
fun AlreadyUsedResultScreen(
    result: ValidationResult,
    onScanNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    val purple = ValidationColors.Purple

    ValidationCard(
        color = purple,
        buttonText = "Scan Next",
        onButtonClick = onScanNext,
        modifier = modifier
    ) {
        StatusHeader(Icons.Filled.Refresh, "ALREADY USED", purple)

        Spacer(Modifier.height(22.dp))

        DetailRows(
            listOf(
                "First Check-in" to result.firstCheckIn,
                "Gate" to result.gate,
                "Ticket Code" to result.ticketCode,
                "Ticket Type" to result.ticketType,
                "Holder Name" to result.holderName,
                "Event" to result.event
            )
        )

        Spacer(Modifier.height(16.dp))
        WarningBanner(
            "Possible duplicate entry.\nThis ticket has already been used.",
            purple
        )
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun PreviewAlreadyUsed() = ValidationTheme {
    AlreadyUsedResultScreen(
        ValidationResult(
            status = ValidationStatus.ALREADY_USED,
            ticketCode = "TM-6655-4433", ticketType = "VIP", holderName = "George Banda",
            event = "Malawi Music Concert", firstCheckIn = "26 Apr 2025 · 3:15 PM", gate = "Gate 2"
        ),
        onScanNext = {}
    )
}