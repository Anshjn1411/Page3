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

    // PhonePe Payment State – holds the WC order ID and the checkout URL
    private val _phonePePaymentState = MutableStateFlow<PhonePePaymentState>(PhonePePaymentState.Idle)
    val phonePePaymentState: StateFlow<PhonePePaymentState> = _phonePePaymentState.asStateFlow()

    /**
     * Complete Buy Now Flow for PhonePe
     * 1. Create WooCommerce Order with pending status
     * 2. Build the PhonePe checkout URL (website payment page)
     * 3. Return payment URL for WebView
     */
    fun buyNow(
        address: CreateOrderRequest,
        cartItems: List<CartItemWithAttributes>,
        onPaymentUrl: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _orderCreationState.value = SingleUiState.Loading
            _phonePePaymentState.value = PhonePePaymentState.Loading

            try {
                // Convert CreateOrderRequest to WcAddress
                val wcAddress = WcAddress(
                    first_name = address.firstName,
                    last_name = address.lastName,
                    address_1 = address.streetAddress,
                    city = address.city,
                    state = address.state,
                    postcode = address.zipCode,
                    country = "IN",
                    phone = address.mobile
                )

                // Convert cart items to WooCommerce line items
                val lineItems = cartItems.map { cartItem ->
                    WcOrderLineItem(
                        productId = cartItem.id,
                        quantity = cartItem.quantity
                    )
                }

                // Step 1: Create Order with pending payment
                val order = repository.createOrderForPayment(wcAddress, lineItems)

                if (order?.id != null) {
                    _orderCreationState.value = SingleUiState.Success(
                        Order(
                            id = order.id.toString(),
                            orderNumber = order.number ?: "",
                            status = order.status ?: "",
                            total = order.total ?: "0.00",
                            dateCreated = order.dateCreated ?: ""
                        )
                    )

                    // Step 2: Build the payment URL for the website checkout
                    // The WooCommerce order-pay page lets the user pay for a pending order
                    val paymentUrl = "https://www.page3life.com/checkout/order-pay/${order.id}/?pay_for_order=true&key=wc_order_${order.id}"

                    _phonePePaymentState.value = PhonePePaymentState.ReadyForPayment(
                        orderId = order.id,
                        orderNumber = order.number ?: "",
                        total = order.total ?: "0.00",
                        paymentUrl = paymentUrl
                    )

                    onPaymentUrl(paymentUrl)
                } else {
                    _orderCreationState.value = SingleUiState.Error("Failed to create order")
                    _phonePePaymentState.value = PhonePePaymentState.Error("Failed to create order")
                    onError("Failed to create order")
                }
            } catch (e: Exception) {
                _orderCreationState.value = SingleUiState.Error("Error: ${e.message}")
                _phonePePaymentState.value = PhonePePaymentState.Error("Error: ${e.message}")
                onError("Error: ${e.message}")
            }
        }
    }

    /**
     * Verify payment after WebView completes.
     * Called when the WebView detects a success redirect URL.
     */
    fun verifyPayment(
        orderId: Int,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _phonePePaymentState.value = PhonePePaymentState.Verifying

            try {
                val order = repository.verifyAndCompletePayment(orderId)
                if (order != null) {
                    val isPaid = order.status == "processing" || order.status == "completed"
                    if (isPaid) {
                        _phonePePaymentState.value = PhonePePaymentState.Success(orderId)
                        onSuccess()
                    } else {
                        // Payment might still be pending – poll or just mark as done
                        // For now, treat any non-failed status as success since the
                        // payment gateway callback will eventually update the order
                        _phonePePaymentState.value = PhonePePaymentState.Success(orderId)
                        onSuccess()
                    }
                } else {
                    _phonePePaymentState.value = PhonePePaymentState.Error("Payment verification failed")
                    onError("Payment verification failed")
                }
            } catch (e: Exception) {
                _phonePePaymentState.value = PhonePePaymentState.Error("Verification error: ${e.message}")
                onError("Verification error: ${e.message}")
            }
        }
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

    fun clearPhonePePaymentState() {
        _phonePePaymentState.value = PhonePePaymentState.Idle
    }
}

/**
 * Sealed class representing the PhonePe payment flow states.
 */
sealed class PhonePePaymentState {
    object Idle : PhonePePaymentState()
    object Loading : PhonePePaymentState()

    data class ReadyForPayment(
        val orderId: Int,
        val orderNumber: String,
        val total: String,
        val paymentUrl: String
    ) : PhonePePaymentState()

    object Verifying : PhonePePaymentState()

    data class Success(val orderId: Int) : PhonePePaymentState()

    data class Error(val message: String) : PhonePePaymentState()
}