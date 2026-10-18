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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            TicketScannerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
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
    var notificationItems by remember { mutableStateOf(MockData.sampleNotifications) }

    val coroutineScope = rememberCoroutineScope()

    if (showSplash) {
        SplashScreen(
            onTimeout = { showSplash = false }
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
                            delay(1500) // Simulate verification reading
                            val newScan = ScanItem(
                                id = System.currentTimeMillis().toString(),
                                attendeeName = "Scanned Attendee",
                                ticketCode = ticketCode,
                                time = "Just now",
                                status = ScanStatus.VALID,
                                isSynced = true
                            )
                            scanHistoryItems = listOf(newScan) + scanHistoryItems
                            scanStats = scanStats.copy(
                                scannedCount = scanStats.scannedCount + 1,
                                validCount = scanStats.validCount + 1
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
                        notificationItems = notificationItems.map { it.copy(isRead = true) }
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
