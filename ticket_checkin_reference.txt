package com.example.ticketcheck_in

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                CheckInScreen()
            }
        }
    }
}

@Composable
fun CheckInScreen() {

    var ticketId by remember { mutableStateOf("TKT-001") }
    var ticketStatus by remember { mutableStateOf("VALID") }
    var message by remember { mutableStateOf("") }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.Start
        ) {

            Text(
                text = "EVENT TICKET SCANNER",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "Ticket Check-in",
                style = MaterialTheme.typography.headlineMedium
            )

            Text(
                text = "Verify the ticket before allowing entry.",
                style = MaterialTheme.typography.bodyLarge
            )

            OutlinedTextField(
                value = ticketId,
                onValueChange = { newValue ->
                    ticketId = newValue
                    ticketStatus = "VALID"
                    message = ""
                },
                label = {
                    Text("Ticket ID (demo)")
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {

                    Text(
                        text = "Ticket information",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(text = "Ticket ID: $ticketId")
                    Text(text = "Event: Sample Event")

                    Text(
                        text = "Status: $ticketStatus",
                        fontSize = 18.sp
                    )
                }
            }

            Button(
                onClick = {
                    if (ticketId.isBlank()) {
                        message = "Enter a ticket ID first."
                    } else if (ticketStatus == "VALID") {
                        ticketStatus = "USED"
                        message = "Demo check-in successful!"
                    } else {
                        message = "Entry rejected: ticket already used."
                    }
                },
                enabled = ticketStatus == "VALID" &&
                        ticketId.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Confirm Check-in")
            }

            if (message.isNotEmpty()) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (ticketStatus == "USED") {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    }
                )
            }

            Text(
                text = "DEMO MODE — no real ticket is verified.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}