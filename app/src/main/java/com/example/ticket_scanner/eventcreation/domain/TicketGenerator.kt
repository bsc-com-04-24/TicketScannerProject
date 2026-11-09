package com.example.ticket_scanner.eventcreation.domain

import com.example.ticket_scanner.eventcreation.model.EventDetails
import com.example.ticket_scanner.eventcreation.model.GeneratedTicket
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


object TicketGenerator {

    const val MAX_TICKETS = 100

    /**
     * @param codeExists tells us whether a code is already taken, so new tickets
     *                   never reuse an existing code.
     */
    fun generate(
        event: EventDetails,
        quantity: Int,
        ticketType: String,
        codeExists: (String) -> Boolean
    ): List<GeneratedTicket> {
        val prefix = codePrefix(event.name)
        val year = Calendar.getInstance().apply { timeInMillis = event.startMillis }
            .get(Calendar.YEAR)

        val tickets = ArrayList<GeneratedTicket>(quantity)
        var sequence = 0

        while (tickets.size < quantity) {
            sequence++
            val code = String.format(Locale.US, "%s-%d-%06d", prefix, year, sequence)
            if (codeExists(code)) continue

            val number = tickets.size + 1
            tickets.add(
                GeneratedTicket(
                    code = code,
                    ticketType = ticketType,
                    eventName = event.name,
                    venue = event.venue,
                    startMillis = event.startMillis,
                    ticketNumber = number,
                    totalTickets = quantity,
                    qrPayload = buildPayload(
                        code = code,
                        ticketType = ticketType,
                        eventName = event.name,
                        startMillis = event.startMillis,
                        ticketNumber = number,
                        totalTickets = quantity
                    )
                )
            )
        }
        return tickets
    }

    /** The text a ticket's QR code carries. */
    fun buildPayload(
        code: String,
        ticketType: String,
        eventName: String,
        startMillis: Long,
        ticketNumber: Int,
        totalTickets: Int
    ): String {
        val dateText = SimpleDateFormat("d MMM yyyy, h:mm a", Locale.getDefault())
            .format(Date(startMillis))
        return JSONObject().apply {
            put("ticketId", code)
            put("ticketType", ticketType)
            put("event", eventName)
            put("date", dateText)
            put("ticketNo", "$ticketNumber of $totalTickets")
        }.toString()
    }

    /** "Malawi Tech & Innovation Expo" becomes "MTIE". */
    private fun codePrefix(eventName: String): String {
        val initials = eventName
            .split(Regex("[^A-Za-z0-9]+"))
            .filter { it.isNotEmpty() }
            .map { it.first().uppercaseChar() }
            .joinToString("")
            .take(4)
        return initials.ifEmpty { "EVT" }
    }
}