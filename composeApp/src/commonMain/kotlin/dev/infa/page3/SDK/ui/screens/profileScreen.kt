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
import cafe.adriel.voyager.navigator.*
import dev.infa.page3.SDK.ui.DailogScreen.*
import dev.infa.page3.SDK.ui.components.*
import dev.infa.page3.SDK.ui.navigation.*
import dev.infa.page3.SDK.ui.theme.*
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
        containerColor = AppColors.BackgroundPrimary,
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
                .background(AppColors.BackgroundPrimary)
                .padding(padding)
        ) {

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = AppDimensions.ScreenPadding.Horizontal),
                verticalArrangement = Arrangement.spacedBy(AppDimensions.Spacing.ExtraLarge)
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
                    .padding(AppDimensions.Spacing.ExtraLarge)
                    .fillMaxWidth(0.9f),
                colors = CardDefaults.cardColors(
                    containerColor = AppColors.Primary
                )
            ) {
                Text(
                    text = message,
                    modifier = Modifier.padding(AppDimensions.Spacing.ExtraLarge),
                    color = AppColors.BackgroundPrimary,
                    style = AppTypography.BodyMedium.copy(fontWeight = FontWeight.SemiBold)
                )
            }
        }
    }

    // Loading overlay
    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.OverlayDarker),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = AppColors.Primary)
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
            .clip(AppShapes.CardExtraLarge)
            .background(Color(0xFF0F1F1A))
            .padding(AppDimensions.CardPadding.ExtraLarge)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {

            Box(
                modifier = Modifier
                    .size(AppDimensions.IconSize.Massive)
                    .clip(AppShapes.CardSmall)
                    .background(AppColors.Primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Watch,
                    contentDescription = null,
                    tint = AppColors.Primary,
                    modifier = Modifier.size(AppDimensions.IconSize.Massive)
                )
            }

            Spacer(modifier = Modifier.width(AppDimensions.Spacing.Default))

            Column(modifier = Modifier.weight(1f)) {
                if (isConnected) {
                    Text(
                        deviceName,
                        color = AppColors.TextPrimary,
                        style = AppTypography.HeadingExtraSmall
                    )
                    Text(
                        deviceAddress,
                        color = AppColors.TextSecondary,
                        style = AppTypography.LabelMedium
                    )
                } else {
                    Text(
                        "No Device Connected",
                        color = AppColors.TextPrimary,
                        style = AppTypography.HeadingExtraSmall
                    )
                    Text(
                        "Tap below to bind your device",
                        color = AppColors.TextSecondary,
                        style = AppTypography.LabelMedium
                    )
                }
            }

            if (isConnected) {
                Text(
                    "$batteryLevel%",
                    color = AppColors.Primary,
                    style = AppTypography.BodySmall.copy(fontWeight = FontWeight.Bold)
                )
            }
        }

        Spacer(modifier = Modifier.height(AppDimensions.Spacing.Large))

        if (isConnected) {

            Row(horizontalArrangement = Arrangement.spacedBy(AppDimensions.Spacing.Medium)) {
                MiniInfoCard("Status", "Connected")
                MiniInfoCard("Battery", "$batteryLevel%")
            }

            Spacer(modifier = Modifier.height(AppDimensions.Spacing.Large))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(AppShapes.CardSmall)
                    .background(AppColors.Error.copy(alpha = 0.15f))
                    .clickable { onDisconnect() }
                    .padding(vertical = AppDimensions.Spacing.Default),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Disconnect Device",
                    color = AppColors.Error,
                    style = AppTypography.BodySmall
                )
            }

        } else {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(AppShapes.CardSmall)
                    .background(AppColors.Primary.copy(alpha = 0.15f))
                    .clickable { onBindClick() }
                    .padding(vertical = AppDimensions.Spacing.Large),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Bind Device",
                    color = AppColors.Primary,
                    style = AppTypography.BodyMedium.copy(fontWeight = FontWeight.SemiBold)
                )
            }
        }
    }
}

@Composable
fun MiniInfoCard(title: String, subtitle: String) {
    Column(
        modifier = Modifier
            .clip(AppShapes.CardSmall)
            .background(AppColors.BackgroundTertiary)
            .padding(AppDimensions.Spacing.Default)
    ) {
        Text(
            title,
            color = AppColors.TextPrimary,
            style = AppTypography.BodyExtraSmall
        )
        Text(
            subtitle,
            color = AppColors.TextSecondary,
            style = AppTypography.LabelExtraSmall
        )
    }
}

@Composable
fun AppInfoFooter() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = AppDimensions.Spacing.Huge),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Page3 Fitness Tracker",
            color = AppColors.TextSecondary,
            style = AppTypography.LabelMedium
        )
        Text(
            "Version 2",
            color = AppColors.TextTertiary,
            style = AppTypography.LabelSmall
        )
        Text(
            "© 2025 Page3 Mumbai .",
            color = AppColors.TextDisabled,
            style = AppTypography.LabelSmall
        )
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
            .clip(AppShapes.CardExtraLarge)
            .background(AppColors.BackgroundCard)
            .padding(AppDimensions.CardPadding.ExtraLarge)
    ) {

        Text(
            title,
            color = AppColors.TextPrimary,
            style = AppTypography.HeadingExtraSmall
        )
        Spacer(modifier = Modifier.height(AppDimensions.Spacing.Default))

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
            .clip(AppShapes.CardSmall)
            .background(AppColors.BackgroundTertiary)
            .clickable { onClick() }
            .padding(AppDimensions.Spacing.Large),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                color = AppColors.TextPrimary,
                style = AppTypography.BodySmall
            )
            Text(
                subtitle,
                color = AppColors.TextSecondary,
                style = AppTypography.LabelSmall
            )
        }
        Icon(
            Icons.Default.ChevronRight,
            null,
            tint = AppColors.TextSecondary
        )
    }

    Spacer(modifier = Modifier.height(AppDimensions.Spacing.Medium))
}
data class SettingItem(
    val title: String,
    val subTitle: String,
    val onClick: () -> Unit
)
