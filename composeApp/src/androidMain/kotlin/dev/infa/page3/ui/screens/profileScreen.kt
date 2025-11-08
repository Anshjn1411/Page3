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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Battery3Bar
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import dev.infa.page3.models.SmartWatch
import dev.infa.page3.ui.components.AppSideBar
import dev.infa.page3.ui.components.BottomNavBar
import dev.infa.page3.ui.components.TopBarScreen
import dev.infa.page3.ui.navigation.Routes
import dev.infa.page3.viewmodels.AppType
import dev.infa.page3.viewmodels.ConnectionViewModel
import dev.infa.page3.viewmodels.ProfileViewModel
import dev.infa.page3.viewmodels.ThemeStyle
import dev.infa.page3.viewmodels.TimeFormat
import dev.infa.page3.viewmodels.TouchSettings
import dev.infa.page3.viewmodels.UnitSystem
import dev.infa.page3.viewmodels.UserSettings
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    connectionViewModel: ConnectionViewModel,
    profileViewModel: ProfileViewModel,
    navController: NavController
) {
    val uiState by connectionViewModel.uiState.collectAsState()
    val isConnected = uiState.isConnected
    val deviceName = uiState.connectedDevice?.deviceName ?: ""
    val deviceAddress = uiState.connectedDevice?.deviceAddress ?: ""
    val batteryLevel = uiState.batteryLevel ?: 0

    // Profile ViewModel States
    val userSettings by profileViewModel.userSettings.collectAsState()
    val isLoading by profileViewModel.isLoading.collectAsState()
    val errorMessage by profileViewModel.errorMessage.collectAsState()
    val successMessage by profileViewModel.successMessage.collectAsState()

    // Dialog States
    val showUnitDialog by profileViewModel.showUnitDialog.collectAsState()
    val showTimeFormatDialog by profileViewModel.showTimeFormatDialog.collectAsState()
    val showThemeDialog by profileViewModel.showThemeDialog.collectAsState()
    val showTouchDialog by profileViewModel.showTouchDialog.collectAsState()

    var showConnectionAlert by remember { mutableStateOf(false) }
    var currentTab by remember { mutableStateOf("me") }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

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
            delay(2000)
            profileViewModel.clearSuccessMessage()
        }
    }

    LaunchedEffect(isConnected) {
        if (isConnected) {
            connectionViewModel.getBatteryLevel()
        }
    }

    ModalNavigationDrawer(
        drawerContent = { AppSideBar(navController) },
        drawerState = drawerState
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Me",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    actions = {
                        IconButton(onClick = { navController.navigate(Routes.Scan) }) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(Color(0xFF2196F3), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add Device",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                )
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
                ProfileContent(
                    isConnected = isConnected,
                    deviceName = deviceName,
                    deviceAddress = deviceAddress,
                    batteryLevel = batteryLevel,
                    userSettings = userSettings,
                    onBindClick = { navController.navigate(Routes.Scan) },
                    onDisconnect = { connectionViewModel.disconnect() },
                    onFindDevice = {
                        if (isConnected) {
                            profileViewModel.findMyDevice()
                        } else {
                            showConnectionAlert = true
                        }
                    },
                    onUnitClick = { profileViewModel.showUnitDialog() },
                    onTimeFormatClick = { profileViewModel.showTimeFormatDialog() },
                    onThemeClick = { profileViewModel.showThemeDialog() },
                    onTouchSettingsClick = { profileViewModel.showTouchDialog() },
                    onLowBatteryToggle = { profileViewModel.toggleLowBatteryPrompt(it) }
                )

                // Connection Alert
                if (showConnectionAlert) {
                    ConnectionRequiredAlert()
                }

                // Success Message
                successMessage?.let {
                    SuccessAlert(message = it)
                }

                // Error Message
                errorMessage?.let {
                    ErrorAlert(message = it)
                }

                // Loading Indicator
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF2196F3))
                    }
                }
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

    if (showThemeDialog) {
        ThemeDialog(
            currentTheme = userSettings.themeStyle,
            onDismiss = { profileViewModel.dismissThemeDialog() },
            onSelect = { profileViewModel.updateThemeStyle(it) }
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
}

@Composable
private fun ProfileContent(
    isConnected: Boolean,
    deviceName: String,
    deviceAddress: String,
    batteryLevel: Int,
    userSettings: UserSettings,
    onBindClick: () -> Unit,
    onDisconnect: () -> Unit,
    onFindDevice: () -> Unit,
    onUnitClick: () -> Unit,
    onTimeFormatClick: () -> Unit,
    onThemeClick: () -> Unit,
    onTouchSettingsClick: () -> Unit,
    onLowBatteryToggle: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(Color(0xFFF5F5F5))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Device Status Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            if (isConnected) {
                ConnectedDeviceSection(
                    deviceName = deviceName,
                    deviceAddress = deviceAddress,
                    batteryLevel = batteryLevel,
                    onDisconnect = onDisconnect
                )
            } else {
                NotBoundSection(onBindClick = onBindClick)
            }
        }

        // Feature Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FeatureCard(
                modifier = Modifier.weight(1f),
                icon = "❤️",
                title = "Health\nMonitoring",
                backgroundColor = Color(0xFFFFA726)
            )
            FeatureCard(
                modifier = Modifier.weight(1f),
                icon = "📷",
                title = "Take Pictures",
                backgroundColor = Color(0xFFFF7043)
            )
        }

        // Settings List
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column {
                SettingsItem(
                    icon = "🔍",
                    title = "Find My Device",
                    iconColor = Color(0xFF00BCD4),
                    onClick = onFindDevice
                )
                Divider(color = Color(0xFFF0F0F0))

                SettingsItem(
                    icon = "👋",
                    title = "Touch & Gestures",
                    iconColor = Color(0xFF9C27B0),
                    onClick = onTouchSettingsClick
                )
                Divider(color = Color(0xFFF0F0F0))


                SettingsItem(
                    icon = "📏",
                    title = "Units Format",
                    value = userSettings.unitSystem.displayName,
                    iconColor = Color(0xFF4CAF50),
                    onClick = onUnitClick
                )
                Divider(color = Color(0xFFF0F0F0))

                SettingsItem(
                    icon = "🕐",
                    title = "Time Format",
                    value = userSettings.timeFormat.displayName,
                    iconColor = Color(0xFFFFC107),
                    onClick = onTimeFormatClick
                )
                Divider(color = Color(0xFFF0F0F0))

                SettingsItemWithToggle(
                    icon = "🔋",
                    title = "Low Battery Prompt",
                    checked = userSettings.lowBatteryPrompt,
                    iconColor = Color(0xFFFFA726),
                    onToggle = onLowBatteryToggle
                )
            }
        }
    }
}

