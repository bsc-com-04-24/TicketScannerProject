package com.example.ticket_scanner.validation.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.ticket_scanner.validation.model.ScannedTicket

@Composable
fun SampleTicketPicker(
    onValidate: (ScannedTicket) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val text: @Composable () -> Unit = {
            Text(text = "Scan a QR code to validate tickets.")
        }
        text()
    }
}
