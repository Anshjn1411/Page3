package dev.infa.page3.ui.orderscreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.Navigator
import dev.infa.page3.presentation.viewModel.OrderViewModel
import dev.infa.page3.presentation.viewModel.PhonePePaymentState

/**
 * Screen states for the payment WebView flow.
 */
enum class PaymentWebViewStep {
    LOADING_WEBVIEW,
    PAYMENT_IN_PROGRESS,
    VERIFYING,
    SUCCESS,
    FAILED
}

/**
 * Payment WebView Screen that loads the PhonePe / WooCommerce checkout page
 * and monitors for payment success/failure.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentWebViewScreenContent(
    navigator: Navigator,
    paymentUrl: String,
    orderId: Int,
    orderNumber: String,
    total: String,
    orderViewModel: OrderViewModel
) {
    var currentStep by remember { mutableStateOf(PaymentWebViewStep.LOADING_WEBVIEW) }
    var currentUrl by remember { mutableStateOf(paymentUrl) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val phonePeState by orderViewModel.phonePePaymentState.collectAsState()

    // React to ViewModel state changes
    LaunchedEffect(phonePeState) {
        when (phonePeState) {
            is PhonePePaymentState.Verifying -> {
                currentStep = PaymentWebViewStep.VERIFYING
            }
            is PhonePePaymentState.Success -> {
                currentStep = PaymentWebViewStep.SUCCESS
            }
            is PhonePePaymentState.Error -> {
                errorMessage = (phonePeState as PhonePePaymentState.Error).message
                currentStep = PaymentWebViewStep.FAILED
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            if (currentStep == PaymentWebViewStep.LOADING_WEBVIEW ||
                currentStep == PaymentWebViewStep.PAYMENT_IN_PROGRESS
            ) {
                TopAppBar(
                    title = { Text("Complete Payment") },
                    navigationIcon = {
                        IconButton(onClick = {
                            // Warn before leaving? For now, just pop
                            navigator.pop()
                        }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentStep) {
                PaymentWebViewStep.LOADING_WEBVIEW,
                PaymentWebViewStep.PAYMENT_IN_PROGRESS -> {
                    // Show the WebView
                    PlatformWebView(
                        url = paymentUrl,
                        onPaymentSuccess = {
                            currentStep = PaymentWebViewStep.VERIFYING
                            // Verify the payment on the server
                            orderViewModel.verifyPayment(
                                orderId = orderId,
                                onSuccess = {
                                    currentStep = PaymentWebViewStep.SUCCESS
                                },
                                onError = { error ->
                                    errorMessage = error
                                    currentStep = PaymentWebViewStep.FAILED
                                }
                            )
                        },
                        onPaymentFailed = {
                            errorMessage = "Payment was cancelled or failed"
                            currentStep = PaymentWebViewStep.FAILED
                        },
                        onUrlChanged = { url ->
                            currentUrl = url
                            if (currentStep == PaymentWebViewStep.LOADING_WEBVIEW) {
                                currentStep = PaymentWebViewStep.PAYMENT_IN_PROGRESS
                            }
                        }
                    )
                }

                PaymentWebViewStep.VERIFYING -> {
                    // Verifying payment overlay
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(64.dp),
                            strokeWidth = 4.dp
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Verifying Payment...",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Please wait while we confirm your payment",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                PaymentWebViewStep.SUCCESS -> {
                    // Payment successful – show confirmation
                    OrderSuccessScreenContent(navigator)
                }

                PaymentWebViewStep.FAILED -> {
                    // Payment failed
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = "Failed",
                            modifier = Modifier.size(100.dp),
                            tint = MaterialTheme.colorScheme.error
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "Payment Failed",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = errorMessage ?: "Something went wrong during payment",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        // Retry button – reloads the WebView
                        Button(
                            onClick = {
                                errorMessage = null
                                currentStep = PaymentWebViewStep.LOADING_WEBVIEW
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Retry Payment", fontSize = 16.sp)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = {
                                navigator.pop()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Go Back", fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}