// ========== DIALOGS ==========

@Composable
fun UnitSystemDialog(
    currentUnit: UnitSystem,
    onDismiss: () -> Unit,
    onSelect: (UnitSystem) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Select Unit System",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                UnitSystem.values().forEach { unit ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onSelect(unit) }
                            .background(
                                if (unit == currentUnit) Color(0xFFE3F2FD)
                                else Color.Transparent
                            )
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = unit == currentUnit,
                            onClick = { onSelect(unit) },
                            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF2196F3))
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = unit.displayName,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun TimeFormatDialog(
    currentFormat: TimeFormat,
    onDismiss: () -> Unit,
    onSelect: (TimeFormat) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Select Time Format",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TimeFormat.values().forEach { format ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onSelect(format) }
                            .background(
                                if (format == currentFormat) Color(0xFFE3F2FD)
                                else Color.Transparent
                            )
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = format == currentFormat,
                            onClick = { onSelect(format) },
                            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF2196F3))
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = format.displayName,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ThemeDialog(
    currentTheme: ThemeStyle,
    onDismiss: () -> Unit,
    onSelect: (ThemeStyle) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Select Theme",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ThemeStyle.values().forEach { theme ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onSelect(theme) }
                            .background(
                                if (theme == currentTheme) Color(0xFFE3F2FD)
                                else Color.Transparent
                            )
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = theme == currentTheme,
                            onClick = { onSelect(theme) },
                            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF2196F3))
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = theme.displayName,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun TouchSettingsDialog(
    touchSettings: TouchSettings,
    onDismiss: () -> Unit,
    onSave: (Int, Boolean, Int) -> Unit
) {
    var selectedAppType by remember { mutableStateOf(touchSettings.appType) }
    var isTouch by remember { mutableStateOf(touchSettings.isTouch) }
    var strength by remember { mutableStateOf(touchSettings.strength.toFloat()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Touch & Gestures Settings",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // App Type Selection
                Text(
                    "App Type",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    AppType.values().forEach { app ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { selectedAppType = app.code }
                                .background(
                                    if (selectedAppType == app.code) Color(0xFFE3F2FD)
                                    else Color.Transparent
                                )
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedAppType == app.code,
                                onClick = { selectedAppType = app.code },
                                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF2196F3))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(app.displayName, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                Divider()

                // Touch vs Gestures
                Text(
                    "Control Type",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Touch Mode")
                    Switch(
                        checked = isTouch,
                        onCheckedChange = { isTouch = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF2196F3))
                    )
                }

                Divider()

                // Strength Slider
                Text(
                    "Vibration Strength: ${strength.toInt()}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Slider(
                    value = strength,
                    onValueChange = { strength = it },
                    valueRange = 1f..10f,
                    steps = 8,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF2196F3),
                        activeTrackColor = Color(0xFF2196F3)
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(selectedAppType, isTouch, strength.toInt()) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// ========== HELPER COMPOSABLES ==========

@Composable
fun SettingsItem(
    icon: String,
    title: String,
    value: String? = null,
    iconColor: Color,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(iconColor.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(icon, fontSize = 20.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                title,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 2
            )
        }

        if (value != null) {
            Text(
                value,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }

        if (onClick != null) {
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.Gray
            )
        }
    }
}

@Composable
fun SettingsItemWithToggle(
    icon: String,
    title: String,
    checked: Boolean,
    iconColor: Color,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(iconColor.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(icon, fontSize = 20.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(title, style = MaterialTheme.typography.bodyLarge)
        }
        Switch(
            checked = checked,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF2196F3))
        )
    }
}

