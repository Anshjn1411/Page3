package dev.infa.page3.ui.productscreen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.Navigator
import com.seiko.imageloader.rememberImagePainter
import dev.infa.page3.data.model.Product
import dev.infa.page3.data.model.WcAttributes
import dev.infa.page3.data.remote.SessionManager
import dev.infa.page3.navigation.CartScreenNav
import dev.infa.page3.presentation.api.ApiService
import dev.infa.page3.presentation.repositary.RatingRepository
import dev.infa.page3.presentation.repositary.ReviewRepository
import dev.infa.page3.presentation.viewModel.ProductViewModel
import dev.infa.page3.presentation.viewModel.RatingViewModel
import dev.infa.page3.presentation.uiSatateClaases.SingleUiState
import dev.infa.page3.presentation.viewModel.AuthViewModel
import dev.infa.page3.presentation.viewModel.CartViewModel
import dev.infa.page3.presentation.viewModel.CategoryViewModel
import dev.infa.page3.presentation.viewModel.ReviewViewModel
import dev.infa.page3.presentation.viewmodel.WishlistViewModel
import dev.infa.page3.ui.AppSideBar
import dev.infa.page3.ui.components.BottomNavBar
import dev.infa.page3.ui.components.ErrorScreen
import dev.infa.page3.ui.components.LoadingScreen
import dev.infa.page3.ui.components.TopBarScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    productId: String,
    navigator: Navigator,
    wishlistViewModel: WishlistViewModel,
    productViewModel: ProductViewModel,
    cartViewModel: CartViewModel,
    authViewModel: AuthViewModel,
    categoryViewModel: CategoryViewModel
) {
    val ratingViewModel: RatingViewModel = remember {
        RatingViewModel(RatingRepository(ApiService(), SessionManager()))
    }

    val reviewViewModel: ReviewViewModel = remember {
        ReviewViewModel(ReviewRepository(ApiService(), SessionManager()))
    }

    val productState by productViewModel.selectedProductState.collectAsState()
    val averageRating by ratingViewModel.averageRating.collectAsState()
    val reviewCount by reviewViewModel.reviewCount.collectAsState()

    // State for selected attributes and quantity
    var selectedAttributes by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var quantity by remember { mutableStateOf(1) }
    var showAttributeError by remember { mutableStateOf(false) }

    LaunchedEffect(productId) {
        productViewModel.getProductById(productId)
        ratingViewModel.loadProductRatings(productId)
        reviewViewModel.loadProductReviews(productId)
        // Reset selections when product changes
        selectedAttributes = emptyMap()
        quantity = 1
    }

    val totalitem by cartViewModel.totalItems.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var currentTab by remember { mutableStateOf("home") }

    ModalNavigationDrawer(
        drawerContent = {
            AppSideBar(
                navigator = navigator,
                wishlistViewModel,
                authViewModel = authViewModel,
                productViewModel = productViewModel,
                cartViewModel = cartViewModel,
                categoryViewModel = categoryViewModel
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
            val wishlistIds by wishlistViewModel.wishlistProductIds.collectAsState()

            when (productState) {
                is SingleUiState.Loading -> {
                    LoadingScreen(modifier = Modifier.fillMaxSize())
                }

                is SingleUiState.Success -> {
                    val product = (productState as SingleUiState.Success).data

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        ProductDetailsTab(
                            product = product,
                            averageRating = averageRating,
                            reviewCount = reviewCount,
                            selectedAttributes = selectedAttributes,
                            quantity = quantity,
                            onAttributeSelected = { attributeKey, option ->
                                selectedAttributes = selectedAttributes.toMutableMap().apply {
                                    this[attributeKey] = option
                                }
                                showAttributeError = false
                            },
                            onQuantityChange = { newQuantity ->
                                quantity = newQuantity
                            },
                            onAddtoCartClick = {
                                val attributesWithOptions = product.attributes.filter {
                                    !it.options.isNullOrEmpty()
                                }
                                if (attributesWithOptions.isNotEmpty() &&
                                    attributesWithOptions.any {
                                        selectedAttributes[it.slug ?: it.name].isNullOrBlank()
                                    }
                                ) {
                                    showAttributeError = true
                                } else {
                                    cartViewModel.addToCart(product, selectedAttributes)
                                    showAttributeError = false
                                    navigator.push(CartScreenNav())
                                }
                            },
                            onBuyNowClick = {
                                // Validate that all attributes with options are selected
                                val attributesWithOptions = product.attributes.filter {
                                    !it.options.isNullOrEmpty()
                                }
                                if (attributesWithOptions.isNotEmpty() &&
                                    attributesWithOptions.any {
                                        selectedAttributes[it.slug ?: it.name].isNullOrBlank()
                                    }
                                ) {
                                    showAttributeError = true
                                } else {
                                    cartViewModel.addToCart(product, selectedAttributes)
                                    navigator.push(CartScreenNav())
                                }
                            },
                            onAddtoWishlist = {
                                wishlistViewModel.toggleWishlist(product)
                            },
                            inWishList = wishlistIds.contains(product.id)
                        )
                    }

                    // Error Dialog for missing attributes
                    if (showAttributeError) {
                        AlertDialog(
                            onDismissRequest = { showAttributeError = false },
                            title = {
                                Text(
                                    text = "Selection Required",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            },
                            text = {
                                Text(
                                    text = "Please select all required attributes (size, color, etc.) before adding to cart.",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            },
                            confirmButton = {
                                TextButton(
                                    onClick = { showAttributeError = false }
                                ) {
                                    Text("OK")
                                }
                            }
                        )
                    }
                }

                is SingleUiState.Error -> {
                    ErrorScreen(message = (productState as SingleUiState.Error).message)
                }

                SingleUiState.Idle -> Unit
            }
        }
    }
}

@Composable
fun ProductDetailsTab(
    product: Product,
    averageRating: Double,
    reviewCount: Int,
    selectedAttributes: Map<String, String>,
    quantity: Int,
    onAttributeSelected: (String, String) -> Unit,
    onQuantityChange: (Int) -> Unit,
    onAddtoCartClick: () -> Unit,
    onBuyNowClick: () -> Unit,
    onAddtoWishlist: () -> Unit,
    inWishList: Boolean
) {
    var isDescriptionExpanded by remember { mutableStateOf(false) }
    var isAdditionalInfoExpanded by remember { mutableStateOf(false) }
    var isReviewsExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Product Image
        fun normalize(url: String?): String {
            if (url.isNullOrBlank()) return ""
            return when {
                url.startsWith("//") -> "https:$url"
                url.startsWith("/") -> "https://www.page3life.com$url"
                else -> url
            }
        }

        val painter = rememberImagePainter(
            url = normalize(product.images.firstOrNull()?.src)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
        ) {
            Image(
                painter = painter,
                contentDescription = product.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        // Product Details Section
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = product.name ?: "",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                ),
                color = Color(0xFF2C2C2C)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "₹${(product.price) ?: "0"}.00",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = Color(0xFF2C2C2C)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable { onAddtoWishlist() }
                    .padding(vertical = 4.dp)
            ) {
                Icon(
                    imageVector = if (inWishList) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Add to wishlist",
                    tint = Color(0xFF2C2C2C),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Add to wishlist",
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                    color = Color(0xFF2C2C2C)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Attributes Section with Selection (inline display)
            if (product.attributes.isNotEmpty()) {
                ProductAttributesSelectionSection(
                    attributes = product.attributes,
                    selectedAttributes = selectedAttributes,
                    onAttributeSelected = onAttributeSelected
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Quantity Selector and Action Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    QuantitySelector(
                        quantity = quantity,
                        onQuantityChange = onQuantityChange,
                        modifier = Modifier.weight(0.35f)
                    )

                    Button(
                        onClick = { onAddtoCartClick() },
                        modifier = Modifier
                            .weight(0.65f)
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Black
                        ),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        Text(
                            text = "Add To Cart",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp
                            )
                        )
                    }
                }

                Button(
                    onClick = { onBuyNowClick() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black
                    ),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Text(
                        text = "Buy Now",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Divider(color = Color(0xFFE0E0E0))

            CollapsibleInfoCard(
                title = "Description",
                isExpanded = isDescriptionExpanded,
                onToggle = { isDescriptionExpanded = !isDescriptionExpanded }
            ) {
                val rawHtml = product.description ?: product.shortDescription
                if (!rawHtml.isNullOrBlank()) {
                    FormattedDescription(htmlContent = rawHtml)
                } else {
                    Text(
                        text = "No description available",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp,
                            lineHeight = 22.sp
                        ),
                        color = Color(0xFF666666)
                    )
                }
            }

            Divider(color = Color(0xFFE0E0E0))

            CollapsibleInfoCard(
                title = "Additional information",
                isExpanded = isAdditionalInfoExpanded,
                onToggle = { isAdditionalInfoExpanded = !isAdditionalInfoExpanded }
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    product.attributes.forEach { attribute ->
                        if (!attribute.options.isNullOrEmpty()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${attribute.name}:",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = Color(0xFF2C2C2C)
                                )
                                Text(
                                    text = attribute.options.joinToString(", "),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF666666)
                                )
                            }
                        }
                    }
                }
            }

            Divider(color = Color(0xFFE0E0E0))

            CollapsibleInfoCard(
                title = "Reviews ($reviewCount)",
                isExpanded = isReviewsExpanded,
                onToggle = { isReviewsExpanded = !isReviewsExpanded }
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RatingBar(
                            rating = averageRating.toFloat(),
                            size = 20.dp
                        )
                        Text(
                            text = averageRating.toString(),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    Text(
                        text = if (reviewCount > 0) {
                            "$reviewCount customer reviews"
                        } else {
                            "No reviews yet"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF666666)
                    )
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun ProductAttributesSelectionSection(
    attributes: List<WcAttributes>,
    selectedAttributes: Map<String, String>,
    onAttributeSelected: (String, String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        attributes.forEach { attribute ->
            if (!attribute.options.isNullOrEmpty()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = (attribute.name ?: "").replaceFirstChar {
                            if (it.isLowerCase()) it.titlecase() else it.toString()
                        },
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = Color(0xFF2C2C2C)
                    )

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        attribute.options?.forEach { option ->
                            val attributeKey: String = attribute.slug ?: attribute.name
                            val isSelected = selectedAttributes[attributeKey] == option

                            Surface(
                                modifier = Modifier.clickable {
                                    onAttributeSelected(attributeKey, option)
                                },
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) Color.Black else Color.White,
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) Color.Black else Color(0xFFE0E0E0)
                                )
                            ) {
                                Text(
                                    text = option,
                                    modifier = Modifier.padding(
                                        horizontal = 16.dp,
                                        vertical = 12.dp
                                    ),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Medium
                                    ),
                                    color = if (isSelected) Color.White else Color(0xFF2C2C2C)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuantitySelector(
    quantity: Int,
    onQuantityChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.height(56.dp),
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
        color = Color.White
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { if (quantity > 1) onQuantityChange(quantity - 1) }
            ) {
                Text(
                    text = "-",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color(0xFF2C2C2C)
                )
            }

            Text(
                text = quantity.toString(),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = Color(0xFF2C2C2C)
            )

            IconButton(
                onClick = { onQuantityChange(quantity + 1) }
            ) {
                Text(
                    text = "+",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color(0xFF2C2C2C)
                )
            }
        }
    }
}


// ======================= 2. CHECKOUT SCREEN (Voyager Screen) =======================



@Composable
fun CollapsibleInfoCard(
    title: String,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggle() }
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal
                ),
                color = Color(0xFF2C2C2C)
            )

            Icon(
                imageVector = if (isExpanded) {
                    Icons.Default.KeyboardArrowUp
                } else {
                    Icons.Default.KeyboardArrowDown
                },
                contentDescription = if (isExpanded) "Collapse" else "Expand",
                tint = Color(0xFF666666)
            )
        }

        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
