package dev.infa.page3.ui


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import cafe.adriel.voyager.navigator.Navigator
import dev.infa.page3.data.model.User
import dev.infa.page3.data.remote.SessionManager
import dev.infa.page3.navigation.AddressAmangementNav
import dev.infa.page3.navigation.AppViewModels.categoryViewModel
import dev.infa.page3.navigation.AppViewModels.productViewModel
import dev.infa.page3.navigation.AuthManager
import dev.infa.page3.navigation.CartScreenNav
import dev.infa.page3.navigation.CategoriesOverviewScreenNav
import dev.infa.page3.navigation.HomeMainScreen
import dev.infa.page3.navigation.OTPScreen
import dev.infa.page3.navigation.WishlistScreenNav
import dev.infa.page3.presentation.api.ApiService
import dev.infa.page3.presentation.repositary.AuthRepository
import dev.infa.page3.presentation.repository.UserRepository
import dev.infa.page3.presentation.repository.UserData
import dev.infa.page3.presentation.uiSatateClaases.AuthUiState
import dev.infa.page3.presentation.uiSatateClaases.UserProfileUiState
import dev.infa.page3.presentation.viewModel.AuthViewModel
import dev.infa.page3.presentation.viewModel.CartViewModel
import dev.infa.page3.presentation.viewModel.CategoryViewModel
import dev.infa.page3.presentation.viewModel.ProductViewModel
import dev.infa.page3.presentation.viewModel.UserProfileViewModel
import dev.infa.page3.presentation.viewmodel.WishlistViewModel
import dev.infa.page3.ui.auth.WelcomeScreen
import dev.infa.page3.ui.components.BottomNavBar
import dev.infa.page3.ui.components.ErrorScreen
import dev.infa.page3.ui.components.LoginButton
import dev.infa.page3.ui.components.SignUpButton
import dev.infa.page3.ui.components.TopBarScreen
import dev.infa.page3.ui.components.UnderDevelopmentDialog
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountDetailsScreen(
    navigator: Navigator,
    wishlistViewModel: WishlistViewModel,
    productViewModel: ProductViewModel,
    cartViewModel: CartViewModel,
    authViewModel: AuthViewModel,
    categoryViewModel: CategoryViewModel
) {
    val totalitem by cartViewModel.totalItems.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val userRepository = remember { UserRepository() }
    val userData = remember { userRepository.getUserData() }
    var currentTab by remember { mutableStateOf("profile") }
    var showEditDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    ModalNavigationDrawer(
        drawerContent = {
            AppSideBar(
                navigator = navigator,
                wishlistViewModel,
                cartViewModel = cartViewModel,
                categoryViewModel = categoryViewModel,
                productViewModel = productViewModel,
                authViewModel = authViewModel
            )
        },
        drawerState = drawerState
    ) {
        Scaffold(
            topBar = {
                TopBarScreen(
                    onClickMenu = { scope.launch { drawerState.open() } },
                    onClickShop = { navigator.push(CartScreenNav()) },
                    totalitem
                )
            },
            bottomBar = {
                BottomNavBar(
                    currentNav = currentTab,
                    navigator,
                    categoryViewModel = categoryViewModel,
                    productViewModel = productViewModel,
                    wishListViewModel = wishlistViewModel,
                    cartViewModel = cartViewModel,
                    authViewModel = authViewModel
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { innerPadding ->
            if (userData != null) {
                AccountDetailsContent(
                    userData = userData,
                    padding = innerPadding,
                    onEditClick = { showEditDialog = true }
                )

                if (showEditDialog) {
                    EditAccountDialog(
                        userData = userData,
                        isLoading = isLoading,
                        onDismiss = { showEditDialog = false },
                        onSave = { firstName, lastName, email, phone, address, city, state, postcode, country ->
                            isLoading = true
                            scope.launch {
                                try {
                                    // Update user data
                                    val updatedBilling = userData.billingAddress.copy(
                                        first_name = firstName,
                                        last_name = lastName,
                                        email = email,
                                        phone = phone,
                                        address_1 = address,
                                        city = city,
                                        state = state,
                                        postcode = postcode,
                                        country = country
                                    )

                                    val updatedShipping = userData.shippingAddress.copy(
                                        first_name = firstName,
                                        last_name = lastName,
                                        address_1 = address,
                                        city = city,
                                        state = state,
                                        postcode = postcode,
                                        country = country
                                    )

                                    val updatedUserData = userData.copy(
                                        firstName = firstName,
                                        lastName = lastName,
                                        email = email,
                                        phone = phone,
                                        billingAddress = updatedBilling,
                                        shippingAddress = updatedShipping
                                    )

                                    userRepository.saveUserData(updatedUserData)

                                    snackbarHostState.showSnackbar(
                                        message = "Profile updated successfully",
                                        duration = SnackbarDuration.Short
                                    )
                                    showEditDialog = false
                                } catch (e: Exception) {
                                    snackbarHostState.showSnackbar(
                                        message = "Failed to update profile",
                                        duration = SnackbarDuration.Short
                                    )
                                } finally {
                                    isLoading = false
                                }
                            }
                        }
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No user data found")
                }
            }
        }
    }
}

@Composable
fun AccountDetailsContent(
    userData: UserData,
    padding: PaddingValues,
    onEditClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .verticalScroll(rememberScrollState())
            .background(Color(0xFFF8F8F8))
    ) {
        // Header with Edit Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Account Details",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )

            Surface(
                onClick = onEditClick,
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF1A1A1A)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Edit",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Personal Information Card
        DetailCard(title = "Personal Information") {
            DetailRow(label = "First Name", value = userData.firstName)
            DetailRow(label = "Last Name", value = userData.lastName)
            DetailRow(label = "Email", value = userData.email)
            DetailRow(label = "Username", value = userData.username)
            DetailRow(label = "Phone", value = userData.phone)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Billing Address Card
        DetailCard(title = "Billing Address") {
            DetailRow(label = "Address", value = userData.billingAddress.address_1 ?: "Not provided")
            DetailRow(label = "City", value = userData.billingAddress.city ?: "Not provided")
            DetailRow(label = "State", value = userData.billingAddress.state ?: "Not provided")
            DetailRow(label = "Postcode", value = userData.billingAddress.postcode ?: "Not provided")
            DetailRow(label = "Country", value = userData.billingAddress.country ?: "Not provided")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Shipping Address Card
        DetailCard(title = "Shipping Address") {
            DetailRow(label = "Address", value = userData.shippingAddress.address_1 ?: "Not provided")
            DetailRow(label = "City", value = userData.shippingAddress.city ?: "Not provided")
            DetailRow(label = "State", value = userData.shippingAddress.state ?: "Not provided")
            DetailRow(label = "Postcode", value = userData.shippingAddress.postcode ?: "Not provided")
            DetailRow(label = "Country", value = userData.shippingAddress.country ?: "Not provided")
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun DetailCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A),
                modifier = Modifier.padding(bottom = 16.dp)
            )
            content()
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color(0xFF6B6B6B),
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 15.sp,
            color = Color(0xFF1A1A1A),
            fontWeight = FontWeight.Normal
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditAccountDialog(
    userData: UserData,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onSave: (firstName: String, lastName: String, email: String, phone: String,
             address: String, city: String, state: String, postcode: String, country: String) -> Unit
) {
    var firstName by remember { mutableStateOf(userData.firstName) }
    var lastName by remember { mutableStateOf(userData.lastName) }
    var email by remember { mutableStateOf(userData.email) }
    var phone by remember { mutableStateOf(userData.phone) }
    var address by remember { mutableStateOf(userData.billingAddress.address_1 ?: "") }
    var city by remember { mutableStateOf(userData.billingAddress.city ?: "") }
    var state by remember { mutableStateOf(userData.billingAddress.state ?: "") }
    var postcode by remember { mutableStateOf(userData.billingAddress.postcode ?: "") }
    var country by remember { mutableStateOf(userData.billingAddress.country ?: "") }

    Dialog(onDismissRequest = { if (!isLoading) onDismiss() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .heightIn(max = 600.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Edit Profile",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A)
                    )
                    if (!isLoading) {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color(0xFF1A1A1A)
                            )
                        }
                    }
                }

                HorizontalDivider(color = Color(0xFFE8E8E8))

                // Personal Information Section
                Text(
                    text = "Personal Information",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF6B6B6B)
                )

                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = { Text("First Name *") },
                    singleLine = true,
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF1A1A1A),
                        focusedLabelColor = Color(0xFF1A1A1A)
                    )
                )

                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = { Text("Last Name *") },
                    singleLine = true,
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF1A1A1A),
                        focusedLabelColor = Color(0xFF1A1A1A)
                    )
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email *") },
                    singleLine = true,
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF1A1A1A),
                        focusedLabelColor = Color(0xFF1A1A1A)
                    )
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = {
                        if (it.length <= 10 && it.all { char -> char.isDigit() }) {
                            phone = it
                        }
                    },
                    label = { Text("Phone *") },
                    singleLine = true,
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF1A1A1A),
                        focusedLabelColor = Color(0xFF1A1A1A)
                    )
                )

                // Address Section
                Text(
                    text = "Address Information",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF6B6B6B),
                    modifier = Modifier.padding(top = 8.dp)
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address *") },
                    singleLine = true,
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF1A1A1A),
                        focusedLabelColor = Color(0xFF1A1A1A)
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = city,
                        onValueChange = { city = it },
                        label = { Text("City *") },
                        singleLine = true,
                        enabled = !isLoading,
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF1A1A1A),
                            focusedLabelColor = Color(0xFF1A1A1A)
                        )
                    )

                    OutlinedTextField(
                        value = state,
                        onValueChange = { state = it },
                        label = { Text("State *") },
                        singleLine = true,
                        enabled = !isLoading,
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF1A1A1A),
                            focusedLabelColor = Color(0xFF1A1A1A)
                        )
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = postcode,
                        onValueChange = { postcode = it },
                        label = { Text("Postcode *") },
                        singleLine = true,
                        enabled = !isLoading,
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF1A1A1A),
                            focusedLabelColor = Color(0xFF1A1A1A)
                        )
                    )

                    OutlinedTextField(
                        value = country,
                        onValueChange = { country = it },
                        label = { Text("Country *") },
                        singleLine = true,
                        enabled = !isLoading,
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF1A1A1A),
                            focusedLabelColor = Color(0xFF1A1A1A)
                        )
                    )
                }

                Text(
                    text = "* Required fields",
                    fontSize = 12.sp,
                    color = Color(0xFF6B6B6B)
                )

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFFE8E8E8)),
                        color = Color.White,
                        enabled = !isLoading
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Cancel",
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF1A1A1A)
                            )
                        }
                    }

                    Surface(
                        onClick = {
                            onSave(
                                firstName.trim(),
                                lastName.trim(),
                                email.trim(),
                                phone.trim(),
                                address.trim(),
                                city.trim(),
                                state.trim(),
                                postcode.trim(),
                                country.trim()
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF1A1A1A),
                        enabled = !isLoading && firstName.isNotBlank() && lastName.isNotBlank() &&
                                email.isNotBlank() && phone.length == 10 && address.isNotBlank() &&
                                city.isNotBlank() && state.isNotBlank() && postcode.isNotBlank() &&
                                country.isNotBlank()
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.White
                                )
                            } else {
                                Text(
                                    text = "Save",
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}