@Composable
fun SuccessAlert(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50)),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Text(
                text = message,
                color = Color.White,
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
fun ErrorAlert(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF44336)),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Text(
                text = message,
                color = Color.White,
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}



@Composable
private fun ConnectedDeviceSection(
    deviceName: String,
    deviceAddress: String,
    batteryLevel: Int,
    onDisconnect: () -> Unit
) {
    Column(
        modifier = Modifier.padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color(0xFF2196F3), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("⌚", fontSize = 24.sp)
                }
                Column {
                    Text(
                        text = deviceName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A)
                    )
                    Text(
                        text = deviceAddress,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF666666)
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(Color(0xFF4CAF50), CircleShape)
                )
                Text(
                    text = "Connected",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF4CAF50),
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Divider(color = Color(0xFFE0E0E0))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Battery3Bar,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Battery: $batteryLevel%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF333333)
                )
            }
            TextButton(onClick = onDisconnect) {
                Text(
                    text = "Disconnect",
                    color = Color(0xFFFF3B30),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun NotBoundSection(onBindClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "You have not bound the device yet",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1A1A)
        )
        Button(
            onClick = onBindClick,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF64B5F6)),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Text(
                text = "Bind immediately",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun FeatureCard(
    modifier: Modifier = Modifier,
    icon: String,
    title: String,
    backgroundColor: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(backgroundColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(icon, fontSize = 24.sp)
            }
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                color = Color(0xFF1A1A1A)
            )
        }
    }
}


