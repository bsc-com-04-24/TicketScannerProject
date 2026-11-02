package com.example.ticket_scanner.validation.model

/** The four outcomes of validating a ticket. */
enum class ValidationStatus { VALID, INVALID, EXPIRED, ALREADY_USED }

/**
 * INPUT to validation: what the scanning/decoding feature hands over.
 * Only the code is required; the other fields are checked against our records when present.
 */
data class ScannedTicket(
    val ticketCode: String,
    val ticketType: String = "",
    val holderName: String = "",
    val event: String = ""
)

/** A ticket as stored in our records (the "database"). */
data class TicketRecord(
    val code: String,
    val type: String,
    val event: String,
    val purchasedAt: Long,
    val expiresAt: Long,
    val holderName: String? = null,   // not printed on the ticket, so optional
    val seatZone: String? = null,     // general admission has no seat
    val checkedInAt: Long? = null,
    val gate: String? = null
)

/** OUTPUT of validation: what the four result screens display. */
data class ValidationResult(
    val status: ValidationStatus,
    val ticketCode: String,
    val ticketType: String? = null,
    val holderName: String? = null,
    val event: String? = null,
    val seatZone: String? = null,
    val purchaseDate: String? = null,
    val checkInTime: String? = null,   // Valid
    val expiryDate: String? = null,    // Expired
    val firstCheckIn: String? = null,  // Already used
    val gate: String? = null,          // Already used
    val reason: String? = null         // Invalid
)