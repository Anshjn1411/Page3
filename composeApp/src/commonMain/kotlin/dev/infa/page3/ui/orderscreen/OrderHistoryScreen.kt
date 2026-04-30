package dev.infa.page3.ui.orderscreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.Navigator
import dev.infa.page3.data.remote.SessionManager
import dev.infa.page3.presentation.api.*
import dev.infa.page3.presentation.repositary.OrderRepository
import dev.infa.page3.presentation.uiSatateClaases.ListUiState
import dev.infa.page3.presentation.uiSatateClaases.SingleUiState
import dev.infa.page3.presentation.viewModel.CartViewModel
import dev.infa.page3.presentation.viewModel.OrderViewModel
import dev.infa.page3.navigation.OrderDetailScreenNav
import dev.infa.page3.ui.orderscreen.componenets.OrderCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderHistoryScreenContent(
    navigator: Navigator,
    orderViewModel1: CartViewModel,
    defaultEmail: String? = null
) {
    val orderViewModel: OrderViewModel = remember {
        OrderViewModel(OrderRepository(ApiService(), SessionManager()))
    }

    val orderHistoryState by orderViewModel.orderHistoryState.collectAsState()
    val orderDetailsState by orderViewModel.orderDetailsState.collectAsState()
    var emailFilter by remember { mutableStateOf(defaultEmail ?: "") }
    var orderIdToTrack by remember { mutableStateOf("") }
    var cardError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(defaultEmail) {
        // Guest order history requires email match, so only auto-load when we have it.
        if (!emailFilter.isNullOrBlank()) {
            orderViewModel.loadOrderHistory(email = emailFilter)
        }
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            OutlinedTextField(
                value = emailFilter,
                onValueChange = { emailFilter = it.trim() },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Track with email") },
                singleLine = true
            )

            OutlinedTextField(
                value = orderIdToTrack,
                onValueChange = { orderIdToTrack = it.trim() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                label = { Text("Order ID") },
                singleLine = true
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { orderViewModel.trackOrderByIdAndEmail(orderIdToTrack, emailFilter) },
                    enabled = orderIdToTrack.isNotBlank() && emailFilter.isNotBlank()
                ) {
                    Text("Track")
                }

                Button(
                    onClick = { orderViewModel.loadOrderHistory(email = emailFilter.ifBlank { null }) },
                    enabled = emailFilter.isNotBlank()
                ) {
                    Text("My Orders")
                }
            }

            if (cardError != null) {
                Text(
                    text = cardError ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (orderDetailsState is SingleUiState.Success) {
                val tracked = (orderDetailsState as SingleUiState.Success).data
                Text(
                    text = "Tracked order status: ${tracked.status ?: "unknown"}",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

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
                            .padding(top = 12.dp),
                        contentPadding = PaddingValues(bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(orders) { order ->
                            OrderCard(order = order) {
                                cardError = null
                                val email = emailFilter.trim()
                                val id = order.id
                                if (id == null) {
                                    cardError = "Order id not available."
                                    return@OrderCard
                                }
                                if (email.isBlank()) {
                                    cardError = "Please enter your email to view order detail."
                                    return@OrderCard
                                }
                                navigator.push(OrderDetailScreenNav(orderId = id, email = email))
                            }
                        }
                    }
                }
                is ListUiState.Empty -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
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
}