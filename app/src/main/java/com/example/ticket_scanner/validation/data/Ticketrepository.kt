package com.example.ticket_scanner.validation.data

import com.example.ticket_scanner.localdb.LocalTicketStore
import com.example.ticket_scanner.localdb.StoredTicket
import com.example.ticket_scanner.validation.model.TicketRecord
import java.util.Calendar

/**
 * Where validation looks tickets up.
 *
 * 1. The phone's database first: the tickets that were created and saved on this phone.
 * 2. The built-in sample tickets second. TEMPORARY: delete [samples] when the team
 *    decides validation should use only real, saved tickets.
 */
object TicketRepository {

    private val samples: MutableMap<String, TicketRecord> =
        sampleRecords().associateBy { it.code }.toMutableMap()

    suspend fun find(code: String): TicketRecord? =
        LocalTicketStore.find(code)?.toRecord() ?: samples[code]

    suspend fun markCheckedIn(code: String, time: Long, gate: String) {
        if (LocalTicketStore.find(code) != null) {
            LocalTicketStore.markCheckedIn(code, time, gate)
        } else {
            samples[code]?.let { samples[code] = it.copy(checkedInAt = time, gate = gate) }
        }
    }

    private fun StoredTicket.toRecord() = TicketRecord(
        code = code,
        type = ticketType,
        event = eventName,
        purchasedAt = purchasedAt,
        expiresAt = expiresAt,
        checkedInAt = checkedInAt,
        gate = gate
    )

    // ---- TEMPORARY sample tickets (remove later) ----

    private const val EVENT = "Malawi Tech & Innovation Expo"
    private const val TYPE = "General Admission"

    /** Builds a timestamp in the phone's time zone. [month] is 1-12. */
    private fun at(year: Int, month: Int, day: Int, hour: Int, minute: Int): Long =
        Calendar.getInstance().apply {
            clear()
            set(year, month - 1, day, hour, minute)
        }.timeInMillis

    private fun sampleRecords() = listOf(
        TicketRecord(
            code = "MTIE-2026-004281", type = TYPE, event = EVENT,
            purchasedAt = at(2026, 10, 2, 10, 30),
            expiresAt = at(2026, 11, 14, 17, 0)
        ),
        TicketRecord(
            code = "MTIE-2026-001150", type = TYPE, event = EVENT,
            purchasedAt = at(2026, 9, 15, 14, 5),
            expiresAt = at(2026, 10, 10, 17, 0)
        ),
        TicketRecord(
            code = "MTIE-2026-002077", type = TYPE, event = EVENT,
            purchasedAt = at(2026, 9, 28, 9, 45),
            expiresAt = at(2026, 11, 14, 17, 0),
            checkedInAt = at(2026, 11, 14, 9, 42),
            gate = "Gate 2"
        )
    )
}