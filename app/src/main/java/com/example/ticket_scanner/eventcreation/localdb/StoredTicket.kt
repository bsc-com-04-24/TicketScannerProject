package com.example.ticket_scanner.eventcreation.localdb

/** One ticket as saved in the phone's database. */
data class StoredTicket(
    val code: String,
    val eventId: Long,
    val eventName: String,
    val venue: String,
    val startMillis: Long,
    val ticketType: String,
    val ticketNumber: Int,
    val totalTickets: Int,
    val qrPayload: String,
    val purchasedAt: Long,
    val expiresAt: Long,
    val checkedInAt: Long?,
    val gate: String?
)

/** One event as saved in the phone's database. */
data class StoredEvent(
    val id: Long,
    val name: String,
    val description: String,
    val venue: String,
    val startMillis: Long,
    val endMillis: Long
)
