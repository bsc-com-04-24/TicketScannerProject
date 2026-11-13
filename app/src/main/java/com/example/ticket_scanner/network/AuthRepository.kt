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
        accountType: AccountType
    ): retrofit2.Response<AuthResponse> {

        val request = RegisterRequest(
            email = email,
            password = password
        )

        return when (accountType) {
            AccountType.CUSTOMER -> RetrofitClient.authApi.register(request)
            AccountType.ADMIN -> RetrofitClient.authApi.registerAdmin(request)
        }
    }
}
