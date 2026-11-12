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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ticket_scanner.network.AuthTokenStore
import com.example.ticket_scanner.network.TicketValidationRepository
import com.example.ticket_scanner.eventcreation.ui.EventCreationFlow
import com.example.ticket_scanner.eventcreation.ui.EventCreationViewModel
import com.example.ticket_scanner.events.EventsScreen
import com.example.ticket_scanner.model.HistoryStorageManager
import com.example.ticket_scanner.model.MockData
import com.example.ticket_scanner.model.ScanItem
import com.example.ticket_scanner.model.ScanStatus
import com.example.ticket_scanner.ui.components.BottomNavBar
import com.example.ticket_scanner.ui.components.NavTab
import com.example.ticket_scanner.ui.screens.*
import com.example.ticket_scanner.ui.theme.TicketScannerTheme
import com.example.ticket_scanner.ui.viewmodel.AuthViewModel
import com.example.ticket_scanner.util.ScanFeedback
import com.example.ticket_scanner.validation.domain.TicketValidator
import com.example.ticket_scanner.validation.model.ScannedTicket
import com.example.ticket_scanner.validation.model.ValidationResult
import com.example.ticket_scanner.validation.model.ValidationStatus
import com.example.ticket_scanner.validation.ui.ValidationResultScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val eventCreationViewModel: EventCreationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AuthTokenStore.initialize(applicationContext)

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

@Composable
fun TicketScannerMainApp(
    eventCreationViewModel: EventCreationViewModel,
    isDarkMode: Boolean,
    onToggleDarkMode: (Boolean) -> Unit
) {
    val context = LocalContext.current

    var showSplash by remember { mutableStateOf(true) }
    var isLoggedIn by remember { mutableStateOf(AuthTokenStore.hasTokens()) }
    var loginError by remember { mutableStateOf<String?>(null) }

    val authViewModel: AuthViewModel = viewModel()
    val authUiState = authViewModel.uiState

    LaunchedEffect(authUiState.authResponse) {
        authUiState.authResponse?.let { response ->
            AuthTokenStore.save(response)
            loginError = null
            isLoggedIn = true
        }
    }

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

        // LOGIN / REGISTER SCREEN
        else if (!isLoggedIn) {
            AuthScreen(
                errorMessage = authUiState.errorMessage,
                successMessage = authUiState.successMessage,
                isLoading = authUiState.isLoading,
                registrationEnabled = false,

                onLoginClick = { email, password ->
                    authViewModel.login(email, password)
                },

                onRegisterClick = { fullName, email, password, confirmPassword ->
                    authViewModel.register(fullName, email, password, confirmPassword)
                },

                onModeChange = {
                    authViewModel.clearMessages()
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

        // OFFLINE SCANNER
        else if (isOfflineMode) {
            OfflineScannerScreen(
                onBack = {
                    isOfflineMode = false
                },
                onScanTicket = { ticketCode ->
                    coroutineScope.launch {
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

                        scanStats = scanStats.copy(
                            scannedCount = scanStats.scannedCount + 1,
                            validCount = scanStats.validCount +
                                    if (status == ScanStatus.VALID) 1 else 0,
                            rejectedCount = scanStats.rejectedCount +
                                    if (status != ScanStatus.VALID) 1 else 0,
                            isOnline = false
                        )

                        lastValidation = validation
                        showTicketResult = true
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

                                    val validation = TicketValidationRepository.validate(
                                        ScannedTicket(
                                            ticketCode = actualTicketCode,
                                            ticketType = decoded.fields["ticketType"] ?: "",
                                            holderName = decoded.fields["holderName"]
                                                ?: decoded.fields["attendeeName"] ?: "",
                                            event = decoded.fields["event"]
                                                ?: decoded.fields["eventName"] ?: ""
                                        )
                                    )

                                    if (validation.status == ValidationStatus.SESSION_EXPIRED) {
                                        authViewModel.logout(sessionExpired = true)
                                        isLoggedIn = false
                                        isVerifying = false
                                        lastValidation = null
                                        return@launch
                                    }

                                    if (validation.status == ValidationStatus.VALID) {
                                        ScanFeedback.playSuccess(context)
                                    } else {
                                        ScanFeedback.playError(context)
                                    }

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

                                    scanHistoryItems = listOf(newScan) + scanHistoryItems

                                    lastValidation = validation
                                    isVerifying = false
                                    // Keep showTicketResult = false so camera scanner stays active and open
                                    showTicketResult = false

                                    scanStats = scanStats.copy(
                                        scannedCount = scanStats.scannedCount + 1,
                                        validCount = scanStats.validCount +
                                                if (status == ScanStatus.VALID) 1 else 0,
                                        rejectedCount = scanStats.rejectedCount +
                                                if (status != ScanStatus.VALID &&
                                                    status != ScanStatus.PENDING
                                                ) 1 else 0,
                                        isOnline = validation.status != ValidationStatus.UNAVAILABLE
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
                            isDarkMode = isDarkMode
                        )
                    }

                    // EVENTS
                    NavTab.EVENTS -> {
                        EventsScreen(
                            selectedTab = selectedTab,
                            onTabSelected = { tab ->
                                selectedTab = tab
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(tab.ordinal)
                                }
                            }
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
                                authViewModel.logout()
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
        ValidationStatus.UNAVAILABLE,
        ValidationStatus.SESSION_EXPIRED -> ScanStatus.PENDING
    }
