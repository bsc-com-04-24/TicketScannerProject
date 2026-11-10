
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

    val sampleNotifications = emptyList<NotificationItem>()
}
