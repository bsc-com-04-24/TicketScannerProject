package com.example.ticket_scanner.network

import android.content.Context
import android.content.SharedPreferences

object AuthTokenStore {

    private const val PREFERENCES_NAME = "scanner_auth"
    private const val ACCESS_TOKEN_KEY = "access_token"
    private const val REFRESH_TOKEN_KEY = "refresh_token"
    private const val BIOMETRIC_ENABLED_KEY = "biometric_enabled"

    @Volatile
    private var preferences: SharedPreferences? = null

    fun initialize(context: Context) {
        if (preferences == null) {
            preferences = context.applicationContext.getSharedPreferences(
                PREFERENCES_NAME,
                Context.MODE_PRIVATE
            )
        }
    }

    fun accessToken(): String? = preferences?.getString(ACCESS_TOKEN_KEY, null)

    fun refreshToken(): String? = preferences?.getString(REFRESH_TOKEN_KEY, null)

    fun hasTokens(): Boolean =
        !accessToken().isNullOrBlank()

    fun isBiometricEnabled(): Boolean = preferences?.getBoolean(BIOMETRIC_ENABLED_KEY, false) == true

    fun setBiometricEnabled(enabled: Boolean) {
        preferences?.edit()?.putBoolean(BIOMETRIC_ENABLED_KEY, enabled)?.apply()
    }

    fun save(response: AuthResponse, enableBiometric: Boolean = false) {
        require(!response.token.isNullOrBlank()) { "The server returned no access token." }

        checkNotNull(preferences) { "AuthTokenStore has not been initialized." }
            .edit()
            .putString(ACCESS_TOKEN_KEY, response.token)
            .putString(REFRESH_TOKEN_KEY, response.token)
            .putBoolean(BIOMETRIC_ENABLED_KEY, enableBiometric)
            .apply()
    }

    fun clear() {
        preferences?.edit()
            ?.remove(ACCESS_TOKEN_KEY)
            ?.remove(REFRESH_TOKEN_KEY)
            ?.remove(BIOMETRIC_ENABLED_KEY)
            ?.apply()
    }
}
