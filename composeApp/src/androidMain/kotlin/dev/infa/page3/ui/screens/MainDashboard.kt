//package dev.infa.page3.ui.screens
//
//import android.os.Build
//import androidx.annotation.RequiresApi
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.*
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.vector.ImageVector
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.navigation.NavController
//import dev.infa.page3.ui.components.DateSelector
//import dev.infa.page3.ui.components.CircularProgressRing
//import dev.infa.page3.viewmodels.*
//import java.time.LocalDate
//import java.time.format.DateTimeFormatter
//
//@RequiresApi(Build.VERSION_CODES.O)
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun DashboardScreen(
//    modifier: Modifier = Modifier,
//    connectionViewModel: ConnectionViewModel,
//    homeViewModel: HomeViewModel,
//    stepViewModel: StepAnalyticsViewModel,
//    navController: NavController
//) {
//    // Connection state
//    val uiState by connectionViewModel.uiState.collectAsState()
//    val isConnected = uiState.isConnected
//    val deviceName = uiState.connectedDevice?.deviceName ?: ""
//
//    // Step data from StepViewModel
//    val selectedDateString by stepViewModel.selectedDate.collectAsState()
//    val selectedStepData by stepViewModel.selectedStepData.collectAsState()
//    val isLoadingSteps by stepViewModel.isLoading.collectAsState()
//
//    // Goals and other data from HomeViewModel
//    val stepGoal by homeViewModel.stepGoal.collectAsState()
//    val homeSleepData by homeViewModel.todaySleep.collectAsState()
//    val batteryLevel by homeViewModel.batteryValue.collectAsState()
//
//    // Track if we've synced today's data on screen open
//    var hasSyncedTodayOnOpen by remember { mutableStateOf(false) }
//    var hasInitiallyConnected by remember { mutableStateOf(false) }
//
//    // Get today's date for comparison
//    val todayDate = remember {
//        LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
//    }
//
//    // Convert String date to LocalDate for DateSelector
//    val selectedLocalDate = remember(selectedDateString) {
//        try {
//            LocalDate.parse(selectedDateString, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
//        } catch (e: Exception) {
//            LocalDate.now()
//        }
//    }
//
//    // ONE-TIME sync when screen opens and device is connected
//    LaunchedEffect(isConnected) {
//        if (isConnected && !hasInitiallyConnected) {
//            hasInitiallyConnected = true
//
//            // Sync other data
//            homeViewModel.getBatteryLevel()
//            homeViewModel.fetchDeviceCapabilities()
//
//            // Sync today's step data only once
//            if (!hasSyncedTodayOnOpen) {
//                stepViewModel.syncTodaySteps()
//                hasSyncedTodayOnOpen = true
//            }
//        } else if (!isConnected) {
//            hasInitiallyConnected = false
//        }
//    }
//
//    // Sync ONLY when user changes date (and it's not today)
//    LaunchedEffect(selectedDateString) {
//        if (isConnected && selectedDateString != todayDate && hasSyncedTodayOnOpen) {
//            // User changed to a different day, fetch that specific day's data
//            stepViewModel.syncStepDataForDate(selectedDateString)
//        }
//    }
//
//    Scaffold(
//        containerColor = Color.Black,
//        topBar = {
//            DashboardTopBar(
//                isConnected = isConnected,
//                deviceName = deviceName,
//                batteryLevel = batteryLevel ?: 0
//            )
//        }
//    ) { padding ->
//
//        if (!isConnected) {
//            DeviceDisconnectedState(
//                onRetry = {
//                    // Handle retry connection logic
//                    // connectionViewModel.reconnect()
//                }
//            )
//        } else {
//            LazyColumn(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(padding),
//                verticalArrangement = Arrangement.spacedBy(16.dp)
//            ) {
//
//                item {
//                    DateSelector(
//                        selectedDate = selectedLocalDate,
//                        onDateChange = { newLocalDate ->
//                            // Convert LocalDate to String format for ViewModel
//                            val dateString = newLocalDate.format(
//                                DateTimeFormatter.ofPattern("yyyy-MM-dd")
//                            )
//                            stepViewModel.selectDate(dateString)
//                        }
//                    )
//                }
//
//                item {
//                    ProgressTripleRow(
//                        stepData = selectedStepData,
//                        stepGoal = stepGoal,
//                        sleepData = homeSleepData,
//                        isLoadingSteps = isLoadingSteps,
//                        navController = navController
//                    )
//                }
//                item {
//                    StepDetailsCard(
//                        stepData = selectedStepData,
//                        isLoading = isLoadingSteps
//                    )
//                }
//            }
//        }
//    }
//}
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun DashboardTopBar(
//    isConnected: Boolean,
//    deviceName: String,
//    batteryLevel: Int
//) {
//    TopAppBar(
//        title = {
//            Row(verticalAlignment = Alignment.CenterVertically) {
//                Icon(
//                    imageVector = Icons.Default.Bluetooth,
//                    contentDescription = "Bluetooth Status",
//                    tint = if (isConnected) Color(0xFF00FF88) else Color.Red,
//                    modifier = Modifier.size(16.dp)
//                )
//                Spacer(Modifier.width(6.dp))
//                Text(
//                    text = if (isConnected) deviceName else "Disconnected",
//                    fontSize = 12.sp,
//                    color = Color.Gray
//                )
//            }
//        },
//        actions = {
//            if (isConnected) {
//                Text(
//                    text = "$batteryLevel%",
//                    color = Color(0xFF00FF88),
//                    fontSize = 12.sp,
//                    fontWeight = FontWeight.Medium
//                )
//                Spacer(Modifier.width(6.dp))
//                Icon(
//                    imageVector = Icons.Default.BatteryFull,
//                    contentDescription = "Battery Level",
//                    tint = Color(0xFF00FF88),
//                    modifier = Modifier.size(20.dp)
//                )
//                Spacer(Modifier.width(8.dp))
//            }
//        },
//        colors = TopAppBarDefaults.topAppBarColors(
//            containerColor = Color.Black
//        )
//    )
//}
//
//@Composable
//fun DeviceDisconnectedState(onRetry: () -> Unit) {
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(24.dp),
//        verticalArrangement = Arrangement.Center,
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        Icon(
//            imageVector = Icons.Default.BluetoothDisabled,
//            contentDescription = "Device Disconnected",
//            tint = Color.Red,
//            modifier = Modifier.size(64.dp)
//        )
//
//        Spacer(Modifier.height(16.dp))
//
//        Text(
//            text = "Device Disconnected",
//            color = Color.White,
//            fontSize = 20.sp,
//            fontWeight = FontWeight.Bold
//        )
//
//        Spacer(Modifier.height(8.dp))
//
//        Text(
//            text = "Please reconnect your device to view data",
//            color = Color.Gray,
//            fontSize = 14.sp
//        )
//
//        Spacer(Modifier.height(24.dp))
//
//        Button(
//            onClick = onRetry,
//            colors = ButtonDefaults.buttonColors(
//                containerColor = Color(0xFF00FF88)
//            ),
//            modifier = Modifier
//                .fillMaxWidth(0.6f)
//                .height(48.dp)
//        ) {
//            Icon(
//                imageVector = Icons.Default.Refresh,
//                contentDescription = null,
//                tint = Color.Black
//            )
//            Spacer(Modifier.width(8.dp))
//            Text(
//                text = "Retry Connection",
//                color = Color.Black,
//                fontWeight = FontWeight.Bold
//            )
//        }
//    }
//}
//
//@Composable
//fun ProgressTripleRow(
//    stepData: StepData?,
//    stepGoal: Int,
//    sleepData: SleepData?,
//    isLoadingSteps: Boolean,
//    navController: NavController
//) {
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(horizontal = 16.dp, vertical = 8.dp),
//        horizontalArrangement = Arrangement.SpaceEvenly
//    ) {
//        // Recovery Ring (placeholder - can be connected to heart rate data later)
//        CircularProgressRing(
//            value = 68,
//            max = 100,
//            label = "Recovery",
//            color = Color(0xFF00FF88),
//            icon = Icons.Default.Favorite
//        )
//
//        // Sleep Ring - Shows sleep score
//        CircularProgressRing(
//            value = sleepData?.sleepScore ?: 0,
//            max = 100,
//            label = "Sleep",
//            color = Color(0xFF3B82F6),
//            icon = Icons.Default.Nightlight
//        )
//
//        // Steps Ring - Connected to ViewModel with loading state
//        if (isLoadingSteps) {
//            CircularProgressRingLoading(
//                label = "Steps",
//                color = Color(0xFF6366F1),
//                icon = Icons.Default.DirectionsWalk
//            )
//        } else {
//            val stepValue = stepData?.totalSteps?.toInt() ?: 0
//            val stepPercentage = if (stepGoal > 0) {
//                ((stepValue.toFloat() / stepGoal.toFloat()) * 100).toInt().coerceIn(0, 100)
//            } else {
//                0
//            }
//
//            CircularProgressRing(
//                value = stepPercentage,
//                max = 100,
//                label = "Steps",
//                color = Color(0xFF6366F1),
//                icon = Icons.Default.DirectionsWalk,
//                onClick = {
//                    // Navigate to steps detail screen
//                    navController.navigate("steps_detail")
//                }
//            )
//        }
//    }
//}
//
//@Composable
//fun CircularProgressRingLoading(
//    label: String,
//    color: Color,
//    icon: ImageVector
//) {
//    Column(
//        horizontalAlignment = Alignment.CenterHorizontally,
//        modifier = Modifier.padding(8.dp)
//    ) {
//        Box(
//            contentAlignment = Alignment.Center,
//            modifier = Modifier.size(100.dp)
//        ) {
//            // Loading spinner
//            CircularProgressIndicator(
//                modifier = Modifier.size(100.dp),
//                color = color,
//                strokeWidth = 6.dp
//            )
//
//            // Icon in center
//            Icon(
//                imageVector = icon,
//                contentDescription = null,
//                tint = color.copy(alpha = 0.6f),
//                modifier = Modifier.size(24.dp)
//            )
//        }
//
//        Spacer(Modifier.height(6.dp))
//
//        Text(
//            text = label,
//            color = Color.Gray,
//            fontSize = 12.sp
//        )
//    }
//}
//
//@Composable
//fun StepDetailsCard(
//    stepData: StepData?,
//    isLoading: Boolean
//) {
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(horizontal = 16.dp),
//        colors = CardDefaults.cardColors(
//            containerColor = Color(0xFF1A1A1A)
//        ),
//        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
//    ) {
//        Column(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(20.dp)
//        ) {
//            // Header
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.SpaceBetween,
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Text(
//                    text = "Activity Details",
//                    color = Color.White,
//                    fontSize = 18.sp,
//                    fontWeight = FontWeight.Bold
//                )
//
//                Icon(
//                    imageVector = Icons.Default.DirectionsWalk,
//                    contentDescription = null,
//                    tint = Color(0xFF6366F1),
//                    modifier = Modifier.size(24.dp)
//                )
//            }
//
//            Spacer(Modifier.height(20.dp))
//
//            if (isLoading) {
//                // Loading state
//                Box(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .height(120.dp),
//                    contentAlignment = Alignment.Center
//                ) {
//                    CircularProgressIndicator(
//                        color = Color(0xFF00FF88),
//                        strokeWidth = 3.dp
//                    )
//                }
//            } else {
//                // Step data display
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.SpaceBetween
//                ) {
//                    // Steps
//                    StepMetricItem(
//                        icon = Icons.Default.DirectionsWalk,
//                        label = "Steps",
//                        value = formatSteps(stepData?.totalSteps ?: 0),
//                        color = Color(0xFF6366F1)
//                    )
//
//                    // Calories
//                    StepMetricItem(
//                        icon = Icons.Default.LocalFireDepartment,
//                        label = "Calories",
//                        value = formatCalories((stepData?.calories?.div(1000)) ?: 0),
//                        color = Color(0xFFFF6B6B)
//                    )
//
//                    // Distance
//                    StepMetricItem(
//                        icon = Icons.Default.Straighten,
//                        label = "Distance",
//                        value = formatDistance(stepData?.distance ?: 0),
//                        color = Color(0xFF00FF88)
//                    )
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun StepMetricItem(
//    icon: ImageVector,
//    label: String,
//    value: String,
//    color: Color
//) {
//    Column(
//        horizontalAlignment = Alignment.CenterHorizontally,
//        modifier = Modifier.padding(8.dp)
//    ) {
//        // Icon with background
//        Box(
//            modifier = Modifier
//                .size(48.dp)
//                .background(
//                    color.copy(alpha = 0.2f),
//                    androidx.compose.foundation.shape.CircleShape
//                ),
//            contentAlignment = Alignment.Center
//        ) {
//            Icon(
//                imageVector = icon,
//                contentDescription = null,
//                tint = color,
//                modifier = Modifier.size(24.dp)
//            )
//        }
//
//        Spacer(Modifier.height(8.dp))
//
//        Text(
//            text = value,
//            color = Color.White,
//            fontSize = 18.sp,
//            fontWeight = FontWeight.Bold
//        )
//
//        Spacer(Modifier.height(4.dp))
//
//        Text(
//            text = label,
//            color = Color.Gray,
//            fontSize = 12.sp
//        )
//    }
//}
//
//// Helper functions to format the data
//private fun formatSteps(steps: Long): String {
//    return when {
//        steps >= 1000 -> String.format("%.1fk", steps / 1000.0)
//        else -> steps.toString()
//    }
//}
//
//private fun formatCalories(calories: Long): String {
//    return "$calories kcal"
//}
//
//private fun formatDistance(distanceInMeters: Long): String {
//    val km = distanceInMeters / 1000.0
//    return String.format("%.2f km", km)
//}