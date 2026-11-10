package com.example.ticket_scanner.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ticket_scanner.model.DownloadedTicket
import com.example.ticket_scanner.model.DownloadedTicketRepository
import com.example.ticket_scanner.ui.theme.*
import com.example.ticket_scanner.util.LocalBarcodeGenerator
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketDetailScreen(
    ticketId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var ticket by remember { mutableStateOf(DownloadedTicketRepository.getTicket(ticketId)) }
    var isRefreshing by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    val qrBitmap: Bitmap? = remember(ticket?.code) {
        ticket?.code?.let { LocalBarcodeGenerator.generateQRCode(it, 512, 512) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ticket Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                isRefreshing = true
                                delay(600)
                                val updated = ticket?.id?.let { DownloadedTicketRepository.refreshTicket(it) }
                                if (updated != null) {
                                    ticket = updated
                                    message = "Ticket refreshed successfully from server."
                                } else {
                                    message = "Refresh failed."
                                }
                                isRefreshing = false
                            }
                        }
                    ) {
                        Icon(Icons.Default.CloudSync, contentDescription = "Refresh Online (GET /api/tickets/{id})")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BlueHeaderBackground,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (ticket == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("Ticket not found in local database.", color = TextMuted)
            }
        } else {
            val currentTicket = ticket!!
            val statusColor = when (currentTicket.status.uppercase()) {
                "VALID" -> ValidGreen
                "EXPIRED" -> ExpiredOrange
                "USED" -> Color(0xFF8B5CF6)
                else -> RejectedRed
            }

            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // QR Code Card
                Card(
                    modifier = Modifier.size(220.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (qrBitmap != null) {
                            Image(
                                bitmap = qrBitmap.asImageBitmap(),
                                contentDescription = "Local QR Code for ${currentTicket.code}",
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Text("QR Generation Failed", color = RejectedRed, fontSize = 12.sp)
                        }
                    }
                }

                Text(
                    text = currentTicket.code,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Status & Sync Badge Row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = statusColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = currentTicket.status,
                            color = statusColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                    }

                    Surface(
                        color = if (currentTicket.isSynced) ValidGreen.copy(alpha = 0.15f) else ExpiredOrange.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = if (currentTicket.isSynced) "Synced" else "Pending Sync",
                            color = if (currentTicket.isSynced) ValidGreen else ExpiredOrange,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                    }
                }

                // Details Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(14.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Ticket Information",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Divider(color = Color(0xFFE2E8F0))

                        DetailRow("Holder Name", currentTicket.holderName)
                        DetailRow("Ticket Type", currentTicket.type)
                        DetailRow("Event", currentTicket.event)
                        DetailRow("Seat / Zone", currentTicket.seatZone)
                        DetailRow("Last Updated", currentTicket.formattedLastUpdated)
                    }
                }

                if (message != null) {
                    Text(
                        text = message!!,
                        color = BluePrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = TextMuted, fontSize = 13.sp)
        Text(text = value, color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}
