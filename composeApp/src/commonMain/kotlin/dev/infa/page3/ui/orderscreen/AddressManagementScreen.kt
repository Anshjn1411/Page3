package dev.infa.page3.ui.orderscreen


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.seiko.imageloader.rememberImagePainter
import dev.infa.page3.data.model.Address
import dev.infa.page3.data.model.AddressDetail
import dev.infa.page3.data.model.CreateOrderRequest
import dev.infa.page3.data.model.OrderDetailed
import dev.infa.page3.data.model.Product
import dev.infa.page3.data.model.ProductDetailed
import dev.infa.page3.data.model.Rating
import dev.infa.page3.data.model.ReviewDetailed
import dev.infa.page3.data.remote.SessionManager
import dev.infa.page3.navigation.AuthManager
import dev.infa.page3.navigation.CheckoutScreen
import dev.infa.page3.presentation.api.ApiService
import dev.infa.page3.presentation.repositary.AddressRepository
import dev.infa.page3.presentation.uiSatateClaases.ListUiState
import dev.infa.page3.presentation.uiSatateClaases.SingleUiState
import dev.infa.page3.presentation.uiSatateClaases.AuthUiState
import dev.infa.page3.presentation.viewModel.AddressViewModel
import dev.infa.page3.presentation.viewModel.CartViewModel
import dev.infa.page3.presentation.viewModel.ReviewViewModel
import dev.infa.page3.ui.components.ErrorScreen
import dev.infa.page3.ui.components.LoadingScreen
import dev.infa.page3.ui.components.LoginButton
import dev.infa.page3.ui.components.SignUpButton

/**
 * Example: How to use AddressViewModel in a Composable
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressManagementScreen(
    navigator: Navigator
) {
    val viewModel: AddressViewModel = remember {
        AddressViewModel(AddressRepository(ApiService(), SessionManager()))
    }
    val userAddressesState by viewModel.userAddressesState.collectAsState()
    val createAddressState by viewModel.createAddressState.collectAsState()
    val deleteAddressState by viewModel.deleteAddressState.collectAsState()

    val scope = rememberCoroutineScope()
    var showCreateDialog by remember { mutableStateOf(false) }

    // Load addresses when screen opens
    LaunchedEffect(Unit) {
        viewModel.loadUserAddresses()
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Address") },
                navigationIcon = {
                    IconButton(onClick = { navigator.pop() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Address List
            when (val state = userAddressesState) {
                is ListUiState.Loading -> {
                    CircularProgressIndicator()
                }
                is ListUiState.Success -> {
                    AddressList(
                        addresses = state.data,
                        onSetDefault = { addressId ->
                            viewModel.setAddressAsDefault(
                                addressId = addressId,
                                onSuccess = { /* Show success message */ },
                                onError = { error -> /* Show error */ }
                            )
                        },
                        onEdit = { address -> /* Navigate to edit screen */ },
                        onDelete = { addressId ->
                            viewModel.deleteAddress(
                                addressId = addressId,
                                onSuccess = { /* Show success message */ },
                                onError = { error -> /* Show error */ }
                            )
                        }
                    )
                }
                is ListUiState.Empty -> {
                    Text("No addresses found. Add your first address!")
                }
                is ListUiState.Error -> {
                    Text("Error: ${state.message}", color = MaterialTheme.colorScheme.error)
                }
                is ListUiState.Idle -> {
                    // Initial state
                }
            }
        }


        // Create Address Dialog
        if (showCreateDialog) {
            CreateAddressDialog(
                onDismiss = {
                    showCreateDialog = false
                    viewModel.resetCreateAddressState()
                },
                onSubmit = { firstName, lastName, street, city, state, zip, mobile, isDefault ->
                    viewModel.createAddress(
                        firstName = firstName,
                        lastName = lastName,
                        streetAddress = street,
                        city = city,
                        state = state,
                        zipCode = zip,
                        mobile = mobile,
                        isDefault = isDefault,
                        onSuccess = {
                            showCreateDialog = false
                            // Show success message
                        },
                        onError = { error ->
                            // Show error message
                        }
                    )
                },
                isLoading = createAddressState is SingleUiState.Loading
            )
        }

    }

}

@Composable
fun AddressList(
    addresses: List<AddressDetail>,
    onSetDefault: (String) -> Unit,
    onEdit: (AddressDetail) -> Unit,
    onDelete: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        addresses.forEach { address ->
            AddressCard(
                address = address,
                onSetDefault = { onSetDefault(address._id) },
                onEdit = { onEdit(address) },
                onDelete = { onDelete(address._id) }
            )
        }
    }
}

@Composable
fun AddressCard(
    address: AddressDetail,
    onSetDefault: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Default badge
            if (address.isDefault) {
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = "Default",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Name
            Text(
                text = "${address.firstName} ${address.lastName}",
                style = MaterialTheme.typography.titleMedium
            )

            // Address
            Text(
                text = address.streetAddress,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "${address.city}, ${address.state} ${address.zipCode}",
                style = MaterialTheme.typography.bodyMedium
            )

            // Mobile
            Text(
                text = "Mobile: ${address.mobile}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Actions
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!address.isDefault) {
                    TextButton(onClick = onSetDefault) {
                        Text("Set as Default")
                    }
                }
                TextButton(onClick = onEdit) {
                    Text("Edit")
                }
                TextButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            }
        }
    }
}

/**
 * Example: Create/Edit Address Dialog
 */
@Composable
fun CreateAddressDialog(
    onDismiss: () -> Unit,
    onSubmit: (String, String, String, String, String, String, String, Boolean) -> Unit,
    isLoading: Boolean = false
) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var streetAddress by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    var zipCode by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var isDefault by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Address") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = { Text("First Name") },
                    enabled = !isLoading
                )
                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = { Text("Last Name") },
                    enabled = !isLoading
                )
                OutlinedTextField(
                    value = streetAddress,
                    onValueChange = { streetAddress = it },
                    label = { Text("Street Address") },
                    enabled = !isLoading
                )
                OutlinedTextField(
                    value = city,
                    onValueChange = { city = it },
                    label = { Text("City") },
                    enabled = !isLoading
                )
                OutlinedTextField(
                    value = state,
                    onValueChange = { state = it },
                    label = { Text("State") },
                    enabled = !isLoading
                )
                OutlinedTextField(
                    value = zipCode,
                    onValueChange = { zipCode = it },
                    label = { Text("Zip Code") },
                    enabled = !isLoading
                )
                OutlinedTextField(
                    value = mobile,
                    onValueChange = { mobile = it },
                    label = { Text("Mobile") },
                    enabled = !isLoading
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isDefault,
                        onCheckedChange = { isDefault = it },
                        enabled = !isLoading
                    )
                    Text("Set as default address")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSubmit(
                        firstName, lastName, streetAddress,
                        city, state, zipCode, mobile, isDefault
                    )
                },
                enabled = !isLoading && firstName.isNotBlank() && lastName.isNotBlank() &&
                        streetAddress.isNotBlank() && city.isNotBlank() && state.isNotBlank() &&
                        zipCode.isNotBlank() && mobile.isNotBlank()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Save")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isLoading) {
                Text("Cancel")
            }
        }
    )
}
