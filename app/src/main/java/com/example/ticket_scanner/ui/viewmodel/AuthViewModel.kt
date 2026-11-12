package com.example.ticket_scanner.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ticket_scanner.network.AuthRepository
import com.example.ticket_scanner.network.AuthResponse
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
                    uiState = AuthUiState(
                        authResponse = response.body(),
                        successMessage = "Login successful!"
                    )
                } else {
                    uiState = AuthUiState(
                        errorMessage = "Login failed: ${response.code()} ${response.message()}"
                    )
                }
            } catch (e: IOException) {
                uiState = AuthUiState(
                    errorMessage = "Cannot connect to the server. Check internet connection."
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
        confirmPassword: String
    ) {
        val trimmedName = fullName.trim()
        val trimmedEmail = email.trim()

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
                // Pass email, password, and default role VALIDATOR to Spring Boot backend
                val response = authRepository.register(
                    email = trimmedEmail,
                    password = password,
                    role = "VALIDATOR"
                )

                if (response.isSuccessful) {
                    uiState = AuthUiState(
                        authResponse = response.body(),
                        successMessage = "Registration successful!"
                    )
                } else {
                    uiState = AuthUiState(
                        errorMessage = "Registration failed: ${response.code()} ${response.message()}"
                    )
                }
            } catch (e: IOException) {
                uiState = AuthUiState(
                    errorMessage = "Cannot connect to the server. Check internet connection."
                )
            } catch (e: Exception) {
                uiState = AuthUiState(
                    errorMessage = "Registration error: ${e.message}"
                )
            }
        }
    }
}
