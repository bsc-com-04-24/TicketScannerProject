package com.example.ticket_scanner.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ticket_scanner.model.ScanItem
import com.example.ticket_scanner.model.ScanStatus
import com.example.ticket_scanner.ui.components.BottomNavBar
import com.example.ticket_scanner.ui.components.NavTab
import com.example.ticket_scanner.ui.theme.BlueHeaderBackground
import com.example.ticket_scanner.ui.theme.BluePrimary
import com.example.ticket_scanner.ui.theme.ExpiredOrange
import com.example.ticket_scanner.ui.theme.LightBackground
import com.example.ticket_scanner.ui.theme.PendingGray
import com.example.ticket_scanner.ui.theme.RejectedRed
import com.example.ticket_scanner.ui.theme.TextDark
import com.example.ticket_scanner.ui.theme.TextMuted
import com.example.ticket_scanner.ui.theme.ValidGreen

enum class HistoryFilter {
    ALL,
    VALID,
    REJECTED,
    PENDING
}

@Composable
fun HistoryScreen(
    items: List<ScanItem>,
    selectedTab: NavTab,
    onTabSelected: (NavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(HistoryFilter.ALL) }

    val filteredItems = items.filter { item ->
        val matchesSearch = item.attendeeName.contains(searchQuery, ignoreCase = true) ||
                item.ticketCode.contains(searchQuery, ignoreCase = true)

        val matchesFilter = when (selectedFilter) {
            HistoryFilter.ALL -> true
            HistoryFilter.VALID -> item.status == ScanStatus.VALID
            HistoryFilter.REJECTED -> item.status in listOf(
                ScanStatus.REJECTED,
                ScanStatus.EXPIRED,
                ScanStatus.CANCELLED,
                ScanStatus.ALREADY_USED
            )
            HistoryFilter.PENDING -> item.status == ScanStatus.PENDING
        }

        matchesSearch && matchesFilter
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(LightBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 70.dp)
        ) {
            // Header Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BlueHeaderBackground)
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Scan History",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                IconButton(onClick = { }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Options",
                        tint = Color.White
                    )
                }
            }

            // Search Bar & Filter Bar Card Container
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(16.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            "Search by code or name...",
                            fontSize = 14.sp,
                            color = TextMuted
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = TextMuted
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BluePrimary,
                        unfocusedBorderColor = Color(0xFFE2E8F0),
                        focusedContainerColor = Color(0xFFF8FAFC),
                        unfocusedContainerColor = Color(0xFFF8FAFC)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Filter Chips Row
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(HistoryFilter.entries.toTypedArray()) { filter ->
                        val isSelected = filter == selectedFilter
                        FilterChipItem(
                            label = filter.name.lowercase().replaceFirstChar { it.uppercase() },
                            isSelected = isSelected,
                            onClick = { selectedFilter = filter }
                        )
                    }
                }
            }

            // Attendees Ticket List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredItems) { item ->
                    ScanHistoryCard(scanItem = item)
                }
            }
        }

        // Bottom Navigation Bar
        BottomNavBar(
            selectedTab = selectedTab,
            onTabSelected = onTabSelected,
            isDark = false,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun FilterChipItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bg = if (isSelected) BluePrimary else Color(0xFFF1F5F9)
    val textColor = if (isSelected) Color.White else TextDark

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .clickable { onClick() }
            .padding(horizontal = 18.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
fun ScanHistoryCard(
    scanItem: ScanItem
) {
    val (statusColor, statusIcon, statusLabel) = when (scanItem.status) {
        ScanStatus.VALID -> Triple(ValidGreen, Icons.Default.QrCode, "Valid")
        ScanStatus.EXPIRED -> Triple(ExpiredOrange, Icons.Default.Timer, "Expired")
        ScanStatus.CANCELLED -> Triple(RejectedRed, Icons.Default.Block, "Cancelled")
        ScanStatus.ALREADY_USED -> Triple(Color(0xFF8B5CF6), Icons.Default.History, "Already used")
        ScanStatus.PENDING -> Triple(PendingGray, Icons.Default.Check, "Pending")
        ScanStatus.REJECTED -> Triple(RejectedRed, Icons.Default.Block, "Rejected")
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Circle Status Avatar
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(statusColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = statusIcon,
                    contentDescription = statusLabel,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = scanItem.attendeeName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = scanItem.ticketCode,
                    fontSize = 13.sp,
                    color = TextMuted
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = "$statusLabel • ${if (scanItem.isSynced) "Synced" else "Pending"}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = statusColor
                    )
                }
            }

            Text(
                text = scanItem.time,
                fontSize = 12.sp,
                color = TextMuted
            )
        }
    }
}
