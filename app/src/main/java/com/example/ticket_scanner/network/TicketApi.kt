package com.example.ticket_scanner.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

data class TicketValidationRequest(
    val ticketCode: String
)

data class TicketValidationResponse(
    val ticketId: Long,
    val ticketCode: String,
    val eventTitle: String,
    val status: String,
    val usedAt: String?,
    val message: String
)

interface TicketApi {

    @POST("api/tickets/validate")
    suspend fun validate(
        @Body request: TicketValidationRequest
    ): Response<TicketValidationResponse>
}
