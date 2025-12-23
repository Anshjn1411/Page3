package dev.infa.page3.SDK.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import cafe.adriel.voyager.navigator.*
import dev.infa.page3.SDK.ui.DailogScreen.*
import dev.infa.page3.SDK.ui.components.*
import dev.infa.page3.SDK.ui.navigation.*
import dev.infa.page3.SDK.viewModel.*
import kotlinx.coroutines.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    connectionViewModel: ConnectionViewModel,
    profileViewModel: ProfileViewModel,
    navController: Navigator
) {
    val uiState by connectionViewModel.uiState.collectAsState()
    val isConnected = uiState.isConnected
    val deviceName = uiState.connectedDevice?.name ?: ""
    val deviceAddress = uiState.connectedDevice?.address ?: ""
    val batteryLevel = uiState.batteryLevel ?: 0

    // Profile ViewModel States
    val userSettings by profileViewModel.userSettings.collectAsState()
    val deviceInfo by profileViewModel.deviceInfo.collectAsState()
    val isLoading by profileViewModel.isLoading.collectAsState()
    val errorMessage by profileViewModel.errorMessage.collectAsState()
    val successMessage by profileViewModel.successMessage.collectAsState()

    // Dialog States
    val showUnitDialog by profileViewModel.showUnitDialog.collectAsState()
    val showTimeFormatDialog by profileViewModel.showTimeFormatDialog.collectAsState()
    val showTouchDialog by profileViewModel.showTouchDialog.collectAsState()

    var showConnectionAlert by remember { mutableStateOf(false) }
    var showToast by remember { mutableStateOf<String?>(null) }

    // Auto-dismiss alerts
    LaunchedEffect(showConnectionAlert) {
        if (showConnectionAlert) {
            delay(2000)
            showConnectionAlert = false
        }
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            delay(3000)
            profileViewModel.clearErrorMessage()
        }
    }

    LaunchedEffect(successMessage) {
        successMessage?.let {
            showToast = it
            delay(2000)
            profileViewModel.clearSuccessMessage()
            showToast = null
        }
    }
    var currentTab by remember { mutableStateOf(BottomTab.PROFILE) }

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            CommonTopAppBar("Profile", { navController.pop() })
        },
        bottomBar = {
            BottomNavBar(
                currentTab = currentTab,
                onTabSelected = { tab ->
                    currentTab = tab

                    when (tab) {
                        BottomTab.HOME -> navController.push(HomeScreenSDK())
                        BottomTab.STRAIN -> navController.push(ExerciseScreenSDK())
                        BottomTab.RECOVERY -> navController.push(HeartRateScreenSDK())
                        BottomTab.STEP -> navController.push(StepsScreenSDK())
                        BottomTab.PROFILE -> navController.replace(ProfileScreenSDK())
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black).padding(padding)
        ) {

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                item {
                    DeviceCard(
                        isConnected = isConnected,
                        deviceName = deviceName,
                        deviceAddress = deviceAddress,
                        batteryLevel = batteryLevel,
                        onBindClick = { navController.push(ScannerScreen()) },
                        onDisconnect = { connectionViewModel.disconnect() }
                    )
                }

                // Device Management Section
                item {
                    SettingsSection(
                        title = "Device Management",
                        items = listOf(
                            SettingItem(
                                "Device Details",
                                deviceInfo?.firmwareVersion?.let { "Page3 $it" }
                                    ?: "Firmware v4.2.1",
                                onClick = {
                                    if (isConnected) {
                                        navController.push(DeviceDetailScreenSDK())
                                    } else {
                                        showConnectionAlert = true
                                    }
                                }
                            ),
                            SettingItem(
                                "Find My Device",
                                "Make device vibrate",
                                onClick = {
                                    showToast = "Finding Device..."
                                    if (isConnected) {
                                        profileViewModel.findMyDevice()
                                    } else {
                                        showConnectionAlert = true
                                    }
                                }
                            ),
                            SettingItem(
                                "Device Capability",
                                "Features of Device",
                                onClick = {
                                    if (isConnected) {
                                        navController.push(DeviceCapabilityScreenSDK())
                                    } else {
                                        showConnectionAlert = true
                                    }
                                }
                            )
                        )
                    )
                }

                // Device Settings Section
                item {
                    SettingsSection(
                        title = "Device Setting",
                        items = listOf(
                            SettingItem(
                                "Set Goal",
                                "Update your Goal",
                                onClick = {
                                    navController.push(GoalsSettingsScreen())
                                }
                            ),
                            SettingItem(
                                "Unit Format",
                                userSettings.unitSystem.displayName,
                                onClick = {
                                    if (isConnected) {
                                        profileViewModel.showUnitDialog()
                                    } else {
                                        showConnectionAlert = true
                                    }
                                }
                            ),
                            SettingItem(
                                "Touch gestures",
                                "Enable the touch on device",
                                onClick = {
                                    if (isConnected) {
                                        profileViewModel.showTouchDialog()
                                    } else {
                                        showConnectionAlert = true
                                    }
                                }
                            ),
                            SettingItem(
                                "Time Format",
                                userSettings.timeFormat.displayName,
                                onClick = {
                                    if (isConnected) {
                                        profileViewModel.showTimeFormatDialog()
                                    } else {
                                        showConnectionAlert = true
                                    }
                                }
                            )
                        )
                    )
                }

                item { AppInfoFooter() }
            }
        }
    }

    // Dialogs
    if (showUnitDialog) {
        UnitSystemDialog(
            currentUnit = userSettings.unitSystem,
            onDismiss = { profileViewModel.dismissUnitDialog() },
            onSelect = { profileViewModel.updateUnitSystem(it) }
        )
    }

    if (showTimeFormatDialog) {
        TimeFormatDialog(
            currentFormat = userSettings.timeFormat,
            onDismiss = { profileViewModel.dismissTimeFormatDialog() },
            onSelect = { profileViewModel.updateTimeFormat(it) }
        )
    }

    if (showTouchDialog) {
        TouchSettingsDialog(
            touchSettings = profileViewModel.touchSettings.collectAsState().value,
            onDismiss = { profileViewModel.dismissTouchDialog() },
            onSave = { appType, isTouch, strength ->
                profileViewModel.updateTouchSettings(appType, isTouch, strength)
            }
        )
    }

    if (showConnectionAlert) {
        ConnectionRequiredAlert()
    }

    // Toast-like message display
    showToast?.let { message ->
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Card(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(0.9f),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF00FF88)
                )
            ) {
                Text(
                    text = message,
                    modifier = Modifier.padding(16.dp),
                    color = Color.Black,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }

    // Loading overlay
    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color(0xFF00FF88))
        }
    }
}

