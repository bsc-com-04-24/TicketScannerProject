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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Warning
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
import com.example.ticket_scanner.model.NotificationItem
import com.example.ticket_scanner.model.NotificationType
import com.example.ticket_scanner.ui.components.BottomNavBar
import com.example.ticket_scanner.ui.components.NavTab
import com.example.ticket_scanner.ui.theme.BlueHeaderBackground
import com.example.ticket_scanner.ui.theme.BluePrimary
import com.example.ticket_scanner.ui.theme.RejectedRed
import com.example.ticket_scanner.ui.theme.TextDark
import com.example.ticket_scanner.ui.theme.TextMuted
import com.example.ticket_scanner.ui.theme.ValidGreen

enum class AlertFilter {
    ALL,
    ALERTS,
    SYNC,
    DUPLICATES
}

@Composable
fun AlertsScreen(
    notifications: List<NotificationItem>,
    selectedTab: NavTab,
    onTabSelected: (NavTab) -> Unit,
    onMarkAllAsRead: () -> Unit = {},
    onDeleteNotification: (String) -> Unit = {},
    onDeleteSelectedNotifications: (Set<String>) -> Unit = {},
    onClearAllNotifications: () -> Unit = {},
    isDarkMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(AlertFilter.ALL) }
    var showMenu by remember { mutableStateOf(false) }

    // Multi-Selection State
    var isSelectionMode by remember { mutableStateOf(false) }
    var selectedIds by remember { mutableStateOf(setOf<String>()) }

    val filteredNotifications = notifications.filter { notif ->
        val matchesSearch = searchQuery.isBlank() ||
                notif.title.contains(searchQuery, ignoreCase = true) ||
                notif.description.contains(searchQuery, ignoreCase = true)

        val matchesFilter = when (selectedFilter) {
            AlertFilter.ALL -> true
            AlertFilter.ALERTS -> notif.type in listOf(NotificationType.EVENT_ALERT, NotificationType.INFO)
            AlertFilter.SYNC -> notif.type == NotificationType.SYNC_SUCCESS
            AlertFilter.DUPLICATES -> notif.type == NotificationType.DUPLICATE_CHECKIN
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
                        text = if (isSelectionMode) "${selectedIds.size} Selected" else "Notifications & Alerts",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (isSelectionMode) {
                        Text(
                            text = "Select alerts to delete",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 11.sp
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Multi-Select Toggle Icon
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
                                    selectedIds = notifications.map { it.id }.toSet()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Mark All as Read") },
                                onClick = {
                                    showMenu = false
                                    onMarkAllAsRead()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Clear All Alerts") },
                                onClick = {
                                    showMenu = false
                                    selectedIds = emptySet()
                                    isSelectionMode = false
                                    onClearAllNotifications()
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
                            selectedIds = if (selectedIds.size == filteredNotifications.size) emptySet()
                            else filteredNotifications.map { it.id }.toSet()
                        },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = selectedIds.isNotEmpty() && selectedIds.size == filteredNotifications.size,
                            onCheckedChange = { checked ->
                                selectedIds = if (checked) filteredNotifications.map { it.id }.toSet() else emptySet()
                            },
                            colors = CheckboxDefaults.colors(checkedColor = BluePrimary)
                        )
                        Text(
                            text = if (selectedIds.size == filteredNotifications.size) "Deselect All" else "Select All Filtered",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (selectedIds.isNotEmpty()) {
                        Button(
                            onClick = {
                                onDeleteSelectedNotifications(selectedIds)
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

            // Search Bar & Filter Chips Container
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
                            "Search alerts & notifications...",
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

                Spacer(modifier = Modifier.height(12.dp))

                // Filter Chips Row
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(AlertFilter.entries.toTypedArray()) { filter ->
                        val isSelected = filter == selectedFilter
                        val label = filter.name.lowercase().replaceFirstChar { it.uppercase() }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) BluePrimary else Color(0xFFF1F5F9))
                                .clickable { selectedFilter = filter }
                                .padding(horizontal = 18.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) Color.White else TextDark,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            // Notifications List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (filteredNotifications.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (notifications.isEmpty()) "No alert notifications yet.\nSystem alerts will appear here when scanning." else "No matching alerts found.",
                                color = TextMuted,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    items(filteredNotifications, key = { it.id }) { notification ->
                        val isSelected = notification.id in selectedIds
                        NotificationCard(
                            notification = notification,
                            isSelectionMode = isSelectionMode,
                            isSelected = isSelected,
                            onToggleSelect = {
                                selectedIds = if (isSelected) selectedIds - notification.id
                                else selectedIds + notification.id
                            },
                            onDelete = { onDeleteNotification(notification.id) }
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
fun NotificationCard(
    notification: NotificationItem,
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    onToggleSelect: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    var isExpanded by remember { mutableStateOf(false) }

    val (iconColor, iconVector) = when (notification.type) {
        NotificationType.INFO -> Pair(BluePrimary, Icons.Default.ConfirmationNumber)
        NotificationType.EVENT_ALERT -> Pair(Color(0xFF3B82F6), Icons.Default.EventRepeat)
        NotificationType.SYNC_SUCCESS -> Pair(ValidGreen, Icons.Default.CheckCircle)
        NotificationType.DUPLICATE_CHECKIN -> Pair(RejectedRed, Icons.Default.Warning)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                if (isSelectionMode) onToggleSelect()
                else isExpanded = !isExpanded
            },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) BluePrimary.copy(alpha = 0.12f)
            else MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
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

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(iconColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = iconVector,
                        contentDescription = notification.title,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = notification.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(Modifier.height(2.dp))

                    Text(
                        text = notification.description,
                        fontSize = 12.sp,
                        color = TextMuted,
                        maxLines = if (isExpanded) 10 else 1
                    )
                }

                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = notification.timestamp,
                        fontSize = 11.sp,
                        color = TextMuted
                    )

                    Spacer(Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable {
                                    isExpanded = !isExpanded
                                }
                                .padding(horizontal = 6.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isExpanded) "Hide" else "More",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = BluePrimary
                            )

                            Icon(
                                imageVector = if (isExpanded)
                                    Icons.Default.KeyboardArrowUp
                                else Icons.Default.KeyboardArrowDown,
                                contentDescription = "Expand or collapse alert info",
                                tint = BluePrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete notification",
                                tint = RejectedRed,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }
                }
            }

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
                        text = "ALERT DETAILS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = BluePrimary,
                        letterSpacing = 0.8.sp
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = notification.description,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 18.sp
                    )

                    Spacer(Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Category", fontSize = 11.sp, color = TextMuted)
                        Text(
                            notification.type.name,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = BluePrimary
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Timestamp", fontSize = 11.sp, color = TextMuted)
                        Text(
                            notification.timestamp,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

