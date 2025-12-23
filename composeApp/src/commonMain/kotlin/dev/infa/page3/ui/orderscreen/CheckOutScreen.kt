package dev.infa.page3.ui.orderscreen

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.Navigator
import dev.infa.page3.data.model.CartItemWithAttributes
import dev.infa.page3.data.model.CreateOrderRequest
import dev.infa.page3.data.remote.SessionManager
import dev.infa.page3.presentation.api.ApiService
import dev.infa.page3.presentation.repositary.OrderRepository
import dev.infa.page3.presentation.repository.UserRepository
import dev.infa.page3.presentation.uiSatateClaases.ListUiState
import dev.infa.page3.presentation.uiSatateClaases.SingleUiState
import dev.infa.page3.presentation.viewModel.AuthViewModel
import dev.infa.page3.presentation.viewModel.CartViewModel
import dev.infa.page3.presentation.viewModel.OrderViewModel
import dev.infa.page3.presentation.viewModel.ProductViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreenContent(
    navigator: Navigator,
    cartViewModel: CartViewModel,
    productViewModel: ProductViewModel,
) {
    val orderViewModel: OrderViewModel = remember {
        OrderViewModel(OrderRepository(ApiService(), SessionManager()))
    }

    val userRepository = remember { UserRepository() }
    val savedUserData = remember { userRepository.getUserData() }

    val orderCreationState by orderViewModel.orderCreationState.collectAsState()
    val paymentLinkState by orderViewModel.paymentLinkState.collectAsState()
    val cartState by cartViewModel.cartState.collectAsState()

    var useSameAsBilling by remember { mutableStateOf(false) }

    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var streetAddress by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    var zipCode by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var selectedPaymentMethod by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    // Auto-fill if using same as billing
    LaunchedEffect(useSameAsBilling) {
        if (useSameAsBilling && savedUserData != null) {
            val billing = savedUserData.billingAddress
            firstName = billing.first_name ?: ""
            lastName = billing.last_name ?: ""
            streetAddress = billing.address_1 ?: ""
            city = billing.city ?: ""
            state = billing.state ?: ""
            zipCode = billing.postcode ?: ""
            mobile = billing.phone ?: ""
        } else if (!useSameAsBilling) {
            // Clear fields when unchecked
            firstName = ""
            lastName = ""
            streetAddress = ""
            city = ""
            state = ""
            zipCode = ""
            mobile = ""
        }
    }

    // Handle payment link state
    LaunchedEffect(paymentLinkState) {
        when (paymentLinkState) {
            is SingleUiState.Success -> {
                val paymentUrl = (paymentLinkState as SingleUiState.Success).data.payment_link_url
                isLoading = false
                showSuccessDialog = true
            }
            is SingleUiState.Error -> {
                errorMessage = (paymentLinkState as SingleUiState.Error).message
                isLoading = false
            }
            is SingleUiState.Loading -> {
                isLoading = true
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Checkout") },
                navigationIcon = {
                    IconButton(onClick = { navigator.pop() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Shipping Address",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                // Show "Same as Billing" option only if billing address exists
                if (savedUserData?.billingAddress != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { useSameAsBilling = !useSameAsBilling },
                        colors = CardDefaults.cardColors(
                            containerColor = if (useSameAsBilling)
                                MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = useSameAsBilling,
                                onCheckedChange = { useSameAsBilling = it }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Use Billing Address",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Ship to the same address as billing",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }

                // First Name
                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = { Text("First Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isLoading && !useSameAsBilling
                )

                // Last Name
                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = { Text("Last Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isLoading && !useSameAsBilling
                )

                // Street Address
                OutlinedTextField(
                    value = streetAddress,
                    onValueChange = { streetAddress = it },
                    label = { Text("Street Address") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 3,
                    enabled = !isLoading && !useSameAsBilling
                )

                // City
                OutlinedTextField(
                    value = city,
                    onValueChange = { city = it },
                    label = { Text("City") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isLoading && !useSameAsBilling
                )

                // State
                OutlinedTextField(
                    value = state,
                    onValueChange = { state = it },
                    label = { Text("State") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isLoading && !useSameAsBilling
                )

                // Zip Code
                OutlinedTextField(
                    value = zipCode,
                    onValueChange = { zipCode = it },
                    label = { Text("Zip Code") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    enabled = !isLoading && !useSameAsBilling
                )

                // Mobile
                OutlinedTextField(
                    value = mobile,
                    onValueChange = { if (it.length <= 10) mobile = it },
                    label = { Text("Mobile Number") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    enabled = !isLoading && !useSameAsBilling,
                    prefix = { Text("+91 ") }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Payment Method Selection
                Text(
                    text = "Payment Method",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                // COD Option
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedPaymentMethod = "cod" },
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedPaymentMethod == "cod")
                            MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedPaymentMethod == "cod",
                            onClick = { selectedPaymentMethod = "cod" }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Cash on Delivery (COD)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Pay when your order is delivered",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Error Message
                if (errorMessage != null) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = errorMessage ?: "",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Place Order Button
                Button(
                    onClick = {
                        errorMessage = null

                        if (selectedPaymentMethod.isEmpty()) {
                            errorMessage = "Please select a payment method"
                            return@Button
                        }

                        val address = CreateOrderRequest(
                            firstName = firstName,
                            lastName = lastName,
                            streetAddress = streetAddress,
                            city = city,
                            state = state,
                            zipCode = zipCode,
                            mobile = mobile
                        )

                        if (validateAddress(address)) {
                            // For COD, create order directly
                            if (selectedPaymentMethod == "cod") {
                                val cartItems = when (cartState) {
                                    is ListUiState.Success -> (cartState as ListUiState.Success<CartItemWithAttributes>).data
                                    else -> emptyList()
                                }

                                if (cartItems.isEmpty()) {
                                    errorMessage = "Your cart is empty. Please add items to cart first."
                                    return@Button
                                }

                                orderViewModel.createCodOrder(
                                    address = address,
                                    cartItems = cartItems,
                                    onSuccess = {
                                        showSuccessDialog = true
                                    },
                                    onError = { error ->
                                        errorMessage = error
                                    }
                                )
                            } else {
                                val cartItems = when (cartState) {
                                    is ListUiState.Success -> (cartState as ListUiState.Success<CartItemWithAttributes>).data
                                    else -> emptyList()
                                }

                                if (cartItems.isEmpty()) {
                                    errorMessage = "Your cart is empty. Please add items to cart first."
                                    return@Button
                                }

                                orderViewModel.buyNow(
                                    address = address,
                                    cartItems = cartItems,
                                    onPaymentUrl = { url ->
                                        showSuccessDialog = true
                                    },
                                    onError = { error ->
                                        errorMessage = error
                                    }
                                )
                            }
                        } else {
                            errorMessage = "Please fill all fields correctly"
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !isLoading,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Place Order & Pay", fontSize = 16.sp)
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }

            // Loading Overlay
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier.padding(32.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator()
                            Text("Processing your order...")
                        }
                    }
                }
            }
        }

        // Success Dialog
        if (showSuccessDialog) {
            OrderSuccessScreenContent(navigator)
        }
    }
}


private fun validateAddress(address: CreateOrderRequest): Boolean {
    return address.firstName.isNotBlank() &&
            address.lastName.isNotBlank() &&
            address.streetAddress.isNotBlank() &&
            address.city.isNotBlank() &&
            address.state.isNotBlank() &&
            address.zipCode.isNotBlank() &&
            address.mobile.length == 10
}