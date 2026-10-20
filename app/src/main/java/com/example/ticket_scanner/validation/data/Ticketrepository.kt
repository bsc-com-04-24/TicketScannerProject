package com.example.ticket_scanner.validation.data

import com.example.ticket_scanner.validation.model.TicketRecord
import java.util.Calendar

/**
 * In-memory ticket records with sample data (they last until the app process ends).
 * Replace this one file with Room or an API later; nothing else needs to change.
 */
object TicketRepository {

    private val records: MutableMap<String, TicketRecord> =
        sampleRecords().associateBy { it.code }.toMutableMap()

    fun find(code: String): TicketRecord? = records[code]

    fun markCheckedIn(code: String, time: Long, gate: String) {
        records[code]?.let { records[code] = it.copy(checkedInAt = time, gate = gate) }
    }

    private const val EVENT = "Malawi Tech & Innovation Expo"
    private const val TYPE = "General Admission"

    /** Builds a timestamp in the phone's time zone. [month] is 1-12. */
    private fun at(year: Int, month: Int, day: Int, hour: Int, minute: Int): Long =
        Calendar.getInstance().apply {
            clear()
            set(year, month - 1, day, hour, minute)
        }.timeInMillis

    private fun sampleRecords() = listOf(
        // Matches the sample ticket. Valid on the first scan, then ALREADY_USED.
        TicketRecord(
            code = "MTIE-2026-004281", type = TYPE, event = EVENT,
            purchasedAt = at(2026, 10, 2, 10, 30),
            expiresAt = at(2026, 11, 14, 17, 0)        // 14 November 2026, 05:00 PM
        ),
        // Expired (expiry date already in the past)
        TicketRecord(
            code = "MTIE-2026-001150", type = TYPE, event = EVENT,
            purchasedAt = at(2026, 9, 15, 14, 5),
            expiresAt = at(2026, 10, 10, 17, 0)
        ),
        // Already used
        TicketRecord(
            code = "MTIE-2026-002077", type = TYPE, event = EVENT,
            purchasedAt = at(2026, 9, 28, 9, 45),
            expiresAt = at(2026, 11, 14, 17, 0),
            checkedInAt = at(2026, 11, 14, 9, 42),
            gate = "Gate 2"
        )
        // MTIE-2026-999999 is deliberately NOT here: it is the INVALID sample.
    )
}