fun ProductAttributesSection(
    attributes: List<WcAttributes>,
    modifier: Modifier = Modifier
) {
    if (attributes.isEmpty()) return

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        attributes.forEach { attribute ->
            if (!attribute.options.isNullOrEmpty() && attribute.visible != "false") {
                when (attribute.slug?.lowercase()) {
                    "size" -> SizeAttributeView(attribute)
                    "color", "colour" -> ColorAttributeView(attribute)
                    else -> GenericAttributeView(attribute)
                }
            }
        }
    }
}

@Composable
private fun ColorAttributeView(attribute: WcAttributes) {
    var selectedColor by remember { mutableStateOf<String?>(null) }

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "${attribute.name}:",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 16.sp
                ),
                color = Color(0xFF2C2C2C)
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            attribute.options?.forEach { color ->
                ColorCircle(
                    colorName = color,
                    isSelected = selectedColor == color,
                    onClick = { selectedColor = color }
                )
            }
        }
    }
}

@Composable
private fun ColorCircle(
    colorName: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colorValue = getColorFromName(colorName)

    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(colorValue)
            .border(
                width = if (isSelected) 3.dp else 1.dp,
                color = if (isSelected) Color.Black else Color(0xFFE0E0E0),
                shape = CircleShape
            )
            .clickable { onClick() }
    )
}

