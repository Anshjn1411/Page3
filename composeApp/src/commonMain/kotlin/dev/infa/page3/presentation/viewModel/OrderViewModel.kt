package dev.infa.page3.presentation.viewModel

import dev.infa.page3.data.model.CreateOrderRequest
import dev.infa.page3.data.model.Order
import dev.infa.page3.data.model.PaymentLinkResponse
import dev.infa.page3.data.model.WcAddress
import dev.infa.page3.data.model.WcOrder
import dev.infa.page3.data.model.WcOrderNote
import dev.infa.page3.data.model.WcOrderBatchRequest
import dev.infa.page3.data.model.WcOrderLineItem
import dev.infa.page3.data.model.WcRefund
import dev.infa.page3.data.model.CartItemWithAttributes
import dev.infa.page3.payment.PhonePeSDKHelper
import dev.infa.page3.presentation.repositary.OrderRepository
import dev.infa.page3.presentation.uiSatateClaases.ListUiState
import dev.infa.page3.presentation.uiSatateClaases.OperationUiState
import dev.infa.page3.presentation.uiSatateClaases.SingleUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OrderViewModel(
    private val repository: OrderRepository
) {
    private val viewModelScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // Order Creation State
    private val _orderCreationState = MutableStateFlow<SingleUiState<Order>>(SingleUiState.Idle)
    val orderCreationState: StateFlow<SingleUiState<Order>> = _orderCreationState.asStateFlow()

    // Payment Link State (legacy)
    private val _paymentLinkState = MutableStateFlow<SingleUiState<PaymentLinkResponse>>(SingleUiState.Idle)
    val paymentLinkState: StateFlow<SingleUiState<PaymentLinkResponse>> = _paymentLinkState.asStateFlow()

    // Order Details State
    private val _orderDetailsState = MutableStateFlow<SingleUiState<WcOrder>>(SingleUiState.Idle)
    val orderDetailsState: StateFlow<SingleUiState<WcOrder>> = _orderDetailsState.asStateFlow()

    // Order History State
    private val _orderHistoryState = MutableStateFlow<ListUiState<WcOrder>>(ListUiState.Idle)
    val orderHistoryState: StateFlow<ListUiState<WcOrder>> = _orderHistoryState.asStateFlow()

    private val _orderOperationState = MutableStateFlow<OperationUiState>(OperationUiState.Idle)
    val orderOperationState: StateFlow<OperationUiState> = _orderOperationState.asStateFlow()

    // ======================== Order Detail (Refunds + Notes) ========================
    private val _orderRefundsState = MutableStateFlow<ListUiState<WcRefund>>(ListUiState.Idle)
    val orderRefundsState: StateFlow<ListUiState<WcRefund>> = _orderRefundsState.asStateFlow()

    private val _orderNotesState = MutableStateFlow<ListUiState<WcOrderNote>>(ListUiState.Idle)
    val orderNotesState: StateFlow<ListUiState<WcOrderNote>> = _orderNotesState.asStateFlow()

    // Payment Update State
    private val _paymentUpdateState = MutableStateFlow<OperationUiState>(OperationUiState.Idle)
    val paymentUpdateState: StateFlow<OperationUiState> = _paymentUpdateState.asStateFlow()

    // PhonePe Payment State
    private val _phonePePaymentState = MutableStateFlow<PhonePePaymentState>(PhonePePaymentState.Idle)
    val phonePePaymentState: StateFlow<PhonePePaymentState> = _phonePePaymentState.asStateFlow()

    // PhonePe credentials
    private var phonePeClientId: String = ""
    private var phonePeClientSecret: String = ""
    private var phonePeClientVersion: String = "1"
    private var phonePeMerchantId: String = ""

    /**
     * Set PhonePe credentials from BuildConfig.
     */
    fun setPhonePeCredentials(
        clientId: String,
        clientSecret: String,
        clientVersion: String,
        merchantId: String
    ) {
        phonePeClientId = clientId
        phonePeClientSecret = clientSecret
        phonePeClientVersion = clientVersion
        phonePeMerchantId = merchantId
    }

    // ======================== PhonePe SDK Payment Flow ========================
    //
    // CORRECTED FLOW (Payment-First):
    //  1. Calculate total from cart items locally
    //  2. Get PhonePe auth token
    //  3. Create PhonePe order (no WC order yet!)
    //  4. Launch SDK checkout
    //  5. On SDK callback → check PhonePe order status
    //  6. If COMPLETED → Create WC order (set_paid=true, status=processing)
    //  7. If FAILED  → Do NOT create any WC order
    //
    // This ensures NO orphan orders are created when payment fails.
    // =========================================================================

    /**
     * Initiate PhonePe SDK payment flow.
     * Does NOT create a WooCommerce order — that only happens after payment succeeds.
     */
    fun buyNowWithSdk(
        address: CreateOrderRequest,
        cartItems: List<CartItemWithAttributes>,
        onSdkLaunched: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _phonePePaymentState.value = PhonePePaymentState.Loading

            try {
                // Convert address for later use (stored in state)
                val wcAddress = WcAddress(
                    first_name = address.firstName,
                    last_name = address.lastName,
                    email = address.email,
                    address_1 = address.streetAddress,
                    city = address.city,
                    state = address.state,
                    postcode = address.zipCode,
                    country = "IN",
                    phone = address.mobile
                )

                // Convert cart items for later use (stored in state)
                val lineItems = cartItems.map { cartItem ->
                    WcOrderLineItem(
                        productId = cartItem.id,
                        quantity = cartItem.quantity
                    )
                }

                // Calculate total from cart items locally
                val totalAmount = cartItems.sumOf { item ->
                    val price = item.salePrice?.toDoubleOrNull()
                        ?: item.price?.toDoubleOrNull()
                        ?: 0.0
                    price * item.quantity
                }

                if (totalAmount <= 0) {
                    _phonePePaymentState.value = PhonePePaymentState.Error("Invalid cart total")
                    onError("Invalid cart total. Please check your cart items.")
                    return@launch
                }

                val totalInPaise = (totalAmount * 100).toLong()

                // Generate a unique merchant order ID (no WC order yet)
                val uniqueId = kotlin.random.Random.nextLong(100000000, 999999999)
                val merchantOrderId = "PP_${phonePeMerchantId}_$uniqueId"

                // Step 1: Get PhonePe auth token
                val authResponse = repository.getPhonePeAuthToken(
                    clientId = phonePeClientId,
                    clientSecret = phonePeClientSecret,
                    clientVersion = phonePeClientVersion
                )

                if (authResponse == null || authResponse.accessToken.isEmpty()) {
                    _phonePePaymentState.value = PhonePePaymentState.Error("Failed to authenticate with PhonePe")
                    onError("Payment service unavailable. Please try again.")
                    return@launch
                }

                // Step 2: Create PhonePe payment order (NO WC order created yet!)
                val phonePeOrder = repository.createPhonePeOrder(
                    authToken = authResponse.accessToken,
                    merchantOrderId = merchantOrderId,
                    amountInPaise = totalInPaise
                )

                if (phonePeOrder == null) {
                    _phonePePaymentState.value = PhonePePaymentState.Error("Failed to create payment order")
                    onError("Failed to create payment order. Please try again.")
                    return@launch
                }

                val checkoutToken = phonePeOrder.getCheckoutToken()
                if (checkoutToken.isEmpty()) {
                    _phonePePaymentState.value = PhonePePaymentState.Error("Failed to get payment token")
                    onError("Payment token not received. Please try again.")
                    return@launch
                }

                // Step 3: Store all info needed for post-payment order creation
                _phonePePaymentState.value = PhonePePaymentState.SdkCheckoutActive(
                    wcAddress = wcAddress,
                    lineItems = lineItems,
                    totalAmount = totalAmount.toString(),
                    phonePeOrderId = phonePeOrder.orderId,
                    phonePeToken = checkoutToken,
                    merchantOrderId = merchantOrderId,
                    authToken = authResponse.accessToken
                )

                // Step 4: Set callback for when SDK returns
                PhonePeSDKHelper.setPaymentResultCallback { sdkReturned ->
                    if (sdkReturned) {
                        handlePhonePeSdkResult()
                    }
                }

                // Step 5: Launch SDK checkout
                val launched = PhonePeSDKHelper.startCheckout(
                    token = checkoutToken,
                    orderId = phonePeOrder.orderId
                )

                if (launched) {
                    onSdkLaunched()
                } else {
                    _phonePePaymentState.value = PhonePePaymentState.Error("Could not open PhonePe. Is the app installed?")
                    onError("Could not open PhonePe. Please ensure it's installed.")
                }
            } catch (e: Exception) {
                _phonePePaymentState.value = PhonePePaymentState.Error("Error: ${e.message}")
                onError("Something went wrong: ${e.message}")
            }
        }
    }

    /**
     * Called when PhonePe SDK returns a result.
     * Checks payment status with PhonePe API, then:
     *  - COMPLETED → Creates WC order (set_paid=true)
     *  - FAILED    → Shows error, NO WC order created
     *  - PENDING   → Polls up to 5 times, then creates order optimistically
     */
    private fun handlePhonePeSdkResult() {
        viewModelScope.launch {
            val currentState = _phonePePaymentState.value
            if (currentState !is PhonePePaymentState.SdkCheckoutActive) {
                _phonePePaymentState.value = PhonePePaymentState.Error("Invalid payment state")
                return@launch
            }

            _phonePePaymentState.value = PhonePePaymentState.Verifying

            try {
                var attempts = 0
                val maxAttempts = 5
                var finalStatus: String? = null
                var transactionId = ""

                // Poll PhonePe for payment status
                while (attempts < maxAttempts) {
                    val statusResponse = repository.checkPhonePeOrderStatus(
                        authToken = currentState.authToken,
                        merchantOrderId = currentState.merchantOrderId
                    )

                    val state = statusResponse?.state ?: "UNKNOWN"

                    when (state) {
                        "COMPLETED" -> {
                            finalStatus = "COMPLETED"
                            transactionId = statusResponse?.paymentDetails
                                ?.firstOrNull()?.transactionId ?: currentState.merchantOrderId
                            break
                        }
                        "FAILED" -> {
                            finalStatus = "FAILED"
                            break
                        }
                        "PENDING" -> {
                            attempts++
                            if (attempts < maxAttempts) {
                                delay(3000) // Wait 3 seconds before retry
                            }
                        }
                        else -> {
                            attempts++
                            if (attempts < maxAttempts) {
                                delay(3000)
                            }
                        }
                    }
                }

                // If still pending after all attempts, treat as completed
                // (PhonePe webhook will handle actual status on backend)
                if (finalStatus == null) {
                    finalStatus = "COMPLETED"
                    transactionId = currentState.merchantOrderId
                }

                when (finalStatus) {
                    "COMPLETED" -> {
                        // ✅ Payment succeeded → NOW create the WC order
                        val wcOrder = repository.createPaidOrder(
                            address = currentState.wcAddress,
                            lineItems = currentState.lineItems,
                            transactionId = transactionId
                        )

                        if (wcOrder?.id != null) {
                            _orderCreationState.value = SingleUiState.Success(
                                Order(
                                    id = wcOrder.id.toString(),
                                    orderNumber = wcOrder.number ?: "",
                                    status = wcOrder.status ?: "processing",
                                    total = wcOrder.total ?: currentState.totalAmount,
                                    dateCreated = wcOrder.dateCreated ?: ""
                                )
                            )
                            _phonePePaymentState.value = PhonePePaymentState.Success(
                                wcOrderId = wcOrder.id,
                                orderNumber = wcOrder.number ?: ""
                            )
                        } else {
                            // Payment succeeded but order creation failed — critical error
                            // User should contact support with their transaction ID
                            _phonePePaymentState.value = PhonePePaymentState.Error(
                                "Payment successful but order creation failed. " +
                                "Transaction ID: $transactionId. Please contact support."
                            )
                        }
                    }
                    "FAILED" -> {
                        // ❌ Payment failed → NO WC order created
                        _phonePePaymentState.value = PhonePePaymentState.Error(
                            "Payment failed. No order was placed. Please try again."
                        )
                    }
                }
            } catch (e: Exception) {
                _phonePePaymentState.value = PhonePePaymentState.Error(
                    "Payment verification failed: ${e.message}"
                )
            }
        }
    }

    // ======================== COD Flow ========================

    /**
     * Create COD Order — order is created immediately with set_paid=true.
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
                val wcAddress = WcAddress(
                    first_name = address.firstName,
                    last_name = address.lastName,
                    email = address.email,
                    address_1 = address.streetAddress,
                    city = address.city,
                    state = address.state,
                    postcode = address.zipCode,
                    country = "IN",
                    phone = address.mobile
                )

                val lineItems = cartItems.map { cartItem ->
                    WcOrderLineItem(
                        productId = cartItem.id,
                        quantity = cartItem.quantity
                    )
                }

                val order = repository.createCodOrder(
                    address = wcAddress,
                    lineItems = lineItems
                )

                if (order?.id != null) {
                    _orderCreationState.value = SingleUiState.Success(
                        Order(
                            id = order.id.toString(),
                            orderNumber = order.number ?: "",
                            status = order.status ?: "processing",
                            total = order.total ?: "0.00",
                            dateCreated = order.dateCreated ?: ""
                        )
                    )
                    onSuccess()
                } else {
                    _orderCreationState.value = SingleUiState.Error("Failed to create order")
                    onError("Failed to create order. Please try again.")
                }
            } catch (e: Exception) {
                _orderCreationState.value = SingleUiState.Error("Error: ${e.message}")
                onError("Something went wrong: ${e.message}")
            }
        }
    }

    // ======================== Order History ========================

    /**
     * Load order history for the current user.
     * Note: Uses WcOrder from WooCommerce API.
     */
    fun loadOrderHistory(customerId: Int? = null, email: String? = null, status: String? = null) {
        viewModelScope.launch {
            _orderHistoryState.value = ListUiState.Loading
            try {
                val orders = repository.getOrderHistory(customerId = customerId, email = email, status = status)
                if (orders.isNotEmpty()) {
                    _orderHistoryState.value = ListUiState.Success(orders)
                } else {
                    _orderHistoryState.value = ListUiState.Empty
                }
            } catch (e: Exception) {
                _orderHistoryState.value = ListUiState.Error("Failed to load orders: ${e.message}")
            }
        }
    }

    fun trackOrderByIdAndEmail(orderId: String, email: String) {
        viewModelScope.launch {
            _orderDetailsState.value = SingleUiState.Loading
            try {
                val order = repository.getOrderByIdAndEmail(orderId, email)
                if (order != null) {
                    _orderDetailsState.value = SingleUiState.Success(order)
                } else {
                    _orderDetailsState.value = SingleUiState.Error("Order not found for this email")
                }
            } catch (e: Exception) {
                _orderDetailsState.value = SingleUiState.Error("Failed to fetch order: ${e.message}")
            }
        }
    }

    fun loadOrderRefunds(orderId: Int) {
        viewModelScope.launch {
            _orderRefundsState.value = ListUiState.Loading
            try {
                val refunds = repository.listRefunds(orderId)
                _orderRefundsState.value = if (refunds.isNotEmpty()) ListUiState.Success(refunds) else ListUiState.Empty
            } catch (e: Exception) {
                _orderRefundsState.value = ListUiState.Error("Failed to load refunds: ${e.message}")
            }
        }
    }

    fun loadOrderNotes(orderId: Int) {
        viewModelScope.launch {
            _orderNotesState.value = ListUiState.Loading
            try {
                val notes = repository.listOrderNotes(orderId)
                _orderNotesState.value = if (notes.isNotEmpty()) ListUiState.Success(notes) else ListUiState.Empty
            } catch (e: Exception) {
                _orderNotesState.value = ListUiState.Error("Failed to load notes: ${e.message}")
            }
        }
    }

    fun deleteRefund(orderId: Int, refundId: Int) {
        viewModelScope.launch {
            _orderOperationState.value = OperationUiState.Loading
            val deleted = repository.deleteRefund(orderId = orderId, refundId = refundId, force = true)
            _orderOperationState.value = if (deleted) OperationUiState.Success else OperationUiState.Error("Unable to delete refund")
        }
    }

    fun deleteOrderNote(orderId: Int, noteId: Int) {
        viewModelScope.launch {
            _orderOperationState.value = OperationUiState.Loading
            val deleted = repository.deleteOrderNote(orderId = orderId, noteId = noteId, force = true)
            _orderOperationState.value = if (deleted) OperationUiState.Success else OperationUiState.Error("Unable to delete note")
        }
    }

    fun updateOrder(orderId: Int, status: String, customerNote: String? = null) {
        viewModelScope.launch {
            _orderOperationState.value = OperationUiState.Loading
            val updated = repository.updateOrder(orderId, status, customerNote)
            _orderOperationState.value = if (updated != null) {
                OperationUiState.Success
            } else {
                OperationUiState.Error("Unable to update order")
            }
        }
    }

    fun deleteOrder(orderId: Int, force: Boolean = false) {
        viewModelScope.launch {
            _orderOperationState.value = OperationUiState.Loading
            val deleted = repository.deleteOrder(orderId, force)
            _orderOperationState.value = if (deleted != null) {
                OperationUiState.Success
            } else {
                OperationUiState.Error("Unable to delete order")
            }
        }
    }

    fun refundOrder(orderId: Int, amount: String, reason: String? = null) {
        viewModelScope.launch {
            _orderOperationState.value = OperationUiState.Loading
            val success = repository.refundOrder(orderId, amount, reason)
            _orderOperationState.value = if (success) {
                OperationUiState.Success
            } else {
                OperationUiState.Error("Unable to create refund")
            }
        }
    }

    fun addOrderNote(orderId: Int, note: String, customerNote: Boolean = true) {
        viewModelScope.launch {
            _orderOperationState.value = OperationUiState.Loading
            val success = repository.addOrderNote(orderId, note, customerNote)
            _orderOperationState.value = if (success) {
                OperationUiState.Success
            } else {
                OperationUiState.Error("Unable to add order note")
            }
        }
    }

    fun runOrderAction(orderId: Int, action: String) {
        viewModelScope.launch {
            _orderOperationState.value = OperationUiState.Loading
            val success = repository.runOrderAction(orderId, action) != null
            _orderOperationState.value = if (success) {
                OperationUiState.Success
            } else {
                OperationUiState.Error("Unable to run action: $action")
            }
        }
    }

    fun batchUpdateOrders(body: WcOrderBatchRequest) {
        viewModelScope.launch {
            _orderOperationState.value = OperationUiState.Loading
            val success = repository.batchOrders(body) != null
            _orderOperationState.value = if (success) {
                OperationUiState.Success
            } else {
                OperationUiState.Error("Batch order operation failed")
            }
        }
    }

    // ======================== Legacy WebView Payment Verification ========================

    /**
     * Verify payment after WebView completes (legacy flow / fallback).
     * Updates the existing WC order status to processing + set_paid.
     */
    fun verifyPayment(
        orderId: Int,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _phonePePaymentState.value = PhonePePaymentState.Verifying
            try {
                val order = repository.updateOrderStatus(orderId, "processing", true)
                if (order != null) {
                    _phonePePaymentState.value = PhonePePaymentState.Success(
                        wcOrderId = orderId,
                        orderNumber = order.number ?: ""
                    )
                    onSuccess()
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

    // ======================== Utility ========================

    fun clearOrderCreationState() {
        _orderCreationState.value = SingleUiState.Idle
    }

    fun clearPaymentLinkState() {
        _paymentLinkState.value = SingleUiState.Idle
    }

    fun clearPaymentUpdateState() {
        _paymentUpdateState.value = OperationUiState.Idle
    }

    fun clearOrderOperationState() {
        _orderOperationState.value = OperationUiState.Idle
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
 *
 * Flow: Idle → Loading → SdkCheckoutActive → Verifying → Success/Error
 */
sealed class PhonePePaymentState {
    /** Initial state — no payment in progress */
    object Idle : PhonePePaymentState()

    /** Getting auth token + creating PhonePe order */
    object Loading : PhonePePaymentState()

    /**
     * SDK checkout is active — user is in PhonePe app.
     * Stores all the data needed to create WC order AFTER payment succeeds.
     */
    data class SdkCheckoutActive(
        val wcAddress: WcAddress,
        val lineItems: List<WcOrderLineItem>,
        val totalAmount: String,
        val phonePeOrderId: String,
        val phonePeToken: String,
        val merchantOrderId: String,
        val authToken: String
    ) : PhonePePaymentState()

    /** Checking payment status with PhonePe after SDK returns */
    object Verifying : PhonePePaymentState()

    /** Payment succeeded AND WC order was created */
    data class Success(
        val wcOrderId: Int,
        val orderNumber: String
    ) : PhonePePaymentState()

    /** Payment failed OR an error occurred — NO WC order was created */
    data class Error(val message: String) : PhonePePaymentState()
}