package dev.infa.page3.navigation

import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import androidx.compose.runtime.*
import dev.infa.page3.data.remote.SessionManager
import dev.infa.page3.presentation.api.ApiService
import dev.infa.page3.presentation.repositary.AuthRepository
import dev.infa.page3.presentation.repositary.CategoryRepository
import dev.infa.page3.presentation.repositary.ProductRepository
import dev.infa.page3.presentation.repository.CartRepository
import dev.infa.page3.presentation.repository.WishlistRepository
import dev.infa.page3.presentation.repository.UserRepository
import dev.infa.page3.presentation.uiSatateClaases.AuthUiState
import dev.infa.page3.presentation.viewModel.AuthViewModel
import dev.infa.page3.presentation.viewModel.CartViewModel
import dev.infa.page3.presentation.viewModel.CategoryViewModel
import dev.infa.page3.presentation.viewModel.OrderViewModel
import dev.infa.page3.presentation.viewModel.ProductViewModel
import dev.infa.page3.presentation.viewmodel.WishlistViewModel
import dev.infa.page3.ui.AccountDetailsContent
import dev.infa.page3.ui.AccountDetailsScreen
import dev.infa.page3.ui.MainScreen
import dev.infa.page3.ui.SplashScreen
import dev.infa.page3.ui.auth.OtpVerificationScreen
import dev.infa.page3.ui.auth.RegistrationScreen
import dev.infa.page3.ui.auth.WelcomeScreen
import dev.infa.page3.ui.CartScreen
import dev.infa.page3.ui.ShopScreen
import dev.infa.page3.ui.ProfileScreen
import dev.infa.page3.ui.SettingScreen
import dev.infa.page3.ui.productscreen.CategoryScreen
import dev.infa.page3.ui.productscreen.ProductDetailScreen
import dev.infa.page3.ui.WishlistScreen
import dev.infa.page3.ui.orderscreen.AddressManagementScreen
import dev.infa.page3.ui.orderscreen.CheckoutScreenContent
import dev.infa.page3.ui.otherScreen.InboxScreen
import dev.infa.page3.ui.orderscreen.OrderHistoryScreenContent
import dev.infa.page3.ui.orderscreen.OrderSuccessScreenContent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.serialization.Transient

// ============= TAB DEFINITIONS =============


object AuthManager {
    private val _authState = MutableStateFlow<AuthUiState>(AuthUiState.Loading)
    val authState: StateFlow<AuthUiState> = _authState

    private val viewModel by lazy {
        AuthViewModel(
            AuthRepository(ApiService(), SessionManager()),
            UserRepository()
        )
    }

    fun init() {
        // run only once when app launches - check for persistent login
        viewModel.autoLogin()
        viewModel.uiState.onEach { state ->
            _authState.value = state
        }.launchIn(CoroutineScope(Dispatchers.Main))
    }

    fun logout() {
        viewModel.logout()
    }
}


class SplashScreen(
) : Screen  {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        // Initialize all ViewModels once
        LaunchedEffect(Unit) {
            AppViewModels.init()
        }

        SplashScreen(
            navigator
        )
    }
}

// Wrapper screen for Home content
class HomeMainScreen(
) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val categoryViewModel = AppViewModels.categoryViewModel
        val productViewModel = AppViewModels.productViewModel
        val wishListViewModel = AppViewModels.wishListViewModel
        val cartViewModel = AppViewModels.cartViewModel
        val authViewModel = AppViewModels.authViewModel

        MainScreen(
            navigator = navigator,
            viewModel = categoryViewModel,
            productViewModel = productViewModel,
            wishListViewModel = wishListViewModel,
            cartViewModel = cartViewModel,
            authViewModel = authViewModel,
            categoryViewModel = categoryViewModel,
        )
    }
}


// ============= SCREEN DEFINITIONS =============

// Splash Screen

