package com.example.ticket_scanner

import com.example.ticket_scanner.validation.domain.TicketValidator
import com.example.ticket_scanner.validation.model.ScannedTicket
import com.example.ticket_scanner.validation.model.ValidationStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class TicketScanNotificationTest {

    @Test
    fun testValidationStatuses() {
        val validTicket = TicketValidator.validate(
            ScannedTicket("MTIE-2026-004281")
        )
        assertEquals(ValidationStatus.VALID, validTicket.status)

        val alreadyUsedTicket = TicketValidator.validate(
            ScannedTicket("MTIE-2026-002077")
        )
        assertEquals(ValidationStatus.ALREADY_USED, alreadyUsedTicket.status)

        val expiredTicket = TicketValidator.validate(
            ScannedTicket("MTIE-2026-001150")
        )
        assertEquals(ValidationStatus.EXPIRED, expiredTicket.status)

        val invalidTicket = TicketValidator.validate(
            ScannedTicket("MTIE-2026-UNKNOWN")
        )
        assertEquals(ValidationStatus.INVALID, invalidTicket.status)
    }
}
