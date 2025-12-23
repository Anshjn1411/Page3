package dev.infa.page3.ui

import androidx.compose.foundation.background
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
import androidx.compose.material3.Scaffold
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
import dev.infa.page3.navigation.AccountDetail
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
    val authState by AuthManager.authState.collectAsState()
    val userData = remember { userRepository.getUserData() }

    var currentTab by remember { mutableStateOf("profile") }
    var showLogoutDialog by remember { mutableStateOf(false) }

    when (authState) {
        is AuthUiState.LoggedIn -> {
            ModalNavigationDrawer(
                drawerContent = {
                    AppSideBar(
                        navigator = navigator,
                        wishlistViewModel,
                        cartViewModel = cartViewModel,
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
                    }
                ) { innerPadding ->
                    if (userData != null) {
                        ModernProfileContent(
                            userData = userData,
                            padding = innerPadding,
                            navigator = navigator,
                            onLogout = { showLogoutDialog = true }
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No user data found")
                        }
                    }

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
                        navigator = navigator,
                        wishlistViewModel,
                        cartViewModel = cartViewModel,
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
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier.fillMaxSize().padding(innerPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            LoginButton { navigator.push(OTPScreen()) }
                            Spacer(Modifier.height(10.dp))
                            SignUpButton { navigator.push(OTPScreen()) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ModernProfileContent(
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
            .verticalScroll(rememberScrollState())
            .background(Color(0xFFF8F8F8)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Profile Icon (Centered)
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(
                    color = Color(0xFFE8E8E8),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = "Profile",
                modifier = Modifier.size(60.dp),
                tint = Color(0xFF6B6B6B)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // User Name
        Text(
            text = "${userData.firstName} ${userData.lastName}",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1A1A)
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Email
        Text(
            text = userData.email,
            fontSize = 14.sp,
            color = Color(0xFF6B6B6B)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // My Account Section Title
        Text(
            text = "MY ACCOUNT",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF6B6B6B),
            letterSpacing = 1.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Menu Cards
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ProfileMenuCard(
                icon = Icons.Default.ShoppingBag,
                title = "Orders",
                onClick = { showDialog = true }
            )

            ProfileMenuCard(
                icon = Icons.Default.LocationOn,
                title = "Addresses",
                onClick = { navigator.push(AccountDetail()) }
            )

            ProfileMenuCard(
                icon = Icons.Default.Person,
                title = "Account details",
                onClick = { navigator.push(AccountDetail()) }
            )

            ProfileMenuCard(
                icon = Icons.Outlined.FavoriteBorder,
                title = "Wishlist",
                onClick = { navigator.push(WishlistScreenNav()) }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Logout Button
        Surface(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(56.dp),
            color = Color.Black,
            shape = RoundedCornerShape(12.dp),
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = "Logout",
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Logout",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Thank You Message
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.FavoriteBorder,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Thank you for choosing",
                    fontSize = 14.sp,
                    color = Color(0xFF6B6B6B),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "India's Best Fitness App",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Page3",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    if (showDialog) {
        UnderDevelopmentDialog(onDismiss = { showDialog = false })
    }
}

@Composable
fun ProfileMenuCard(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp),
        color = Color.White,
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Circle
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = Color(0xFFF5F5F5),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color(0xFF2C2C2C),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Title
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF1A1A1A),
                modifier = Modifier.weight(1f)
            )

            // Arrow
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Color(0xFF6B6B6B)
            )
        }
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


