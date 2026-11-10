package com.example.ticket_scanner.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ticket_scanner.model.ScanItem
import com.example.ticket_scanner.model.ScanStatus
import com.example.ticket_scanner.ui.components.BottomNavBar
import com.example.ticket_scanner.ui.components.NavTab
import com.example.ticket_scanner.ui.theme.BlueHeaderBackground
import com.example.ticket_scanner.ui.theme.BluePrimary
import com.example.ticket_scanner.ui.theme.ExpiredOrange
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
    onClearHistory: () -> Unit,
    onDeleteTicket: (String) -> Unit,
    onDeleteSelectedTickets: (Set<String>) -> Unit = {},
    isDarkMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(HistoryFilter.ALL) }
    var showMenu by remember { mutableStateOf(false) }

    // Multi-Selection State
    var isSelectionMode by remember { mutableStateOf(false) }
    var selectedIds by remember { mutableStateOf(setOf<String>()) }

    val filteredItems = items.filter { item ->
        val matchesSearch = searchQuery.isBlank() ||
                item.attendeeName.contains(searchQuery, ignoreCase = true) ||
                item.ticketCode.contains(searchQuery, ignoreCase = true) ||
                item.event.contains(searchQuery, ignoreCase = true)

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
            .background(MaterialTheme.colorScheme.background)
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
                Column {
                    Text(
                        text = if (isSelectionMode) "${selectedIds.size} Selected" else "Scan History",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (isSelectionMode) {
                        Text(
                            text = "Select items to delete",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 11.sp
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Multi-Select Toggle Button
                    IconButton(
                        onClick = {
                            isSelectionMode = !isSelectionMode
                            if (!isSelectionMode) selectedIds = emptySet()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.SelectAll,
                            contentDescription = "Toggle Selection Mode",
                            tint = if (isSelectionMode) Color(0xFFFBBF24) else Color.White
                        )
                    }

                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Options",
                                tint = Color.White
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Select All") },
                                onClick = {
                                    showMenu = false
                                    isSelectionMode = true
                                    selectedIds = items.map { it.id }.toSet()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Clear All History") },
                                onClick = {
                                    showMenu = false
                                    selectedIds = emptySet()
                                    isSelectionMode = false
                                    onClearHistory()
                                }
                            )
                        }
                    }
                }
            }

            // Bulk Delete Action Bar (Shows when items are selected)
            AnimatedVisibility(
                visible = isSelectionMode || selectedIds.isNotEmpty(),
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.clickable {
                            selectedIds = if (selectedIds.size == filteredItems.size) emptySet()
                            else filteredItems.map { it.id }.toSet()
                        },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = selectedIds.isNotEmpty() && selectedIds.size == filteredItems.size,
                            onCheckedChange = { checked ->
                                selectedIds = if (checked) filteredItems.map { it.id }.toSet() else emptySet()
                            },
                            colors = CheckboxDefaults.colors(checkedColor = BluePrimary)
                        )
                        Text(
                            text = if (selectedIds.size == filteredItems.size) "Deselect All" else "Select All Filtered",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (selectedIds.isNotEmpty()) {
                        Button(
                            onClick = {
                                onDeleteSelectedTickets(selectedIds)
                                selectedIds = emptySet()
                                isSelectionMode = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = RejectedRed),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "Delete (${selectedIds.size})",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Search Bar & Filter Bar Card Container
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            "Search by code, name or event...",
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
                        focusedContainerColor = MaterialTheme.colorScheme.background,
                        unfocusedContainerColor = MaterialTheme.colorScheme.background,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        cursorColor = BluePrimary
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
                if (filteredItems.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (items.isEmpty()) "No scanned tickets yet.\nScan tickets from the scanner to view history." else "No matching scan records found.",
                                color = TextMuted,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    items(filteredItems, key = { it.id }) { item ->
                        val isSelected = item.id in selectedIds
                        ScanHistoryCard(
                            scanItem = item,
                            isSelectionMode = isSelectionMode,
                            isSelected = isSelected,
                            onToggleSelect = {
                                selectedIds = if (isSelected) selectedIds - item.id else selectedIds + item.id
                            },
                            onDelete = { onDeleteTicket(item.id) }
                        )
                    }
                }
            }
        }

        // Bottom Navigation Bar
        BottomNavBar(
            selectedTab = selectedTab,
            onTabSelected = onTabSelected,
            isDark = isDarkMode,
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
    scanItem: ScanItem,
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    onToggleSelect: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    var isExpanded by remember { mutableStateOf(false) }

    val (statusColor, statusIcon, statusLabel) = when (scanItem.status) {
        ScanStatus.VALID -> Triple(ValidGreen, Icons.Default.QrCode, "Valid")
        ScanStatus.EXPIRED -> Triple(ExpiredOrange, Icons.Default.Timer, "Expired")
        ScanStatus.CANCELLED -> Triple(RejectedRed, Icons.Default.Block, "Cancelled")
        ScanStatus.ALREADY_USED -> Triple(Color(0xFF8B5CF6), Icons.Default.History, "Already used")
        ScanStatus.PENDING -> Triple(PendingGray, Icons.Default.Check, "Pending")
        ScanStatus.REJECTED -> Triple(RejectedRed, Icons.Default.Block, "Rejected")
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                if (isSelectionMode) onToggleSelect()
                else isExpanded = !isExpanded
            },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) BluePrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // SUMMARY ROW (COLLAPSED VIEW)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isSelectionMode) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = { onToggleSelect() },
                        colors = CheckboxDefaults.colors(checkedColor = BluePrimary)
                    )
                    Spacer(Modifier.width(6.dp))
                }

                // Circle Status Avatar
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(statusColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = statusIcon,
                        contentDescription = statusLabel,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = scanItem.attendeeName,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = scanItem.ticketCode,
                        fontSize = 12.sp,
                        color = TextMuted
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(statusColor)
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        Text(
                            text = "$statusLabel • ${if (scanItem.isSynced) "Synced" else "Pending"}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = statusColor
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = scanItem.time,
                        fontSize = 11.sp,
                        color = TextMuted
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (isExpanded) "Hide" else "Details",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = BluePrimary
                        )
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = "Expand details",
                            tint = BluePrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // EXPANDED DETAILS SECTION (APPEARS ONLY WHEN EXPANDED)
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            RoundedCornerShape(10.dp)
                        )
                        .padding(12.dp)
                ) {
                    Text(
                        text = "TICKET DETAILS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = BluePrimary,
                        letterSpacing = 0.8.sp
                    )

                    Spacer(Modifier.height(8.dp))

                    DetailItemRow("Event Name", scanItem.event.ifBlank { "Malawi Live Concert 2025" })
                    DetailItemRow("Ticket Code", scanItem.ticketCode)
                    DetailItemRow("Attendee Name", scanItem.attendeeName)
                    DetailItemRow("Validation Outcome", "$statusLabel (${scanItem.status.name})")
                    DetailItemRow("Scan Timestamp", scanItem.time)
                    DetailItemRow("Cloud Sync Status", if (scanItem.isSynced) "Synced to Cloud Server" else "Pending Synchronisation")

                    Spacer(Modifier.height(10.dp))

                    // DELETE RECORD BUTTON (LOCATED INSIDE EXPANDED SECTION)
                    OutlinedButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(38.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = RejectedRed),
                        border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                            brush = androidx.compose.ui.graphics.SolidColor(RejectedRed)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete record",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "Delete History Record",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailItemRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = TextMuted
        )
        Text(
            text = value,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.End
        )
    }
}
