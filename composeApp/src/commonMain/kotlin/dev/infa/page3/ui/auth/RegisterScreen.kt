package dev.infa.page3.ui.auth


import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import page3.composeapp.generated.resources.splash
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
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
import page3.composeapp.generated.resources.Res
import page3.composeapp.generated.resources.login
import page3.composeapp.generated.resources.splash



@Composable
fun RegistrationScreen(
    mobile: String,
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

    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    var postcode by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf(mobile) }

    // React to state changes
    LaunchedEffect(uiState) {
        when (uiState) {
            is AuthUiState.RegisterSuccess -> {
                onNavigateToMain()
            }
            else -> Unit
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
            contentScale = ContentScale.Crop,
            alpha = 0.8f
        )

        // Light overlay for better text readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.2f))
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

        // Main Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(top = 100.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Text(
                text = "Complete Your Profile",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    fontSize = 28.sp
                )
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Fill in your details to get started",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = Color.White.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center
                )
            )

            Spacer(Modifier.height(24.dp))

            // Scrollable Form
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                // First & Last Name
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        CustomTextField(
                            value = firstName,
                            onValueChange = { firstName = it },
                            label = "First Name",
                            modifier = Modifier.weight(1f)
                        )
                        CustomTextField(
                            value = lastName,
                            onValueChange = { lastName = it },
                            label = "Last Name",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Email & Username
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        CustomTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = "Email",
                            keyboardType = KeyboardType.Email,
                            modifier = Modifier.weight(1f)
                        )
                        CustomTextField(
                            value = username,
                            onValueChange = { username = it },
                            label = "Username",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Address
                item {
                    CustomTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = "Address"
                    )
                }

                // City & State
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        CustomTextField(
                            value = city,
                            onValueChange = { city = it },
                            label = "City",
                            modifier = Modifier.weight(1f)
                        )
                        CustomTextField(
                            value = state,
                            onValueChange = { state = it },
                            label = "State",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Postcode & Country
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        CustomTextField(
                            value = postcode,
                            onValueChange = { postcode = it },
                            label = "Postcode",
                            keyboardType = KeyboardType.Number,
                            modifier = Modifier.weight(1f)
                        )
                        CustomTextField(
                            value = country,
                            onValueChange = { country = it },
                            label = "Country",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Phone
                item {
                    CustomTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = "Phone",
                        keyboardType = KeyboardType.Phone,
                        enabled = false
                    )
                }

                // Error Message
                if (uiState is AuthUiState.Error) {
                    item {
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
                                .padding(12.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Register Button
            Button(
                onClick = {
                    viewModel.registerWithDetails(
                        firstName = firstName,
                        lastName = lastName,
                        email = email,
                        username = username,
                        address = address,
                        city = city,
                        state = state,
                        postcode = postcode,
                        country = country,
                        phone = phone
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = uiState !is AuthUiState.Loading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (uiState is AuthUiState.Loading)
                        MaterialTheme.colorScheme.surfaceVariant
                    else MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
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
                        text = "COMPLETE REGISTRATION",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(
                label,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium,
                    color = Color.Black.copy(alpha = 0.6f)
                )
            )
        },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .border(
                2.dp,
                Color.Black,
                RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.White.copy(alpha = 0.3f),
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black,
            disabledTextColor = Color.Black
        ),
        textStyle = MaterialTheme.typography.bodyLarge.copy(
            fontWeight = FontWeight.Bold
        ),
        singleLine = true
    )
}


