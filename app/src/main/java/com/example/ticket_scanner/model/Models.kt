package com.example.ticket_scanner.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class ScanStatus {
    VALID,
    EXPIRED,
    CANCELLED,
    ALREADY_USED,
    PENDING,
    REJECTED
}

data class ScanItem(
    val id: String,
    val attendeeName: String,
    val ticketCode: String,
    val time: String,
    val timestampMillis: Long = System.currentTimeMillis(),
    val status: ScanStatus,
    val isSynced: Boolean = true,
    val event: String = ""
)

fun formatRelativeTime(timestampMillis: Long, now: Long = System.currentTimeMillis()): String {
    val diff = (now - timestampMillis).coerceAtLeast(0L)
    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24

    return when {
        seconds < 60 -> "Just now"
        minutes < 60 -> "${minutes}m ago"
        hours < 24 -> "${hours}h ago"
        days < 7 -> "${days}d ago"
        else -> {
            val sdf = SimpleDateFormat("d MMM, h:mm a", Locale.getDefault())
            sdf.format(Date(timestampMillis))
        }
    }
}

enum class NotificationType {
    INFO,
    EVENT_ALERT,
    SYNC_SUCCESS,
    DUPLICATE_CHECKIN
}

data class NotificationItem(
    val id: String,
    val title: String,
    val description: String,
    val timestamp: String,
    val type: NotificationType,
    val isRead: Boolean = false
)

data class ScanStats(
    val scannedCount: Int = 0,
    val validCount: Int = 0,
    val rejectedCount: Int = 0,
    val isOnline: Boolean = true
)

object MockData {
    val sampleScanStats = ScanStats()

    val sampleScanItems = emptyList<ScanItem>()

    val sampleNotifications = listOf(
        NotificationItem(
            id = "1",
            title = "Ticket list updated",
            description = "New tickets available for event",
            timestamp = "26 Apr 2025 • 3:45 PM",
            type = NotificationType.INFO,
            isRead = false
        ),
        NotificationItem(
            id = "2",
            title = "Event starting soon",
            description = "Malawi Music Concert starts in 1 hour",
            timestamp = "26 Apr 2025 • 3:00 PM",
            type = NotificationType.EVENT_ALERT,
            isRead = false
        ),
        NotificationItem(
            id = "3",
            title = "Synchronisation completed",
            description = "12 check-ins synced successfully",
            timestamp = "26 Apr 2025 • 2:20 PM",
            type = NotificationType.SYNC_SUCCESS,
            isRead = true
        ),
        NotificationItem(
            id = "4",
            title = "Duplicate check-in found",
            description = "Ticket TM-8855-4433 used on another device",
            timestamp = "26 Apr 2025 • 1:12 PM",
            type = NotificationType.DUPLICATE_CHECKIN,
            isRead = true
        )
    )
}
