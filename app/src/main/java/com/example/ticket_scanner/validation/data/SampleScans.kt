
package com.example.ticket_scanner.validation.data

import com.example.ticket_scanner.validation.model.ScannedTicket

data class SampleScan(
    val ticket: ScannedTicket,
    val expectedOutcome: String
)

object SampleScans {
    val all = emptyList<SampleScan>()
}
