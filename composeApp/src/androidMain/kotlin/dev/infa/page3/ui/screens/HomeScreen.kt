package dev.infa.page3.ui.screens

import android.annotation.SuppressLint
import android.util.Log
import dev.infa.page3.R
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.infa.page3.models.DeviceCapabilities
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import dev.infa.page3.ui.components.AppSideBar
import dev.infa.page3.ui.components.BottomNavBar
import dev.infa.page3.ui.components.SDKTopBarScreen
import dev.infa.page3.ui.components.TopBarScreen
import dev.infa.page3.ui.navigation.Routes

import dev.infa.page3.ui.theme.Page3Theme
import dev.infa.page3.viewmodels.ConnectionViewModel
import dev.infa.page3.viewmodels.HomeViewModel
import dev.infa.page3.viewmodels.SleepViewModel

import dev.infa.page3.viewmodels.StepViewmodel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*



@SuppressLint("UnrememberedMutableState", "DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    connectionViewModel: ConnectionViewModel,
    homeViewModel: HomeViewModel,
    stepViewModel: StepViewmodel,
    navController: NavController
) {

    var currentTab by remember { mutableStateOf("home") }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerContent = { AppSideBar(navController) },
        drawerState = drawerState
    ) {
        Scaffold(
            bottomBar = {
                BottomNavBar(currentNav = currentTab, navController)
            }
        ) { innerPadding ->
            DashboardScreen(
                Modifier.padding(innerPadding),
                connectionViewModel
                ,homeViewModel,
                stepViewModel = stepViewModel,
                navController

            )

//            val activityCardData = if (displayStepData != null) {
//                val activityPercentage = ((displayStepData.totalSteps.toFloat() / 5000f) * 100)
//                    .toInt().coerceIn(0, 100)
//
//                HealthCardData(
//                    title = "Activity",
//                    date = todayDate,
//                    backgroundImageRes = R.drawable.activity_image,
//                    progressPercentage = activityPercentage,
//                    progressIcon = Icons.Default.Star,
//                    statusText = when {
//                        activityPercentage >= 80 -> "Great job!"
//                        activityPercentage >= 50 -> "Keep going!"
//                        activityPercentage >= 20 -> "Getting started"
//                        else -> "Lack of exercise"
//                    },
//                    metrics = listOf(
//                        HealthMetric(
//                            Icons.Default.LocalFireDepartment,
//                            (displayStepData.calories/1000).toString(),
//                            "Kcal"
//                        ),
//                        HealthMetric(
//                            Icons.Default.DirectionsWalk,
//                            displayStepData.totalSteps.toString(),
//                            "Steps"
//                        ),
//                        HealthMetric(
//                            Icons.Default.LocationOn,
//                            String.format("%.2f", displayStepData.distance / 1000f),
//                            "Km"
//                        )
//                    )
//                )
//            } else {
//                HealthCardData(
//                    title = "Activity",
//                    date = todayDate,
//                    backgroundImageRes = R.drawable.activity_image,
//                    progressPercentage = 0,
//                    progressIcon = Icons.Default.Star,
//                    statusText = when {
//                        isLoadingData -> "Syncing..."
//                        !isConnected -> "Device not connected"
//                        else -> "No data yet"
//                    },
//                    metrics = listOf(
//                        HealthMetric(Icons.Default.LocalFireDepartment, "--", "Kcal"),
//                        HealthMetric(Icons.Default.DirectionsWalk, "--", "Steps"),
//                        HealthMetric(Icons.Default.LocationOn, "--", "Km")
//                    )
//                )
//            }
//
//            val sleepCardData = if (homeSleepData != null) {
//                HealthCardData(
//                    title = "Sleep",
//                    date = homeSleepData!!.date,
//                    backgroundImageRes = R.drawable.activity_image,
//                    progressPercentage = homeSleepData!!.sleepScore,
//                    progressIcon = Icons.Default.Star,
//                    statusText = when {
//                        homeSleepData!!.sleepScore >= 90 -> "Excellent sleep!"
//                        homeSleepData!!.sleepScore >= 75 -> "Good sleep"
//                        homeSleepData!!.sleepScore >= 60 -> "Fair sleep"
//                        else -> "Poor sleep"
//                    },
//                    metrics = listOf(
//                        HealthMetric(
//                            Icons.Default.Bedtime,
//                            "${homeSleepData!!.totalDuration / 60}h ${homeSleepData!!.totalDuration % 60}m",
//                            "Duration"
//                        ),
//                        HealthMetric(
//                            Icons.Default.TrendingUp,
//                            "${homeSleepData!!.sleepEfficiency}%",
//                            "Efficiency"
//                        ),
//                        HealthMetric(
//                            Icons.Default.Nightlight,
//                            "${homeSleepData!!.deepSleep}m",
//                            "Deep"
//                        )
//                    )
//                )
//            } else {
//                HealthCardData(
//                    title = "Sleep",
//                    date = todayDate,
//                    backgroundImageRes = R.drawable.activity_image,
//                    progressPercentage = 0,
//                    progressIcon = Icons.Default.Star,
//                    statusText = "Hardcoded data",
//                    metrics = listOf(
//                        HealthMetric(Icons.Default.Bedtime, "--", "Duration"),
//                        HealthMetric(Icons.Default.TrendingUp, "--", "Efficiency"),
//                        HealthMetric(Icons.Default.Nightlight, "--", "Deep")
//                    )
//                )
//            }
//
//            val heartRateCard = SimpleCardData(
//                title = "Heart Rate",
//                icon = Icons.Default.Favorite,
//                message = if (isConnected) {
//                    "Tap to view your heart rate data"
//                } else {
//                    "Connect your device to track heart rate"
//                },
//                backgroundColor = Color(0xFF5B8FA3)
//            )
//
//            val hrvCard = SimpleCardData(
//                title = "HRV",
//                icon = Icons.Default.Favorite,
//                message = if (isConnected) {
//                    "Tap to view heart rate variability"
//                } else {
//                    "Connect your device to track HRV"
//                },
//                backgroundColor = Color(0xFF6B8FA3)
//            )
//
//            val bloodOxygenCard = SimpleCardData(
//                title = "Blood Oxygen",
//                icon = Icons.Default.WaterDrop,
//                message = if (isConnected) {
//                    "Tap to view blood oxygen levels"
//                } else {
//                    "Connect your device to track blood oxygen"
//                },
//                backgroundColor = Color(0xFF7B99C8)
//            )
//
//            val stressCard = SimpleCardData(
//                title = "Stress",
//                icon = Icons.Default.Spa,
//                message = if (isConnected) {
//                    "Tap to view stress analysis"
//                } else {
//                    "Connect your device to track stress"
//                },
//                backgroundColor = Color(0xFF5B9BA3)
//            )
//
//            val exerciseCard = SimpleCardData(
//                title = "Exercise",
//                icon = Icons.Default.DirectionsRun,
//                message = if (isConnected) {
//                    "Tap to start a workout"
//                } else {
//                    "Connect your device to exercise"
//                },
//                backgroundColor = Color(0xFF4D96FF)
//            )
//
//            LazyColumn(
//                modifier = modifier
//                    .fillMaxSize()
//                    .padding(innerPadding)
//                    .padding(horizontal = 5.dp),
//                verticalArrangement = Arrangement.spacedBy(10.dp)
//            ) {
//
//                item {
//                    if (isConnected) {
//                        ConnectedDeviceInfoCard(
//                            deviceName = deviceName,
//                            batteryLevel = batteryLevel,
//                            connectionStatus = "Connected",
//                            onManageClick = { navController.navigate(Routes.Profile) }
//                        )
//                    } else {
//                        BindCard(onBindClick = { navController.navigate(Routes.Profile) })
//                    }
//                }
//
//                item {
//                    Box(
//                        modifier = Modifier.clickable {
//                            if (isConnected) navController.navigate(Routes.Step)
//                        }
//                    ) {
//                        HealthCard(data = activityCardData)
//                    }
//                }
//
//                item {
//                    Box(
//                        modifier = Modifier.clickable {
//                            if (isConnected) navController.navigate(Routes.Sleep)
//                        }
//                    ) {
//                        HealthCard(data = sleepCardData)
//                    }
//                }
//
//
//                item {
//                    SimpleInfoCard(
//                        data = heartRateCard,
//                        onCLick = {
//                            if (isConnected) {
//                                navController.navigate(Routes.Heart)
//                            } else {
//                                navController.navigate(Routes.Profile)
//                            }
//                        }
//                    )
//                }
//
//                if (deviceCapabilities?.supportHrv == true || !isConnected) {
//                    item {
//                        SimpleInfoCard(
//                            data = hrvCard,
//                            onCLick = {
//                                if (isConnected) {
//                                    navController.navigate(Routes.HRV)
//                                } else {
//                                    navController.navigate(Routes.Profile)
//                                }
//                            }
//                        )
//                    }
//                }
//
//
//                if (deviceCapabilities?.supportBloodOxygen == true || !isConnected) {
//                    item {
//                        SimpleInfoCard(
//                            data = bloodOxygenCard,
//                            onCLick = {
//                                if (isConnected) {
//                                    navController.navigate(Routes.BloodOxygen)
//                                } else {
//                                    navController.navigate(Routes.Profile)
//                                }
//                            }
//                        )
//                    }
//                }
//
//
//                item {
//                    SimpleInfoCard(
//                        data = stressCard,
//                        onCLick = {
//                            if (isConnected) {
//                                navController.navigate(Routes.Stress)
//                            } else {
//                                navController.navigate(Routes.Profile)
//                            }
//                        }
//                    )
//                }
//
//                item {
//                    SimpleInfoCard(
//                        data = exerciseCard,
//                        onCLick = {
//                            if (isConnected) {
//                                navController.navigate(Routes.Exercise)
//                            } else {
//                                navController.navigate(Routes.Profile)
//                            }
//                        }
//                    )
//                }
//            }
//        }
        }
    }
}

