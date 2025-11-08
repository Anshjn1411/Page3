package dev.infa.page3.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.Navigator
import com.seiko.imageloader.rememberImagePainter
import dev.infa.page3.data.model.CartItemWithAttributes
import dev.infa.page3.navigation.CartScreenNav
import dev.infa.page3.navigation.CategoriesOverviewScreenNav
import dev.infa.page3.navigation.CheckoutScreen
import dev.infa.page3.navigation.ProductDetail
import dev.infa.page3.navigation.ProfileScreenNav
import dev.infa.page3.navigation.WishlistScreenNav
import dev.infa.page3.presentation.repository.CartRepository
import dev.infa.page3.presentation.viewModel.CartViewModel
import dev.infa.page3.presentation.uiSatateClaases.ListUiState
import dev.infa.page3.presentation.uiSatateClaases.OperationUiState
import dev.infa.page3.presentation.viewModel.AuthViewModel
import dev.infa.page3.presentation.viewModel.CategoryViewModel
import dev.infa.page3.presentation.viewModel.ProductViewModel
import dev.infa.page3.presentation.viewmodel.WishlistViewModel
import dev.infa.page3.ui.components.BottomNavBar
import dev.infa.page3.ui.components.EmptyScreen
import dev.infa.page3.ui.components.ErrorScreen
import dev.infa.page3.ui.components.LoadingScreen
import dev.infa.page3.ui.components.TopBarScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(navigator: Navigator, wishlistViewModel: WishlistViewModel, productViewModel: ProductViewModel,

               cartViewModel: CartViewModel,
               authViewModel: AuthViewModel,
               categoryViewModel: CategoryViewModel) {

    val cartState by cartViewModel.cartState.collectAsState()
    val actionState by cartViewModel.cartActionState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        cartViewModel.loadCart()
    }

    LaunchedEffect(actionState) {
        when (actionState) {
            is OperationUiState.Success -> {
                snackbarHostState.showSnackbar("Cart updated successfully")
                cartViewModel.resetActionState()
            }
            is OperationUiState.Error -> {
                snackbarHostState.showSnackbar(
                    (actionState as OperationUiState.Error).message
                )
                cartViewModel.resetActionState()
            }
            else -> {}
        }
    }
    val totalitem by cartViewModel.totalItems.collectAsState()

    var currentTab by remember { mutableStateOf("cart") }

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
                    },
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
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (val state = cartState) {
                    is ListUiState.Idle,
                    is ListUiState.Loading -> LoadingScreen()

                    is ListUiState.Empty -> EmptyScreen("Your cart is empty")

                    is ListUiState.Success -> {
                        val items = state.data
                        CartContent(
                            cart = items,
                            onProductClick = { productId ->
                                navigator.push(
                                    ProductDetail(
                                        productId,
                                    )
                                )
                            },
                            onCheckout = {
                                navigator.push(
                                    CheckoutScreen(
                                    )
                                )
                            },
                            onRemoveItem = { productId, attributes ->
                                cartViewModel.removeFromCart(productId, attributes)
                            },
                            onUpdateQuantity = { productId, attributes, quantity ->
                                cartViewModel.updateQuantity(productId, attributes, quantity)
                            }
                        )
                    }

                    is ListUiState.Error -> ErrorScreen(
                        (state as ListUiState.Error).message ?: "Failed to fetch cart"
                    )
                }
            }
        }
    }
}

@Composable
private fun CartContent(
    cart: List<CartItemWithAttributes>,
    onProductClick: (String) -> Unit,
    onCheckout: () -> Unit,
    onRemoveItem: (Int, Map<String, String>) -> Unit,
    onUpdateQuantity: (Int, Map<String, String>, Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                TopSection(
                    name = "Cart",
                    totalItems = cart.size
                )
            }

            items(
                items = cart,
                key = { "${it.id}-${it.selectedAttributes.hashCode()}" }
            ) { cartItem ->
                CartItemCard(
                    cartItem = cartItem,
                    onClick = { onProductClick(cartItem.id.toString()) },
                    onDelete = { onRemoveItem(cartItem.id, cartItem.selectedAttributes) },
                    onQuantityChange = { newQty ->
                        onUpdateQuantity(cartItem.id, cartItem.selectedAttributes, newQty)
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                CartBottomBar(
                    cartItems = cart,
                    onCheckout = onCheckout
                )
            }
        }
    }
}

@Composable
private fun CartHeader(itemCount: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "$itemCount ${if (itemCount == 1) "Item" else "Items"} in Cart",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Icon(
                imageVector = Icons.Default.ShoppingCart,
                contentDescription = "Cart",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
private fun CartItemCard(
    cartItem: CartItemWithAttributes,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onQuantityChange: (Int) -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showQuantityDialog by remember { mutableStateOf(false) }
    var newQuantity by remember { mutableStateOf(cartItem.quantity) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val imageUrl = cartItem.images.firstOrNull()?.src ?: ""
            val painter = rememberImagePainter(url = imageUrl)
            Image(
                painter = painter,
                contentDescription = cartItem.shortDescription,
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = cartItem.shortDescription ?: "Unknown Product",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (cartItem.selectedAttributes.isNotEmpty()) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            cartItem.selectedAttributes.forEach { (attributeName, attributeValue) ->
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${attributeName.replaceFirstChar { it.uppercase() }}:",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Surface(
                                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = attributeValue.replaceFirstChar { it.uppercase() },
                                            style = MaterialTheme.typography.bodySmall,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Only use product.price
                val itemPrice = cartItem.price?.toDoubleOrNull() ?: 0.0
                Text(
                    text = "₹${itemPrice}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        onClick = { showQuantityDialog = true },
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Qty: ${cartItem.quantity}",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Change quantity",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Remove",
                            tint = Color(0xFFD32F2F),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = Color(0xFFD32F2F)
                )
            },
            title = {
                Text(
                    text = "Remove Item?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("Are you sure you want to remove '${cartItem.shortDescription}' from your cart?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD32F2F)
                    )
                ) {
                    Text("Remove")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showQuantityDialog) {
        AlertDialog(
            onDismissRequest = { showQuantityDialog = false },
            title = { Text("Update Quantity") },
            text = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { if (newQuantity > 1) newQuantity-- },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Decrease"
                        )
                    }

                    Text(
                        text = newQuantity.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )

                    IconButton(
                        onClick = { newQuantity++ },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Increase"
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onQuantityChange(newQuantity)
                        showQuantityDialog = false
                    }
                ) {
                    Text("Update")
                }
            },
            dismissButton = {
                TextButton(onClick = { showQuantityDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun CartBottomBar(
    cartItems: List<CartItemWithAttributes>,
    onCheckout: () -> Unit
) {
    // Calculate total using only product.price
    val totalPrice = cartItems.sumOf { item ->
        val itemPrice = item.price?.toDoubleOrNull() ?: 0.0
        itemPrice * item.quantity
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Price Details",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Show item breakdown
                    cartItems.forEach { item ->
                        val itemPrice = item.price?.toDoubleOrNull() ?: 0.0
                        val itemTotal = itemPrice * item.quantity

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${item.shortDescription} (x${item.quantity})",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "₹${itemTotal}",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Medium
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Divider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Total Amount",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "₹${totalPrice}",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onCheckout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Proceed to Checkout",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}