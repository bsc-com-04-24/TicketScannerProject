package com.example.ticket_scanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.ticket_scanner.eventcreation.ui.EventCreationFlow
import com.example.ticket_scanner.eventcreation.ui.EventCreationViewModel
import com.example.ticket_scanner.model.HistoryStorageManager
import com.example.ticket_scanner.model.MockData
import com.example.ticket_scanner.model.NotificationItem
import com.example.ticket_scanner.model.NotificationType
import com.example.ticket_scanner.model.ScanItem
import com.example.ticket_scanner.model.ScanStatus
import com.example.ticket_scanner.ui.components.BottomNavBar
import com.example.ticket_scanner.ui.components.NavTab
import com.example.ticket_scanner.ui.screens.*
import com.example.ticket_scanner.ui.theme.TicketScannerTheme
import com.example.ticket_scanner.util.ScanFeedback
import com.example.ticket_scanner.validation.domain.TicketValidator
import com.example.ticket_scanner.validation.model.ScannedTicket
import com.example.ticket_scanner.validation.model.ValidationResult
import com.example.ticket_scanner.validation.model.ValidationStatus
import com.example.ticket_scanner.validation.ui.ValidationResultScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    private val eventCreationViewModel: EventCreationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val context = LocalContext.current
            var isDarkMode by remember { mutableStateOf(HistoryStorageManager.loadDarkMode(context)) }

            TicketScannerTheme(darkTheme = isDarkMode) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    TicketScannerMainApp(
                        eventCreationViewModel = eventCreationViewModel,
                        isDarkMode = isDarkMode,
                        onToggleDarkMode = { enabled ->
                            isDarkMode = enabled
                            HistoryStorageManager.saveDarkMode(context, enabled)
                        }
                    )
                }
            }
        }
    }
}

private fun getFormattedCurrentTime(): String {
    val sdf = SimpleDateFormat("d MMM, h:mm:ss a", Locale.getDefault())
    return sdf.format(Date())
}

