package com.example.ticket_scanner.network

data class LoginRequest(
    val email: String,
    val password: String
)

data class AuthResponse(
    val token: String,
    val email: String,
    val role: String
)

data class RegisterRequest(
    val email: String,
    val password: String,
    val role: String = "VALIDATOR"
)
