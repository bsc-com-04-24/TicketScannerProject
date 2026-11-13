package com.example.ticket_scanner.network

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.JsonParseException
import com.google.gson.JsonParser
import java.nio.charset.StandardCharsets
import java.util.Base64

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

    fun hasRole(authority: String): Boolean {
        val payload = accessToken()?.split('.')?.getOrNull(1) ?: return false
        val decoded = try {
            Base64.getUrlDecoder().decode(payload)
        } catch (_: IllegalArgumentException) {
            return false
        }
        val claims = try {
            JsonParser.parseString(String(decoded, StandardCharsets.UTF_8))
        } catch (_: JsonParseException) {
            return false
        }
        // This only controls UI affordances; protected APIs still verify the signed token.
        return claims.isJsonObject &&
                claims.asJsonObject.get("role")?.let {
                    it.isJsonPrimitive && it.asJsonPrimitive.isString && it.asString == authority
                } == true
    }

    fun isBiometricEnabled(): Boolean = preferences?.getBoolean(BIOMETRIC_ENABLED_KEY, false) == true

    fun setBiometricEnabled(enabled: Boolean) {
        preferences?.edit()?.putBoolean(BIOMETRIC_ENABLED_KEY, enabled)?.apply()
    }

    fun save(response: AuthResponse, enableBiometric: Boolean = false) {
        require(!response.token.isNullOrBlank()) { "The server returned no access token." }
        require(!response.refreshToken.isNullOrBlank()) { "The server returned no refresh token." }

        checkNotNull(preferences) { "AuthTokenStore has not been initialized." }
            .edit()
            .putString(ACCESS_TOKEN_KEY, response.token)
            .putString(REFRESH_TOKEN_KEY, response.refreshToken)
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