private fun getColorFromName(colorName: String): Color {
    return when (colorName.lowercase()) {
        "red" -> Color(0xFFE53935)
        "blue", "celeste blue" -> Color(0xFF1E88E5)
        "purple" -> Color(0xFF8E24AA)
        "espresso", "black" -> Color(0xFF212121)
        "white" -> Color(0xFFFAFAFA)
        "green" -> Color(0xFF43A047)
        "yellow" -> Color(0xFFFDD835)
        "orange" -> Color(0xFFFB8C00)
        "pink" -> Color(0xFFEC407A)
        "gray", "grey" -> Color(0xFF757575)
        "beige", "tan" -> Color(0xFFD2B48C)
        "silver" -> Color(0xFFC0C0C0)
        else -> Color(0xFFCCCCCC)
    }
}

@Composable
private fun SizeAttributeView(attribute: WcAttributes) {
    var selectedSize by remember { mutableStateOf<String?>(null) }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "${attribute.name}:",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 16.sp
            ),
            color = Color(0xFF2C2C2C)
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            attribute.options?.forEach { size ->
                SizeChip(
                    label = size,
                    isSelected = selectedSize == size,
                    onClick = { selectedSize = size }
                )
            }
        }
    }
}

@Composable
private fun SizeChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) Color.Black else Color.White,
        border = BorderStroke(
            width = 1.dp,
            color = if (isSelected) Color.Black else Color(0xFFE0E0E0)
        ),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = if (isSelected) Color.White else Color(0xFF2C2C2C)
        )
    }
}

