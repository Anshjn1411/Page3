package dev.infa.page3.ui

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
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
fun ProfileScreen(
    navigator: Navigator, wishlistViewModel: WishlistViewModel, productViewModel: ProductViewModel,

    cartViewModel: CartViewModel,
    authViewModel: AuthViewModel,
    categoryViewModel: CategoryViewModel
) {


    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val userRepository = remember { UserRepository() }
    val authState by AuthManager.authState.collectAsState()
    val userData = remember { userRepository.getUserData() }

    var currentTab by remember { mutableStateOf("profile") }
    var showLogoutDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    when (authState) {
        is AuthUiState.LoggedIn -> {
            ModalNavigationDrawer(
                drawerContent = {
                    AppSideBar(
                        navigator = navigator, wishlistViewModel, cartViewModel = cartViewModel,
                        categoryViewModel = categoryViewModel,
                        productViewModel = productViewModel,
                        authViewModel = authViewModel,
                    )
                },
                drawerState = drawerState
            ) {

                Scaffold(
                    topBar = {
                        TopBarScreen(
                            onClickMenu = { scope.launch { drawerState.open() } },
                            onClickShop = {
                                navigator.push(
                                    CartScreenNav(
                                    )
                                )
                            }
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
                    }
                ) { innerPadding ->
                if (userData != null) {
                    ProfileContent(
                        userData = userData,
                        padding = innerPadding,
                        navigator = navigator,
                        onLogout = {
                            showLogoutDialog = true
                        }
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No user data found")
                    }
                }

                // Logout Confirmation Dialog
                if (showLogoutDialog) {
                    LogoutDialog(
                        onDismiss = { showLogoutDialog = false },
                        onConfirm = {
                            authViewModel.logout()
                            navigator.push(OTPScreen())
                            showLogoutDialog = false
                        }
                    )
                }

        }
    }
        }

        else -> {
            ModalNavigationDrawer(
                drawerContent = {
                    AppSideBar(
                        navigator = navigator, wishlistViewModel, cartViewModel = cartViewModel,
                        categoryViewModel = categoryViewModel,
                        productViewModel = productViewModel,
                        authViewModel = authViewModel,
                    )
                },
                drawerState = drawerState
            ) {

                Scaffold(
                    topBar = {
                        TopBarScreen(
                            onClickMenu = { scope.launch { drawerState.open() } },
                            onClickShop = {
                                navigator.push(
                                    CartScreenNav(
                                    )
                                )
                            }
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
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier.fillMaxSize().padding(innerPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            LoginButton {navigator.push(OTPScreen())}
                            Spacer(Modifier.height(10.dp))
                            SignUpButton {navigator.push(OTPScreen()) }
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun ProfileContent(
    userData: UserData,
    padding: PaddingValues,
    navigator: Navigator,
    onLogout: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Profile Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Profile",
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Column {
                    Text(
                        text = "${userData.firstName} ${userData.lastName}",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = userData.email,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = userData.phone,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        // User Details Card
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Personal Information",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                ProfileDetailRow(label = "First Name", value = userData.firstName)
                ProfileDetailRow(label = "Last Name", value = userData.lastName)
                ProfileDetailRow(label = "Email", value = userData.email)
                ProfileDetailRow(label = "Username", value = userData.username)
                ProfileDetailRow(label = "Phone", value = userData.phone)
            }
        }

        // Billing Address Card
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Billing Address",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                val billing = userData.billingAddress
                ProfileDetailRow(label = "Address", value = billing.address_1 ?: "Not provided")
                ProfileDetailRow(label = "City", value = billing.city ?: "Not provided")
                ProfileDetailRow(label = "State", value = billing.state ?: "Not provided")
                ProfileDetailRow(label = "Postcode", value = billing.postcode ?: "Not provided")
                ProfileDetailRow(label = "Country", value = billing.country ?: "Not provided")
            }
        }

        // Shipping Address Card
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Shipping Address",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                val shipping = userData.shippingAddress
                ProfileDetailRow(label = "Address", value = shipping.address_1 ?: "Not provided")
                ProfileDetailRow(label = "City", value = shipping.city ?: "Not provided")
                ProfileDetailRow(label = "State", value = shipping.state ?: "Not provided")
                ProfileDetailRow(label = "Postcode", value = shipping.postcode ?: "Not provided")
                ProfileDetailRow(label = "Country", value = shipping.country ?: "Not provided")
            }
        }

        // Menu Items
        ProfileMenuItem(
            icon = Icons.Default.ShoppingBag,
            title = "My Orders",
            onClick = {  showDialog = true
            }
        )
        ProfileMenuItem(
            icon = Icons.Default.LocationOn,
            title = "Addresses",
            subtitle = "Manage your addresses",
            onClick = { showDialog = true }
        )
        ProfileMenuItem(
            icon = Icons.Default.Settings,
            title = "Settings",
            onClick = {showDialog = true }
        )
        ProfileMenuItem(
            icon = Icons.Default.Info,
            title = "About",
            onClick = { showDialog = true }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Logout Button
        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            )
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                contentDescription = "Logout"
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Logout")
        }
    }
    if (showDialog) {
        UnderDevelopmentDialog(
            onDismiss = { showDialog = false }
        )
    }
}
@Composable
fun ProfileDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
    }


@Composable
fun LogoutDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Logout",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "Are you sure you want to logout?",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Logout")
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.primary
                )
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Navigate"
            )
        }
    }
}

// ============================================
// FILE 5: EditProfileDialog.kt
// ============================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileDialog(
    user: User,
    onDismiss: () -> Unit,
    onSave: (firstName: String, lastName: String, email: String?, mobile: String, dob: String?) -> Unit,
    isLoading: Boolean
) {
    var firstName by remember { mutableStateOf(user.firstName) }
    var lastName by remember { mutableStateOf(user.lastName) }
    var email by remember { mutableStateOf(user.email ?: "") }
    var mobile by remember { mutableStateOf(user.mobile) }
    var dob by remember { mutableStateOf(user.dob ?: "") }

    Dialog(onDismissRequest = { if (!isLoading) onDismiss() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
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
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    if (!isLoading) {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close"
                            )
                        }
                    }
                }

                HorizontalDivider()

                // First Name
                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = { Text("First Name *") },
                    singleLine = true,
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "First Name"
                        )
                    }
                )

                // Last Name
                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = { Text("Last Name *") },
                    singleLine = true,
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Last Name"
                        )
                    }
                )

                // Email
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    singleLine = true,
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email
                    ),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = "Email"
                        )
                    }
                )

                // Mobile
                OutlinedTextField(
                    value = mobile,
                    onValueChange = {
                        if (it.length <= 10 && it.all { char -> char.isDigit() }) {
                            mobile = it
                        }
                    },
                    label = { Text("Mobile *") },
                    singleLine = true,
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone
                    ),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = "Mobile"
                        )
                    }
                )

                // Date of Birth
                OutlinedTextField(
                    value = dob,
                    onValueChange = { dob = it },
                    label = { Text("Date of Birth (YYYY-MM-DD)") },
                    singleLine = true,
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("YYYY-MM-DD") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Date of Birth"
                        )
                    }
                )

                Text(
                    text = "* Required fields",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            onSave(
                                firstName.trim(),
                                lastName.trim(),
                                email.trim().ifBlank { null },
                                mobile.trim(),
                                dob.trim().ifBlank { null }
                            )
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading && firstName.isNotBlank() &&
                                lastName.isNotBlank() && mobile.length == 10
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text("Save")
                        }
                    }
                }
            }
        }
    }
}


