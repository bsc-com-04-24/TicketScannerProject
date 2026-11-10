
package com.example.ticket_scanner.validation.data

import com.example.ticket_scanner.validation.model.TicketRecord
import java.util.Locale

/**
 * Temporary in-memory repository with no demo tickets.
 *
 * The backend API will eventually provide real ticket records
 * and handle check-in updates.
 */
object TicketRepository {

    private val records: MutableMap<String, TicketRecord> = mutableMapOf()

    fun find(code: String): TicketRecord? {
        val normalizedCode = code.trim().uppercase(Locale.ROOT)
        return records[normalizedCode]
    }

    fun markCheckedIn(code: String, time: Long, gate: String) {
        val normalizedCode = code.trim().uppercase(Locale.ROOT)

        records[normalizedCode]?.let {
            records[normalizedCode] = it.copy(
                checkedInAt = time,
                gate = gate
            )
        }
    }

    /**
     * Keeps support for tickets created within the app.
     * This is temporary until backend integration is completed.
     */
    fun add(record: TicketRecord) {
        val normalizedCode = record.code.trim().uppercase(Locale.ROOT)
        records[normalizedCode] = record.copy(code = normalizedCode)
    }
}
