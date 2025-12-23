package dev.infa.page3.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.infa.page3.data.remote.SessionManager
import dev.infa.page3.navigation.AuthManager
import dev.infa.page3.presentation.api.ApiService
import dev.infa.page3.presentation.repositary.AuthRepository
import dev.infa.page3.presentation.uiSatateClaases.AuthUiState
import dev.infa.page3.presentation.viewModel.AuthViewModel
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import page3.composeapp.generated.resources.Res
import page3.composeapp.generated.resources.login
import page3.composeapp.generated.resources.splash

@Composable
fun OtpVerificationScreen(
    mobile: String,
    onNavigateToRegister: (String) -> Unit,
    onNavigateToMain: () -> Unit,
    onNavigateBack: () -> Unit
) {
    LaunchedEffect(Unit) {
        AuthManager.init()
    }

    val viewModel: AuthViewModel = remember {
        AuthViewModel(
            AuthRepository(
                ApiService(),
                SessionManager()
            )
        )
    }
    val uiState by viewModel.uiState.collectAsState()
    var otp by remember { mutableStateOf("") }

    LaunchedEffect(uiState) {
        println("📌 Auth UI State changed: $uiState")

        when (uiState) {
            is AuthUiState.LoginSuccess,
            is AuthUiState.LoggedIn -> {
                println("✅ Navigation: User logged in successfully")
                onNavigateToMain()
            }

            is AuthUiState.NewUser -> {
                val state = uiState as AuthUiState.NewUser
                println("🆕 Navigation: New user detected, mobile = ${state.mobile}")
                onNavigateToRegister(state.mobile)
            }

            is AuthUiState.Error -> {
                val state = uiState as AuthUiState.Error
                println("❌ Error occurred: ${state.message}")
            }

            else -> {
                println("ℹ️ UI State: $uiState")
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Full-screen background image
        Image(
            painter = painterResource(Res.drawable.login),
            contentDescription = "Splash Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Back Button (Top Left)
        IconButton(
            onClick = { onNavigateBack() },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 48.dp, start = 16.dp)
                .statusBarsPadding()
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Go Back",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }

        // Center content: Welcome message and phone number
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Verify OTP",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    fontSize = 32.sp
                )
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Enter the 6-digit code sent to",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = Color.White.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center
                )
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = mobile,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            )
        }

        // Bottom content: OTP input and Verify button
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Error message
            if (uiState is AuthUiState.Error) {
                Text(
                    text = (uiState as AuthUiState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Color.White.copy(alpha = 0.9f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(12.dp)
                        .padding(bottom = 12.dp),
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(12.dp))
            }

            // OTP TextField with enhanced styling
            TextField(
                value = otp,
                onValueChange = {
                    if (it.length <= 6) {
                        otp = it
                        if (uiState is AuthUiState.Error) {
                            viewModel.clearError()
                        }
                    }
                },
                placeholder = {
                    Text(
                        "Enter 6-digit OTP",
                        style = MaterialTheme.typography.bodyLarge
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        2.dp,
                        Color.White.copy(alpha = 0.3f),
                        RoundedCornerShape(12.dp)
                    ),
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedContainerColor = Color.White.copy(alpha = 0.95f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.85f),
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                ),
                textStyle = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 8.sp,
                    textAlign = TextAlign.Center
                ),
                singleLine = true
            )

            Spacer(Modifier.height(16.dp))

            // Verify Button
            Button(
                onClick = { viewModel.verifyOtp(mobile, otp) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = uiState !is AuthUiState.Loading && otp.length == 6,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (uiState is AuthUiState.Loading)
                        MaterialTheme.colorScheme.surfaceVariant
                    else MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    disabledContainerColor = Color.Gray.copy(alpha = 0.5f)
                )
            ) {
                if (uiState is AuthUiState.Loading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "VERIFY OTP",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Didn't receive code?",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.White.copy(alpha = 0.9f)
                    )
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}