@Composable
fun ConnectedDeviceInfoCard(
    deviceName: String,
    batteryLevel: Int?,
    connectionStatus: String,
    onManageClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Device Icon with Status Indicator
            Box(
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF4D96FF).copy(alpha = 0.15f),
                                    Color(0xFF5B8FA3).copy(alpha = 0.08f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Watch,
                        contentDescription = "Smart Watch",
                        tint = Color(0xFF4D96FF),
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Status indicator dot
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF4CAF50))
                        .border(2.dp, Color.White, CircleShape)
                        .align(Alignment.TopEnd)
                        .offset(x = 4.dp, y = (-4).dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Device Info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⌚",
                        fontSize = 16.sp,
                        modifier = Modifier.padding(end = 6.dp)
                    )
                    Text(
                        text = deviceName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2C3E50),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Status badge
                    Box(
                        modifier = Modifier
                            .background(
                                color = Color(0xFF4CAF50).copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = connectionStatus,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF4CAF50)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    // Battery indicator
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when {
                                batteryLevel == null -> Icons.Default.Refresh
                                batteryLevel > 80 -> Icons.Default.BatteryFull
                                batteryLevel > 50 -> Icons.Default.Battery6Bar
                                batteryLevel > 20 -> Icons.Default.Battery3Bar
                                else -> Icons.Default.Battery2Bar
                            },
                            contentDescription = "Battery",
                            tint = when {
                                batteryLevel == null -> Color.Blue
                                batteryLevel > 20 -> Color(0xFF4CAF50)
                                else -> Color(0xFFFF9800)
                            },
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$batteryLevel%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF7F8C8D)
                        )
                    }
                }
            }

            // Manage button
            IconButton(
                onClick = onManageClick,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF5F5F5))
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Manage Device",
                    tint = Color(0xFF4D96FF),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun BindCard(
    onBindClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF6B8FA3)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF5B8FA3),
                            Color(0xFF4D96FF)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Watch,
                        contentDescription = "Smart Watch",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "No Device Connected",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Connect your smartwatch to track your health data",
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { onBindClick() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = Color(0xFF4D96FF),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Bind Device",
                        color = Color(0xFF4D96FF),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}