@Composable
fun DeviceCard(
    isConnected: Boolean,
    deviceName: String,
    deviceAddress: String,
    batteryLevel: Int,
    onBindClick: () -> Unit,
    onDisconnect: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF0F1F1A))
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {

            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0x2200FF88)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Watch,
                    contentDescription = null,
                    tint = Color(0xFF00FF88),
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {

                if (isConnected) {
                    Text(deviceName, color = Color.White, fontSize = 16.sp)
                    Text(deviceAddress, color = Color.Gray, fontSize = 12.sp)
                } else {
                    Text("No Device Connected", color = Color.White, fontSize = 16.sp)
                    Text("Tap below to bind your device", color = Color.Gray, fontSize = 12.sp)
                }
            }

            if (isConnected) {
                Text(
                    "$batteryLevel%",
                    color = Color(0xFF00FF88),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (isConnected) {

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {

                MiniInfoCard("Status", "Connected")
                MiniInfoCard("Battery", "$batteryLevel%")

            }

            Spacer(modifier = Modifier.height(14.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0x22FF4444))
                    .clickable { onDisconnect() }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Disconnect Device", color = Color.Red, fontSize = 14.sp)
            }

        } else {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0x2200FF88))
                    .clickable { onBindClick() }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Bind Device",
                    color = Color(0xFF00FF88),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}


@Composable
fun MiniInfoCard(title: String, subtitle: String) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF1A1A1A))
            .padding(12.dp)
    ) {
        Text(title, color = Color.White, fontSize = 13.sp)
        Text(subtitle, color = Color.Gray, fontSize = 10.sp)
    }
}



@Composable
fun AppInfoFooter() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Page3 Fitness Tracker", color = Color.Gray, fontSize = 12.sp)
        Text("Version 2", color = Color.DarkGray, fontSize = 11.sp)
        Text("© 2025 Page3 Mumbai .", color = Color(0xFF444444), fontSize = 11.sp)
    }
}

@Composable
fun SettingsSection(
    title: String,
    items: List<SettingItem>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF121212))
            .padding(20.dp)
    ) {

        Text(title, color = Color.White, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(12.dp))

        items.forEach {
            SettingsRow(
                title = it.title,
                subtitle = it.subTitle,
                onClick = it.onClick
            )
        }
    }
}
@Composable
fun SettingsRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF1A1A1A))
            .clickable { onClick() }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = Color.White, fontSize = 14.sp)
            Text(subtitle, color = Color.Gray, fontSize = 11.sp)
        }
        Icon(Icons.Default.ChevronRight, null, tint = Color.Gray)
    }

    Spacer(modifier = Modifier.height(8.dp))
}
data class SettingItem(
    val title: String,
    val subTitle: String,
    val onClick: () -> Unit
)
