package com.example.ticket_scanner.validation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ticket_scanner.validation.model.ValidationResult
import com.example.ticket_scanner.validation.model.ValidationStatus

@Composable
fun ValidResultScreen(
    result: ValidationResult,
    onScanNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    val green = ValidationColors.Green

    ValidationCard(
        color = green,
        buttonText = "Scan Next",
        onButtonClick = onScanNext,
        modifier = modifier
    ) {
        StatusHeader(Icons.Filled.Check, "VALID TICKET", green)

        Spacer(Modifier.height(10.dp))
        Text(
            text = "CHECKED IN",
            color = green,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(green.copy(alpha = 0.12f))
                .border(1.dp, green.copy(alpha = 0.4f), RoundedCornerShape(50))
                .padding(horizontal = 22.dp, vertical = 7.dp)
        )

        Spacer(Modifier.height(22.dp))

        DetailRows(
            listOf(
                "Ticket Code" to result.ticketCode,
                "Ticket Type" to result.ticketType,
                "Holder Name" to result.holderName,
                "Event" to result.event,
                "Seat/Zone" to result.seatZone,
                "Purchase Date" to result.purchaseDate,
                "Check-in Time" to result.checkInTime
            )
        )
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun PreviewValid() = ValidationTheme {
    ValidResultScreen(
        ValidationResult(
            status = ValidationStatus.VALID,
            ticketCode = "TM-7842-2310", ticketType = "VIP", holderName = "Blessings Chirwa",
            event = "Malawi Music Concert", seatZone = "A1",
            purchaseDate = "20 Apr 2025 · 2:10 PM", checkInTime = "26 Apr 2025 · 4:32 PM"
        ),
        onScanNext = {}
    )
}