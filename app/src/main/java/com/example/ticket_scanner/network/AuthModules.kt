package com.example.ticket_scanner.network

enum class AccountType {
    CUSTOMER,
    ADMIN
}

data class LoginRequest(
    val email: String,
    val password: String
)

data class AuthResponse(
    val token: String,
    val refreshToken: String
)

data class RegisterRequest(
    val email: String,
    val password: String
)

data class RefreshTokenRequest(
    val refreshToken: String
)
