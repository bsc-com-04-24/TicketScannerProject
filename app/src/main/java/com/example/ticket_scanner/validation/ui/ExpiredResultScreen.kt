package com.example.ticket_scanner.validation.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.ticket_scanner.validation.model.ValidationResult
import com.example.ticket_scanner.validation.model.ValidationStatus

@Composable
fun ExpiredResultScreen(
    result: ValidationResult,
    onScanNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    val orange = ValidationColors.Orange

    ValidationCard(
        color = orange,
        buttonText = "Scan Next",
        onButtonClick = onScanNext,
        modifier = modifier
    ) {
        // Core icon used so no extra dependency is needed.
        // With material-icons-extended you can use Icons.Filled.Schedule (a clock) instead.
        StatusHeader(Icons.Filled.Warning, "EXPIRED TICKET", orange)

        Spacer(Modifier.height(22.dp))

        DetailRows(
            listOf(
                "Expiry Date" to result.expiryDate,
                "Ticket Code" to result.ticketCode,
                "Ticket Type" to result.ticketType,
                "Holder Name" to result.holderName,
                "Event" to result.event,
                "Seat/Zone" to result.seatZone
            )
        )
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun PreviewExpired() = ValidationTheme {
    ExpiredResultScreen(
        ValidationResult(
            status = ValidationStatus.EXPIRED,
            ticketCode = "TM-5566-7788", ticketType = "General", holderName = "Daniel Phiri",
            event = "Malawi Music Concert", seatZone = "C3", expiryDate = "25 Apr 2025"
        ),
        onScanNext = {}
    )
}