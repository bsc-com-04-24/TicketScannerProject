package com.example.ticket_scanner.validation.data

import com.example.ticket_scanner.validation.model.ScannedTicket

/**
 * Sample INPUT: what the scanning feature will hand to validation.
 * Used by the demo picker so you can test validation before scanning is connected.
 * The barcode on the ticket encodes only the Ticket ID; type and event are optional extras.
 */
data class SampleScan(val ticket: ScannedTicket, val expectedOutcome: String)

object SampleScans {
    private const val EVENT = "Malawi Tech & Innovation Expo"
    private const val TYPE = "General Admission"

    val all = listOf(
        SampleScan(
            ScannedTicket("MTIE-2026-004281", TYPE, event = EVENT),
            "Valid (validate again: Already Used)"
        ),
        SampleScan(
            ScannedTicket("MTIE-2026-999999", TYPE, event = EVENT),
            "Invalid (not in records)"
        ),
        SampleScan(
            ScannedTicket("MTIE-2026-001150", TYPE, event = EVENT),
            "Expired"
        ),
        SampleScan(
            ScannedTicket("MTIE-2026-002077", TYPE, event = EVENT),
            "Already Used"
        )
    )
}