package com.example.ticket_scanner.validation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ticket_scanner.validation.data.SampleScans
import com.example.ticket_scanner.validation.model.ScannedTicket

/**
 * DEMO screen only: lists sample scanned tickets so you can test validation on its own.
 * Once scanning is connected, the scanner/display feature supplies the ScannedTicket instead.
 */
@Composable
fun SampleTicketPicker(
    onValidate: (ScannedTicket) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ValidationColors.PageBackground)
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Spacer(Modifier.height(16.dp))
        Text(
            "Ticket Validation",
            color = ValidationColors.ValueDark,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(6.dp))
        Text(
            "Demo: pick a sample scanned ticket and tap Validate.",
            color = ValidationColors.LabelGray,
            fontSize = 14.sp
        )
        Spacer(Modifier.height(20.dp))

        SampleScans.all.forEach { sample ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        sample.ticket.ticketCode,
                        color = ValidationColors.ValueDark,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        listOf(sample.ticket.ticketType, sample.ticket.holderName)
                            .filter { it.isNotBlank() }
                            .joinToString(" · "),
                        color = ValidationColors.LabelGray,
                        fontSize = 12.sp
                    )
                    Text(
                        "Expected: ${sample.expectedOutcome}",
                        color = ValidationColors.Purple,
                        fontSize = 12.sp
                    )
                }
                Spacer(Modifier.width(10.dp))
                Button(onClick = { onValidate(sample.ticket) }) {
                    Text("Validate")
                }
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun PreviewPicker() = ValidationTheme {
    SampleTicketPicker(onValidate = {})
}