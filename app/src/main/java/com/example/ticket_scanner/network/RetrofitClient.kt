package com.example.ticket_scanner.network

import com.example.ticket_scanner.BuildConfig
import com.google.gson.Gson
import okhttp3.Authenticator
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.Route
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    val baseUrl: String = BuildConfig.BACKEND_BASE_URL

    private val gson = Gson()
    private val refreshClient = OkHttpClient.Builder()
        .addInterceptor(
            HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
        )
        .build()

    private val authenticator = AuthTokenAuthenticator(baseUrl, gson, refreshClient)

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(
            HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
        )
        .addInterceptor { chain ->
            val request = chain.request()
            val path = request.url.encodedPath
            val isAuthRequest = path.startsWith("/api/auth/")
            val token = AuthTokenStore.accessToken()
            if (isAuthRequest || token.isNullOrBlank()) {
                chain.proceed(request)
            } else {
                chain.proceed(
                    request.newBuilder()
                        .header("Authorization", "Bearer $token")
                        .build()
                )
            }
        }
        .authenticator(authenticator)
        .build()

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(httpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    val authApi: AuthApi = retrofit.create(AuthApi::class.java)
    val ticketApi: TicketApi = retrofit.create(TicketApi::class.java)

    private class AuthTokenAuthenticator(
        private val baseUrl: String,
        private val gson: Gson,
        private val refreshClient: OkHttpClient
    ) : Authenticator {

        override fun authenticate(route: Route?, response: Response): Request? {
            val path = response.request.url.encodedPath
            if (path.startsWith("/api/auth/")) return null

            if (responseCount(response) > 1) {
                AuthTokenStore.clear()
                return null
            }

            val failedToken = response.request.header("Authorization")
                ?.removePrefix("Bearer ")
            synchronized(this) {
                val currentToken = AuthTokenStore.accessToken() ?: return null
                if (currentToken != failedToken) {
                    return response.request.newBuilder()
                        .header("Authorization", "Bearer $currentToken")
                        .build()
                }

                val refreshToken = AuthTokenStore.refreshToken()
                if (refreshToken.isNullOrBlank()) {
                    AuthTokenStore.clear()
                    return null
                }

                val body = gson.toJson(RefreshTokenRequest(refreshToken))
                    .toRequestBody("application/json".toMediaType())
                val refreshRequest = Request.Builder()
                    .url("${baseUrl}api/auth/refresh")
                    .post(body)
                    .build()

                refreshClient.newCall(refreshRequest).execute().use { refreshResponse ->
                    if (!refreshResponse.isSuccessful) {
                        AuthTokenStore.clear()
                        return null
                    }

                    val json = refreshResponse.body?.string()
                    val tokens = json?.let {
                        gson.fromJson(it, AuthResponse::class.java)
                    }
                    if (tokens == null || tokens.token.isNullOrBlank() ||
                        tokens.refreshToken.isNullOrBlank()
                    ) {
                        AuthTokenStore.clear()
                        return null
                    }

                    AuthTokenStore.save(tokens, AuthTokenStore.isBiometricEnabled())
                    return response.request.newBuilder()
                        .header("Authorization", "Bearer ${tokens.token}")
                        .build()
                }
            }
        }

        private fun responseCount(response: Response): Int {
            var count = 1
            var previous = response.priorResponse
            while (previous != null) {
                count++
                previous = previous.priorResponse
            }
            return count
        }
    }
}
