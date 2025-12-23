package dev.infa.page3.navigation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import cafe.adriel.voyager.navigator.Navigator
import dev.infa.page3.data.remote.SessionManager
import dev.infa.page3.presentation.api.ApiService
import dev.infa.page3.presentation.repositary.AuthRepository
import dev.infa.page3.presentation.repositary.CategoryRepository
import dev.infa.page3.presentation.repositary.ProductRepository
import dev.infa.page3.presentation.repository.CartRepository
import dev.infa.page3.presentation.repository.WishlistRepository
import dev.infa.page3.presentation.viewModel.AuthViewModel
import dev.infa.page3.presentation.viewModel.CartViewModel
import dev.infa.page3.presentation.viewModel.CategoryViewModel
import dev.infa.page3.presentation.viewModel.ProductViewModel
import dev.infa.page3.presentation.viewmodel.WishlistViewModel

@Composable
fun MainNavigation() {

    Navigator(SplashScreen())
}