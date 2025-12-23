package dev.infa.page3.presentation.viewModel

import dev.infa.page3.data.model.CreateOrderRequest
import dev.infa.page3.data.model.Order
import dev.infa.page3.data.model.OrderDetailed
import dev.infa.page3.data.model.PaymentLinkResponse
import dev.infa.page3.data.model.WcAddress
import dev.infa.page3.data.model.WcOrderLineItem
import dev.infa.page3.data.model.CartItemWithAttributes
import dev.infa.page3.presentation.repositary.OrderRepository
import dev.infa.page3.presentation.uiSatateClaases.ListUiState
import dev.infa.page3.presentation.uiSatateClaases.OperationUiState
import dev.infa.page3.presentation.uiSatateClaases.SingleUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OrderViewModel(
    private val repository: OrderRepository
)
{
    private val viewModelScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // Order Creation State
    private val _orderCreationState = MutableStateFlow<SingleUiState<Order>>(SingleUiState.Idle)
    val orderCreationState: StateFlow<SingleUiState<Order>> = _orderCreationState.asStateFlow()

    // Payment Link State
    private val _paymentLinkState = MutableStateFlow<SingleUiState<PaymentLinkResponse>>(SingleUiState.Idle)
    val paymentLinkState: StateFlow<SingleUiState<PaymentLinkResponse>> = _paymentLinkState.asStateFlow()

    // Order Details State
    private val _orderDetailsState = MutableStateFlow<SingleUiState<OrderDetailed>>(SingleUiState.Idle)
    val orderDetailsState: StateFlow<SingleUiState<OrderDetailed>> = _orderDetailsState.asStateFlow()

    // Order History State
    private val _orderHistoryState = MutableStateFlow<ListUiState<OrderDetailed>>(ListUiState.Idle)
    val orderHistoryState: StateFlow<ListUiState<OrderDetailed>> = _orderHistoryState.asStateFlow()

    // Payment Update State
    private val _paymentUpdateState = MutableStateFlow<OperationUiState>(OperationUiState.Idle)
    val paymentUpdateState: StateFlow<OperationUiState> = _paymentUpdateState.asStateFlow()

    /**
     * Complete Buy Now Flow
     * 1. Create Order from Cart
     * 2. Create Payment Link
     * 3. Return payment URL
     */
    fun buyNow(
        address: CreateOrderRequest,
        cartItems: List<CartItemWithAttributes>,
        onPaymentUrl: (String) -> Unit,
        onError: (String) -> Unit
    ) {
//        viewModelScope.launch {
//            _orderCreationState.value = SingleUiState.Loading
//
//            try {
//                // Step 1: Create Order
//                val order = repository.createOrder(address)
//
//                if (order?.id != null) {
//                    _orderCreationState.value = SingleUiState.Success(order)
//
//                    // Step 2: Create Payment Link
//                    _paymentLinkState.value = SingleUiState.Loading
//                    val paymentLink = repository.createPaymentLink(order._id)
//
//                    if (paymentLink != null) {
//                        _paymentLinkState.value = SingleUiState.Success(paymentLink)
//                        onPaymentUrl(paymentLink.payment_link_url)
//                    } else {
//                        _paymentLinkState.value = SingleUiState.Error("Failed to create payment link")
//                        onError("Failed to create payment link")
//                    }
//                } else {
//                    _orderCreationState.value = SingleUiState.Error("Failed to create order")
//                    onError("Failed to create order")
//                }
//            } catch (e: Exception) {
//                _orderCreationState.value = SingleUiState.Error("Error: ${e.message}")
//                onError("Error: ${e.message}")
//            }
//        }
    }

    /**
     * Create COD Order
     * Creates order with COD payment method using real cart data
     */
    fun createCodOrder(
        address: CreateOrderRequest,
        cartItems: List<CartItemWithAttributes>,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _orderCreationState.value = SingleUiState.Loading
            
            try {
                // Convert CreateOrderRequest to WcAddress
                val wcAddress = WcAddress(
                    first_name = address.firstName,
                    last_name = address.lastName,
                    address_1 = address.streetAddress,
                    city = address.city,
                    state = address.state,
                    postcode = address.zipCode,
                    country = "IN", // Default to India
                    phone = address.mobile
                )

                // Convert cart items to WooCommerce line items
                val lineItems = cartItems.map { cartItem ->
                    WcOrderLineItem(
                        productId = cartItem.id,
                        quantity = cartItem.quantity
                    )
                }
                
                val order = repository.createOrder(wcAddress, lineItems)
                
                if (order != null) {
                    _orderCreationState.value = SingleUiState.Success(
                        Order(
                            id = order.id?.toString() ?: "",
                            orderNumber = order.number ?: "",
                            status = order.status ?: "",
                            total = order.total ?: "0.00",
                            dateCreated = order.dateCreated ?: ""
                        )
                    )
                    onSuccess()
                } else {
                    _orderCreationState.value = SingleUiState.Error("Failed to create order")
                    onError("Failed to create order")
                }
            } catch (e: Exception) {
                _orderCreationState.value = SingleUiState.Error("Error: ${e.message}")
                onError("Error: ${e.message}")
            }
        }
    }

    /**
     * Update Payment Information (After Razorpay callback)
     */
    fun updatePaymentInformation(paymentId: String, orderId: String) {
//        viewModelScope.launch {
//            _paymentUpdateState.value = OperationUiState.Loading
//            try {
//                val success = repository.updatePaymentInformation(paymentId, orderId)
//                _paymentUpdateState.value = if (success) {
//                    OperationUiState.Success
//                } else {
//                    OperationUiState.Error("Payment verification failed")
//                }
//            } catch (e: Exception) {
//                _paymentUpdateState.value = OperationUiState.Error("Error: ${e.message}")
//            }
//        }
    }

    /**
     * Load Order by ID
     */
    fun loadOrderById(orderId: String) {
//        viewModelScope.launch {
//            _orderDetailsState.value = SingleUiState.Loading
//            try {
//                val order = repository.getOrderById(orderId)
//                _orderDetailsState.value = if (order != null) {
//                    SingleUiState.Success(order)
//                } else {
//                    SingleUiState.Error("Order not found")
//                }
//            } catch (e: Exception) {
//                _orderDetailsState.value = SingleUiState.Error("Error: ${e.message}")
//            }
//        }
    }

    /**
     * Load Order History
     */
    fun loadOrderHistory() {
//        viewModelScope.launch {
//            _orderHistoryState.value = ListUiState.Loading
//            try {
//                val orders = repository.getOrderHistory()
//                _orderHistoryState.value = when {
//                    orders.isEmpty() -> ListUiState.Empty
//                    else -> ListUiState.Success(orders)
//                }
//            } catch (e: Exception) {
//                _orderHistoryState.value = ListUiState.Error("Failed to load orders: ${e.message}")
//            }
//        }
    }

    /**
     * Download Invoice
     */
    fun downloadInvoice(
        orderNumber: String,
        onSuccess: (ByteArray) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
//                //val invoiceData = repository.downloadInvoice(orderNumber)
//                if (invoiceData != null) {
//                    onSuccess(invoiceData)
//                } else {
//                    onError("Failed to download invoice")
//                }
            } catch (e: Exception) {
                onError("Error: ${e.message}")
            }
        }
    }

    // Clear state functions
    fun clearOrderCreationState() {
        _orderCreationState.value = SingleUiState.Idle
    }

    fun clearPaymentLinkState() {
        _paymentLinkState.value = SingleUiState.Idle
    }

    fun clearPaymentUpdateState() {
        _paymentUpdateState.value = OperationUiState.Idle
    }

    fun clearOrderDetailsState() {
        _orderDetailsState.value = SingleUiState.Idle
    }
}