package com.example.ticket_scanner.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import java.math.BigDecimal

data class CreateEventRequest(
    val title: String,
    val venue: String,
    val location: String,
    val startTime: String,
    val endTime: String
)

data class EventResponse(
    val id: Long,
    val title: String,
    val venue: String,
    val location: String,
    val startTime: String,
    val endTime: String,
    val status: String,
    val organizerUsername: String
)

data class CreateTicketTypeRequest(
    val name: String,
    val price: BigDecimal,
    val totalQuantity: Int
)

data class TicketTypeResponse(
    val id: Long,
    val name: String,
    val price: BigDecimal,
    val totalQuantity: Int,
    val quantitySold: Int
)

data class TicketResponse(
    val id: Long,
    val orderId: Long?,
    val ticketCode: String,
    val eventId: Long,
    val eventTitle: String,
    val venue: String,
    val location: String,
    val eventDateTime: String?,
    val eventStartTime: String?,
    val eventEndTime: String?,
    val ticketTypeId: Long,
    val ticketTypeName: String,
    val ticketPrice: BigDecimal,
    val status: String,
    val issuedAt: String?,
    val usedAt: String?
)

data class GenerateTicketsRequest(
    val ticketCodes: List<String>
)

interface EventApi {
    @GET("api/events")
    suspend fun getAllEvents(): Response<List<EventResponse>>

    @POST("api/events")
    suspend fun createEvent(
        @Body request: CreateEventRequest
    ): Response<EventResponse>

    @POST("api/events/{eventId}/ticket-types")
    suspend fun createTicketType(
        @Path("eventId") eventId: Long,
        @Body request: CreateTicketTypeRequest
    ): Response<TicketTypeResponse>

    @POST("api/tickets/events/{eventId}/ticket-types/{ticketTypeId}/generate")
    suspend fun generateTickets(
        @Path("eventId") eventId: Long,
        @Path("ticketTypeId") ticketTypeId: Long,
        @Body request: GenerateTicketsRequest
    ): Response<List<TicketResponse>>
}
