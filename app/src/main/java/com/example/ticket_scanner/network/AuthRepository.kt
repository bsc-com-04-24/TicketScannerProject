package com.example.ticket_scanner.network

class AuthRepository {

    suspend fun login(
        email: String,
        password: String
    ): retrofit2.Response<AuthResponse> {

        val request = LoginRequest(email, password)

        return RetrofitClient.authApi.login(request)
    }

    suspend fun register(
        email: String,
        password: String,
        role: String = "VALIDATOR"
    ): retrofit2.Response<AuthResponse> {

        val request = RegisterRequest(
            email = email,
            password = password,
            role = role
        )

        return RetrofitClient.authApi.register(request)
    }
}
