package dev.infa.page3.ui

import androidx.compose.foundation.Image
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.Navigator
import dev.infa.page3.data.remote.SessionManager
import dev.infa.page3.navigation.AuthManager
import dev.infa.page3.navigation.HomeMainScreen
import dev.infa.page3.navigation.OTPScreen
import dev.infa.page3.presentation.api.ApiService
import dev.infa.page3.presentation.repositary.AuthRepository
import dev.infa.page3.presentation.repositary.CategoryRepository
import dev.infa.page3.presentation.repositary.ProductRepository
import dev.infa.page3.presentation.repository.CartRepository
import dev.infa.page3.presentation.repository.WishlistRepository
import dev.infa.page3.presentation.repository.UserRepository
import dev.infa.page3.presentation.viewModel.AuthViewModel
import dev.infa.page3.presentation.viewModel.CartViewModel
import dev.infa.page3.presentation.viewModel.CategoryViewModel
import dev.infa.page3.presentation.viewModel.ProductViewModel
import dev.infa.page3.presentation.viewmodel.WishlistViewModel
import dev.infa.page3.ui.auth.OtpVerificationScreen
import dev.infa.page3.ui.components.CustomTopBar
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import page3.composeapp.generated.resources.Res
import page3.composeapp.generated.resources.splash


@Composable
fun SplashScreen(navigator : Navigator) {
    val categoryViewModel: CategoryViewModel = remember {
        CategoryViewModel(CategoryRepository(ApiService(), SessionManager()))
    }
    val productViewModel: ProductViewModel = remember {
        ProductViewModel(ProductRepository(ApiService(), SessionManager()))
    }
    val wishListViewModel: WishlistViewModel = remember {
        WishlistViewModel(WishlistRepository())
    }
    val cartViewModel: CartViewModel = remember {
        CartViewModel(CartRepository())
    }
    val authViewModel: AuthViewModel = remember { AuthViewModel(
        AuthRepository(
            ApiService(),
            SessionManager()
        ),
        UserRepository()
    ) }
    LaunchedEffect(Unit) {
        AuthManager.init()
    }
    val userRepository = UserRepository()
    LaunchedEffect(Unit){
        if (userRepository.isFullyLoggedIn()) {
            navigator.push(
                HomeMainScreen(
                )
            )
        } else {
            //for-Now let user enter direct to home screen
            navigator.push(
                HomeMainScreen(
                )
            )
        }
    }
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(Res.drawable.splash),
                contentDescription = null
            )
        }
    }
}