// OTP Screen (Welcome Screen)
class OTPScreen() : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val authViewModel = AppViewModels.authViewModel

        WelcomeScreen(
            onNavigateToVerify = { mobile ->
                navigator.push(OTPScreenVerify( mobile))
            },
            onNavigateMain = {
                navigator.push(HomeMainScreen())
            }
        )
    }
}

data class OTPScreenVerify(
    val mobile: String
) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        OtpVerificationScreen(
            mobile = mobile,
            onNavigateToRegister = { mobile ->
                navigator.push(RegisterScreen(mobile))
            },
            onNavigateToMain = {
                navigator.push(HomeMainScreen(

                ))
            },
            onNavigateBack = { navigator.pop() }
        )
    }
}

data class RegisterScreen(
    val mobile: String,
) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        RegistrationScreen(
            mobile = mobile,
            onNavigateToMain = {
                navigator.push(HomeMainScreen(

                ))
            },
            onNavigateBack = { navigator.pop() }
        )
    }
}



// Register Screen
// Category Detail Screen
data class CategoryScreenNav(
    val categoryID: String,
    val categoryName: String
) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val categoryViewModel = AppViewModels.categoryViewModel
        val productViewModel = AppViewModels.productViewModel
        val wishlistViewModel = AppViewModels.wishListViewModel
        val cartViewModel = AppViewModels.cartViewModel
        val authViewModel = AppViewModels.authViewModel
        CategoryScreen(
            CategoryId = categoryID,
            CategoryName = categoryName,
            navigator = navigator,
            categoryViewModel = categoryViewModel,
            productViewModel = productViewModel,
            cartViewModel = cartViewModel,
            authViewModel = authViewModel,
            wishlistViewModel = wishlistViewModel
        )
    }
}

data class ProductDetail(
    val productId: String,

) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val categoryViewModel = AppViewModels.categoryViewModel
        val productViewModel = AppViewModels.productViewModel
        val wishlistViewModel = AppViewModels.wishListViewModel
        val cartViewModel = AppViewModels.cartViewModel
        val authViewModel = AppViewModels.authViewModel
        ProductDetailScreen(
            productId = productId,
            navigator = navigator,
            categoryViewModel = categoryViewModel,
            productViewModel = productViewModel,
            cartViewModel = cartViewModel,
            authViewModel = authViewModel,
            wishlistViewModel = wishlistViewModel
        )
    }
}
class AccountDetail() : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val categoryViewModel = AppViewModels.categoryViewModel
        val productViewModel = AppViewModels.productViewModel
        val wishlistViewModel = AppViewModels.wishListViewModel
        val cartViewModel = AppViewModels.cartViewModel
        val authViewModel = AppViewModels.authViewModel
        AccountDetailsScreen(
            navigator,
            categoryViewModel = categoryViewModel,
            productViewModel = productViewModel,
            cartViewModel = cartViewModel,
            authViewModel = authViewModel,
            wishlistViewModel = wishlistViewModel
        )
    }
}



class CategoriesOverviewScreenNav(

) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val categoryViewModel = AppViewModels.categoryViewModel
        val productViewModel = AppViewModels.productViewModel
        val wishlistViewModel = AppViewModels.wishListViewModel
        val cartViewModel = AppViewModels.cartViewModel
        val authViewModel = AppViewModels.authViewModel
        ShopScreen(
            navigator = navigator,
            categoryViewModel = categoryViewModel,
            productViewModel = productViewModel,
            cartViewModel = cartViewModel,
            authViewModel = authViewModel,
            wishlistViewModel = wishlistViewModel
        )
    }
}


class InboxScreenNav() : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        InboxScreen(navigator)
    }
}
class AddressAmangementNav():Screen{
    @Composable
    override fun Content(){
        val navigator = LocalNavigator.currentOrThrow

        AddressManagementScreen(navigator)
    }


}