@Composable
private fun GenericAttributeView(attribute: WcAttributes) {
    var selectedOption by remember { mutableStateOf<String?>(null) }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "${attribute.name}:",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 16.sp
            ),
            color = Color(0xFF2C2C2C)
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            attribute.options?.forEach { option ->
                SizeChip(
                    label = option,
                    isSelected = selectedOption == option,
                    onClick = { selectedOption = option }
                )
            }
        }
    }
}

@Composable
fun FormattedDescription(htmlContent: String) {
    val formattedText = remember(htmlContent) {
        parseHtmlToAnnotatedString(htmlContent)
    }

    Text(
        text = formattedText,
        style = MaterialTheme.typography.bodyMedium.copy(
            fontSize = 15.sp,
            lineHeight = 24.sp
        ),
        color = Color(0xFF2C2C2C)
    )
}

fun parseHtmlToAnnotatedString(html: String): AnnotatedString {
    return buildAnnotatedString {
        // Remove class attributes and span tags
        var cleanHtml = html
            .replace(Regex("<span[^>]*>"), "")
            .replace("</span>", "")
            .replace(Regex("class=\"[^\"]*\""), "")

        // Parse list items
        val listItemRegex = Regex("<li>(.*?)</li>", RegexOption.MULTILINE)
        val listItems = listItemRegex.findAll(cleanHtml).map { it.groupValues[1].trim() }.toList()

        if (listItems.isNotEmpty()) {
            listItems.forEachIndexed { index, item ->
                // Extract bold text
                val boldRegex = Regex("<strong>(.*?)</strong>|<b>(.*?)</b>", RegexOption.MULTILINE)
                var currentIndex = 0
                val cleanItem = item
                    .replace(Regex("<[^>]*>"), "")
                    .replace("&nbsp;", " ")
                    .trim()

                // Add bullet point
                pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                append("• ")
                pop()

                // Check if item starts with bold text (like 【COMPATIBLE DEVICES】)
                val startBoldMatch = Regex("^【[^】]+】").find(cleanItem)
                if (startBoldMatch != null) {
                    val boldText = startBoldMatch.value
                    pushStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 15.sp))
                    append(boldText)
                    pop()
                    append(" ")
                    append(cleanItem.substring(boldText.length).trim())
                } else {
                    append(cleanItem)
                }

                // Add spacing between items
                if (index < listItems.size - 1) {
                    append("\n\n")
                }
            }
        } else {
            // If no list items, just clean and display
            val cleanText = cleanHtml
                .replace(Regex("<[^>]*>"), "")
                .replace("&nbsp;", " ")
                .replace(Regex("\n+"), "\n")
                .trim()
            append(cleanText)
        }
    }
}



