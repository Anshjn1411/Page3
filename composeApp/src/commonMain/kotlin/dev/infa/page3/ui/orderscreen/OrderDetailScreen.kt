package dev.infa.page3.ui.orderscreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.Navigator
import dev.infa.page3.presentation.api.*
import dev.infa.page3.presentation.repositary.OrderRepository
import dev.infa.page3.presentation.uiSatateClaases.ListUiState
import dev.infa.page3.presentation.uiSatateClaases.OperationUiState
import dev.infa.page3.presentation.uiSatateClaases.SingleUiState
import dev.infa.page3.presentation.viewModel.OrderViewModel
import dev.infa.page3.ui.orderscreen.componenets.OrderStatusChip
import dev.infa.page3.data.model.WcOrder
import dev.infa.page3.data.model.WcOrderNote
import dev.infa.page3.data.model.WcRefund
import dev.infa.page3.data.remote.SessionManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreenContent(
    navigator: Navigator,
    orderId: Int,
    email: String
) {
    val viewModel: OrderViewModel = remember {
        OrderViewModel(
            OrderRepository(
                api = ApiService(),
                sessionManager = SessionManager()
            )
        )
    }

    val orderState by viewModel.orderDetailsState.collectAsState()
    val refundsState by viewModel.orderRefundsState.collectAsState()
    val notesState by viewModel.orderNotesState.collectAsState()
    val operationState by viewModel.orderOperationState.collectAsState()

    var statusInput by remember { mutableStateOf("cancelled") }
    var refundAmountInput by remember { mutableStateOf("") }
    var refundReasonInput by remember { mutableStateOf("") }
    var noteInput by remember { mutableStateOf("") }
    var actionInput by remember { mutableStateOf("cancel") }

    LaunchedEffect(orderId, email) {
        if (email.isBlank()) return@LaunchedEffect
        viewModel.trackOrderByIdAndEmail(orderId = orderId.toString(), email = email)
        viewModel.loadOrderRefunds(orderId = orderId)
        viewModel.loadOrderNotes(orderId = orderId)
    }

    LaunchedEffect(operationState) {
        if (operationState is OperationUiState.Success) {
            viewModel.trackOrderByIdAndEmail(orderId = orderId.toString(), email = email)
            viewModel.loadOrderRefunds(orderId = orderId)
            viewModel.loadOrderNotes(orderId = orderId)
            viewModel.clearOrderOperationState()
        }
    }

    fun refreshAll() {
        viewModel.trackOrderByIdAndEmail(orderId = orderId.toString(), email = email)
        viewModel.loadOrderRefunds(orderId = orderId)
        viewModel.loadOrderNotes(orderId = orderId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Order Detail") },
                navigationIcon = {
                    IconButton(onClick = { navigator.pop() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { refreshAll() }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (orderState) {
                is SingleUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is SingleUiState.Error -> {
                    val msg = (orderState as SingleUiState.Error).message
                    Text(
                        text = msg,
                        modifier = Modifier.align(Alignment.Center),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                is SingleUiState.Success -> {
                    val order = (orderState as SingleUiState.Success).data
                    OrderDetailContent(
                        order = order,
                        refundsState = refundsState,
                        notesState = notesState,
                        operationState = operationState,
                        statusInput = statusInput,
                        onStatusInputChange = { statusInput = it },
                        refundAmountInput = refundAmountInput,
                        onRefundAmountInputChange = { refundAmountInput = it },
                        refundReasonInput = refundReasonInput,
                        onRefundReasonInputChange = { refundReasonInput = it },
                        noteInput = noteInput,
                        onNoteInputChange = { noteInput = it },
                        actionInput = actionInput,
                        onActionInputChange = { actionInput = it },
                        onCancelOrder = { viewModel.updateOrder(orderId = orderId, status = "cancelled") },
                        onDeleteOrder = { viewModel.deleteOrder(orderId = orderId, force = false) },
                        onUpdateStatus = { viewModel.updateOrder(orderId = orderId, status = statusInput.trim()) },
                        onCreateRefund = {
                            val amount = refundAmountInput.trim()
                            val reason = refundReasonInput.trim().ifBlank { null }
                            if (amount.isNotBlank()) viewModel.refundOrder(orderId = orderId, amount = amount, reason = reason)
                        },
                        onAddNote = {
                            val note = noteInput.trim()
                            if (note.isNotBlank()) viewModel.addOrderNote(orderId = orderId, note = note, customerNote = true)
                        },
                        onDeleteNote = { noteId -> viewModel.deleteOrderNote(orderId = orderId, noteId = noteId) },
                        onRunAction = {
                            val action = actionInput.trim()
                            if (action.isNotBlank()) viewModel.runOrderAction(orderId = orderId, action = action)
                        },
                        onDeleteRefund = { refundId -> viewModel.deleteRefund(orderId = orderId, refundId = refundId) }
                    )
                }
                else -> Unit
            }
        }
    }
}

@Composable
private fun OrderDetailContent(
    order: WcOrder,
    refundsState: ListUiState<WcRefund>,
    notesState: ListUiState<WcOrderNote>,
    operationState: OperationUiState,
    statusInput: String,
    onStatusInputChange: (String) -> Unit,
    refundAmountInput: String,
    onRefundAmountInputChange: (String) -> Unit,
    refundReasonInput: String,
    onRefundReasonInputChange: (String) -> Unit,
    noteInput: String,
    onNoteInputChange: (String) -> Unit,
    actionInput: String,
    onActionInputChange: (String) -> Unit,
    onCancelOrder: () -> Unit,
    onDeleteOrder: () -> Unit,
    onUpdateStatus: () -> Unit,
    onCreateRefund: () -> Unit,
    onAddNote: () -> Unit,
    onDeleteNote: (Int) -> Unit,
    onRunAction: () -> Unit,
    onDeleteRefund: (Int) -> Unit
) {
    val billingEmail = order.billing?.email

    val showOpError = operationState is OperationUiState.Error
    val opErrorMessage = (operationState as? OperationUiState.Error)?.message

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Order #${order.number ?: order.id ?: "-"}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OrderStatusChip(status = order.status ?: "pending")
                Text(
                    text = order.dateCreated ?: "-",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = "Email: ${billingEmail ?: "-"}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Divider()

            Text(
                text = "Order Status",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            OutlinedTextField(
                value = statusInput,
                onValueChange = onStatusInputChange,
                label = { Text("New status") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(onClick = onUpdateStatus, modifier = Modifier.weight(1f)) {
                    Text("Update")
                }
                Button(onClick = onCancelOrder, modifier = Modifier.weight(1f)) {
                    Text("Cancel")
                }
            }

            Button(
                onClick = onDeleteOrder,
                modifier = Modifier.fillMaxWidth(),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Text("Delete order", color = MaterialTheme.colorScheme.onErrorContainer)
            }

            Divider()

            Text(
                text = "Refunds",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            OutlinedTextField(
                value = refundAmountInput,
                onValueChange = onRefundAmountInputChange,
                label = { Text("Refund amount") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = refundReasonInput,
                onValueChange = onRefundReasonInputChange,
                label = { Text("Reason (optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Button(onClick = onCreateRefund, modifier = Modifier.fillMaxWidth()) {
                Text("Create refund")
            }

            when (refundsState) {
                is ListUiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                is ListUiState.Empty -> Text("No refunds yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
                is ListUiState.Success -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        refundsState.data.forEach { refund ->
                            RefundRow(
                                refund = refund,
                                onDelete = onDeleteRefund
                            )
                        }
                    }
                }
                is ListUiState.Error -> Text(refundsState.message, color = MaterialTheme.colorScheme.error)
                else -> Unit
            }

            Divider()

            Text(
                text = "Order Notes",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            OutlinedTextField(
                value = noteInput,
                onValueChange = onNoteInputChange,
                label = { Text("Add note") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4
            )

            Button(onClick = onAddNote, modifier = Modifier.fillMaxWidth()) {
                Text("Add note")
            }

            when (notesState) {
                is ListUiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                is ListUiState.Empty -> Text("No notes yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
                is ListUiState.Success -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        notesState.data.forEach { note ->
                            NoteRow(
                                note = note,
                                onDelete = { id ->
                                    if (id != null) onDeleteNote(id)
                                }
                            )
                        }
                    }
                }
                is ListUiState.Error -> Text(notesState.message, color = MaterialTheme.colorScheme.error)
                else -> Unit
            }

            Divider()

            Text(
                text = "Order Actions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            OutlinedTextField(
                value = actionInput,
                onValueChange = onActionInputChange,
                label = { Text("Action (WooCommerce order action)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Button(onClick = onRunAction, modifier = Modifier.fillMaxWidth()) {
                Text("Run action")
            }

            if (showOpError && !opErrorMessage.isNullOrBlank()) {
                Text(
                    text = opErrorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (operationState is OperationUiState.Loading) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp))
                }
            }
        }
    }
}

@Composable
private fun RefundRow(
    refund: WcRefund,
    onDelete: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Refund #${refund.id}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Amount: ${refund.amount ?: "-"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (!refund.reason.isNullOrBlank()) {
                Text(
                    text = "Reason: ${refund.reason}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        IconButton(onClick = { onDelete(refund.id) }) {
            Icon(Icons.Filled.Delete, contentDescription = "Delete refund")
        }
    }
}

@Composable
private fun NoteRow(
    note: WcOrderNote,
    onDelete: (Int?) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = note.note ?: "-",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = note.dateCreated ?: "-",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(onClick = { onDelete(note.id) }) {
            Icon(Icons.Filled.Delete, contentDescription = "Delete note")
        }
    }
}

