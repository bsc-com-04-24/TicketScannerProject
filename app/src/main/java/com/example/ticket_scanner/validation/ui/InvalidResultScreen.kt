package com.example.ticket_scanner.validation.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.ticket_scanner.validation.model.ValidationResult
import com.example.ticket_scanner.validation.model.ValidationStatus

@Composable
fun InvalidResultScreen(
    result: ValidationResult,
    onScanNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    val red = ValidationColors.Red

    ValidationCard(
        color = red,
        buttonText = "Scan Next",
        onButtonClick = onScanNext,
        modifier = modifier
    ) {
        StatusHeader(Icons.Filled.Close, "INVALID TICKET", red)

        Spacer(Modifier.height(22.dp))

        // Shows what was scanned (the ticket is not in our records, so nothing else is known)
        DetailRows(
            listOf(
                "Ticket Code" to result.ticketCode,
                "Ticket Type" to result.ticketType,
                "Holder Name" to result.holderName,
                "Event" to result.event
            )
        )

        Spacer(Modifier.height(18.dp))
        StatusMessage(result.reason ?: "Ticket not recognised.", red)
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun PreviewInvalid() = ValidationTheme {
    InvalidResultScreen(
        ValidationResult(
            status = ValidationStatus.INVALID,
            ticketCode = "TM-1234-5678", ticketType = "General", holderName = "unknown",
            event = "Malawi Music Concert", reason = "Ticket not recognised."
        )
        onScanNext = {}
    )
}