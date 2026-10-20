
package com.example.ticket_scanner.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun OfflineTicketResultScreen(
    ticketId: String,
    eventName: String,
    status: String,
    details: Map<String, String> = emptyMap(),
    onBack: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = "EVENT TICKET SCANNER",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "Ticket Information",
                style = MaterialTheme.typography.headlineMedium
            )

            Text(
                text = "Offline verification result",
                style = MaterialTheme.typography.bodyLarge
            )

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Ticket Details",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text("Ticket ID: $ticketId")
                    Text("Event: $eventName")

                    Text(
                        text = "Status: $status",
                        style = MaterialTheme.typography.titleMedium
                    )

                    details.forEach { (key, value) ->
                        Text("$key: $value")
                    }
                }
            }

            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Back to Scanner")
            }

            Text(
                text = "Verification must use trusted ticket data stored on the device.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
