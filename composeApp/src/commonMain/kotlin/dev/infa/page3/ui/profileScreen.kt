package dev.infa.page3.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Dialog
import cafe.adriel.voyager.navigator.Navigator
import dev.infa.page3.data.model.*
import dev.infa.page3.navigation.*
import dev.infa.page3.presentation.uiSatateClaases.*
import dev.infa.page3.presentation.viewModel.*
import dev.infa.page3.presentation.viewmodel.WishlistViewModel
import dev.infa.page3.ui.components.*
import dev.infa.page3.ui.otherScreen.ShippingPolicyScreen
import dev.infa.page3.ui.otherScreen.TermsAndConditionsScreen
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

    val authState by AuthManager.authState.collectAsState()
    val currentUser by AuthManager.currentUser.collectAsState()
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
                    if (currentUser != null) {
                        ModernProfileContent(
                            userData = currentUser!!,
                            padding = innerPadding,
                            navigator = navigator,
                            onLogout = { showLogoutDialog = true }
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
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
                            Spacer(Modifier.height(32.dp))
                            HelpAndPoliciesSection(navigator)

                            Spacer(modifier = Modifier.height(32.dp))
                            AppFooter()

                            Spacer(modifier = Modifier.height(24.dp))

                        }
                    }
                }
            }
        }
    }
}
@Composable
fun HelpAndPoliciesSection(navigator: Navigator) {

    Text(
        text = "HELP & POLICIES",
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = Color(0xFF6B6B6B),
        letterSpacing = 1.sp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    )

    Spacer(modifier = Modifier.height(12.dp))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        ProfileMenuCard(
            icon = Icons.Default.Info,
            title = "FAQ",
            onClick = { navigator.push(FaqScreenNav()) }
        )

        ProfileMenuCard(
            icon = Icons.Default.AssignmentReturn,
            title = "Return Policy",
            onClick = { navigator.push(RefundReturnPolicyScreenNav()) }
        )

        ProfileMenuCard(
            icon = Icons.Default.LocalShipping,
            title = "Shipping Policy",
            onClick = { navigator.push(ShippingPolicyScreenNav()) }
        )

        ProfileMenuCard(
            icon = Icons.Default.Gavel,
            title = "Terms & Conditions",
            onClick = { navigator.push(TermsAndConditionsScreenNav()) }
        )

        ProfileMenuCard(
            icon = Icons.Default.Lock,
            title = "Privacy Policy",
            onClick = { navigator.push(PrivacyPolicyScreenNav()) }
        )

        ProfileMenuCard(
            icon = Icons.Default.Phone,
            title = "Contact Us",
            onClick = { navigator.push(ContactUsScreenNav()) }
        )

        ProfileMenuCard(
            icon = Icons.Default.Info,
            title = "About Us",
            onClick = { navigator.push(AboutUsScreenNav()) }
        )
    }
}


@Composable
fun ModernProfileContent(
    userData: WcCustomer,
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
            text = "${userData.first_name} ${userData.last_name}",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1A1A)
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Email
        userData.email?.let {
            Text(
                text = it,
                fontSize = 14.sp,
                color = Color(0xFF6B6B6B)
            )
        }

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
        )
        {
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

        Text(
            text = "HELP & POLICIES",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF6B6B6B),
            letterSpacing = 1.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            ProfileMenuCard(
                icon = Icons.Default.Info,
                title = "FAQ",
                onClick = { navigator.push(FaqScreenNav()) }
            )

            ProfileMenuCard(
                icon = Icons.Default.AssignmentReturn,
                title = "Return Policy",
                onClick = { navigator.push(RefundReturnPolicyScreenNav())}
            )

            ProfileMenuCard(
                icon = Icons.Default.Info,
                title = "About Us",
                onClick = { navigator.push(AboutUsScreenNav()) }
            )

            ProfileMenuCard(
                icon = Icons.Default.Phone,
                title = "Contact Us",
                onClick = { navigator.push(ContactUsScreenNav()) }
            )

            ProfileMenuCard(
                icon = Icons.Default.Gavel,
                title = "Terms of Services",
                onClick = { navigator.push(TermsAndConditionsScreenNav()) }
            )

            ProfileMenuCard(
                icon = Icons.Default.Lock,
                title = "Privacy Policy",
                onClick = {navigator.push(PrivacyPolicyScreenNav())}
            )

            ProfileMenuCard(
                icon = Icons.Default.LocalShipping,
                title = "Shipping Policy",
                onClick = { navigator.push(ShippingPolicyScreenNav())}
            )
        }


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
        Spacer(modifier = Modifier.height(32.dp))
        AppFooter()
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


