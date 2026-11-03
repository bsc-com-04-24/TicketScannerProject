
package com.example.ticketcheck_in

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Navy = Color(0xFF102B55)
private val Blue = Color(0xFF2463A6)
private val Background = Color(0xFFF4F6FA)
private val DarkText = Color(0xFF18263B)
private val Muted = Color(0xFF718096)
private val Green = Color(0xFF168653)
private val Red = Color(0xFFD94343)
private val Orange = Color(0xFFE69A24)
private val Grey = Color(0xFF64748B)

enum class TicketStatus {
    EXPIRED,
    DUPLICATE,
    SUSPICIOUS,
    CANCELLED
}

data class TicketRecord(
    val ticketId: String,
    val status: TicketStatus,
    val time: String,
    val reason: String
)

private fun sampleRecords() = listOf(
    TicketRecord("TKT-1024", TicketStatus.EXPIRED, "10:24 AM",
        "Ticket validity period has ended."),
    TicketRecord("TKT-1031", TicketStatus.DUPLICATE, "10:30 AM",
        "Repeated check-in attempt detected."),
    TicketRecord("TKT-1040", TicketStatus.SUSPICIOUS, "10:35 AM",
        "Ticket flagged as suspicious by the backend."),
    TicketRecord("TKT-1045", TicketStatus.CANCELLED, "10:42 AM",
        "Ticket cancellation recorded."),
    TicketRecord("TKT-1050", TicketStatus.DUPLICATE, "10:48 AM",
        "Ticket was previously checked in.")
)

class MainActivity : ComponentActivity() {

    private val ticketRecords = mutableStateListOf<TicketRecord>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ticketRecords.addAll(sampleRecords())
        receiveRecord(intent)

        setContent {
            MaterialTheme {
                TicketManagementApp(ticketRecords)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        receiveRecord(intent)
    }

    private fun receiveRecord(intent: Intent?) {
        if (intent == null) return

        val id = intent.getStringExtra("CHECKIN_TICKET_ID")
            ?.trim()
            ?.takeIf { it.isNotEmpty() } ?: return

        val statusText = intent.getStringExtra("CHECKIN_STATUS")
            ?.uppercase(Locale.ROOT) ?: return

        val status = runCatching {
            TicketStatus.valueOf(statusText)
        }.getOrNull() ?: return

        val record = TicketRecord(
            ticketId = id,
            status = status,
            time = intent.getStringExtra("CHECKIN_TIME")
                ?: SimpleDateFormat("hh:mm a", Locale.getDefault())
                    .format(Date()),
            reason = intent.getStringExtra("CHECKIN_REASON")
                ?: "Security activity received."
        )

        ticketRecords.add(0, record)
    }
}

@Composable
fun TicketManagementApp(records: List<TicketRecord>) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var search by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf("All") }
    var selectedRecord by remember { mutableStateOf<TicketRecord?>(null) }
    var notifications by remember { mutableStateOf(true) }

    Scaffold(
        containerColor = Background,
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                val tabs = listOf(
                    Triple("Dashboard", Icons.Default.Dashboard, 0),
                    Triple("Records", Icons.Default.ReceiptLong, 1),
                    Triple("Alerts", Icons.Default.Warning, 2),
                    Triple("Settings", Icons.Default.Settings, 3)
                )

                tabs.forEach { (label, icon, index) ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = { Icon(icon, contentDescription = label) },
                        label = { Text(label, fontSize = 10.sp) }
                    )
                }
            }
        }
    ) { padding ->
        Box(
            Modifier.fillMaxSize().padding(padding)
        ) {
            when (selectedTab) {
                0 -> DashboardScreen(records) {
                    selectedTab = 1
                }

                1 -> RecordsScreen(
                    records = records,
                    search = search,
                    onSearch = { search = it },
                    filter = filter,
                    onFilter = { filter = it },
                    onSelect = { selectedRecord = it }
                )

                2 -> AlertsScreen(records) {
                    selectedRecord = it
                }

                3 -> SettingsScreen(
                    notifications = notifications,
                    onNotifications = { notifications = it }
                )
            }
        }
    }

    selectedRecord?.let { record ->
        RecordDetailsDialog(record) {
            selectedRecord = null
        }
    }
}

