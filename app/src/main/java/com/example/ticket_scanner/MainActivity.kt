
package com.example.ticket_scanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.ticket_scanner.QrMessageDecoder
import com.example.ticket_scanner.QrMessageType
import com.example.ticket_scanner.model.MockData
import com.example.ticket_scanner.model.ScanItem
import com.example.ticket_scanner.model.ScanStatus
import com.example.ticket_scanner.ui.components.NavTab
import com.example.ticket_scanner.ui.screens.AlertsScreen
import com.example.ticket_scanner.ui.screens.HistoryScreen
import com.example.ticket_scanner.ui.screens.ScannerScreen
import com.example.ticket_scanner.ui.screens.SettingsScreen
import com.example.ticket_scanner.ui.screens.SplashScreen
import com.example.ticket_scanner.ui.theme.TicketScannerTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.ticket_scanner.ui.screens.OfflineTicketResultScreen
import androidx.compose.runtime.LaunchedEffect

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            TicketScannerTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    TicketScannerMainApp()
                }
            }
        }
    }
}

@Composable
fun TicketScannerMainApp() {
    var showSplash by remember { mutableStateOf(true) }
    var selectedTab by remember { mutableStateOf(NavTab.SCAN) }
    var scanStats by remember { mutableStateOf(MockData.sampleScanStats) }
    var scanHistoryItems by remember { mutableStateOf(MockData.sampleScanItems) }
    var notificationItems by remember {
        mutableStateOf(MockData.sampleNotifications)
    }
    var showTicketResult by remember { mutableStateOf(false) }
    var lastTicketResult by remember {
        mutableStateOf<ScanItem?>(null)
    }

    val coroutineScope = rememberCoroutineScope()

    if (showSplash) {
        SplashScreen(
            onTimeout = { showSplash = false }
        )
    } else if (showTicketResult && lastTicketResult != null) {
        val ticket = lastTicketResult!!

        OfflineTicketResultScreen(
            ticketId = ticket.ticketCode,
            eventName = "Malawi Music Concert",
            status = ticket.status.name,
            details = mapOf(
                "Attendee" to ticket.attendeeName,
                "Offline status" to if (ticket.isSynced) "Synced" else "Not synced"
            ),
            onBack = {
                showTicketResult = false
            }
        )
    } else {
        when (selectedTab) {
            NavTab.SCAN -> {
                ScannerScreen(
                    stats = scanStats,
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it },
                    onVerifyTicket = { ticketCode ->
                        coroutineScope.launch {
                            delay(500)

                            val decoded = QrMessageDecoder.decode(ticketCode)

                            val actualTicketCode =
                                decoded.fields["ticketId"]
                                    ?: decoded.fields["ticketCode"]
                                    ?: decoded.rawMessage

                            val existingTicket = scanHistoryItems.find {
                                it.ticketCode.equals(
                                    actualTicketCode,
                                    ignoreCase = true
                                )
                            }

                            val status = when {
                                decoded.type == QrMessageType.UNKNOWN ->
                                    ScanStatus.REJECTED

                                existingTicket == null ->
                                    ScanStatus.REJECTED

                                existingTicket.status == ScanStatus.EXPIRED ->
                                    ScanStatus.EXPIRED

                                existingTicket.status == ScanStatus.CANCELLED ->
                                    ScanStatus.CANCELLED

                                existingTicket.status == ScanStatus.ALREADY_USED ->
                                    ScanStatus.ALREADY_USED

                                existingTicket.status == ScanStatus.REJECTED ->
                                    ScanStatus.REJECTED

                                else -> ScanStatus.VALID
                            }

                            val newScan = ScanItem(
                                id = System.currentTimeMillis().toString(),
                                attendeeName = existingTicket?.attendeeName
                                    ?: "Unknown Attendee",
                                ticketCode = actualTicketCode,
                                time = "Just now",
                                status = status,
                                isSynced = false
                            )

                            scanHistoryItems =
                                listOf(newScan) + scanHistoryItems
                            lastTicketResult = newScan
                            showTicketResult = true

                            scanStats = scanStats.copy(
                                scannedCount = scanStats.scannedCount + 1,
                                validCount = scanStats.validCount +
                                        if (status == ScanStatus.VALID) 1 else 0,
                                rejectedCount = scanStats.rejectedCount +
                                        if (status != ScanStatus.VALID) 1 else 0,
                                isOnline = false
                            )
                        }
                    }
                )
            }

            NavTab.HISTORY -> {
                HistoryScreen(
                    items = scanHistoryItems,
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it }
                )
            }

            NavTab.ALERTS -> {
                AlertsScreen(
                    notifications = notificationItems,
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it },
                    onMarkAllAsRead = {
                        notificationItems = notificationItems.map {
                            it.copy(isRead = true)
                        }
                    }
                )
            }

            NavTab.SETTINGS -> {
                SettingsScreen(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it }
                )
            }
        }
    }
}
