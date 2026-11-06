
package com.example.ticket_scanner

import android.os.Bundle

import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent

import androidx.compose.foundation.layout.Box
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
import com.example.ticket_scanner.ui.screens.AuthScreen
import com.example.ticket_scanner.ui.screens.HistoryScreen
import com.example.ticket_scanner.ui.screens.OfflineScannerScreen
import com.example.ticket_scanner.ui.screens.ScannerScreen
import com.example.ticket_scanner.ui.screens.SettingsScreen
import com.example.ticket_scanner.ui.screens.SplashScreen
import com.example.ticket_scanner.ui.screens.VerifyingOverlay

import com.example.ticket_scanner.ui.theme.TicketScannerTheme

import com.example.ticket_scanner.validation.domain.TicketValidator
import com.example.ticket_scanner.validation.model.ScannedTicket
import com.example.ticket_scanner.validation.model.ValidationResult
import com.example.ticket_scanner.validation.model.ValidationStatus
import com.example.ticket_scanner.validation.ui.ValidationResultScreen

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


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
    var isLoggedIn by remember { mutableStateOf(false) }
    var loginError by remember { mutableStateOf<String?>(null) }

    var selectedTab by remember { mutableStateOf(NavTab.SCAN) }

    var scanStats by remember {
        mutableStateOf(MockData.sampleScanStats)
    }

    var scanHistoryItems by remember {
        mutableStateOf(MockData.sampleScanItems)
    }

    var notificationItems by remember {
        mutableStateOf(MockData.sampleNotifications)
    }

    var showTicketResult by remember { mutableStateOf(false) }
    var isVerifying by remember { mutableStateOf(false) }
    var isOfflineMode by remember { mutableStateOf(false) }

    var lastValidation by remember {
        mutableStateOf<ValidationResult?>(null)
    }

    val coroutineScope = rememberCoroutineScope()


    // BACK NAVIGATION
    BackHandler(enabled = showTicketResult) {
        showTicketResult = false
    }

    BackHandler(enabled = isOfflineMode && !showTicketResult) {
        isOfflineMode = false
    }


    Box(modifier = Modifier.fillMaxSize()) {

        // SPLASH SCREEN
        if (showSplash) {

            SplashScreen(
                onTimeout = {
                    showSplash = false
                }
            )

        }

        // LOGIN SCREEN
        else if (!isLoggedIn) {

            AuthScreen(
                errorMessage = loginError,
                onLoginClick = { username, password ->

                    if (username.trim() == "user" && password == "user") {
                        loginError = null
                        isLoggedIn = true
                    } else {
                        loginError = "Invalid username or password"
                    }
                }
            )

        }

        // VALIDATION RESULT (Online & Offline)
        else if (showTicketResult && lastValidation != null) {

            ValidationResultScreen(
                result = lastValidation!!,
                onScanNext = {
                    showTicketResult = false
                }
            )

        }

        // OFFLINE SCANNER
        else if (isOfflineMode) {

            OfflineScannerScreen(

                onBack = {
                    isOfflineMode = false
                },

                onScanTicket = { ticketCode ->

                    // Decode actual QR content
                    val decoded = QrMessageDecoder.decode(ticketCode)

                    val actualTicketCode =
                        decoded.fields["ticketId"]
                            ?: decoded.fields["ticketCode"]
                            ?: decoded.rawMessage

                    // Validate ticket
                    val validation = TicketValidator.validate(
                        ScannedTicket(
                            ticketCode = actualTicketCode,
                            ticketType = decoded.fields["ticketType"] ?: "",
                            holderName = decoded.fields["holderName"]
                                ?: decoded.fields["attendeeName"] ?: "",
                            event = decoded.fields["event"]
                                ?: decoded.fields["eventName"] ?: ""
                        )
                    )

                    val status = validation.status.toScanStatus()

                    // Add scan to history
                    val newScan = ScanItem(
                        id = System.currentTimeMillis().toString(),
                        attendeeName = validation.holderName
                            ?: "Unknown Attendee",
                        ticketCode = validation.ticketCode,
                        time = "Just now",
                        status = status,
                        isSynced = false,
                        event = validation.event ?: ""
                    )

                    scanHistoryItems = listOf(newScan) + scanHistoryItems

                    // Update statistics
                    scanStats = scanStats.copy(
                        scannedCount = scanStats.scannedCount + 1,

                        validCount = scanStats.validCount +
                                if (status == ScanStatus.VALID) 1 else 0,

                        rejectedCount = scanStats.rejectedCount +
                                if (status != ScanStatus.VALID) 1 else 0,

                        isOnline = false
                    )

                    // Display validation result screen
                    lastValidation = validation
                    showTicketResult = true
                }
            )

        }

        // MAIN APPLICATION SCREENS
        else {

            when (selectedTab) {

                // ONLINE SCANNER
                NavTab.SCAN -> {

                    ScannerScreen(
                        stats = scanStats,
                        selectedTab = selectedTab,

                        onTabSelected = {
                            selectedTab = it
                        },

                        onOpenOfflineMode = {
                            isOfflineMode = true
                        },

                        onVerifyTicket = { ticketCode ->

                            isVerifying = true

                            coroutineScope.launch {

                                delay(600)

                                val decoded = QrMessageDecoder.decode(ticketCode)

                                val actualTicketCode =
                                    decoded.fields["ticketId"]
                                        ?: decoded.fields["ticketCode"]
                                        ?: decoded.rawMessage

                                val validation = TicketValidator.validate(
                                    ScannedTicket(
                                        ticketCode = actualTicketCode,
                                        ticketType = decoded.fields["ticketType"] ?: "",
                                        holderName = decoded.fields["holderName"]
                                            ?: decoded.fields["attendeeName"] ?: "",
                                        event = decoded.fields["event"]
                                            ?: decoded.fields["eventName"] ?: ""
                                    )
                                )

                                val status = validation.status.toScanStatus()

                                val newScan = ScanItem(
                                    id = System.currentTimeMillis().toString(),
                                    attendeeName = validation.holderName
                                        ?: "Unknown Attendee",
                                    ticketCode = validation.ticketCode,
                                    time = "Just now",
                                    status = status,
                                    isSynced = false,
                                    event = validation.event ?: ""
                                )

                                scanHistoryItems =
                                    listOf(newScan) + scanHistoryItems

                                lastValidation = validation
                                isVerifying = false
                                showTicketResult = true

                                // Update statistics
                                scanStats = scanStats.copy(
                                    scannedCount = scanStats.scannedCount + 1,

                                    validCount = scanStats.validCount +
                                            if (status == ScanStatus.VALID) 1 else 0,

                                    rejectedCount = scanStats.rejectedCount +
                                            if (status != ScanStatus.VALID) 1 else 0,

                                    isOnline = true
                                )
                            }
                        }
                    )
                }


                // HISTORY
                NavTab.HISTORY -> {

                    HistoryScreen(
                        items = scanHistoryItems,
                        selectedTab = selectedTab,
                        onTabSelected = {
                            selectedTab = it
                        },
                        onClearHistory = {
                            scanHistoryItems = emptyList()
                        },
                        onDeleteTicket = { ticketId ->
                            scanHistoryItems = scanHistoryItems.filter { it.id != ticketId }
                        }
                    )
                }


                // ALERTS
                NavTab.ALERTS -> {

                    AlertsScreen(
                        notifications = notificationItems,
                        selectedTab = selectedTab,

                        onTabSelected = {
                            selectedTab = it
                        },

                        onMarkAllAsRead = {
                            notificationItems = notificationItems.map {
                                it.copy(isRead = true)
                            }
                        }
                    )
                }


                // SETTINGS
                NavTab.SETTINGS -> {

                    SettingsScreen(
                        selectedTab = selectedTab,

                        onTabSelected = {
                            selectedTab = it
                        },

                        onToggleOffline = {
                            isOfflineMode = true
                        },

                        onLogout = {
                            isLoggedIn = false
                            loginError = null
                        }
                    )
                }
            }
        }


        // VERIFYING OVERLAY
        if (isVerifying) {
            VerifyingOverlay()
        }
    }
}


// CONVERT VALIDATION STATUS TO SCAN HISTORY STATUS
private fun ValidationStatus.toScanStatus(): ScanStatus =
    when (this) {

        ValidationStatus.VALID ->
            ScanStatus.VALID

        ValidationStatus.INVALID ->
            ScanStatus.REJECTED

        ValidationStatus.EXPIRED ->
            ScanStatus.EXPIRED

        ValidationStatus.ALREADY_USED ->
            ScanStatus.ALREADY_USED
    }
