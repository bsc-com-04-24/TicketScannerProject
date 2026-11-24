package com.example.ticket_scanner.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class DownloadedTicket(
    val id: String,
    val code: String,
    val type: String,
    val holderName: String,
    val event: String,
    val seatZone: String,
    val status: String, // "VALID", "USED", "EXPIRED", "PENDING"
    val isSynced: Boolean, // true = synced, false = pending
    val lastUpdated: Long = System.currentTimeMillis()
) {
    val formattedLastUpdated: String
        get() = SimpleDateFormat("d MMM yyyy · h:mm a", Locale.getDefault()).format(Date(lastUpdated))
}

object DownloadedTicketRepository {
    private val tickets = mutableListOf<DownloadedTicket>()

    fun getTickets(): List<DownloadedTicket> = tickets.toList()

    fun getTicket(id: String): DownloadedTicket? = tickets.find { it.id == id || it.code == id }

    fun refreshTicket(id: String): DownloadedTicket? {
        val index = tickets.indexOfFirst { it.id == id || it.code == id }
        if (index != -1) {
            val current = tickets[index]
            val refreshed = current.copy(
                isSynced = true,
                lastUpdated = System.currentTimeMillis()
            )
            tickets[index] = refreshed
            return refreshed
        }
        return null
    }

    fun refreshAll(): List<DownloadedTicket> {
        val currentTime = System.currentTimeMillis()
        for (i in tickets.indices) {
            tickets[i] = tickets[i].copy(isSynced = true, lastUpdated = currentTime)
        }
        return getTickets()
    }

    fun addTicket(ticket: DownloadedTicket) {
        if (tickets.none { it.code == ticket.code }) {
            tickets.add(ticket)
        }
    }
}