@Composable
fun Header(title: String, subtitle: String) {
    Column(
        Modifier.fillMaxWidth()
            .background(Navy)
            .padding(horizontal = 20.dp, vertical = 22.dp)
    ) {
        Text(
            "TICKET SECURITY MANAGEMENT",
            color = Color(0xFFB9D6F5),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(7.dp))

        Text(
            title,
            color = Color.White,
            fontSize = 23.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(4.dp))

        Text(
            subtitle,
            color = Color(0xFFD4DFED),
            fontSize = 12.sp
        )
    }
}

@Composable
fun DashboardScreen(
    records: List<TicketRecord>,
    onViewRecords: () -> Unit
) {
    val expired = records.count { it.status == TicketStatus.EXPIRED }
    val duplicate = records.count { it.status == TicketStatus.DUPLICATE }
    val suspicious = records.count { it.status == TicketStatus.SUSPICIOUS }
    val cancelled = records.count { it.status == TicketStatus.CANCELLED }

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 20.dp)
    ) {
        item {
            Header(
                "Security overview",
                "Monitor ticket irregularities and check-in activity."
            )
        }

        item {
            Column(Modifier.padding(16.dp)) {
                Text(
                    "TICKET ACTIVITY SUMMARY",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Muted
                )

                Spacer(Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatCard(
                        "Expired",
                        expired,
                        Orange,
                        Icons.Default.Schedule,
                        Modifier.weight(1f)
                    )

                    StatCard(
                        "Duplicates",
                        duplicate,
                        Red,
                        Icons.Default.ContentCopy,
                        Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatCard(
                        "Suspicious",
                        suspicious,
                        Red,
                        Icons.Default.GppBad,
                        Modifier.weight(1f)
                    )

                    StatCard(
                        "Cancelled",
                        cancelled,
                        Grey,
                        Icons.Default.Cancel,
                        Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(22.dp))

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Recent security activity",
                        fontWeight = FontWeight.Bold,
                        color = DarkText,
                        fontSize = 16.sp
                    )

                    TextButton(onClick = onViewRecords) {
                        Text("View all")
                    }
                }
            }
        }

        if (records.isEmpty()) {
            item {
                EmptyState("No ticket activity received.")
            }
        } else {
            items(records.take(5)) { record ->
                RecordRow(record)
            }
        }

        item {
            Card(
                Modifier.fillMaxWidth().padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFEAF1FA)
                ),
                shape = RoundedCornerShape(13.dp)
            ) {
                Row(
                    Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Security,
                        null,
                        tint = Blue
                    )

                    Spacer(Modifier.width(10.dp))

                    Column {
                        Text(
                            "Security monitoring",
                            color = Navy,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Review irregular ticket activity received from the connected system.",
                            color = Muted,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    count: Int,
    color: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(title, fontSize = 11.sp, color = Muted)
                Icon(
                    icon,
                    null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(Modifier.height(8.dp))

            Text(
                count.toString(),
                fontSize = 26.sp,
                color = DarkText,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun RecordRow(
    record: TicketRecord,
    onClick: (() -> Unit)? = null
) {
    Card(
        Modifier.fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .then(
                if (onClick != null) Modifier.clickable { onClick() }
                else Modifier
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Row(
            Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier.size(39.dp)
                    .background(
                        statusColor(record.status).copy(alpha = 0.12f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    statusIcon(record.status),
                    null,
                    tint = statusColor(record.status),
                    modifier = Modifier.size(19.dp)
                )
            }

            Spacer(Modifier.width(11.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    record.ticketId,
                    color = DarkText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )

                Text(
                    record.reason,
                    color = Muted,
                    fontSize = 10.sp
                )

                Text(
                    record.time,
                    color = Muted,
                    fontSize = 10.sp
                )
            }

            StatusChip(record.status)
        }
    }
}

@Composable
fun StatusChip(status: TicketStatus) {
    Surface(
        color = statusColor(status).copy(alpha = 0.12f),
        shape = RoundedCornerShape(20.dp)
    ) {
        Text(
            status.name.lowercase().replaceFirstChar {
                it.uppercase()
            },
            color = statusColor(status),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(
                horizontal = 8.dp,
                vertical = 6.dp
            )
        )
    }
}

@Composable
fun RecordsScreen(
    records: List<TicketRecord>,
    search: String,
    onSearch: (String) -> Unit,
    filter: String,
    onFilter: (String) -> Unit,
    onSelect: (TicketRecord) -> Unit
) {
    val filters = listOf(
        "All",
        "Expired",
        "Duplicate",
        "Suspicious",
        "Cancelled"
    )

    val filteredRecords = records.filter { record ->
        val matchesSearch =
            record.ticketId.contains(search, ignoreCase = true)

        val matchesFilter =
            filter == "All" ||
                    record.status.name.equals(filter, ignoreCase = true)

        matchesSearch && matchesFilter
    }

    Column(Modifier.fillMaxSize()) {
        Header(
            "Ticket records",
            "Review reported ticket security activity."
        )

        OutlinedTextField(
            value = search,
            onValueChange = onSearch,
            modifier = Modifier.fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            placeholder = { Text("Search ticket ID") },
            leadingIcon = {
                Icon(Icons.Default.Search, null)
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        androidx.compose.foundation.lazy.LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            items(filters) { item ->
                FilterChip(
                    selected = filter == item,
                    onClick = { onFilter(item) },
                    label = { Text(item, fontSize = 10.sp) }
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        if (filteredRecords.isEmpty()) {
            EmptyState("No matching ticket records.")
        } else {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                items(filteredRecords) { record ->
                    RecordRow(record) {
                        onSelect(record)
                    }
                }
            }
        }
    }
}

@Composable
fun AlertsScreen(
    records: List<TicketRecord>,
    onSelect: (TicketRecord) -> Unit
) {
    val alerts = records.filter {
        it.status == TicketStatus.SUSPICIOUS ||
                it.status == TicketStatus.DUPLICATE ||
                it.status == TicketStatus.EXPIRED ||
                it.status == TicketStatus.CANCELLED
    }

    Column(Modifier.fillMaxSize()) {
        Header(
            "Security alerts",
            "Review ticket irregularities and reported incidents."
        )

        if (alerts.isEmpty()) {
            EmptyState("No security alerts received.")
        } else {
            LazyColumn(
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                items(alerts) { record ->
                    RecordRow(record) {
                        onSelect(record)
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(
    notifications: Boolean,
    onNotifications: (Boolean) -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        Header(
            "Settings",
            "Manage security monitoring preferences."
        )

        Card(
            Modifier.fillMaxWidth().padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    "NOTIFICATIONS",
                    color = Muted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    Modifier.fillMaxWidth().padding(vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            "Security notifications",
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "Preference for security activity alerts.",
                            color = Muted,
                            fontSize = 11.sp
                        )
                    }

                    Switch(
                        checked = notifications,
                        onCheckedChange = onNotifications
                    )
                }
            }
        }

        Card(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    "APPLICATION INFORMATION",
                    color = Muted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(12.dp))
                Text("Ticket Check-in Management")
                Text("Version 1.0.0", color = Muted)
                Text("Scanner: Separate application", color = Muted)
                Text("Purpose: Ticket security monitoring", color = Muted)
            }
        }
    }
}

@Composable
fun RecordDetailsDialog(
    record: TicketRecord,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ticket activity details") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DetailLine("Ticket ID", record.ticketId)
                DetailLine("Status", record.status.name)
                DetailLine("Time", record.time)
                DetailLine("Reason", record.reason)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun DetailLine(label: String, value: String) {
    Column {
        Text(label, color = Muted, fontSize = 11.sp)
        Text(
            value,
            color = DarkText,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun EmptyState(message: String) {
    Box(
        Modifier.fillMaxWidth().padding(30.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.Inbox,
                null,
                tint = Muted,
                modifier = Modifier.size(38.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(message, color = Muted)
        }
    }
}

fun statusColor(status: TicketStatus): Color {
    return when (status) {
        TicketStatus.EXPIRED -> Orange
        TicketStatus.DUPLICATE -> Red
        TicketStatus.SUSPICIOUS -> Red
        TicketStatus.CANCELLED -> Grey
    }
}

fun statusIcon(status: TicketStatus): ImageVector {
    return when (status) {
        TicketStatus.EXPIRED -> Icons.Default.Schedule
        TicketStatus.DUPLICATE -> Icons.Default.ContentCopy
        TicketStatus.SUSPICIOUS -> Icons.Default.GppBad
        TicketStatus.CANCELLED -> Icons.Default.Cancel
    }
}
