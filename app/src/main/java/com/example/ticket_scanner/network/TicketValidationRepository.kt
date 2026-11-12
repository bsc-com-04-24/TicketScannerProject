package com.example.ticket_scanner.network

import com.example.ticket_scanner.validation.model.ScannedTicket
import com.example.ticket_scanner.validation.model.ValidationResult
import com.example.ticket_scanner.validation.model.ValidationStatus
import com.google.gson.JsonParseException
import com.google.gson.JsonParser
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

object TicketValidationRepository {

    suspend fun validate(scanned: ScannedTicket): ValidationResult {
        val code = scanned.ticketCode.trim()
        if (code.isBlank()) {
            return result(scanned, ValidationStatus.INVALID, "Ticket code is empty.")
        }

        if (!AuthTokenStore.hasTokens()) {
            AuthTokenStore.clear()
            return result(
                scanned,
                ValidationStatus.SESSION_EXPIRED,
                "Sign in with a provisioned validator account to continue."
            )
        }

        return try {
            val response = RetrofitClient.ticketApi.validate(TicketValidationRequest(code))
            if (response.isSuccessful) {
                val ticket = response.body()
                    ?: return result(
                        scanned,
                        ValidationStatus.UNAVAILABLE,
                        "The server returned an empty validation response."
                    )

                result(
                    scanned = scanned,
                    status = ValidationStatus.VALID,
                    reason = null,
                    event = ticket.eventTitle.takeIf(String::isNotBlank) ?: scanned.event,
                    ticketCode = ticket.ticketCode,
                    checkInTime = ticket.usedAt?.let(::formatDateTime)
                )
            } else {
                val message = response.errorBody()?.string()?.let(::messageFromErrorBody)
                when (response.code()) {
                    400 -> result(
                        scanned,
                        ValidationStatus.INVALID,
                        message ?: "Ticket not recognised."
                    )
                    401 -> {
                        AuthTokenStore.clear()
                        result(
                            scanned,
                            ValidationStatus.SESSION_EXPIRED,
                            "Your session expired. Sign in again to continue."
                        )
                    }
                    403 -> result(
                        scanned,
                        ValidationStatus.UNAVAILABLE,
                        "This account is not authorized to validate tickets. Use a provisioned VALIDATOR account."
                    )
                    404 -> result(
                        scanned,
                        ValidationStatus.UNAVAILABLE,
                        "The ticket validation endpoint was not found (HTTP 404)."
                    )
                    409 -> when {
                        message?.contains("already been used", ignoreCase = true) == true ->
                            result(scanned, ValidationStatus.ALREADY_USED, message)
                        message?.contains("expired", ignoreCase = true) == true ->
                            result(scanned, ValidationStatus.EXPIRED, message)
                        else -> result(
                            scanned,
                            ValidationStatus.INVALID,
                            message ?: "This ticket cannot be validated."
                        )
                    }
                    in 500..599 -> result(
                        scanned,
                        ValidationStatus.UNAVAILABLE,
                        "The server returned HTTP ${response.code()}. Try again later."
                    )
                    else -> result(
                        scanned,
                        ValidationStatus.UNAVAILABLE,
                        message ?: "The server returned HTTP ${response.code()}."
                    )
                }
            }
        } catch (error: UnknownHostException) {
            result(
                scanned,
                ValidationStatus.UNAVAILABLE,
                "Could not resolve the backend host. Check the configured PC Wi-Fi address."
            )
        } catch (error: ConnectException) {
            result(
                scanned,
                ValidationStatus.UNAVAILABLE,
                "Connection refused by the PC on port 1234. Check that the backend is running and port 1234 is published."
            )
        } catch (error: SocketTimeoutException) {
            result(
                scanned,
                ValidationStatus.UNAVAILABLE,
                "Connection timed out. Check that the phone and PC share a network and Windows Firewall allows TCP 1234."
            )
        } catch (error: IOException) {
            result(
                scanned,
                ValidationStatus.UNAVAILABLE,
                "Network request failed: ${error.message ?: "I/O error"}"
            )
        } catch (error: JsonParseException) {
            result(
                scanned,
                ValidationStatus.UNAVAILABLE,
                "The server returned an invalid response: ${error.message ?: "invalid JSON"}"
            )
        }
    }

    private fun messageFromErrorBody(body: String): String? {
        return try {
            val json = JsonParser.parseString(body)
            if (json.isJsonObject) {
                json.asJsonObject.get("message")?.takeIf { it.isJsonPrimitive }?.asString
            } else {
                null
            }
        } catch (_: JsonParseException) {
            null
        }
    }

    private fun result(
        scanned: ScannedTicket,
        status: ValidationStatus,
        reason: String?,
        event: String? = scanned.event.takeIf(String::isNotBlank),
        ticketCode: String = scanned.ticketCode.trim(),
        checkInTime: String? = null
    ) = ValidationResult(
        status = status,
        ticketCode = ticketCode,
        ticketType = scanned.ticketType.takeIf(String::isNotBlank),
        holderName = scanned.holderName.takeIf(String::isNotBlank),
        event = event,
        checkInTime = checkInTime,
        firstCheckIn = if (status == ValidationStatus.ALREADY_USED) checkInTime else null,
        reason = reason
    )

    private fun formatDateTime(value: String): String = try {
        LocalDateTime.parse(value).format(
            DateTimeFormatter.ofPattern("d MMM yyyy · h:mm a", Locale.getDefault())
        )
    } catch (error: DateTimeParseException) {
        value
    }
}
