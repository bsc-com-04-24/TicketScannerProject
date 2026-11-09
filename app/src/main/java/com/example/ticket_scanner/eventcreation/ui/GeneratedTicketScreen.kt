package com.example.ticket_scanner.eventcreation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ticket_scanner.eventcreation.model.EventDetails
import com.example.ticket_scanner.eventcreation.model.GeneratedTicket
import com.example.ticket_scanner.ui.theme.BluePrimary
import com.example.ticket_scanner.ui.theme.LightBackground
import com.example.ticket_scanner.ui.theme.TextDark
import com.example.ticket_scanner.ui.theme.TextMuted

@Composable
fun GeneratedTicketsScreen(
    event: EventDetails,
    tickets: List<GeneratedTicket>,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        EventHeader(title = "Tickets created")

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                EventSummary(event = event, count = tickets.size)
            }
            items(tickets, key = { it.code }) { ticket ->
                TicketCard(ticket)
            }
        }

        Button(
            onClick = onDone,
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .height(52.dp)
        ) {
            Text("Done", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
        }
    }
}

@Composable
private fun EventSummary(event: EventDetails, count: Int) {
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
                text = "$count ticket${if (count == 1) "" else "s"} created. Each QR code below " +
                        "works with the Scan tab and ticket validation.",
                color = BluePrimary,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun TicketCard(ticket: GeneratedTicket) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            QrCodeImage(
                content = ticket.qrPayload,
                modifier = Modifier.size(220.dp)
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = ticket.code,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${ticket.ticketType} · Ticket ${ticket.ticketNumber} of ${ticket.totalTickets}",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
            Text(
                text = ticket.eventName,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                fontSize = 13.sp,
                textAlign = TextAlign.Center
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
        onDone = {}
    )
}