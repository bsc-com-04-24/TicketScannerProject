package com.example.ticket_scanner.model

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
    val status: ScanStatus,
    val isSynced: Boolean = true,
    val event: String = ""
)

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