@Composable
fun RatingBar(
    rating: Float,
    size: Dp = 20.dp,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier) {
        repeat(5) { index ->
            Icon(
                imageVector = if (index < rating.toInt()) Icons.Filled.Star else Icons.Outlined.Star,
                contentDescription = null,
                tint = Color(0xFFFFB300),
                modifier = Modifier.size(size)
            )
        }
    }
}


@Composable
fun AddToCartDialog(
    product: Product,
    onDismiss: () -> Unit,
    onConfirm: (attributes: Map<String, String>, quantity: Int) -> Unit
) {
    var selectedAttributes by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var quantity by remember { mutableStateOf(1) }
    var showError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add to Cart") },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }

                // Display attributes for selection
                if (product.attributes.isNotEmpty()) {
                    items(product.attributes.size) { index ->
                        val attribute = product.attributes[index]
                        if (!attribute.options.isNullOrEmpty()) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = attribute.name.replaceFirstChar { it.uppercase() },
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )

                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    attribute.options?.forEach { option ->
                                        FilterChip(
                                            selected = selectedAttributes[attribute.slug] == option,
                                            onClick = {
                                                selectedAttributes = selectedAttributes.toMutableMap().apply {
                                                    this[attribute.slug ?: attribute.name] = option
                                                }
                                                showError = false
                                            },
                                            label = { Text(option) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Quantity Selector
                item {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Quantity",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            )
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { if (quantity > 1) quantity-- },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Remove,
                                    contentDescription = "Decrease"
                                )
                            }

                            Text(
                                text = quantity.toString(),
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier
                                    .weight(1f)
                            )

                            IconButton(
                                onClick = { quantity++ },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Increase"
                                )
                            }
                        }
                    }
                }

                if (showError) {
                    item {
                        Text(
                            text = "Please select all required attributes",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Red
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    // Validate that required attributes are selected
                    val requiredAttributes = product.attributes.filter { it.variation == "true" }
                    if (requiredAttributes.isNotEmpty() &&
                        requiredAttributes.any { selectedAttributes[it.slug ?: it.name].isNullOrBlank() }) {
                        showError = true
                    } else {
                        onConfirm(selectedAttributes, quantity)
                    }
                }
            ) {
                Text("Add to Cart")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}


data class Attribute(
    val id: Int?,
    val name: String?,
    val slug: String?,
    val options: List<String>?
)