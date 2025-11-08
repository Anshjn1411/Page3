//package dev.infa.page3.ui.screens
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Modifier
//
//import androidx.compose.animation.AnimatedVisibility
//import androidx.compose.animation.expandVertically
//import androidx.compose.animation.fadeIn
//import androidx.compose.animation.fadeOut
//import androidx.compose.animation.shrinkVertically
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.LazyRow
//import androidx.compose.foundation.lazy.items
//import androidx.compose.foundation.rememberScrollState
//import androidx.compose.foundation.text.KeyboardOptions
//import androidx.compose.foundation.verticalScroll
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.KeyboardArrowDown
//import androidx.compose.material.icons.filled.KeyboardArrowUp
//import androidx.compose.material3.*
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.input.KeyboardType
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import dev.infa.page3.models.DeviceCapabilities
//import dev.infa.page3.models.HealthData
//import dev.infa.page3.models.HealthSettings
//import dev.infa.page3.models.SmartWatch
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun MoreFunctionScreen(
//    modifier: Modifier = Modifier,
//    isConnected: Boolean,
//    healthData: HealthData,
//    healthSettings: HealthSettings,
//    deviceCapabilities: DeviceCapabilities,
//    isReadingHealth: Boolean,
//    // Track individual measurement states
//    isReadingBloodPressure: Boolean = false,
//    isReadingHrv: Boolean = false,
//    isReadingBloodOxygen: Boolean = false,
//    isReadingTemperature: Boolean = false,
//    isReadingPressure: Boolean = false,
//    isReadingComprehensive: Boolean = false,
//    // Additional health functions not in Home
//    onMeasureBloodPressureOnce: () -> Unit,
//    onMeasureHrvOnce: () -> Unit,
//    onMeasureBloodOxygenOnce: () -> Unit,
//    onMeasureTemperatureOnce: () -> Unit,
//    onMeasurePressureOnce: () -> Unit,
//    onPerformOneKeyMeasurement: () -> Unit,
//    onToggleBloodPressure: (Boolean) -> Unit,
//    onToggleHrv: (Boolean) -> Unit,
//    onToggleBloodOxygen: (Boolean) -> Unit,
//    onToggleTemperature: (Boolean) -> Unit,
//    onTogglePressure: (Boolean) -> Unit,
//    onReadBloodPressure: () -> Unit,
//    onReadHrv: () -> Unit,
//    onReadBloodOxygen: () -> Unit,
//    onReadPressure: () -> Unit,
//    // Sports & Goals
//    onSetSportsGoals: (Int, Int, Int, Int, Int) -> Unit,
//    // Device utilities
//    onFindDevice: () -> Unit,
//    onFactoryReset: () -> Unit,
//    onStartCalibration: () -> Unit,
//    onStopCalibration: () -> Unit,
//    // Exercise actions
//    onStartExercise: (Int) -> Unit,
//    onPauseExercise: (Int) -> Unit,
//    onResumeExercise: (Int) -> Unit,
//    onEndExercise: (Int) -> Unit,
//    // Camera
//    onEnterCameraMode: () -> Unit,
//    onKeepCameraScreenOn: () -> Unit,
//    onExitCameraMode: () -> Unit,
//    // Message Push
//    onEnableMessagePush: () -> Unit,
//    onPushMessage: (Int, String) -> Unit,
//    // Touch & Gesture
//    onReadTouchSettings: (Boolean) -> Unit,
//    onWriteTouchSettings: (Int, Boolean, Int) -> Unit,
//    // Connection & Utility actions
//    onScanDevices: () -> Unit,
//    onConnectDevice: () -> Unit,
//    onDisconnectDevice: () -> Unit,
//    onSelectDevice: (SmartWatch) -> Unit,
//    onReinitializeSDK: () -> Unit,
//    onRequestPermissions: () -> Unit,
//    onTestBLEScan: () -> Unit,
//    // Navigation
//    onOpenConnect: () -> Unit,
//    onOpenDetail: (String) -> Unit,
//    // Missing Data Sync Functions - ADD THESE TO YOUR CALLING CODE
//    onSyncTodaySteps: () -> Unit,
//    onSyncHeartRateData: () -> Unit,
//    onSyncSleepData: (String, Int) -> Unit,
//    onSyncTemperatureData: () -> Unit,
//    onSyncTrainingRecords: () -> Unit,
//    onSyncMuslimData: (Int) -> Unit,
//    onSyncAllHealthData: () -> Unit,
//    onSyncHistoricalData: (Int) -> Unit,
//    onSyncManualBloodPressure: () -> Unit,
//    onConfirmBloodPressureSync: () -> Unit,
//    // Additional Sync Functions
//    onSyncNewSleepData: (Int, Boolean) -> Unit,
//    onSyncSedentaryData: (Int) -> Unit,
//    onSyncDetailStepData: (Int) -> Unit,
//    onInitTemperatureCallback: () -> Unit,
//    onSyncManualTemperature: (Int) -> Unit,
//    // Raw Data Measurement Functions - ADD THESE TO YOUR CALLING CODE
//    onMeasureHeartRateRaw: (Int) -> Unit,
//    onMeasureBloodOxygenRaw: (Int) -> Unit,
//    onCalculateBloodPressure: (Int, Int) -> Unit,
//    // Stop Measurement Functions - ADD THESE TO YOUR CALLING CODE
//) {
//    val scrollState = rememberScrollState()
//    var expandedSections by remember { mutableStateOf(setOf<String>()) }
//
//    fun toggleSection(section: String) {
//        expandedSections = if (expandedSections.contains(section)) {
//            expandedSections - section
//        } else {
//            expandedSections + section
//        }
//    }
//
//    Column(
//        modifier = modifier
//            .fillMaxSize()
//            .padding(16.dp)
//            .verticalScroll(scrollState),
//        verticalArrangement = Arrangement.spacedBy(16.dp)
//    ) {
//        // Header
//        Text(
//            text = "More Functions",
//            style = MaterialTheme.typography.headlineMedium,
//            fontWeight = FontWeight.Bold,
//            modifier = Modifier.padding(bottom = 8.dp)
//        )
//
//        if (!isConnected) {
//            // Not Connected State
//            Card(
//                modifier = Modifier.fillMaxWidth(),
//                colors = CardDefaults.cardColors(
//                    containerColor = MaterialTheme.colorScheme.errorContainer
//                )
//            ) {
//                Column(
//                    modifier = Modifier.padding(16.dp),
//                    horizontalAlignment = Alignment.CenterHorizontally
//                ) {
//                    Text(
//                        text = "Device Not Connected",
//                        style = MaterialTheme.typography.titleMedium,
//                        color = MaterialTheme.colorScheme.onErrorContainer
//                    )
//                    Text(
//                        text = "Connect your device to access all functions",
//                        style = MaterialTheme.typography.bodyMedium,
//                        color = MaterialTheme.colorScheme.onErrorContainer,
//                        modifier = Modifier.padding(top = 4.dp)
//                    )
//                    Button(
//                        onClick = onOpenConnect,
//                        modifier = Modifier.padding(top = 12.dp)
//                    ) {
//                        Text("Connect Device")
//                    }
//                }
//            }
//        } else {
//            // Data Synchronization Section - NEW
//            ExpandableFunctionSection(
//                title = "Data Synchronization",
//                icon = "🔄",
//                isExpanded = expandedSections.contains("sync"),
//                onToggle = { toggleSection("sync") }
//            ) {
//                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
//                    // Comprehensive Sync
//                    Card(
//                        modifier = Modifier.fillMaxWidth(),
//                        colors = CardDefaults.cardColors(
//                            containerColor = MaterialTheme.colorScheme.primaryContainer
//                        )
//                    ) {
//                        Column(modifier = Modifier.padding(16.dp)) {
//                            Text(
//                                text = "📊 Sync All Health Data",
//                                style = MaterialTheme.typography.titleMedium,
//                                fontWeight = FontWeight.Bold
//                            )
//                            Text(
//                                text = "Sync all health metrics at once",
//                                style = MaterialTheme.typography.bodyMedium,
//                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
//                            )
//                            Button(
//                                onClick = onSyncAllHealthData,
//                                modifier = Modifier.padding(top = 8.dp)
//                            ) {
//                                Text("Sync All Data")
//                            }
//                        }
//                    }
//
//                    // Individual Sync Options
//                    Row(
//                        modifier = Modifier.fillMaxWidth(),
//                        horizontalArrangement = Arrangement.spacedBy(8.dp)
//                    ) {
//                        OutlinedButton(
//                            onClick = onSyncTodaySteps,
//                            modifier = Modifier.weight(1f)
//                        ) {
//                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
//                                Text("👟", fontSize = 20.sp)
//                                Text("Steps", fontSize = 12.sp)
//                            }
//                        }
//                        OutlinedButton(
//                            onClick = onSyncHeartRateData,
//                            modifier = Modifier.weight(1f)
//                        ) {
//                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
//                                Text("💓", fontSize = 20.sp)
//                                Text("Heart Rate", fontSize = 12.sp)
//                            }
//                        }
//                    }
//
//                    Row(
//                        modifier = Modifier.fillMaxWidth(),
//                        horizontalArrangement = Arrangement.spacedBy(8.dp)
//                    ) {
//                        OutlinedButton(
//                            onClick = { onSyncSleepData("", 0) },
//                            modifier = Modifier.weight(1f)
//                        ) {
//                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
//                                Text("😴", fontSize = 20.sp)
//                                Text("Sleep", fontSize = 12.sp)
//                            }
//                        }
//                        OutlinedButton(
//                            onClick = onSyncTemperatureData,
//                            modifier = Modifier.weight(1f)
//                        ) {
//                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
//                                Text("🌡️", fontSize = 20.sp)
//                                Text("Temperature", fontSize = 12.sp)
//                            }
//                        }
//                    }
//
//                    Row(
//                        modifier = Modifier.fillMaxWidth(),
//                        horizontalArrangement = Arrangement.spacedBy(8.dp)
//                    ) {
//                        OutlinedButton(
//                            onClick = onSyncTrainingRecords,
//                            modifier = Modifier.weight(1f)
//                        ) {
//                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
//                                Text("🏃", fontSize = 20.sp)
//                                Text("Training", fontSize = 12.sp)
//                            }
//                        }
//                        OutlinedButton(
//                            onClick = { onSyncMuslimData(0) },
//                            modifier = Modifier.weight(1f)
//                        ) {
//                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
//                                Text("🕌", fontSize = 20.sp)
//                                Text("Prayer", fontSize = 12.sp)
//                            }
//                        }
//                    }
//
//                    // Historical Data Sync
//                    var historicalDays by remember { mutableStateOf("7") }
//                    Card {
//                        Column(modifier = Modifier.padding(16.dp)) {
//                            Text(
//                                text = "📈 Historical Data Sync",
//                                style = MaterialTheme.typography.titleSmall,
//                                fontWeight = FontWeight.Bold
//                            )
//                            Row(
//                                modifier = Modifier.fillMaxWidth(),
//                                verticalAlignment = Alignment.CenterVertically
//                            ) {
//                                Text("Days:", modifier = Modifier.width(60.dp))
//                                OutlinedTextField(
//                                    value = historicalDays,
//                                    onValueChange = { historicalDays = it },
//                                    modifier = Modifier
//                                        .weight(1f)
//                                        .padding(horizontal = 8.dp),
//                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
//                                    singleLine = true
//                                )
//                                Button(
//                                    onClick = {
//                                        val days = historicalDays.toIntOrNull() ?: 7
//                                        onSyncHistoricalData(days)
//                                    }
//                                ) {
//                                    Text("Sync")
//                                }
//                            }
//                        }
//                    }
//
//                    // Blood Pressure Sync & Confirm
//                    Card {
//                        Column(modifier = Modifier.padding(16.dp)) {
//                            Text(
//                                text = "🩸 Manual Blood Pressure",
//                                style = MaterialTheme.typography.titleSmall,
//                                fontWeight = FontWeight.Bold
//                            )
//                            Row(
//                                modifier = Modifier.fillMaxWidth(),
//                                horizontalArrangement = Arrangement.spacedBy(8.dp)
//                            ) {
//                                OutlinedButton(
//                                    onClick = onSyncManualBloodPressure,
//                                    modifier = Modifier.weight(1f)
//                                ) {
//                                    Text("Sync BP Data")
//                                }
//                                Button(
//                                    onClick = onConfirmBloodPressureSync,
//                                    modifier = Modifier.weight(1f)
//                                ) {
//                                    Text("Confirm & Delete")
//                                }
//                            }
//                        }
//                    }
//
//                    // Additional Sync Functions
//                    var sleepOffset by remember { mutableStateOf("0") }
//                    var sedentaryOffset by remember { mutableStateOf("0") }
//                    var stepDetailOffset by remember { mutableStateOf("0") }
//                    var tempDays by remember { mutableStateOf("0") }
//
//                    Card {
//                        Column(modifier = Modifier.padding(16.dp)) {
//                            Text(
//                                text = "🌙 Advanced Sleep Sync",
//                                style = MaterialTheme.typography.titleSmall,
//                                fontWeight = FontWeight.Bold
//                            )
//                            Row(
//                                modifier = Modifier.fillMaxWidth(),
//                                verticalAlignment = Alignment.CenterVertically
//                            ) {
//                                Text("Offset:", modifier = Modifier.width(50.dp))
//                                OutlinedTextField(
//                                    value = sleepOffset,
//                                    onValueChange = { sleepOffset = it },
//                                    modifier = Modifier
//                                        .weight(1f)
//                                        .padding(horizontal = 4.dp),
//                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
//                                    singleLine = true
//                                )
//                                var includeLunch by remember { mutableStateOf(false) }
//                                Checkbox(
//                                    checked = includeLunch,
//                                    onCheckedChange = { includeLunch = it }
//                                )
//                                Text("Lunch", fontSize = 12.sp)
//                                Button(
//                                    onClick = {
//                                        val offset = sleepOffset.toIntOrNull() ?: 0
//                                        onSyncNewSleepData(offset, includeLunch)
//                                    }
//                                ) {
//                                    Text("Sync")
//                                }
//                            }
//                        }
//                    }
//
//                    Row(
//                        modifier = Modifier.fillMaxWidth(),
//                        horizontalArrangement = Arrangement.spacedBy(8.dp)
//                    ) {
//                        Card(modifier = Modifier.weight(1f)) {
//                            Column(modifier = Modifier.padding(12.dp)) {
//                                Text(
//                                    text = "💺 Sedentary",
//                                    style = MaterialTheme.typography.titleSmall,
//                                    fontWeight = FontWeight.Bold
//                                )
//                                Row(
//                                    verticalAlignment = Alignment.CenterVertically,
//                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
//                                ) {
//                                    OutlinedTextField(
//                                        value = sedentaryOffset,
//                                        onValueChange = { sedentaryOffset = it },
//                                        modifier = Modifier.weight(1f),
//                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
//                                        singleLine = true,
//                                        placeholder = { Text("0") }
//                                    )
//                                    Button(
//                                        onClick = {
//                                            val offset = sedentaryOffset.toIntOrNull() ?: 0
//                                            onSyncSedentaryData(offset)
//                                        }
//                                    ) {
//                                        Text("Sync")
//                                    }
//                                }
//                            }
//                        }
//
//                        Card(modifier = Modifier.weight(1f)) {
//                            Column(modifier = Modifier.padding(12.dp)) {
//                                Text(
//                                    text = "🔍 Step Details",
//                                    style = MaterialTheme.typography.titleSmall,
//                                    fontWeight = FontWeight.Bold
//                                )
//                                Row(
//                                    verticalAlignment = Alignment.CenterVertically,
//                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
//                                ) {
//                                    OutlinedTextField(
//                                        value = stepDetailOffset,
//                                        onValueChange = { stepDetailOffset = it },
//                                        modifier = Modifier.weight(1f),
//                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
//                                        singleLine = true,
//                                        placeholder = { Text("0") }
//                                    )
//                                    Button(
//                                        onClick = {
//                                            val offset = stepDetailOffset.toIntOrNull() ?: 0
//                                            onSyncDetailStepData(offset)
//                                        }
//                                    ) {
//                                        Text("Sync")
//                                    }
//                                }
//                            }
//                        }
//                    }
//
//                    // Temperature Functions
//                    Card {
//                        Column(modifier = Modifier.padding(16.dp)) {
//                            Text(
//                                text = "🌡️ Temperature Functions",
//                                style = MaterialTheme.typography.titleSmall,
//                                fontWeight = FontWeight.Bold
//                            )
//                            Row(
//                                modifier = Modifier.fillMaxWidth(),
//                                horizontalArrangement = Arrangement.spacedBy(8.dp)
//                            ) {
//                                OutlinedButton(
//                                    onClick = onInitTemperatureCallback,
//                                    modifier = Modifier.weight(1f)
//                                ) {
//                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
//                                        Text("🔧", fontSize = 16.sp)
//                                        Text("Init Callback", fontSize = 10.sp)
//                                    }
//                                }
//
//                                OutlinedTextField(
//                                    value = tempDays,
//                                    onValueChange = { tempDays = it },
//                                    modifier = Modifier.width(80.dp),
//                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
//                                    singleLine = true,
//                                    placeholder = { Text("Days") }
//                                )
//
//                                Button(
//                                    onClick = {
//                                        val days = tempDays.toIntOrNull() ?: 0
//                                        onSyncManualTemperature(days)
//                                    }
//                                ) {
//                                    Text("Manual Temp")
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//
//            // Raw Data Measurement Section - NEW
//            ExpandableFunctionSection(
//                title = "Raw Data Measurement",
//                icon = "📊",
//                isExpanded = expandedSections.contains("rawdata"),
//                onToggle = { toggleSection("rawdata") }
//            ) {
//                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
//                    var hrDuration by remember { mutableStateOf("30") }
//                    var spo2Duration by remember { mutableStateOf("30") }
//
//                    // Heart Rate Raw Data
//                    Card {
//                        Column(modifier = Modifier.padding(16.dp)) {
//                            Text(
//                                text = "💓 Heart Rate Raw Data",
//                                style = MaterialTheme.typography.titleSmall,
//                                fontWeight = FontWeight.Bold
//                            )
//                            Text(
//                                text = "Get PPG, accelerometer and RRI data",
//                                style = MaterialTheme.typography.bodySmall,
//                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
//                            )
//                            Row(
//                                modifier = Modifier.fillMaxWidth(),
//                                verticalAlignment = Alignment.CenterVertically
//                            ) {
//                                Text("Duration (s):", modifier = Modifier.width(80.dp))
//                                OutlinedTextField(
//                                    value = hrDuration,
//                                    onValueChange = { hrDuration = it },
//                                    modifier = Modifier
//                                        .weight(1f)
//                                        .padding(horizontal = 8.dp),
//                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
//                                    singleLine = true
//                                )
//                                Button(
//                                    onClick = {
//                                        val duration = hrDuration.toIntOrNull() ?: 30
//                                        onMeasureHeartRateRaw(duration)
//                                    }
//                                ) {
//                                    Text("Start")
//                                }
//                            }
//                        }
//                    }
//
//                    // Blood Oxygen Raw Data
//                    Card {
//                        Column(modifier = Modifier.padding(16.dp)) {
//                            Text(
//                                text = "🫁 Blood Oxygen Raw Data",
//                                style = MaterialTheme.typography.titleSmall,
//                                fontWeight = FontWeight.Bold
//                            )
//                            Text(
//                                text = "Get SpO2 and PPG sensor data",
//                                style = MaterialTheme.typography.bodySmall,
//                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
//                            )
//                            Row(
//                                modifier = Modifier.fillMaxWidth(),
//                                verticalAlignment = Alignment.CenterVertically
//                            ) {
//                                Text("Duration (s):", modifier = Modifier.width(80.dp))
//                                OutlinedTextField(
//                                    value = spo2Duration,
//                                    onValueChange = { spo2Duration = it },
//                                    modifier = Modifier
//                                        .weight(1f)
//                                        .padding(horizontal = 8.dp),
//                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
//                                    singleLine = true
//                                )
//                                Button(
//                                    onClick = {
//                                        val duration = spo2Duration.toIntOrNull() ?: 30
//                                        onMeasureBloodOxygenRaw(duration)
//                                    }
//                                ) {
//                                    Text("Start")
//                                }
//                            }
//                        }
//                    }
//
//                    // Blood Pressure Calculation
//                    var heartRateInput by remember { mutableStateOf("70") }
//                    var ageInput by remember { mutableStateOf("30") }
//
//                    Card {
//                        Column(modifier = Modifier.padding(16.dp)) {
//                            Text(
//                                text = "🩸 Calculate Blood Pressure",
//                                style = MaterialTheme.typography.titleSmall,
//                                fontWeight = FontWeight.Bold
//                            )
//                            Text(
//                                text = "Estimate BP from heart rate and age",
//                                style = MaterialTheme.typography.bodySmall,
//                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
//                            )
//
//                            Row(
//                                modifier = Modifier.fillMaxWidth(),
//                                verticalAlignment = Alignment.CenterVertically
//                            ) {
//                                Text("HR:", modifier = Modifier.width(40.dp))
//                                OutlinedTextField(
//                                    value = heartRateInput,
//                                    onValueChange = { heartRateInput = it },
//                                    modifier = Modifier
//                                        .weight(1f)
//                                        .padding(horizontal = 4.dp),
//                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
//                                    singleLine = true
//                                )
//                                Text("Age:", modifier = Modifier.width(40.dp))
//                                OutlinedTextField(
//                                    value = ageInput,
//                                    onValueChange = { ageInput = it },
//                                    modifier = Modifier
//                                        .weight(1f)
//                                        .padding(horizontal = 4.dp),
//                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
//                                    singleLine = true
//                                )
//                                Button(
//                                    onClick = {
//                                        val hr = heartRateInput.toIntOrNull() ?: 70
//                                        val age = ageInput.toIntOrNull() ?: 30
//                                        onCalculateBloodPressure(hr, age)
//                                    }
//                                ) {
//                                    Text("Calc")
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//
//            // Stop Measurements Section - NEW
//            // Advanced Health Monitoring Section
//            ExpandableFunctionSection(
//                title = "Advanced Health Monitoring",
//                icon = "🫀",
//                isExpanded = expandedSections.contains("health"),
//                onToggle = { toggleSection("health") }
//            ) {
//                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
//                    // Blood Pressure Section
//                    if (deviceCapabilities.supportsBloodPressure) {
//                        HealthMonitoringCard(
//                            title = "Blood Pressure",
//                            icon = "🩸",
//                            currentValue = if (healthData.systolic > 0)
//                                "${healthData.systolic}/${healthData.diastolic} mmHg" else "--",
//                            isEnabled = healthSettings.bloodPressureEnabled,
//                            isReading = isReadingBloodPressure,
//                            onToggle = onToggleBloodPressure,
//                            onMeasureOnce = onMeasureBloodPressureOnce,
//                            onReadSettings = onReadBloodPressure
//                        )
//                    }
//
//                    // HRV Section
//                    if (deviceCapabilities.supportsHrv) {
//                        HealthMonitoringCard(
//                            title = "Heart Rate Variability",
//                            icon = "💓",
//                            currentValue = if (healthData.hrvValue > 0)
//                                "${healthData.hrvValue} ms" else "--",
//                            isEnabled = healthSettings.hrvEnabled,
//                            isReading = isReadingHrv,
//                            onToggle = onToggleHrv,
//                            onMeasureOnce = onMeasureHrvOnce,
//                            onReadSettings = onReadHrv
//                        )
//                    }
//
//                    // Blood Oxygen Section
//                    HealthMonitoringCard(
//                        title = "Blood Oxygen (SpO2)",
//                        icon = "🫁",
//                        currentValue = if (healthData.spo2 > 0)
//                            "${healthData.spo2}%" else "--",
//                        isEnabled = healthSettings.bloodOxygenEnabled,
//                        isReading = isReadingBloodOxygen,
//                        onToggle = onToggleBloodOxygen,
//                        onMeasureOnce = onMeasureBloodOxygenOnce,
//                        onReadSettings = onReadBloodOxygen
//                    )
//
//                    // Temperature Section
//                    if (deviceCapabilities.supportsTemperature) {
//                        HealthMonitoringCard(
//                            title = "Body Temperature",
//                            icon = "🌡️",
//                            currentValue = if (healthData.temperature > 0)
//                                String.format("%.1f°C", healthData.temperature) else "--",
//                            isEnabled = healthSettings.temperatureEnabled,
//                            isReading = isReadingTemperature,
//                            onToggle = onToggleTemperature,
//                            onMeasureOnce = onMeasureTemperatureOnce,
//                            onReadSettings = { /* No read function for temperature */ }
//                        )
//                    }
//
//                    // Pressure/Stress Section
//                    HealthMonitoringCard(
//                        title = "Stress Level",
//                        icon = "😰",
//                        currentValue = if (healthData.pressure > 0)
//                            "${healthData.pressure}" else "--",
//                        isEnabled = healthSettings.pressureEnabled,
//                        isReading = isReadingPressure,
//                        onToggle = onTogglePressure,
//                        onMeasureOnce = onMeasurePressureOnce,
//                        onReadSettings = onReadPressure
//                    )
//
//                    // One-Key Comprehensive Measurement
//                    if (deviceCapabilities.supportsOneKeyCheck) {
//                        ComprehensiveMeasurementButton(
//                            isReading = isReadingComprehensive,
//                            onMeasure = onPerformOneKeyMeasurement
//                        )
//                    }
//                }
//            }
//
//            // Sports Goals Section
//            ExpandableFunctionSection(
//                title = "Sports & Goals",
//                icon = "🏃",
//                isExpanded = expandedSections.contains("sports"),
//                onToggle = { toggleSection("sports") }
//            ) {
//                SportsGoalsSection(onSetSportsGoals = onSetSportsGoals)
//            }
//
//            // Exercise Control Section
//            ExpandableFunctionSection(
//                title = "Exercise Control",
//                icon = "🏃",
//                isExpanded = expandedSections.contains("exercise"),
//                onToggle = { toggleSection("exercise") }
//            ) {
//                ExerciseControlSection(
//                    onStartExercise = onStartExercise,
//                    onPauseExercise = onPauseExercise,
//                    onResumeExercise = onResumeExercise,
//                    onEndExercise = onEndExercise
//                )
//            }
//
//            // Device Control Section
//            ExpandableFunctionSection(
//                title = "Device Control",
//                icon = "⚙️",
//                isExpanded = expandedSections.contains("device"),
//                onToggle = { toggleSection("device") }
//            ) {
//                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
//                    // Find Device
//                    DeviceControlButton(
//                        title = "Find Device",
//                        subtitle = "Make your device vibrate and beep",
//                        icon = "🔍",
//                        onClick = onFindDevice
//                    )
//
//                    // Wearing Calibration
//                    Row(
//                        modifier = Modifier.fillMaxWidth(),
//                        horizontalArrangement = Arrangement.spacedBy(8.dp)
//                    ) {
//                        DeviceControlButton(
//                            title = "Start Calibration",
//                            subtitle = "Calibrate wearing detection",
//                            icon = "🎯",
//                            onClick = onStartCalibration,
//                            modifier = Modifier.weight(1f)
//                        )
//                        DeviceControlButton(
//                            title = "Stop Calibration",
//                            subtitle = "End calibration process",
//                            icon = "⏹️",
//                            onClick = onStopCalibration,
//                            modifier = Modifier.weight(1f)
//                        )
//                    }
//
//                    // Factory Reset
//                    DeviceControlButton(
//                        title = "Factory Reset",
//                        subtitle = "Reset device to factory settings",
//                        icon = "🔄",
//                        onClick = onFactoryReset,
//                        isDestructive = true
//                    )
//                }
//            }
//
//            // Camera Control Section
//            ExpandableFunctionSection(
//                title = "Camera Control",
//                icon = "📷",
//                isExpanded = expandedSections.contains("camera"),
//                onToggle = { toggleSection("camera") }
//            ) {
//                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
//                    DeviceControlButton(
//                        title = "Enter Camera Mode",
//                        subtitle = "Activate camera remote control",
//                        icon = "📸",
//                        onClick = onEnterCameraMode
//                    )
//
//                    Row(
//                        modifier = Modifier.fillMaxWidth(),
//                        horizontalArrangement = Arrangement.spacedBy(8.dp)
//                    ) {
//                        DeviceControlButton(
//                            title = "Keep Screen On",
//                            subtitle = "Prevent screen timeout",
//                            icon = "💡",
//                            onClick = onKeepCameraScreenOn,
//                            modifier = Modifier.weight(1f)
//                        )
//                        DeviceControlButton(
//                            title = "Exit Camera",
//                            subtitle = "Leave camera mode",
//                            icon = "❌",
//                            onClick = onExitCameraMode,
//                            modifier = Modifier.weight(1f)
//                        )
//                    }
//                }
//            }
//
//            // Message & Notifications Section
//            ExpandableFunctionSection(
//                title = "Messages & Notifications",
//                icon = "📱",
//                isExpanded = expandedSections.contains("messages"),
//                onToggle = { toggleSection("messages") }
//            ) {
//                MessageControlSection(
//                    onEnableMessagePush = onEnableMessagePush,
//                    onPushMessage = onPushMessage
//                )
//            }
//
//            // Touch & Gesture Settings Section
//            ExpandableFunctionSection(
//                title = "Touch & Gesture Control",
//                icon = "👆",
//                isExpanded = expandedSections.contains("touch"),
//                onToggle = { toggleSection("touch") }
//            ) {
//                TouchGestureSection(
//                    onReadTouchSettings = onReadTouchSettings,
//                    onWriteTouchSettings = onWriteTouchSettings
//                )
//            }
//
//            // Connection & Debug Section
//            ExpandableFunctionSection(
//                title = "Connection & Debug",
//                icon = "🔧",
//                isExpanded = expandedSections.contains("debug"),
//                onToggle = { toggleSection("debug") }
//            ) {
//                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
//                    DeviceControlButton(
//                        title = "Reinitialize SDK",
//                        subtitle = "Reset SDK connection",
//                        icon = "🔄",
//                        onClick = onReinitializeSDK
//                    )
//
//                    DeviceControlButton(
//                        title = "Request Permissions",
//                        subtitle = "Check and request BLE permissions",
//                        icon = "🔐",
//                        onClick = onRequestPermissions
//                    )
//
//                    Row(
//                        modifier = Modifier.fillMaxWidth(),
//                        horizontalArrangement = Arrangement.spacedBy(8.dp)
//                    ) {
//                        DeviceControlButton(
//                            title = "Test BLE Scan",
//                            subtitle = "Test BLE scanning",
//                            icon = "🔍",
//                            onClick = onTestBLEScan,
//                            modifier = Modifier.weight(1f)
//                        )
//                        DeviceControlButton(
//                            title = "Disconnect",
//                            subtitle = "Disconnect current device",
//                            icon = "📴",
//                            onClick = onDisconnectDevice,
//                            modifier = Modifier.weight(1f),
//                            isDestructive = true
//                        )
//                    }
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun HealthMonitoringCard(
//    title: String,
//    icon: String,
//    currentValue: String,
//    isEnabled: Boolean,
//    isReading: Boolean,
//    onToggle: (Boolean) -> Unit,
//    onMeasureOnce: () -> Unit,
//    onReadSettings: () -> Unit
//) {
//    Card(
//        modifier = Modifier.fillMaxWidth(),
//        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
//        colors = CardDefaults.cardColors(
//            containerColor = if (isReading)
//                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
//            else MaterialTheme.colorScheme.surface
//        )
//    ) {
//        Column(
//            modifier = Modifier.padding(16.dp),
//            verticalArrangement = Arrangement.spacedBy(12.dp)
//        ) {
//            // Header with icon and title
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.SpaceBetween,
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Row(
//                    verticalAlignment = Alignment.CenterVertically,
//                    horizontalArrangement = Arrangement.spacedBy(8.dp)
//                ) {
//                    Text(
//                        text = icon,
//                        fontSize = 20.sp
//                    )
//                    Text(
//                        text = title,
//                        style = MaterialTheme.typography.titleMedium,
//                        fontWeight = FontWeight.Medium
//                    )
//                }
//
//                Switch(
//                    checked = isEnabled,
//                    onCheckedChange = onToggle,
//                    enabled = !isReading
//                )
//            }
//
//            // Current Value Display
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.SpaceBetween,
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Text(
//                    text = "Current: $currentValue",
//                    style = MaterialTheme.typography.bodyMedium,
//                    color = MaterialTheme.colorScheme.onSurfaceVariant
//                )
//
//                if (isReading) {
//                    Row(
//                        verticalAlignment = Alignment.CenterVertically,
//                        horizontalArrangement = Arrangement.spacedBy(8.dp)
//                    ) {
//                        CircularProgressIndicator(
//                            modifier = Modifier.size(16.dp),
//                            strokeWidth = 2.dp,
//                            color = MaterialTheme.colorScheme.primary
//                        )
//                        Text(
//                            text = "Measuring...",
//                            style = MaterialTheme.typography.bodySmall,
//                            color = MaterialTheme.colorScheme.primary
//                        )
//                    }
//                }
//            }
//
//            // Action Buttons
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.spacedBy(8.dp)
//            ) {
//                Button(
//                    onClick = onMeasureOnce,
//                    enabled = !isReading,
//                    modifier = Modifier.weight(1f),
//                    colors = ButtonDefaults.buttonColors(
//                        containerColor = MaterialTheme.colorScheme.primary
//                    )
//                ) {
//                    if (isReading) {
//                        CircularProgressIndicator(
//                            modifier = Modifier.size(14.dp),
//                            strokeWidth = 2.dp,
//                            color = MaterialTheme.colorScheme.onPrimary
//                        )
//                    } else {
//                        Text("📊 Measure Once")
//                    }
//                }
//
//                OutlinedButton(
//                    onClick = onReadSettings,
//                    enabled = !isReading,
//                    modifier = Modifier.weight(1f)
//                ) {
//                    Text("⚙️ Settings")
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun ComprehensiveMeasurementButton(
//    isReading: Boolean,
//    onMeasure: () -> Unit
//) {
//    Card(
//        modifier = Modifier.fillMaxWidth(),
//        colors = CardDefaults.cardColors(
//            containerColor = if (isReading)
//                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
//            else MaterialTheme.colorScheme.secondaryContainer
//        ),
//        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
//    ) {
//        Button(
//            onClick = onMeasure,
//            enabled = !isReading,
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(16.dp)
//                .height(64.dp),
//            colors = ButtonDefaults.buttonColors(
//                containerColor = MaterialTheme.colorScheme.secondary,
//                contentColor = MaterialTheme.colorScheme.onSecondary
//            )
//        ) {
//            if (isReading) {
//                Column(
//                    horizontalAlignment = Alignment.CenterHorizontally,
//                    verticalArrangement = Arrangement.Center
//                ) {
//                    CircularProgressIndicator(
//                        modifier = Modifier.size(24.dp),
//                        strokeWidth = 3.dp,
//                        color = MaterialTheme.colorScheme.onSecondary
//                    )
//                    Spacer(modifier = Modifier.height(8.dp))
//                    Text(
//                        text = "Comprehensive Health Check Running...",
//                        style = MaterialTheme.typography.labelMedium,
//                        textAlign = TextAlign.Center
//                    )
//                }
//            } else {
//                Row(
//                    horizontalArrangement = Arrangement.Center,
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    Text(
//                        text = "🔬",
//                        fontSize = 24.sp
//                    )
//                    Spacer(modifier = Modifier.width(12.dp))
//                    Text(
//                        text = "Start Comprehensive Health Check",
//                        style = MaterialTheme.typography.titleMedium,
//                        fontWeight = FontWeight.Bold
//                    )
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun ExpandableFunctionSection(
//    title: String,
//    icon: String,
//    isExpanded: Boolean,
//    onToggle: () -> Unit,
//    content: @Composable () -> Unit
//) {
//    Card(
//        modifier = Modifier.fillMaxWidth(),
//        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
//    ) {
//        Column {
//            // Header
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .clickable { onToggle() }
//                    .padding(16.dp),
//                horizontalArrangement = Arrangement.SpaceBetween,
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Row(
//                    verticalAlignment = Alignment.CenterVertically,
//                    horizontalArrangement = Arrangement.spacedBy(12.dp)
//                ) {
//                    Text(
//                        text = icon,
//                        fontSize = 24.sp
//                    )
//                    Text(
//                        text = title,
//                        style = MaterialTheme.typography.titleMedium,
//                        fontWeight = FontWeight.Bold
//                    )
//                }
//                Icon(
//                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
//                    contentDescription = if (isExpanded) "Collapse" else "Expand",
//                    tint = MaterialTheme.colorScheme.primary
//                )
//            }
//
//            // Content
//            AnimatedVisibility(
//                visible = isExpanded,
//                enter = expandVertically() + fadeIn(),
//                exit = shrinkVertically() + fadeOut()
//            ) {
//                Column(
//                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
//                ) {
//                    content()
//                }
//            }
//        }
//    }
//}
//
//
//@Composable
//fun DeviceControlButton(
//    title: String,
//    subtitle: String,
//    icon: String,
//    onClick: () -> Unit,
//    modifier: Modifier = Modifier,
//    isDestructive: Boolean = false
//) {
//    Card(
//        modifier = modifier
//            .clickable { onClick() },
//        colors = CardDefaults.cardColors(
//            containerColor = if (isDestructive)
//                MaterialTheme.colorScheme.errorContainer
//            else
//                MaterialTheme.colorScheme.surface
//        )
//    ) {
//        Column(
//            modifier = Modifier.padding(16.dp),
//            verticalArrangement = Arrangement.spacedBy(4.dp)
//        ) {
//            Row(
//                verticalAlignment = Alignment.CenterVertically,
//                horizontalArrangement = Arrangement.spacedBy(8.dp)
//            ) {
//                Text(text = icon, fontSize = 18.sp)
//                Text(
//                    text = title,
//                    style = MaterialTheme.typography.titleSmall,
//                    fontWeight = FontWeight.Bold,
//                    color = if (isDestructive)
//                        MaterialTheme.colorScheme.onErrorContainer
//                    else
//                        MaterialTheme.colorScheme.onSurface
//                )
//            }
//            Text(
//                text = subtitle,
//                style = MaterialTheme.typography.bodySmall,
//                color = if (isDestructive)
//                    MaterialTheme.colorScheme.onErrorContainer
//                else
//                    MaterialTheme.colorScheme.onSurfaceVariant
//            )
//        }
//    }
//}
//
//@Composable
//fun SportsGoalsSection(
//    onSetSportsGoals: (Int, Int, Int, Int, Int) -> Unit
//) {
//    var stepGoal by remember { mutableStateOf("10000") }
//    var calorieGoal by remember { mutableStateOf("500") }
//    var distanceGoal by remember { mutableStateOf("5000") }
//    var sportMinutes by remember { mutableStateOf("30") }
//    var sleepMinutes by remember { mutableStateOf("480") }
//
//    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
//        Text(
//            text = "Set your daily health goals",
//            style = MaterialTheme.typography.bodyMedium,
//            color = MaterialTheme.colorScheme.onSurfaceVariant
//        )
//
//        // Goals input fields
//        OutlinedTextField(
//            value = stepGoal,
//            onValueChange = { stepGoal = it },
//            label = { Text("Step Goal") },
//            suffix = { Text("steps") },
//            modifier = Modifier.fillMaxWidth(),
//            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
//        )
//
//        OutlinedTextField(
//            value = calorieGoal,
//            onValueChange = { calorieGoal = it },
//            label = { Text("Calorie Goal") },
//            suffix = { Text("kcal") },
//            modifier = Modifier.fillMaxWidth(),
//            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
//        )
//
//        OutlinedTextField(
//            value = distanceGoal,
//            onValueChange = { distanceGoal = it },
//            label = { Text("Distance Goal") },
//            suffix = { Text("meters") },
//            modifier = Modifier.fillMaxWidth(),
//            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
//        )
//
//        OutlinedTextField(
//            value = sportMinutes,
//            onValueChange = { sportMinutes = it },
//            label = { Text("Exercise Time Goal") },
//            suffix = { Text("minutes") },
//            modifier = Modifier.fillMaxWidth(),
//            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
//        )
//
//        OutlinedTextField(
//            value = sleepMinutes,
//            onValueChange = { sleepMinutes = it },
//            label = { Text("Sleep Goal") },
//            suffix = { Text("minutes") },
//            modifier = Modifier.fillMaxWidth(),
//            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
//        )
//
//        Button(
//            onClick = {
//                try {
//                    onSetSportsGoals(
//                        stepGoal.toIntOrNull() ?: 10000,
//                        calorieGoal.toIntOrNull() ?: 500,
//                        distanceGoal.toIntOrNull() ?: 5000,
//                        sportMinutes.toIntOrNull() ?: 30,
//                        sleepMinutes.toIntOrNull() ?: 480
//                    )
//                } catch (e: Exception) {
//                    // Handle invalid input
//                }
//            },
//            modifier = Modifier.fillMaxWidth()
//        ) {
//            Text("Set Goals")
//        }
//    }
//}
//
//@Composable
//fun MessageControlSection(
//    onEnableMessagePush: () -> Unit,
//    onPushMessage: (Int, String) -> Unit
//) {
//    var messageText by remember { mutableStateOf("") }
//    var messageType by remember { mutableStateOf("1") }
//
//    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
//
//        DeviceControlButton(
//            title = "Enable Message Push",
//            subtitle = "Enable notifications on device",
//            icon = "📬",
//            onClick = onEnableMessagePush
//        )
//
//        OutlinedTextField(
//            value = messageText,
//            onValueChange = { messageText = it },
//            label = { Text("Message Content") },
//            modifier = Modifier.fillMaxWidth(),
//            maxLines = 3
//        )
//
//        OutlinedTextField(
//            value = messageType,
//            onValueChange = { messageType = it },
//            label = { Text("Message Type (1-10)") },
//            modifier = Modifier.fillMaxWidth(),
//            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
//        )
//
//        Button(
//            onClick = {
//                if (messageText.isNotEmpty()) {
//                    onPushMessage(
//                        messageType.toIntOrNull() ?: 1,
//                        messageText
//                    )
//                }
//            },
//            modifier = Modifier.fillMaxWidth(),
//            enabled = messageText.isNotEmpty()
//        ) {
//            Text("Send Message")
//        }
//    }
//}
//
//@Composable
//fun TouchGestureSection(
//    onReadTouchSettings: (Boolean) -> Unit,
//    onWriteTouchSettings: (Int, Boolean, Int) -> Unit
//) {
//    var appType by remember { mutableStateOf("1") }
//    var isTouch by remember { mutableStateOf(true) }
//    var strength by remember { mutableStateOf("5") }
//
//    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
//
//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.spacedBy(8.dp)
//        ) {
//            DeviceControlButton(
//                title = "Read Touch Settings",
//                subtitle = "Get current touch config",
//                icon = "👆",
//                onClick = { onReadTouchSettings(true) },
//                modifier = Modifier.weight(1f)
//            )
//            DeviceControlButton(
//                title = "Read Gesture Settings",
//                subtitle = "Get current gesture config",
//                icon = "✋",
//                onClick = { onReadTouchSettings(false) },
//                modifier = Modifier.weight(1f)
//            )
//        }
//
//        OutlinedTextField(
//            value = appType,
//            onValueChange = { appType = it },
//            label = { Text("App Type") },
//            modifier = Modifier.fillMaxWidth(),
//            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
//        )
//
//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.SpaceBetween,
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Text("Touch Control")
//            Switch(
//                checked = isTouch,
//                onCheckedChange = { isTouch = it }
//            )
//        }
//
//        OutlinedTextField(
//            value = strength,
//            onValueChange = { strength = it },
//            label = { Text("Strength (1-10)") },
//            modifier = Modifier.fillMaxWidth(),
//            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
//        )
//
//        Button(
//            onClick = {
//                onWriteTouchSettings(
//                    appType.toIntOrNull() ?: 1,
//                    isTouch,
//                    strength.toIntOrNull() ?: 5
//                )
//            },
//            modifier = Modifier.fillMaxWidth()
//        ) {
//            Text("Update ${if (isTouch) "Touch" else "Gesture"} Settings")
//        }
//    }
//}
//
//// Create this composable function
//@Composable
//fun ExerciseControlSection(
//    onStartExercise: (Int) -> Unit,
//    onPauseExercise: (Int) -> Unit,
//    onResumeExercise: (Int) -> Unit,
//    onEndExercise: (Int) -> Unit
//) {
//    var selectedSportType by remember { mutableStateOf(4) } // Default to walking
//    var isExerciseActive by remember { mutableStateOf(false) }
//    var isPaused by remember { mutableStateOf(false) }
//    var showSportSelector by remember { mutableStateOf(false) }
//
//    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
//
//        // Sport Type Selector
//        Card(
//            modifier = Modifier.fillMaxWidth(),
//            colors = CardDefaults.cardColors(
//                containerColor = MaterialTheme.colorScheme.surfaceVariant
//            )
//        ) {
//            Column(
//                modifier = Modifier.padding(16.dp),
//                verticalArrangement = Arrangement.spacedBy(8.dp)
//            ) {
//                Text(
//                    text = "Selected Sport",
//                    style = MaterialTheme.typography.labelMedium,
//                    color = MaterialTheme.colorScheme.onSurfaceVariant
//                )
//
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.SpaceBetween,
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    Text(
//                        text = getSportTypeName(selectedSportType),
//                        style = MaterialTheme.typography.titleMedium,
//                        fontWeight = FontWeight.Medium
//                    )
//
//                    Button(
//                        onClick = { showSportSelector = true },
//                        enabled = !isExerciseActive
//                    ) {
//                        Text("Change")
//                    }
//                }
//            }
//        }
//
//        // Exercise Status Display
//        if (isExerciseActive) {
//            Card(
//                modifier = Modifier.fillMaxWidth(),
//                colors = CardDefaults.cardColors(
//                    containerColor = if (isPaused)
//                        MaterialTheme.colorScheme.errorContainer
//                    else
//                        MaterialTheme.colorScheme.primaryContainer
//                )
//            ) {
//                Row(
//                    modifier = Modifier.padding(16.dp),
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    Text(
//                        text = if (isPaused) "⏸️ Exercise Paused" else "▶️ Exercise Active",
//                        style = MaterialTheme.typography.titleMedium,
//                        fontWeight = FontWeight.Medium,
//                        color = if (isPaused)
//                            MaterialTheme.colorScheme.onErrorContainer
//                        else
//                            MaterialTheme.colorScheme.onPrimaryContainer
//                    )
//
//                    if (!isPaused) {
//                        Spacer(modifier = Modifier.width(8.dp))
//                        CircularProgressIndicator(
//                            modifier = Modifier.size(20.dp),
//                            strokeWidth = 2.dp,
//                            color = MaterialTheme.colorScheme.onPrimaryContainer
//                        )
//                    }
//                }
//            }
//        }
//
//        // Exercise Control Buttons
//        if (!isExerciseActive) {
//            // Start Exercise Button
//            Button(
//                onClick = {
//                    onStartExercise(selectedSportType)
//                    isExerciseActive = true
//                    isPaused = false
//                },
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(56.dp),
//                colors = ButtonDefaults.buttonColors(
//                    containerColor = MaterialTheme.colorScheme.primary
//                )
//            ) {
//                Text(
//                    text = "▶️ Start Exercise",
//                    style = MaterialTheme.typography.titleMedium,
//                    fontWeight = FontWeight.Medium
//                )
//            }
//        } else {
//            // Exercise Active Controls
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.spacedBy(8.dp)
//            ) {
//                // Pause/Resume Button
//                Button(
//                    onClick = {
//                        if (isPaused) {
//                            onResumeExercise(selectedSportType)
//                            isPaused = false
//                        } else {
//                            onPauseExercise(selectedSportType)
//                            isPaused = true
//                        }
//                    },
//                    modifier = Modifier
//                        .weight(1f)
//                        .height(48.dp),
//                    colors = ButtonDefaults.buttonColors(
//                        containerColor = if (isPaused)
//                            MaterialTheme.colorScheme.primary
//                        else
//                            MaterialTheme.colorScheme.secondary
//                    )
//                ) {
//                    Text(
//                        text = if (isPaused) "▶️ Resume" else "⏸️ Pause",
//                        style = MaterialTheme.typography.titleSmall
//                    )
//                }
//
//                // End Exercise Button
//                Button(
//                    onClick = {
//                        onEndExercise(selectedSportType)
//                        isExerciseActive = false
//                        isPaused = false
//                    },
//                    modifier = Modifier
//                        .weight(1f)
//                        .height(48.dp),
//                    colors = ButtonDefaults.buttonColors(
//                        containerColor = MaterialTheme.colorScheme.error
//                    )
//                ) {
//                    Text(
//                        text = "⏹️ End",
//                        style = MaterialTheme.typography.titleSmall,
//                        color = MaterialTheme.colorScheme.onError
//                    )
//                }
//            }
//        }
//
//        // Quick Start Buttons for Common Sports
//        if (!isExerciseActive) {
//            Text(
//                text = "Quick Start",
//                style = MaterialTheme.typography.labelMedium,
//                color = MaterialTheme.colorScheme.onSurfaceVariant
//            )
//
//            LazyRow(
//                horizontalArrangement = Arrangement.spacedBy(8.dp)
//            ) {
//                items(getCommonSports()) { sport ->
//                    FilterChip(
//                        onClick = {
//                            selectedSportType = sport.type
//                            onStartExercise(sport.type)
//                            isExerciseActive = true
//                            isPaused = false
//                        },
//                        label = {
//                            Text(
//                                text = "${sport.icon} ${sport.name}",
//                                style = MaterialTheme.typography.labelMedium
//                            )
//                        },
//                        selected = selectedSportType == sport.type
//                    )
//                }
//            }
//        }
//    }
//
//    // Sport Type Selection Dialog
//    if (showSportSelector) {
//        SportTypeSelectionDialog(
//            currentSelection = selectedSportType,
//            onSportSelected = { sportType ->
//                selectedSportType = sportType
//                showSportSelector = false
//            },
//            onDismiss = { showSportSelector = false }
//        )
//    }
//}
//
//@Composable
//fun SportTypeSelectionDialog(
//    currentSelection: Int,
//    onSportSelected: (Int) -> Unit,
//    onDismiss: () -> Unit
//) {
//    AlertDialog(
//        onDismissRequest = onDismiss,
//        title = { Text("Select Sport Type") },
//        text = {
//            LazyColumn(
//                verticalArrangement = Arrangement.spacedBy(8.dp)
//            ) {
//                items(getAllSportTypes()) { sport ->
//                    Card(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .clickable { onSportSelected(sport.type) },
//                        colors = CardDefaults.cardColors(
//                            containerColor = if (currentSelection == sport.type)
//                                MaterialTheme.colorScheme.primaryContainer
//                            else
//                                MaterialTheme.colorScheme.surface
//                        )
//                    ) {
//                        Row(
//                            modifier = Modifier.padding(16.dp),
//                            verticalAlignment = Alignment.CenterVertically
//                        ) {
//                            Text(
//                                text = sport.icon,
//                                style = MaterialTheme.typography.headlineSmall
//                            )
//                            Spacer(modifier = Modifier.width(12.dp))
//                            Column {
//                                Text(
//                                    text = sport.name,
//                                    style = MaterialTheme.typography.titleMedium,
//                                    fontWeight = FontWeight.Medium
//                                )
//                                Text(
//                                    text = "Type ${sport.type}",
//                                    style = MaterialTheme.typography.labelSmall,
//                                    color = MaterialTheme.colorScheme.onSurfaceVariant
//                                )
//                            }
//                        }
//                    }
//                }
//            }
//        },
//        confirmButton = {
//            TextButton(onClick = onDismiss) {
//                Text("Cancel")
//            }
//        }
//    )
//}
//
//// Data classes and helper functions
//data class SportType(
//    val type: Int,
//    val name: String,
//    val icon: String
//)
//
//fun getSportTypeName(sportType: Int): String {
//    return when (sportType) {
//        4 -> "Walking"
//        5 -> "Jump Rope"
//        6 -> "Swimming"
//        7 -> "Running"
//        8 -> "Hiking"
//        9 -> "Cycling"
//        10 -> "Other Sports"
//        20 -> "Climbing"
//        21 -> "Badminton"
//        22 -> "Yoga"
//        23 -> "Aerobics"
//        24 -> "Spinning"
//        25 -> "Kayaking"
//        26 -> "Elliptical"
//        27 -> "Rowing"
//        28 -> "Table Tennis"
//        29 -> "Tennis"
//        30 -> "Golf"
//        31 -> "Basketball"
//        32 -> "Football"
//        33 -> "Volleyball"
//        34 -> "Rock Climbing"
//        35 -> "Dance"
//        36 -> "Roller Skating"
//        60 -> "Outdoor Hiking"
//        else -> "Unknown Sport"
//    }
//}
//
//fun getCommonSports(): List<SportType> {
//    return listOf(
//        SportType(4, "Walking", "🚶"),
//        SportType(7, "Running", "🏃"),
//        SportType(9, "Cycling", "🚴"),
//        SportType(5, "Jump Rope", "🪢"),
//        SportType(6, "Swimming", "🏊"),
//        SportType(22, "Yoga", "🧘")
//    )
//}
//
//fun getAllSportTypes(): List<SportType> {
//    return listOf(
//        SportType(4, "Walking", "🚶"),
//        SportType(5, "Jump Rope", "🪢"),
//        SportType(6, "Swimming", "🏊"),
//        SportType(7, "Running", "🏃"),
//        SportType(8, "Hiking", "🥾"),
//        SportType(9, "Cycling", "🚴"),
//        SportType(10, "Other Sports", "🏃"),
//        SportType(20, "Climbing", "🧗"),
//        SportType(21, "Badminton", "🏸"),
//        SportType(22, "Yoga", "🧘"),
//        SportType(23, "Aerobics", "🤸"),
//        SportType(24, "Spinning", "🚴"),
//        SportType(25, "Kayaking", "🛶"),
//        SportType(26, "Elliptical", "🏃"),
//        SportType(27, "Rowing", "🚣"),
//        SportType(28, "Table Tennis", "🏓"),
//        SportType(29, "Tennis", "🎾"),
//        SportType(30, "Golf", "⛳"),
//        SportType(31, "Basketball", "🏀"),
//        SportType(32, "Football", "⚽"),
//        SportType(33, "Volleyball", "🏐"),
//        SportType(34, "Rock Climbing", "🧗"),
//        SportType(35, "Dance", "💃"),
//        SportType(36, "Roller Skating", "⛸️"),
//        SportType(60, "Outdoor Hiking", "🏔️")
//    )
//}