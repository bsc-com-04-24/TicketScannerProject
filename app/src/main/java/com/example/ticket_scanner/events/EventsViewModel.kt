package com.example.ticket_scanner.events

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ticket_scanner.network.EventResponse
import com.example.ticket_scanner.network.RetrofitClient
import kotlinx.coroutines.launch

class EventsViewModel : ViewModel() {

    var events by mutableStateOf<List<EventResponse>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    init {
        fetchEvents()
    }

    fun fetchEvents() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val response = RetrofitClient.eventApi.getAllEvents()
                if (response.isSuccessful) {
                    val rawEvents = response.body() ?: emptyList()
                    events = rawEvents.sortedByDescending { it.id }
                } else {
                    errorMessage = "Failed to load events (${response.code()})"
                }
            } catch (e: Exception) {
                errorMessage = "Network error: ${e.message}"
            }
            isLoading = false
        }
    }
}
