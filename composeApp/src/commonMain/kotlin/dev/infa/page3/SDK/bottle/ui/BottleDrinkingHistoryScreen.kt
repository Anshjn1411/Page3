package dev.infa.page3.SDK.bottle.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.Navigator
import dev.infa.page3.SDK.bottle.data.DrinkingRecord
import dev.infa.page3.SDK.bottle.viewmodel.BottleViewModel
import dev.infa.page3.SDK.ui.theme.*

private val BottleBlue = Color(0xFF4FC3F7)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottleDrinkingHistoryScreen(
    viewModel: BottleViewModel,
    navigator: Navigator
) {
    val records by viewModel.drinkingRecords.collectAsState()
    var selectedDay by remember { mutableStateOf(0) }

    // Fetch records when day changes
    LaunchedEffect(selectedDay) {
        viewModel.requestDrinkingRecordData(selectedDay)
    }

    Scaffold(
        containerColor = AppColors.BackgroundPrimary,
        topBar = {
            TopAppBar(
                title = {
                    Text("Drinking History", color = AppColors.TextPrimary, style = AppTypography.HeadingSmall)
                },
                navigationIcon = {
                    IconButton(onClick = { navigator.pop() }) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = AppColors.TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppColors.BackgroundPrimary)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = AppDimensions.ScreenPadding.Horizontal),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Day selector
            item {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Select Day",
                    color = AppColors.TextSecondary,
                    style = AppTypography.LabelLarge
                )
                Spacer(Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(8) { day ->
                        val isSelected = day == selectedDay
                        val label = when (day) {
                            0 -> "Today"
                            1 -> "Yesterday"
                            else -> "$day days ago"
                        }
                        Box(
                            modifier = Modifier
                                .clip(AppShapes.Chip)
                                .background(
                                    if (isSelected) BottleBlue.copy(alpha = 0.2f)
                                    else AppColors.SurfaceVariant
                                )
                                .clickable { selectedDay = day }
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) BottleBlue else AppColors.TextSecondary,
                                style = AppTypography.LabelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        }
                    }
                }
            }

            // Summary
            item {
                Spacer(Modifier.height(4.dp))
                val totalMl = records.sumOf { it.waterIntakeMl }
                val avgTemp = if (records.isNotEmpty())
                    records.map { it.temperatureC }.average().toInt()
                else 0

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SummaryChip(
                        modifier = Modifier.weight(1f),
                        emoji = "💧",
                        label = "Total",
                        value = "${totalMl}mL",
                        color = BottleBlue
                    )
                    SummaryChip(
                        modifier = Modifier.weight(1f),
                        emoji = "📋",
                        label = "Records",
                        value = "${records.size}",
                        color = Color(0xFF66BB6A)
                    )
                    SummaryChip(
                        modifier = Modifier.weight(1f),
                        emoji = "🌡️",
                        label = "Avg Temp",
                        value = "${avgTemp}°C",
                        color = Color(0xFFFF8A65)
                    )
                }
            }

            // Records Header
            item {
                Spacer(Modifier.height(4.dp))
                Text(
                    "Records",
                    color = AppColors.TextPrimary,
                    style = AppTypography.HeadingExtraSmall
                )
            }

            // Records List
            if (records.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(AppShapes.CardMedium)
                            .background(AppColors.SurfaceVariant)
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("💧", fontSize = 40.sp)
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "No records for this day",
                                color = AppColors.TextSecondary,
                                style = AppTypography.BodySmall
                            )
                        }
                    }
                }
            } else {
                itemsIndexed(records) { index, record ->
                    RecordCard(index = index + 1, record = record)
                }
            }

            // Confirm Records button
            if (records.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = { viewModel.confirmAcquisition(selectedDay) },
                        shape = AppShapes.Chip,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF66BB6A).copy(alpha = 0.15f),
                            contentColor = Color(0xFF66BB6A)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF66BB6A))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Confirm Records Received",
                            style = AppTypography.ButtonMedium,
                            color = Color(0xFF66BB6A)
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(32.dp)) }
        }
    }
}

// ─── Summary Chip ───────────────────────────────────────────────────────────────

@Composable
private fun SummaryChip(
    modifier: Modifier,
    emoji: String,
    label: String,
    value: String,
    color: Color
) {
    Box(
        modifier = modifier
            .clip(AppShapes.CardMedium)
            .background(
                Brush.linearGradient(
                    listOf(color.copy(alpha = 0.12f), AppColors.BackgroundCard)
                )
            )
            .padding(14.dp)
    ) {
        Column {
            Text(emoji, fontSize = 16.sp)
            Spacer(Modifier.height(6.dp))
            Text(label, color = AppColors.TextSecondary, style = AppTypography.LabelSmall)
            Spacer(Modifier.height(2.dp))
            Text(value, color = AppColors.TextPrimary, style = AppTypography.ValueSmall)
        }
    }
}

// ─── Record Card ────────────────────────────────────────────────────────────────

@Composable
private fun RecordCard(index: Int, record: DrinkingRecord) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(AppShapes.CardMedium)
            .background(AppColors.SurfaceVariant)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Index badge
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(BottleBlue.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "$index",
                color = BottleBlue,
                style = AppTypography.LabelLarge.copy(fontWeight = FontWeight.Bold)
            )
        }

        Spacer(Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = record.formattedTime(),
                color = AppColors.TextSecondary,
                style = AppTypography.LabelMedium
            )
            Spacer(Modifier.height(4.dp))
            Row {
                Text(
                    text = "💧 ${record.waterIntakeMl}mL",
                    color = AppColors.TextPrimary,
                    style = AppTypography.BodyMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(Modifier.width(16.dp))
                Text(
                    text = "🌡️ ${record.temperatureC}°C",
                    color = Color(0xFFFF8A65),
                    style = AppTypography.BodyMedium
                )
            }
        }
    }
}
