package com.example.ticket_scanner.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ticket_scanner.network.AccountType
import com.example.ticket_scanner.network.AuthRepository
import com.example.ticket_scanner.network.AuthResponse
import com.example.ticket_scanner.network.AuthTokenStore
import com.google.gson.JsonParser
import kotlinx.coroutines.launch
import java.io.IOException

data class AuthUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val authResponse: AuthResponse? = null
)

class AuthViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    var uiState by mutableStateOf(AuthUiState())
        private set

    private val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")

    fun clearMessages() {
        uiState = uiState.copy(errorMessage = null, successMessage = null)
    }

    fun logout(sessionExpired: Boolean = false) {
        AuthTokenStore.clear()
        uiState = AuthUiState()
    }

    fun login(email: String, password: String) {
        val trimmedEmail = email.trim()

        if (trimmedEmail.isEmpty()) {
            uiState = AuthUiState(errorMessage = "Please enter your email or username.")
            return
        }

        if (password.isEmpty()) {
            uiState = AuthUiState(errorMessage = "Please enter your password.")
            return
        }

        viewModelScope.launch {
            uiState = AuthUiState(isLoading = true)

            try {
                val response = authRepository.login(trimmedEmail, password)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        AuthTokenStore.save(body, AuthTokenStore.isBiometricEnabled())
                    }
                    uiState = AuthUiState(
                        authResponse = body,
                        successMessage = "Login successful!"
                    )
                } else {
                    val errorMsg = parseErrorMessage(response)
                    uiState = AuthUiState(
                        errorMessage = errorMsg
                    )
                }
            } catch (e: IOException) {
                uiState = AuthUiState(
                    errorMessage = "Cannot connect to the server. Check network connection and server status."
                )
            } catch (e: Exception) {
                uiState = AuthUiState(
                    errorMessage = "Something went wrong: ${e.message}"
                )
            }
        }
    }

    fun register(
        fullName: String,
        email: String,
        password: String,
        confirmPassword: String,
        enableBiometric: Boolean,
        accountType: AccountType?,
        preserveCurrentSession: Boolean = false
    ) {
        val trimmedName = fullName.trim()
        val trimmedEmail = email.trim()

        if (accountType == null) {
            uiState = AuthUiState(errorMessage = "Please select an account type.")
            return
        }

        // 1. Required Fields Validation
        if (trimmedName.isEmpty()) {
            uiState = AuthUiState(errorMessage = "Full name is required.")
            return
        }

        if (trimmedEmail.isEmpty()) {
            uiState = AuthUiState(errorMessage = "Email address is required.")
            return
        }

        // 2. Email Format Validation
        if (!emailRegex.matches(trimmedEmail)) {
            uiState = AuthUiState(errorMessage = "Please enter a valid email address.")
            return
        }

        // 3. Password Validation
        if (password.isEmpty()) {
            uiState = AuthUiState(errorMessage = "Password is required.")
            return
        }

        if (password.length < 6) {
            uiState = AuthUiState(errorMessage = "Password must be at least 6 characters long.")
            return
        }

        // 4. Password Matching Validation
        if (password != confirmPassword) {
            uiState = AuthUiState(errorMessage = "Passwords do not match.")
            return
        }

        viewModelScope.launch {
            uiState = AuthUiState(isLoading = true)

            try {
                val response = authRepository.register(
                    email = trimmedEmail,
                    password = password,
                    accountType = accountType
                )

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body == null) {
                        uiState = AuthUiState(
                            errorMessage = "The server returned an empty registration response."
                        )
                        return@launch
                    }

                    if (preserveCurrentSession) {
                        uiState = AuthUiState(
                            successMessage = "Account created. Your current session is unchanged."
                        )
                        return@launch
                    }

                    AuthTokenStore.save(body, enableBiometric)
                    uiState = AuthUiState(
                        authResponse = body,
                        successMessage = "Registration successful!"
                    )
                } else {
                    val errorMsg = parseErrorMessage(response)
                    uiState = AuthUiState(
                        errorMessage = errorMsg
                    )
                }
            } catch (e: IOException) {
                uiState = AuthUiState(
                    errorMessage = "Cannot connect to the server. Check network connection and server status."
                )
            } catch (e: Exception) {
                uiState = AuthUiState(
                    errorMessage = "Registration error: ${e.message}"
                )
            }
        }
    }

    private fun parseErrorMessage(response: retrofit2.Response<*>): String {
        return try {
            val errorBody = response.errorBody()?.string()
            if (!errorBody.isNullOrBlank()) {
                val json = JsonParser.parseString(errorBody)
                if (json.isJsonObject) {
                    val message = json.asJsonObject.get("message")?.takeIf { it.isJsonPrimitive }?.asString
                    if (!message.isNullOrBlank()) {
                        return message
                    }
                }
            }
            "Request failed: HTTP ${response.code()} ${response.message()}"
        } catch (_: Exception) {
            "Request failed: HTTP ${response.code()} ${response.message()}"
        }
    }
}
