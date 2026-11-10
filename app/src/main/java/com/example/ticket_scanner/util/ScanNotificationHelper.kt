package com.example.ticket_scanner.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.ticket_scanner.MainActivity
import com.example.ticket_scanner.R
import com.example.ticket_scanner.model.HistoryStorageManager
import com.example.ticket_scanner.validation.model.ValidationResult
import com.example.ticket_scanner.validation.model.ValidationStatus

object ScanNotificationHelper {
    private const val CHANNEL_ID = "ticket_scan_channel_v2"
    private const val CHANNEL_NAME = "Ticket Scan Alerts"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Real-time alerts for ticket scanning (Valid, Invalid, Already Used, Expired)"
                enableLights(true)
                lightColor = Color.BLUE
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 250, 150, 250)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.createNotificationChannel(channel)
        }
    }

    fun triggerScanNotification(context: Context, validation: ValidationResult) {
        // Check if notifications are enabled in user settings
        if (!HistoryStorageManager.loadNotificationsEnabled(context)) {
            return
        }

        // On Android 13+ (API 33), check POST_NOTIFICATIONS permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        createNotificationChannel(context)

        val (title, shortMessage, bigText, color) = when (validation.status) {
            ValidationStatus.VALID -> {
                val holder = validation.holderName ?: "Attendee"
                val event = validation.event ?: "Event Check-in"
                val type = if (!validation.ticketType.isNullOrBlank()) " • ${validation.ticketType}" else ""
                val seat = if (!validation.seatZone.isNullOrBlank()) "\nSeat/Zone: ${validation.seatZone}" else ""
                val checkIn = if (!validation.checkInTime.isNullOrBlank()) "\nTime: ${validation.checkInTime}" else ""

                Quadruple(
                    "✅ Valid Ticket Checked In",
                    "Code: ${validation.ticketCode} ($holder$type)",
                    "Status: VALID (Checked In)\nTicket Code: ${validation.ticketCode}\nAttendee: $holder\nEvent: $event$seat$checkIn",
                    0xFF10B981.toInt() // Emerald Green
                )
            }

            ValidationStatus.INVALID -> {
                val reason = validation.reason ?: "Ticket not recognized in database"
                val event = if (!validation.event.isNullOrBlank()) "\nEvent: ${validation.event}" else ""

                Quadruple(
                    "❌ Invalid Ticket Scanned",
                    "Code: ${validation.ticketCode} - $reason",
                    "Status: INVALID (Entry Denied)\nTicket Code: ${validation.ticketCode}\nReason: $reason$event\nWarning: Do not admit holder.",
                    0xFFEF4444.toInt() // Red
                )
            }

            ValidationStatus.ALREADY_USED -> {
                val holder = validation.holderName ?: "Attendee"
                val firstCheckIn = validation.firstCheckIn ?: "Earlier today"
                val gate = if (!validation.gate.isNullOrBlank()) " at ${validation.gate}" else ""

                Quadruple(
                    "⛔ Ticket Already Used",
                    "Code: ${validation.ticketCode} - Checked in at $firstCheckIn",
                    "Status: ALREADY USED (Duplicate Entry Alert)\nTicket Code: ${validation.ticketCode}\nAttendee: $holder\nFirst Checked In: $firstCheckIn$gate\nWarning: Possible duplicate ticket usage!",
                    0xFF8B5CF6.toInt() // Purple
                )
            }

            ValidationStatus.EXPIRED -> {
                val holder = validation.holderName ?: "Attendee"
                val expiry = validation.expiryDate ?: "Past date"

                Quadruple(
                    "⚠️ Expired Ticket Scanned",
                    "Code: ${validation.ticketCode} - Expired on $expiry",
                    "Status: EXPIRED (Entry Denied)\nTicket Code: ${validation.ticketCode}\nAttendee: $holder\nExpired Date: $expiry\nWarning: Ticket has expired and is no longer valid.",
                    0xFFF59E0B.toInt() // Amber Orange
                )
            }
        }

        try {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager

            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(shortMessage)
                .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
                .setColor(color)
                .setColorized(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_STATUS)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build()

            val notificationId = (System.currentTimeMillis() % 100000).toInt()
            notificationManager?.notify(notificationId, notification)
        } catch (_: Exception) {
            // Handled gracefully if notification posting fails
        }
    }

    private data class Quadruple<A, B, C, D>(
        val first: A,
        val second: B,
        val third: C,
        val fourth: D
    )
}