@Composable
fun TicketScannerMainApp(
    eventCreationViewModel: EventCreationViewModel,
    isDarkMode: Boolean,
    onToggleDarkMode: (Boolean) -> Unit
) {
    val context = LocalContext.current

    var showSplash by remember { mutableStateOf(true) }
    var isLoggedIn by remember { mutableStateOf(false) }
    var loginError by remember { mutableStateOf<String?>(null) }

    var selectedTab by remember { mutableStateOf(NavTab.SCAN) }

    var scanStats by remember {
        mutableStateOf(MockData.sampleScanStats)
    }

    var scanHistoryItems by remember {
        mutableStateOf(HistoryStorageManager.loadHistory(context))
    }

    LaunchedEffect(scanHistoryItems) {
        HistoryStorageManager.saveHistory(context, scanHistoryItems)
    }

    var notificationItems by remember {
        mutableStateOf(MockData.sampleNotifications)
    }

    var showTicketResult by remember { mutableStateOf(false) }
    var isVerifying by remember { mutableStateOf(false) }
    var isOfflineMode by remember { mutableStateOf(false) }

    var showDownloadedTickets by remember { mutableStateOf(false) }
    var selectedTicketDetailId by remember { mutableStateOf<String?>(null) }

    var lastValidation by remember {
        mutableStateOf<ValidationResult?>(null)
    }

    val coroutineScope = rememberCoroutineScope()

    // BACK NAVIGATION
    BackHandler(enabled = showTicketResult) {
        showTicketResult = false
    }

    BackHandler(enabled = selectedTicketDetailId != null) {
        selectedTicketDetailId = null
    }

    BackHandler(enabled = showDownloadedTickets) {
        showDownloadedTickets = false
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

        // TICKET DETAIL SCREEN
        else if (selectedTicketDetailId != null) {
            TicketDetailScreen(
                ticketId = selectedTicketDetailId!!,
                onBack = { selectedTicketDetailId = null }
            )
        }

        // DOWNLOADED TICKETS SCREEN
        else if (showDownloadedTickets) {
            DownloadedTicketsScreen(
                onBack = { showDownloadedTickets = false },
                onSelectTicket = { ticket ->
                    selectedTicketDetailId = ticket.id
                },
                isDarkMode = isDarkMode
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

                    if (validation.status == ValidationStatus.VALID) {
                        ScanFeedback.playSuccess(context)
                    } else {
                        ScanFeedback.playError(context)
                    }

                    val status = validation.status.toScanStatus()
                    val scanTime = getFormattedCurrentTime()

                    val newScan = ScanItem(
                        id = System.currentTimeMillis().toString(),
                        attendeeName = validation.holderName ?: "Unknown Attendee",
                        ticketCode = validation.ticketCode,
                        time = scanTime,
                        status = status,
                        isSynced = false,
                        event = validation.event ?: ""
                    )

                    scanHistoryItems = listOf(newScan) + scanHistoryItems

                    // Generate Alert if Push Notifications setting is ON
                    if (HistoryStorageManager.loadPushNotificationsEnabled(context)) {
                        val notifType = when (validation.status) {
                            ValidationStatus.VALID -> NotificationType.SYNC_SUCCESS
                            ValidationStatus.ALREADY_USED -> NotificationType.DUPLICATE_CHECKIN
                            else -> NotificationType.EVENT_ALERT
                        }
                        val alertTitle = when (validation.status) {
                            ValidationStatus.VALID -> "Offline Ticket Validated"
                            ValidationStatus.ALREADY_USED -> "Duplicate Ticket Alert"
                            else -> "Offline Scan Rejected"
                        }
                        val alert = NotificationItem(
                            id = System.currentTimeMillis().toString(),
                            title = alertTitle,
                            description = "${validation.holderName ?: "Attendee"} (${validation.ticketCode}) • ${validation.status.name}",
                            timestamp = scanTime,
                            type = notifType
                        )
                        notificationItems = listOf(alert) + notificationItems
                    }

                    scanStats = scanStats.copy(
                        scannedCount = scanStats.scannedCount + 1,
                        validCount = scanStats.validCount +
                                if (status == ScanStatus.VALID) 1 else 0,
                        rejectedCount = scanStats.rejectedCount +
                                if (status != ScanStatus.VALID) 1 else 0,
                        isOnline = false
                    )

                    lastValidation = validation
                    val isFastModeActive = HistoryStorageManager.loadFastMode(context)
                        showTicketResult = true
                }
            )
        }


        // MAIN APPLICATION SCREENS (Swipeable Pager & Bottom Bar)
        else {
            val pagerState = rememberPagerState(
                initialPage = selectedTab.ordinal,
                pageCount = { NavTab.entries.size }
            )

            LaunchedEffect(pagerState.currentPage) {
                selectedTab = NavTab.entries[pagerState.currentPage]
            }

            LaunchedEffect(selectedTab) {
                if (pagerState.currentPage != selectedTab.ordinal) {
                    pagerState.animateScrollToPage(selectedTab.ordinal)
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (NavTab.entries[page]) {

                    // ONLINE SCANNER
                    NavTab.SCAN -> {
                        ScannerScreen(
                            stats = scanStats,
                            selectedTab = selectedTab,
                            isDarkMode = isDarkMode,
                            recentScans = scanHistoryItems.take(5),
                            lastValidationResult = lastValidation,
                            onTabSelected = { tab ->
                                selectedTab = tab
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(tab.ordinal)
                                }
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

                                    if (validation.status == ValidationStatus.VALID) {
                                        ScanFeedback.playSuccess(context)
                                    } else {
                                        ScanFeedback.playError(context)
                                    }

                                    val status = validation.status.toScanStatus()
                                    val scanTime = getFormattedCurrentTime()
                                    val isAutoSync = HistoryStorageManager.loadAutoSyncEnabled(context)

                                    val newScan = ScanItem(
                                        id = System.currentTimeMillis().toString(),
                                        attendeeName = validation.holderName
                                            ?: "Unknown Attendee",
                                        ticketCode = validation.ticketCode,
                                        time = scanTime,
                                        status = status,
                                        isSynced = isAutoSync,
                                        event = validation.event ?: ""
                                    )

                                    scanHistoryItems = listOf(newScan) + scanHistoryItems

                                    // Generate Alert if Push Notifications setting is ON
                                    if (HistoryStorageManager.loadPushNotificationsEnabled(context)) {
                                        val notifType = when (validation.status) {
                                            ValidationStatus.VALID -> NotificationType.SYNC_SUCCESS
                                            ValidationStatus.ALREADY_USED -> NotificationType.DUPLICATE_CHECKIN
                                            else -> NotificationType.EVENT_ALERT
                                        }
                                        val alertTitle = when (validation.status) {
                                            ValidationStatus.VALID -> "Gate Check-In Approved"
                                            ValidationStatus.ALREADY_USED -> "Duplicate Ticket Alert"
                                            else -> "Scan Rejected: ${validation.status.name}"
                                        }
                                        val alert = NotificationItem(
                                            id = System.currentTimeMillis().toString(),
                                            title = alertTitle,
                                            description = "${validation.holderName ?: "Attendee"} (${validation.ticketCode}) • ${validation.event ?: "Event"}",
                                            timestamp = scanTime,
                                            type = notifType
                                        )
                                        notificationItems = listOf(alert) + notificationItems
                                    }

                                    lastValidation = validation
                                    isVerifying = false
                                    val isFastModeActive = HistoryStorageManager.loadFastMode(context)
                                    if (!isFastModeActive) {
                                        showTicketResult = true
                                    }

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
                            onTabSelected = { tab ->
                                selectedTab = tab
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(tab.ordinal)
                                }
                            },
                            onClearHistory = {
                                scanHistoryItems = emptyList()
                            },
                            onDeleteTicket = { ticketId ->
                                scanHistoryItems = scanHistoryItems.filter { it.id != ticketId }
                            },
                            onDeleteSelectedTickets = { selectedIds ->
                                scanHistoryItems = scanHistoryItems.filter { it.id !in selectedIds }
                            },
                            isDarkMode = isDarkMode
                        )
                    }

                    // ALERTS
                    NavTab.ALERTS -> {
                        AlertsScreen(
                            notifications = notificationItems,
                            selectedTab = selectedTab,
                            isDarkMode = isDarkMode,
                            onTabSelected = { tab ->
                                selectedTab = tab
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(tab.ordinal)
                                }
                            },
                            onMarkAllAsRead = {
                                notificationItems = notificationItems.map {
                                    it.copy(isRead = true)
                                }
                            },
                            onDeleteNotification = { notifId ->
                                notificationItems = notificationItems.filter { it.id != notifId }
                            },
                            onDeleteSelectedNotifications = { selectedIds ->
                                notificationItems = notificationItems.filter { it.id !in selectedIds }
                            },
                            onClearAllNotifications = {
                                notificationItems = emptyList()
                            }
                        )
                    }

                    // CREATE EVENT
                    NavTab.CREATE -> {
                        EventCreationFlow(
                            viewModel = eventCreationViewModel,
                            bottomBar = {
                                BottomNavBar(
                                    selectedTab = selectedTab,
                                    onTabSelected = { tab ->
                                        selectedTab = tab
                                        coroutineScope.launch {
                                            pagerState.animateScrollToPage(tab.ordinal)
                                        }
                                    },
                                    isDark = isDarkMode
                                )
                            }
                        )
                    }

                    // SETTINGS
                    NavTab.SETTINGS -> {
                        SettingsScreen(
                            selectedTab = selectedTab,
                            isDarkMode = isDarkMode,
                            onToggleDarkMode = onToggleDarkMode,
                            onTabSelected = { tab ->
                                selectedTab = tab
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(tab.ordinal)
                                }
                            },
                            onToggleOffline = {
                                isOfflineMode = true
                            },
                            onOpenDownloadedTickets = {
                                showDownloadedTickets = true
                            },
                            onLogout = {
                                isLoggedIn = false
                                loginError = null
                            }
                        )
                    }
                }
            }
        }

        // VERIFYING OVERLAY
        if (isVerifying) {
            VerifyingOverlay()
        }
    }
}

private fun ValidationStatus.toScanStatus(): ScanStatus =
    when (this) {
        ValidationStatus.VALID -> ScanStatus.VALID
        ValidationStatus.INVALID -> ScanStatus.REJECTED
        ValidationStatus.EXPIRED -> ScanStatus.EXPIRED
        ValidationStatus.ALREADY_USED -> ScanStatus.ALREADY_USED
    }
