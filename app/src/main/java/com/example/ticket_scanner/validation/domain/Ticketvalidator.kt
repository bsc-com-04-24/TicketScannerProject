package com.example.ticket_scanner.validation.domain

import com.example.ticket_scanner.validation.data.TicketRepository
import com.example.ticket_scanner.validation.model.ScannedTicket
import com.example.ticket_scanner.validation.model.TicketRecord
import com.example.ticket_scanner.validation.model.ValidationResult
import com.example.ticket_scanner.validation.model.ValidationStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * The validation rules, checked in this order:
 *  1. Code not in our records, or scanned details don't match the record -> INVALID
 *  2. Past its expiry date                                               -> EXPIRED
 *  3. Already checked in                                                 -> ALREADY_USED
 *  4. Otherwise                                                          -> VALID
 *     (and the ticket is marked as checked in, so the next scan is ALREADY_USED)
 */
object TicketValidator {

    private const val GATE_NAME = "Gate 1"

    fun validate(
        scanned: ScannedTicket,
        now: Long = System.currentTimeMillis()
    ): ValidationResult {
        val code = scanned.ticketCode.trim().uppercase(Locale.ROOT)

        val record = TicketRepository.find(code)
            ?: return invalid(scanned, code, "Ticket not recognised.")

        if (!scanned.matches(record)) {
            return invalid(scanned, code, "Ticket details do not match our records.")
        }

        return when {
            record.expiresAt < now ->
                record.toResult(ValidationStatus.EXPIRED).copy(
                    expiryDate = record.expiresAt.asDate()
                )

            record.checkedInAt != null ->
                record.toResult(ValidationStatus.ALREADY_USED).copy(
                    firstCheckIn = record.checkedInAt.asDateTime(),
                    gate = record.gate
                )

            else -> {
                TicketRepository.markCheckedIn(record.code, now, GATE_NAME)
                record.toResult(ValidationStatus.VALID).copy(
                    checkInTime = now.asDateTime()
                )
            }
        }
    }

    /** Scanned details are only compared when they were actually provided. */
    private fun ScannedTicket.matches(record: TicketRecord): Boolean =
        sameOrBlank(ticketType, record.type) &&
                sameOrBlank(holderName, record.holderName) &&
                sameOrBlank(event, record.event)

    /** A field is only compared when it was scanned AND our record has a value for it. */
    private fun sameOrBlank(scanned: String, expected: String?): Boolean =
        scanned.isBlank() || expected == null ||
                scanned.trim().equals(expected.trim(), ignoreCase = true)

    private fun invalid(scanned: ScannedTicket, code: String, reason: String) =
        ValidationResult(
            status = ValidationStatus.INVALID,
            ticketCode = code.ifBlank { "—" },
            ticketType = scanned.ticketType.takeIf { it.isNotBlank() },
            holderName = scanned.holderName.takeIf { it.isNotBlank() },
            event = scanned.event.takeIf { it.isNotBlank() },
            reason = reason
        )

    private fun TicketRecord.toResult(status: ValidationStatus) = ValidationResult(
        status = status,
        ticketCode = code,
        ticketType = type,
        holderName = holderName,
        event = event,
        seatZone = seatZone,
        purchaseDate = purchasedAt.asDateTime()
    )

    private fun Long.asDateTime(): String =
        SimpleDateFormat("d MMM yyyy · h:mm a", Locale.getDefault()).format(Date(this))

    private fun Long.asDate(): String =
        SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(Date(this))
}