// Data class for health metrics
data class HealthMetric(
    val icon: ImageVector,
    val value: String,
    val label: String
)

// Data class for health card
data class HealthCardData(
    val title: String,
    val date: String,
    val backgroundImageRes: Int,
    val progressPercentage: Int,
    val progressIcon: ImageVector,
    val statusText: String,
    val metrics: List<HealthMetric>
)

// Data class for simple info card
data class SimpleCardData(
    val title: String,
    val icon: ImageVector,
    val message: String,
    val backgroundColor: Color
)

@Composable
fun HealthCard(
    data: HealthCardData,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(400.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Background Image
            Image(
                painter = painterResource(id = data.backgroundImageRes),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Gradient Overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.3f),
                                Color.Black.copy(alpha = 0.1f)
                            )
                        )
                    )
            )

            // Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            text = data.title,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = data.date,
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }

                    IconButton(
                        onClick = { /* Navigate action */ },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "View Details",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Semi-Circular Progress Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    SemiCircularProgressBar(
                        percentage = data.progressPercentage,
                        icon = data.progressIcon,
                        statusText = data.statusText
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Metrics Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    data.metrics.forEach { metric ->
                        MetricItem(metric = metric)
                    }
                }
            }
        }
    }
}

@Composable
fun SemiCircularProgressBar(
    percentage: Int,
    icon: ImageVector,
    statusText: String
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(180.dp)
    ) {
        Canvas(modifier = Modifier.size(160.dp)) {
            val strokeWidth = 20f
            val startAngle = 180f
            val sweepAngle = 180f
            val size = Size(size.width - strokeWidth, size.height - strokeWidth)

            // Background Arc
            drawArc(
                color = Color.White.copy(alpha = 0.3f),
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                size = size,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Progress Arc
            drawArc(
                color = Color.White,
                startAngle = startAngle,
                sweepAngle = sweepAngle * (percentage / 100f),
                useCenter = false,
                topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                size = size,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        // Center Content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.offset(y = (-10).dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$percentage",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = statusText,
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.9f)
            )
        }
    }
}

@Composable
fun MetricItem(metric: HealthMetric) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(80.dp)
    ) {
        Icon(
            imageVector = metric.icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = metric.value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = metric.label,
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.9f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun SimpleInfoCard(
    onCLick:()->Unit={},
    data: SimpleCardData,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp).clickable{onCLick()},
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = data.backgroundColor
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header with title and arrow
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = data.title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                IconButton(
                    onClick = { /* Navigate action */ },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "View Details",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Center content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = data.icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = data.message,
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }
        }
    }
}








