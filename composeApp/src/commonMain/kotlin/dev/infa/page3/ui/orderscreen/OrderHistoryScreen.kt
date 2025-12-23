package dev.infa.page3.ui.orderscreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.Navigator
import dev.infa.page3.data.remote.SessionManager
import dev.infa.page3.presentation.api.ApiService
import dev.infa.page3.presentation.repositary.OrderRepository
import dev.infa.page3.presentation.uiSatateClaases.ListUiState
import dev.infa.page3.presentation.viewModel.CartViewModel
import dev.infa.page3.presentation.viewModel.OrderViewModel
import dev.infa.page3.ui.orderscreen.componenets.OrderCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderHistoryScreenContent(navigator: Navigator, orderViewModel1: CartViewModel) {
    val orderViewModel: OrderViewModel = remember {
        OrderViewModel(OrderRepository(ApiService(), SessionManager()))
    }

    val orderHistoryState by orderViewModel.orderHistoryState.collectAsState()

    LaunchedEffect(Unit) {
        orderViewModel.loadOrderHistory()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Orders") },
                navigationIcon = {
                    IconButton(onClick = { navigator.pop() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        when (orderHistoryState) {
            is ListUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is ListUiState.Success -> {
                val orders = (orderHistoryState as ListUiState.Success).data
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(orders) { order ->
                        OrderCard(
                            order = order,
                            onClick = {
                                //navigator.push(OrderDetailScreen(order._id))
                            }
                        )
                    }
                }
            }
            is ListUiState.Empty -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            "No orders yet",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Button(onClick = { navigator.popUntilRoot() }) {
                            Text("Start Shopping")
                        }
                    }
                }
            }
            is ListUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text((orderHistoryState as ListUiState.Error).message)
                }
            }
            else -> {}
        }
    }
}