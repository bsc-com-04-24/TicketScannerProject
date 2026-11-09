package com.example.ticket_scanner.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ticket_scanner.ui.theme.BluePrimary
import com.example.ticket_scanner.ui.theme.RejectedRed
import com.example.ticket_scanner.ui.theme.TextLight
import com.example.ticket_scanner.ui.theme.TextLightMuted
import com.example.ticket_scanner.ui.theme.TicketScannerTheme
import com.example.ticket_scanner.ui.theme.ValidGreen

enum class AuthMode {
    SIGN_IN,
    REGISTER
}

@Composable
fun AuthScreen(
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    successMessage: String? = null,
    onLoginClick: (email: String, password: String) -> Unit = { _, _ -> },
    onRegisterClick: (fullName: String, email: String, password: String, confirmPassword: String) -> Unit = { _, _, _, _ -> },
    onModeChange: () -> Unit = {}
) {
    var authMode by remember { mutableStateOf(AuthMode.SIGN_IN) }

    // Form inputs for Sign In
    var loginEmail by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }

    // Form inputs for Register
    var regFullName by remember { mutableStateOf("") }
    var regEmail by remember { mutableStateOf("") }
    var regPassword by remember { mutableStateOf("") }
    var regConfirmPassword by remember { mutableStateOf("") }

    var isPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Ticket Logo Header (same outline ticket icon as splash)
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .background(
                            color = BluePrimary,
                            shape = RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ConfirmationNumber,
                        contentDescription = "Ticket Logo",
                        tint = Color.White,
                        modifier = Modifier.size(44.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Event Ticket Scanner",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (authMode == AuthMode.SIGN_IN) "Sign in to access scanner" else "Create a validator account",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Error Message Display
                if (errorMessage != null) {
                    Text(
                        text = errorMessage,
                        color = RejectedRed,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }

                // Success Message Display
                if (successMessage != null) {
                    Text(
                        text = successMessage,
                        color = ValidGreen,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }

                Crossfade(targetState = authMode, label = "auth_form_transition") { mode ->
                    if (mode == AuthMode.SIGN_IN) {
                        // ==================== SIGN IN FORM ====================
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            OutlinedTextField(
                                value = loginEmail,
                                onValueChange = { loginEmail = it },
                                label = { Text("Email Address") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Email,
                                        contentDescription = "Email",
                                        tint = TextLightMuted
                                    )
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BluePrimary,
                                    unfocusedBorderColor = TextLightMuted.copy(alpha = 0.4f),
                                    focusedLabelColor = BluePrimary,
                                    unfocusedLabelColor = TextLightMuted,
                                    focusedTextColor = TextLight,
                                    unfocusedTextColor = TextLight
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            OutlinedTextField(
                                value = loginPassword,
                                onValueChange = { loginPassword = it },
                                label = { Text("Password") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Password",
                                        tint = TextLightMuted
                                    )
                                },
                                trailingIcon = {
                                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                        Icon(
                                            imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = if (isPasswordVisible) "Hide password" else "Show password",
                                            tint = TextLightMuted
                                        )
                                    }
                                },
                                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BluePrimary,
                                    unfocusedBorderColor = TextLightMuted.copy(alpha = 0.4f),
                                    focusedLabelColor = BluePrimary,
                                    unfocusedLabelColor = TextLightMuted,
                                    focusedTextColor = TextLight,
                                    unfocusedTextColor = TextLight
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            // Sign In Submit Button
                            Button(
                                onClick = { onLoginClick(loginEmail, loginPassword) },
                                enabled = !isLoading,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = BluePrimary
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text(
                                        text = "Sign In",
                                        color = Color.White,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // Switch to Register Mode Link
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Don't have an account? ",
                                    color = TextLightMuted,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "Register",
                                    color = BluePrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.clickable {
                                        authMode = AuthMode.REGISTER
                                        onModeChange()
                                    }
                                )
                            }
                        }
                    } else {
                        // ==================== REGISTER FORM ====================
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            // Full Name Input
                            OutlinedTextField(
                                value = regFullName,
                                onValueChange = { regFullName = it },
                                label = { Text("Full Name") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "Full Name",
                                        tint = TextLightMuted
                                    )
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BluePrimary,
                                    unfocusedBorderColor = TextLightMuted.copy(alpha = 0.4f),
                                    focusedLabelColor = BluePrimary,
                                    unfocusedLabelColor = TextLightMuted,
                                    focusedTextColor = TextLight,
                                    unfocusedTextColor = TextLight
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Email Address Input
                            OutlinedTextField(
                                value = regEmail,
                                onValueChange = { regEmail = it },
                                label = { Text("Email Address") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Email,
                                        contentDescription = "Email",
                                        tint = TextLightMuted
                                    )
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BluePrimary,
                                    unfocusedBorderColor = TextLightMuted.copy(alpha = 0.4f),
                                    focusedLabelColor = BluePrimary,
                                    unfocusedLabelColor = TextLightMuted,
                                    focusedTextColor = TextLight,
                                    unfocusedTextColor = TextLight
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Password Input
                            OutlinedTextField(
                                value = regPassword,
                                onValueChange = { regPassword = it },
                                label = { Text("Password") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Password",
                                        tint = TextLightMuted
                                    )
                                },
                                trailingIcon = {
                                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                        Icon(
                                            imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = if (isPasswordVisible) "Hide password" else "Show password",
                                            tint = TextLightMuted
                                        )
                                    }
                                },
                                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BluePrimary,
                                    unfocusedBorderColor = TextLightMuted.copy(alpha = 0.4f),
                                    focusedLabelColor = BluePrimary,
                                    unfocusedLabelColor = TextLightMuted,
                                    focusedTextColor = TextLight,
                                    unfocusedTextColor = TextLight
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Confirm Password Input
                            OutlinedTextField(
                                value = regConfirmPassword,
                                onValueChange = { regConfirmPassword = it },
                                label = { Text("Confirm Password") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Confirm Password",
                                        tint = TextLightMuted
                                    )
                                },
                                trailingIcon = {
                                    IconButton(onClick = { isConfirmPasswordVisible = !isConfirmPasswordVisible }) {
                                        Icon(
                                            imageVector = if (isConfirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = if (isConfirmPasswordVisible) "Hide password" else "Show password",
                                            tint = TextLightMuted
                                        )
                                    }
                                },
                                visualTransformation = if (isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BluePrimary,
                                    unfocusedBorderColor = TextLightMuted.copy(alpha = 0.4f),
                                    focusedLabelColor = BluePrimary,
                                    unfocusedLabelColor = TextLightMuted,
                                    focusedTextColor = TextLight,
                                    unfocusedTextColor = TextLight
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            // Register Submit Button
                            Button(
                                onClick = {
                                    onRegisterClick(
                                        regFullName,
                                        regEmail,
                                        regPassword,
                                        regConfirmPassword
                                    )
                                },
                                enabled = !isLoading,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = BluePrimary
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text(
                                        text = "Register",
                                        color = Color.White,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // Switch to Sign In Mode Link
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Already have an account? ",
                                    color = TextLightMuted,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "Sign In",
                                    color = BluePrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.clickable {
                                        authMode = AuthMode.SIGN_IN
                                        onModeChange()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AuthScreenSignInPreview() {
    TicketScannerTheme {
        AuthScreen()
    }
}
