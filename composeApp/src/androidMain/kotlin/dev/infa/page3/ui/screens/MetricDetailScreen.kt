package dev.infa.page3.ui.screens

import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import dev.infa.page3.models.HealthData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MetricDetailScreen(
    type: String,
    isReading: Boolean,
    healthData: HealthData,
    battery: Int,
    onBack: () -> Unit
) {
    val configuration = getMetricConfiguration(type, isReading, healthData, battery)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = configuration.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            configuration.primaryColor.copy(alpha = 0.05f),
                            configuration.secondaryColor.copy(alpha = 0.02f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Main Metric Display Card
            MainMetricCard(
                configuration = configuration,
                isReading = isReading
            )

            // Status Card
            StatusCard(
                configuration = configuration,
                isReading = isReading
            )

            // Additional Info Cards
            AdditionalInfoSection(
                configuration = configuration,
                type = type
            )

            // Historical Trend (Mock data for demo)
            if (!isReading && configuration.hasValidData) {
                TrendCard(configuration = configuration)
            }
        }
    }
}

@Composable
private fun MainMetricCard(
    configuration: MetricConfiguration,
    isReading: Boolean
) {
    val animatedScale by animateFloatAsState(
        targetValue = if (isReading) 1.05f else 1f,
        animationSpec = tween(1000, easing = EaseInOutCubic),
        label = "scale"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(animatedScale),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            configuration.primaryColor.copy(alpha = 0.1f),
                            configuration.secondaryColor.copy(alpha = 0.05f),
                            Color.Transparent
                        ),
                        radius = 800f
                    )
                )
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Metric Icon with Pulse Effect
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    configuration.primaryColor.copy(
                                        alpha = if (isReading) pulseAlpha * 0.3f else 0.2f
                                    ),
                                    configuration.primaryColor.copy(alpha = 0.1f),
                                    Color.Transparent
                                ),
                                radius = 150f
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = configuration.icon,
                        fontSize = 48.sp,
                        modifier = Modifier.rotate(
                            if (isReading) {
                                val rotation by infiniteTransition.animateFloat(
                                    initialValue = 0f,
                                    targetValue = 360f,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(3000, easing = LinearEasing)
                                    ),
                                    label = "rotation"
                                )
                                rotation
                            } else 0f
                        )
                    )
                }

                // Main Value Display
                if (isReading) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            repeat(3) { index ->
                                val alpha by infiniteTransition.animateFloat(
                                    initialValue = 0.3f,
                                    targetValue = 1f,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(600),
                                        repeatMode = RepeatMode.Reverse
                                    ),
                                    label = "dot$index"
                                )
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .background(
                                            configuration.primaryColor.copy(alpha = alpha),
                                            CircleShape
                                        )
                                )
                            }
                        }
                        Text(
                            text = "Reading...",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = configuration.primaryColor.copy(alpha = pulseAlpha)
                        )
                        Text(
                            text = "Please wait",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = configuration.displayValue,
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontSize = 56.sp,
                                fontWeight = FontWeight.ExtraBold
                            ),
                            color = configuration.valueColor
                        )
                        Text(
                            text = configuration.unit,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusCard(
    configuration: MetricConfiguration,
    isReading: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isReading -> MaterialTheme.colorScheme.secondaryContainer
                configuration.hasValidData -> configuration.statusColor.copy(alpha = 0.1f)
                else -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
            }
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            when {
                                isReading -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                                configuration.hasValidData -> configuration.statusColor.copy(alpha = 0.2f)
                                else -> MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                            },
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when {
                            isReading -> "⏳"
                            configuration.hasValidData -> configuration.statusEmoji
                            else -> "❌"
                        },
                        fontSize = 18.sp
                    )
                }

                Column {
                    Text(
                        text = when {
                            isReading -> "Measuring"
                            configuration.hasValidData -> configuration.statusText
                            else -> "No Data"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = when {
                            isReading -> "Please keep device connected"
                            configuration.hasValidData -> "Last updated just now"
                            else -> "Start measurement to see data"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (!isReading && configuration.hasValidData) {
                Text(
                    text = configuration.confidenceLevel,
                    style = MaterialTheme.typography.labelMedium,
                    color = configuration.statusColor,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .background(
                            configuration.statusColor.copy(alpha = 0.1f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun AdditionalInfoSection(
    configuration: MetricConfiguration,
    type: String
) {
    val infoItems = getAdditionalInfo(type, configuration)

    if (infoItems.isNotEmpty()) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(infoItems) { info ->
                Card(
                    modifier = Modifier.width(140.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = info.icon,
                            fontSize = 24.sp
                        )
                        Text(
                            text = info.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = info.value,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TrendCard(configuration: MetricConfiguration) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Trend",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "📈 +2.3%",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFF4CAF50),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .background(
                            Color(0xFF4CAF50).copy(alpha = 0.1f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            // Simple trend visualization
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                repeat(7) { index ->
                    val height = (30 + (index * 5 + Math.random() * 10)).dp
                    Box(
                        modifier = Modifier
                            .width(28.dp)
                            .height(height)
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        configuration.primaryColor,
                                        configuration.primaryColor.copy(alpha = 0.6f)
                                    )
                                ),
                                shape = RoundedCornerShape(
                                    topStart = 4.dp,
                                    topEnd = 4.dp
                                )
                            )
                    )
                }
            }
        }
    }
}

// Data classes and helper functions
data class MetricConfiguration(
    val title: String,
    val icon: String,
    val displayValue: String,
    val unit: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val valueColor: Color,
    val statusColor: Color,
    val statusText: String,
    val statusEmoji: String,
    val confidenceLevel: String,
    val hasValidData: Boolean
)

data class InfoItem(
    val icon: String,
    val label: String,
    val value: String
)

@Composable
private fun getMetricConfiguration(
    type: String,
    isReading: Boolean,
    healthData: HealthData,
    battery: Int
): MetricConfiguration {
    return when (type) {
        "battery" -> MetricConfiguration(
            title = "Battery Level",
            icon = "🔋",
            displayValue = if (battery > 0) "$battery" else "--",
            unit = "Percent",
            primaryColor = when {
                battery > 50 -> Color(0xFF4CAF50)
                battery > 20 -> Color(0xFFFF9800)
                else -> Color(0xFFF44336)
            },
            secondaryColor = Color(0xFF81C784),
            valueColor = when {
                battery > 50 -> Color(0xFF2E7D32)
                battery > 20 -> Color(0xFFE65100)
                else -> Color(0xFFC62828)
            },
            statusColor = when {
                battery > 50 -> Color(0xFF4CAF50)
                battery > 20 -> Color(0xFFFF9800)
                else -> Color(0xFFF44336)
            },
            statusText = when {
                battery > 80 -> "Excellent"
                battery > 50 -> "Good"
                battery > 20 -> "Low"
                else -> "Critical"
            },
            statusEmoji = when {
                battery > 80 -> "🟢"
                battery > 50 -> "🔵"
                battery > 20 -> "🟡"
                else -> "🔴"
            },
            confidenceLevel = "High",
            hasValidData = battery > 0
        )

        "heart" -> MetricConfiguration(
            title = "Heart Rate",
            icon = "❤️",
            displayValue = if (healthData.heartRate > 0) "${healthData.heartRate}" else "Measuring....",
            unit = "BPM",
            primaryColor = Color(0xFFE91E63),
            secondaryColor = Color(0xFFF8BBD9),
            valueColor = Color(0xFFAD1457),
            statusColor = when {
                healthData.heartRate in 60..100 -> Color(0xFF4CAF50)
                healthData.heartRate in 50..120 -> Color(0xFFFF9800)
                else -> Color(0xFFF44336)
            },
            statusText = when {
                healthData.heartRate in 60..100 -> "Normal"
                healthData.heartRate < 60 -> "Low"
                else -> "High"
            },
            statusEmoji = when {
                healthData.heartRate in 60..100 -> "💚"
                healthData.heartRate < 60 -> "💙"
                else -> "❤️"
            },
            confidenceLevel = "High",
            hasValidData = healthData.heartRate > 0
        )

        "spo2" -> MetricConfiguration(
            title = "SpO2 Level",
            icon = "🫁",
            displayValue = if (healthData.spo2 > 0) "${healthData.spo2}" else "Measuring....",
            unit = "Percent",
            primaryColor = Color(0xFF2196F3),
            secondaryColor = Color(0xFFBBDEFB),
            valueColor = Color(0xFF1565C0),
            statusColor = when {
                healthData.spo2 >= 95 -> Color(0xFF4CAF50)
                healthData.spo2 >= 90 -> Color(0xFFFF9800)
                else -> Color(0xFFF44336)
            },
            statusText = when {
                healthData.spo2 >= 95 -> "Normal"
                healthData.spo2 >= 90 -> "Low"
                else -> "Critical"
            },
            statusEmoji = when {
                healthData.spo2 >= 95 -> "🟢"
                healthData.spo2 >= 90 -> "🟡"
                else -> "🔴"
            },
            confidenceLevel = "High",
            hasValidData = healthData.spo2 > 0
        )

        "bp" -> MetricConfiguration(
            title = "Blood Pressure",
            icon = "🩸",
            displayValue = if (healthData.systolic > 0)
                "${healthData.systolic}/${healthData.diastolic}" else "Measuring....",
            unit = "mmHg",
            primaryColor = Color(0xFF9C27B0),
            secondaryColor = Color(0xFFE1BEE7),
            valueColor = Color(0xFF7B1FA2),
            statusColor = when {
                healthData.systolic in 90..120 && healthData.diastolic in 60..80 -> Color(0xFF4CAF50)
                healthData.systolic in 80..140 && healthData.diastolic in 50..90 -> Color(0xFFFF9800)
                else -> Color(0xFFF44336)
            },
            statusText = when {
                healthData.systolic in 90..120 && healthData.diastolic in 60..80 -> "Normal"
                healthData.systolic > 140 || healthData.diastolic > 90 -> "High"
                else -> "Low"
            },
            statusEmoji = "🩺",
            confidenceLevel = "Medium",
            hasValidData = healthData.systolic > 0
        )

        "temp" -> MetricConfiguration(
            title = "Body Temperature",
            icon = "🌡️",
            displayValue = if (healthData.temperature > 0)
                String.format("%.1f", healthData.temperature) else "--",
            unit = "°Celsius",
            primaryColor = Color(0xFFFF5722),
            secondaryColor = Color(0xFFFFCCBC),
            valueColor = Color(0xFFD84315),
            statusColor = when {
                healthData.temperature in 36.1..37.2 -> Color(0xFF4CAF50)
                healthData.temperature > 37.2 -> Color(0xFFF44336)
                else -> Color(0xFF2196F3)
            },
            statusText = when {
                healthData.temperature in 36.1..37.2 -> "Normal"
                healthData.temperature > 37.2 -> "Fever"
                else -> "Low"
            },
            statusEmoji = when {
                healthData.temperature in 36.1..37.2 -> "🟢"
                healthData.temperature > 37.2 -> "🔴"
                else -> "🔵"
            },
            confidenceLevel = "High",
            hasValidData = healthData.temperature > 0
        )

        "hrv" -> MetricConfiguration(
            title = "Heart Rate Variability",
            icon = "📊",
            displayValue = if (healthData.hrvValue > 0) "${healthData.hrvValue}" else "Measuring....",
            unit = "milliseconds",
            primaryColor = Color(0xFF00BCD4),
            secondaryColor = Color(0xFFB2EBF2),
            valueColor = Color(0xFF0097A7),
            statusColor = Color(0xFF4CAF50),
            statusText = "Good",
            statusEmoji = "📈",
            confidenceLevel = "Medium",
            hasValidData = healthData.hrvValue > 0
        )

        "pressure" -> MetricConfiguration(
            title = "Atmospheric Pressure",
            icon = "🌤️",
            displayValue = if (healthData.pressure > 0) "${healthData.pressure}" else "--",
            unit = "hPa",
            primaryColor = Color(0xFF607D8B),
            secondaryColor = Color(0xFFCFD8DC),
            valueColor = Color(0xFF455A64),
            statusColor = Color(0xFF4CAF50),
            statusText = "Normal",
            statusEmoji = "🌤️",
            confidenceLevel = "High",
            hasValidData = healthData.pressure > 0
        )

        else -> MetricConfiguration(
            title = "Measurement",
            icon = "📊",
            displayValue = "Measuring....",
            unit = "",
            primaryColor = Color(0xFF9E9E9E),
            secondaryColor = Color(0xFFE0E0E0),
            valueColor = Color(0xFF616161),
            statusColor = Color(0xFF9E9E9E),
            statusText = "Unknown",
            statusEmoji = "❓",
            confidenceLevel = "Low",
            hasValidData = false
        )
    }
}

private fun getAdditionalInfo(type: String, configuration: MetricConfiguration): List<InfoItem> {
    return when (type) {
        "heart" -> listOf(
            InfoItem("⏱️", "Resting HR", "68 BPM"),
            InfoItem("📈", "Max Today", "145 BPM"),
            InfoItem("🎯", "Target Zone", "120-150")
        )
        "spo2" -> listOf(
            InfoItem("🫁", "Lung Capacity", "Normal"),
            InfoItem("🏃", "Active SpO2", "97%"),
            InfoItem("😴", "Sleep SpO2", "96%")
        )
        "bp" -> listOf(
            InfoItem("💓", "Pulse Pressure", configuration.displayValue.split("/").let {
                if (it.size == 2) "${it[0].toIntOrNull()?.minus(it[1].toIntOrNull() ?: 0) ?: 0}" else "0"
            }),
            InfoItem("⏰", "Last Check", "2 hrs ago"),
            InfoItem("📊", "Avg Today", "118/76")
        )
        "battery" -> listOf(
            InfoItem("⚡", "Charging", "Not plugged"),
            InfoItem("🔋", "Health", "Excellent"),
            InfoItem("⏳", "Time Left", "2 days")
        )
        else -> emptyList()
    }
}
