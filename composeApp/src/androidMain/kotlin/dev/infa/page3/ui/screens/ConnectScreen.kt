package dev.infa.page3.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Battery5Bar
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import android.util.Log
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import dev.infa.page3.models.SmartWatch
import dev.infa.page3.ui.components.AppSideBar
import dev.infa.page3.ui.components.BottomNavBar
import dev.infa.page3.ui.components.TopBarScreen
import dev.infa.page3.viewmodels.ConnectionViewModel
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectScreen(
    viewModel: ConnectionViewModel,
    navController: NavController
) {
    var currentTab by remember { mutableStateOf("setting") }

    // Collect all state from ViewModel
    val isScanning by viewModel.isScanning.collectAsState()
    val isConnected by viewModel.isConnected.collectAsState()
    val isConnecting by viewModel.isConnecting.collectAsState()
    val deviceName by viewModel.deviceName.collectAsState()
    val deviceAddress by viewModel.deviceAddress.collectAsState()
    val discoveredDevices by viewModel.discoveredDevices.collectAsState()
    val selectedDevice by remember { mutableStateOf(SmartWatch("" ,"", 0,"")) }
    val connectionStatus by viewModel.connectionStatus.collectAsState()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerContent = { AppSideBar(navController) },
        drawerState = drawerState
    ) {
        Scaffold(
            topBar = {
                TopBarScreen(onClickMenu = { scope.launch { drawerState.open() } })
            },
            bottomBar = {
                BottomNavBar(currentNav = currentTab, navController)
            },
            containerColor = Color(0xFFF5F5F5)
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFF5F5F5))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Connected Device Section
                    if (isConnected) {
                        ConnectedDeviceCard(
                            deviceName = deviceName,
                            deviceAddress = deviceAddress,
                            connectionStatus = connectionStatus,
                            onDisconnect = { viewModel.disconnectDevice() }
                        )
                    } else {
                        // Scan Controls Section
                        ScanControlCard(
                            isScanning = isScanning,
                            onStartScan = { viewModel.startScanning() },
                            onStopScan = { viewModel.stopScanning() }
                        )
                    }

                    // Available Devices Section
                    if (discoveredDevices.isNotEmpty() && !isConnected) {
                        AvailableDevicesList(
                            devices = discoveredDevices,
                            selectedDevice = selectedDevice,
                            onSelectDevice = { s-> viewModel.connectToDevice(s)  },
                            onConnectDevice = { s-> viewModel.connectToDevice(s) }
                        )
                    } else if (!isScanning && !isConnected && discoveredDevices.isEmpty()) {
                        EmptyDeviceState()
                    }

                    if (isScanning) {
                        ScanningIndicator()
                    }
                }

                // Connecting Overlay
                if (isConnecting) {
                    ConnectingOverlay(deviceName = deviceName)
                }
            }
        }
    }
}

@Composable
private fun ConnectedDeviceCard(
    deviceName: String,
    deviceAddress: String,
    connectionStatus: String,
    onDisconnect: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header with status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CONNECTED DEVICE",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF666666),
                    letterSpacing = 1.2.sp,
                    fontWeight = FontWeight.Medium
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF4CAF50).copy(alpha = 0.15f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF4CAF50))
                        )
                        Text(
                            text = "Active",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF4CAF50),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Divider(color = Color(0xFFE0E0E0), thickness = 1.dp)

            // Device Info
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = deviceName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
                Text(
                    text = deviceAddress,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF666666)
                )
            }

            // Signal Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A)),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.SignalCellularAlt,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            text = connectionStatus,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Signal Strong",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFCCCCCC)
                        )
                    }
                }
            }

            // Disconnect Button
            Button(
                onClick = onDisconnect,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF3B30)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LinkOff,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Disconnect Device", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun ScanControlCard(
    isScanning: Boolean,
    onStartScan: () -> Unit,
    onStopScan: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "DEVICE DISCOVERY",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF666666),
                letterSpacing = 1.2.sp,
                fontWeight = FontWeight.Medium
            )

            Button(
                onClick = { if (isScanning) onStopScan() else onStartScan() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1A1A1A),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isScanning) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Stop Scanning", fontWeight = FontWeight.SemiBold)
                } else {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Scan for Devices", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun AvailableDevicesList(
    devices: List<SmartWatch>,
    selectedDevice: SmartWatch?,
    onSelectDevice: (SmartWatch) -> Unit,
    onConnectDevice: (SmartWatch) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "AVAILABLE DEVICES (${devices.size})",
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF666666),
            letterSpacing = 1.2.sp,
            fontWeight = FontWeight.Medium
        )

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(devices) { device ->
                DeviceListItem(
                    device = device,
                    isConnected = false,
                    onSelect = { onSelectDevice(device) },
                    onConnect ={ onConnectDevice(device) }
                )
            }
        }
    }
}

@Composable
private fun EmptyDeviceState() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(text = "⌚", fontSize = 64.sp)
            Text(
                text = "No Devices Found",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )
            Text(
                text = "Make sure your wearable device is turned on and in pairing mode",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = Color(0xFF666666)
            )
        }
    }
}

@Composable
private fun ScanningIndicator() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 3.dp,
                color = Color(0xFF1A1A1A)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "Scanning for devices...",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1A1A1A)
                )
                Text(
                    text = "Please wait",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF666666)
                )
            }
        }
    }
}

@Composable
private fun ConnectingOverlay(deviceName: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White.copy(alpha = 0.95f))
            .clickable(enabled = false) { },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                color = Color(0xFF1A1A1A),
                strokeWidth = 4.dp,
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = "Connecting...",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )
            Text(
                text = deviceName,
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF666666)
            )
        }
    }
}

@Composable
fun DeviceListItem(
    device: SmartWatch,
    isConnected: Boolean,
    onSelect: () -> Unit,
    onConnect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Device Icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color(0xFF1A1A1A), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when {
                            device.deviceName.contains("watch", true) -> "⌚"
                            device.deviceName.contains("band", true) -> "📱"
                            device.deviceName.contains("ring", true) -> "💍"
                            else -> "📟"
                        },
                        fontSize = 24.sp
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = device.deviceName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1A1A1A)
                    )
                    Text(
                        text = device.deviceAddress,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF666666)
                    )
                    Text(
                        text = "${device.rssi} dBm • ${getSignalStrength(device.rssi)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = getSignalColor(device.rssi),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Button(
                onClick = onConnect,
                enabled = device.deviceAddress.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1A1A1A),
                    contentColor = Color.White,
                    disabledContainerColor = Color(0xFFE0E0E0),
                    disabledContentColor = Color(0xFF999999)
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Text(
                    text = "Connect",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun getSignalColor(rssi: Int): Color {
    return when {
        rssi > -60 -> Color(0xFF4CAF50)
        rssi > -80 -> Color(0xFFFFC107)
        else -> Color(0xFFFF3B30)
    }
}

fun getSignalStrength(rssi: Int): String {
    return when {
        rssi > -60 -> "Excellent"
        rssi > -80 -> "Good"
        else -> "Weak"
    }
}