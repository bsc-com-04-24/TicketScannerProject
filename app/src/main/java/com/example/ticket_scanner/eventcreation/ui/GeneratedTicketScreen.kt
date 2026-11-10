package com.example.ticket_scanner.eventcreation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ticket_scanner.eventcreation.model.EventDetails
import com.example.ticket_scanner.eventcreation.model.GeneratedTicket
import com.example.ticket_scanner.ui.theme.BluePrimary
import com.example.ticket_scanner.ui.theme.LightBackground
import com.example.ticket_scanner.ui.theme.TextDark
import com.example.ticket_scanner.ui.theme.TextMuted

/**
 * Shows the generated QR codes. The tickets are NOT stored until "Save tickets" is tapped.
 * After saving, the buttons change to "View saved tickets" and "Done".
 */
@Composable
fun GeneratedTicketsScreen(
    event: EventDetails,
    tickets: List<GeneratedTicket>,
    isSaving: Boolean,
    isSaved: Boolean,
    saveError: String?,
    onSave: () -> Unit,
    onDiscard: () -> Unit,
    onViewSaved: () -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        EventHeader(title = if (isSaved) "Tickets saved" else "Review tickets")

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { EventSummary(event, tickets.size, isSaved) }
            items(tickets, key = { it.code }) { ticket ->
                QrTicketCard(
                    qrPayload = ticket.qrPayload,
                    code = ticket.code,
                    subtitle = "${ticket.ticketType} · Ticket ${ticket.ticketNumber} of ${ticket.totalTickets}",
                    eventName = ticket.eventName
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            if (saveError != null) {
                Text(saveError, color = Color(0xFFE02B2B), fontSize = 13.sp)
                Spacer(Modifier.height(6.dp))
            }

            if (!isSaved) {
                PrimaryButton(
                    text = if (isSaving) "Saving…" else "Save tickets",
                    enabled = !isSaving,
                    onClick = onSave
                )
                TextButton(
                    onClick = onDiscard,
                    enabled = !isSaving,
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Discard and edit", color = BluePrimary) }
            } else {
                PrimaryButton(text = "View saved tickets", enabled = true, onClick = onViewSaved)
                TextButton(onClick = onDone, modifier = Modifier.fillMaxWidth()) {
                    Text("Done", color = BluePrimary)
                }
            }
        }
    }
}

@Composable
private fun PrimaryButton(text: String, enabled: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
    ) {
        Text(text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
    }
}

@Composable
private fun EventSummary(event: EventDetails, count: Int, isSaved: Boolean) {
    val plural = if (count == 1) "" else "s"
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(event.name, color = MaterialTheme.colorScheme.onSurface, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(formatDateTime(event.startMillis), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), fontSize = 13.sp)
            Text(event.venue, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), fontSize = 13.sp)
            Spacer(Modifier.height(10.dp))
            Text(
                text = if (isSaved) {
                    "$count ticket$plural saved on this phone. Scan them with the Scan tab to validate."
                } else {
                    "$count ticket$plural created. Check the QR codes, then tap Save tickets to store them on this phone."
                },
                color = BluePrimary,
                fontSize = 13.sp
            )
        }
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun PreviewGeneratedTickets() = MaterialTheme {
    val event = EventDetails(
        name = "Malawi Tech & Innovation Expo",
        description = "",
        venue = "Bingu International Convention Centre",
        startMillis = 1794650400000L,
        endMillis = 1794668400000L
    )
    GeneratedTicketsScreen(
        event = event,
        tickets = (1..3).map {
            GeneratedTicket(
                code = "MTIE-2026-00000$it",
                ticketType = "General Admission",
                eventName = event.name,
                venue = event.venue,
                startMillis = event.startMillis,
                ticketNumber = it,
                totalTickets = 3,
                qrPayload = "{\"ticketId\":\"MTIE-2026-00000$it\"}"
            )
        },
        isSaving = false,
        isSaved = false,
        saveError = null,
        onSave = {},
        onDiscard = {},
        onViewSaved = {},
        onDone = {}
    )
}