class CheckoutScreen(
) : Screen {
    @Composable
    override fun Content() {
        val categoryViewModel = AppViewModels.categoryViewModel
        val productViewModel = AppViewModels.productViewModel
        val wishListViewModel = AppViewModels.wishListViewModel
        val cartViewModel = AppViewModels.cartViewModel
        val authViewModel = AppViewModels.authViewModel
        val navigator = LocalNavigator.currentOrThrow
        CheckoutScreenContent(navigator, cartViewModel, productViewModel)
    }
}

class WishlistScreenNav(

) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val categoryViewModel = AppViewModels.categoryViewModel
        val productViewModel = AppViewModels.productViewModel
        val wishlistViewModel = AppViewModels.wishListViewModel
        val cartViewModel = AppViewModels.cartViewModel
        val authViewModel = AppViewModels.authViewModel
        WishlistScreen(
            navigator, wishlistViewModel,
            categoryViewModel = categoryViewModel,
            productViewModel = productViewModel,
            cartViewModel = cartViewModel,
            authViewModel = authViewModel,
        )
    }
}

class CartScreenNav(
) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val categoryViewModel = AppViewModels.categoryViewModel
        val productViewModel = AppViewModels.productViewModel
        val wishlistViewModel = AppViewModels.wishListViewModel
        val cartViewModel = AppViewModels.cartViewModel
        val authViewModel = AppViewModels.authViewModel
        CartScreen(
            navigator, wishlistViewModel,
            categoryViewModel = categoryViewModel,
            productViewModel = productViewModel,
            cartViewModel = cartViewModel,
            authViewModel = authViewModel,
        )
    }
}

class ProfileScreenNav(

) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val categoryViewModel = AppViewModels.categoryViewModel
        val productViewModel = AppViewModels.productViewModel
        val wishlistViewModel = AppViewModels.wishListViewModel
        val cartViewModel = AppViewModels.cartViewModel
        val authViewModel = AppViewModels.authViewModel
        ProfileScreen(navigator, wishlistViewModel=wishlistViewModel,
            categoryViewModel = categoryViewModel,
            productViewModel = productViewModel,
            cartViewModel = cartViewModel,
            authViewModel = authViewModel,)
    }
}

//class SettingScreen(
//    private val authViewModel: AuthViewModel
//) : Screen {
//    @Composable
//    override fun Content() {
//        val navigator = LocalNavigator.currentOrThrow
//        SettingScreen(navigator, authViewModel)
//    }
//}



//
//class OrderHistoryScreen(
//    private val orderViewModel: CartViewModel // assuming you keep orders in cartViewModel or create separate OrderViewModel
//) : Screen {
//    @Composable
//    override fun Content() {
//        val navigator = LocalNavigator.currentOrThrow
//        OrderHistoryScreenContent(navigator, orderViewModel)
//    }
//}

//class OrderDetailScreen(
//    private val orderViewModel: CartViewModel
//) : Screen {
//    @Composable
//    override fun Content() {
//        val navigator = LocalNavigator.currentOrThrow
//        //OrderDetailScreenContent(navigator, orderViewModel)
//    }
//}

//class OrderSuccessScreen(
//    private val orderViewModel: OrderViewModel
//) : Screen {
//    @Composable
//    override fun Content() {
//        val navigator = LocalNavigator.currentOrThrow
//        //OrderSuccessScreenContent(navigator, CartViewModel(ApiService()))
//    }
//}


object AppViewModels {
    lateinit var categoryViewModel: CategoryViewModel
    lateinit var productViewModel: ProductViewModel
    lateinit var wishListViewModel: WishlistViewModel
    lateinit var cartViewModel: CartViewModel
    lateinit var authViewModel: AuthViewModel

    fun init() {
        categoryViewModel = CategoryViewModel(CategoryRepository(ApiService(), SessionManager()))
        productViewModel = ProductViewModel(ProductRepository(ApiService(), SessionManager()))
        wishListViewModel = WishlistViewModel(WishlistRepository())
        cartViewModel = CartViewModel(CartRepository())
        authViewModel = AuthViewModel(AuthRepository(ApiService(), SessionManager()), UserRepository())
    }
}
