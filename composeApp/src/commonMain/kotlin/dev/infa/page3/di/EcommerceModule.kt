package dev.infa.page3.di

import dev.infa.page3.data.remote.SessionManager
import dev.infa.page3.network.NetworkConnectivity
import dev.infa.page3.presentation.api.ApiService
import dev.infa.page3.presentation.repositary.CategoryRepository
import dev.infa.page3.presentation.repository.AuthRepository
import dev.infa.page3.presentation.repositary.ProductRepository
import dev.infa.page3.presentation.repositary.WooCommerceReviewRepository
import dev.infa.page3.presentation.repository.CartRepository
import dev.infa.page3.presentation.repository.WishlistRepository
import dev.infa.page3.presentation.viewModel.AuthViewModel
import dev.infa.page3.presentation.viewModel.CartViewModel
import dev.infa.page3.presentation.viewModel.CategoryViewModel
import dev.infa.page3.presentation.viewModel.ProductReviewsViewModel
import dev.infa.page3.presentation.viewModel.ProductViewModel
import dev.infa.page3.presentation.viewmodel.WishlistViewModel
import org.koin.dsl.module

fun ecommerceModule() = module {
    single { SessionManager() }
    single { ApiService() }
    single { WishlistRepository() }
    single { CartRepository() }
    single { CategoryRepository(get(), get(), get()) }
    single { ProductRepository(get(), get(), get()) }
    single { AuthRepository(get(), get(), get()) }
    single { WooCommerceReviewRepository(get(), get()) }

    single { AuthViewModel(get()) }
    single { CategoryViewModel(get()) }
    single { ProductViewModel(get()) }
    single { WishlistViewModel(get()) }
    single { CartViewModel(get()) }
    single { ProductReviewsViewModel(get()) }
}
