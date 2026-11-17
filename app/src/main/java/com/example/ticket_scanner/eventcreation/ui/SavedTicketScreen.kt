package com.example.ticket_scanner.eventcreation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ticket_scanner.localdb.LocalTicketStore
import com.example.ticket_scanner.localdb.StoredTicket
import com.example.ticket_scanner.ui.theme.BluePrimary
import com.example.ticket_scanner.ui.theme.LightBackground
import com.example.ticket_scanner.ui.theme.TextDark
import com.example.ticket_scanner.ui.theme.TextMuted

private val DeleteRed = Color(0xFFE02B2B)

/**
 * Every ticket saved on this phone, grouped by event, each with its QR code.
 * Reads from the database, so it still shows them after the app is closed.
 *
 * Per event: Edit event, Delete event. Per ticket: Delete ticket.
 * Every change goes to the database, and this list updates by itself.
 */
@Composable
fun SavedTicketsScreen(
    onBack: () -> Unit,
    onEditEvent: (Long) -> Unit,
    onDeleteEvent: (Long) -> Unit,
    onDeleteTicket: (String) -> Unit,
    modifier: Modifier = Modifier,
    bottomBar: @Composable () -> Unit = {}
) {
    val tickets by LocalTicketStore.tickets.collectAsState()
    val byEvent = tickets.groupBy { it.eventId }
    val now = System.currentTimeMillis()

    var ticketToDelete by remember { mutableStateOf<StoredTicket?>(null) }
    var eventToDelete by remember { mutableStateOf<StoredTicket?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LightBackground)
    ) {
        EventHeader(title = "Saved tickets", onBack = onBack)

        if (tickets.isEmpty()) {
            Text(
                text = "No saved tickets yet.\nCreate an event and tap Save tickets.",
                color = TextMuted,
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(32.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                byEvent.forEach { (eventId, list) ->
                    item(key = "event-$eventId") {
                        EventGroupHeader(
                            first = list.first(),
                            count = list.size,
                            onEdit = { onEditEvent(eventId) },
                            onDelete = { eventToDelete = list.first() }
                        )
                    }
                    items(list, key = { it.code }) { t ->
                        QrTicketCard(
                            qrPayload = t.qrPayload,
                            code = t.code,
                            subtitle = "${t.ticketType} · Ticket ${t.ticketNumber} of ${t.totalTickets}",
                            eventName = t.eventName,
                            status = statusText(t, now),
                            footer = {
                                TextButton(onClick = { ticketToDelete = t }) {
                                    Text("Delete ticket", color = DeleteRed)
                                }
                            }
                        )
                    }
                }
            }
        }

        bottomBar()
    }

    ticketToDelete?.let { t ->
        ConfirmDeleteDialog(
            title = "Delete ticket?",
            message = "Delete ${t.code}? Scanning its QR code afterwards will show Invalid.",
            onConfirm = {
                onDeleteTicket(t.code)
                ticketToDelete = null
            },
            onDismiss = { ticketToDelete = null }
        )
    }

    eventToDelete?.let { t ->
        val count = byEvent[t.eventId]?.size ?: 0
        ConfirmDeleteDialog(
            title = "Delete event?",
            message = "Delete ${t.eventName} and its $count ticket${if (count == 1) "" else "s"}? " +
                    "Their QR codes will no longer validate.",
            onConfirm = {
                onDeleteEvent(t.eventId)
                eventToDelete = null
            },
            onDismiss = { eventToDelete = null }
        )
    }
}

@Composable
private fun EventGroupHeader(
    first: StoredTicket,
    count: Int,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(first.eventName, color = TextDark, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(
            text = "${formatDateTime(first.startMillis)} · ${first.venue}",
            color = TextMuted,
            fontSize = 13.sp
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = "$count ticket${if (count == 1) "" else "s"}",
            color = BluePrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
        Row {
            TextButton(onClick = onEdit) { Text("Edit event", color = BluePrimary) }
            TextButton(onClick = onDelete) { Text("Delete event", color = DeleteRed) }
        }
    }
}

@Composable
private fun ConfirmDeleteDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("Delete", color = DeleteRed) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

private fun statusText(t: StoredTicket, now: Long): String = when {
    t.checkedInAt != null -> "Used on ${formatDateTime(t.checkedInAt)}"
    t.expiresAt < now -> "Expired"
    else -> "Not used yet"
}