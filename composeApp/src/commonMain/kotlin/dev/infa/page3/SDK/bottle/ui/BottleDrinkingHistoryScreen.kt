package dev.infa.page3.SDK.bottle.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.Navigator
import dev.infa.page3.SDK.bottle.data.DrinkingRecord
import dev.infa.page3.SDK.bottle.viewmodel.BottleViewModel

private val Blue1   = Color(0xFF4FC3F7)
private val Blue2   = Color(0xFF0288D1)
private val Green1  = Color(0xFF66BB6A)
private val Warm1   = Color(0xFFFF8A65)
private val BgDeep  = Color(0xFF0A0E1A)
private val BgCard  = Color(0xFF111827)
private val BgCard2 = Color(0xFF1A2235)

// ─── Nav bottom padding constant (same as HomeScreen) ────────────────────────────
private val NavBottomPadding = 130.dp

@Composable
fun BottleDrinkingHistoryScreen(
    viewModel: BottleViewModel,
    navigator: Navigator
) {
    val records     by viewModel.drinkingRecords.collectAsState()
    var selectedDay by remember { mutableStateOf(0) }

    LaunchedEffect(selectedDay) { viewModel.requestDrinkingRecordData(selectedDay) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDeep)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start  = 20.dp,
                end    = 20.dp,
                top    = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 16.dp,
                bottom = NavBottomPadding
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Title
            item {
                Text("Drinking History", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black, letterSpacing = 0.3.sp)
                Spacer(Modifier.height(2.dp))
                Text("Track your daily hydration", color = Color.White.copy(alpha = 0.4f), fontSize = 12.sp)
            }

            // Day selector
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(8) { day ->
                        val isSelected = day == selectedDay
                        val label = when (day) {
                            0 -> "Today"
                            1 -> "Yesterday"
                            else -> "${day}d ago"
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    if (isSelected) Brush.horizontalGradient(listOf(Blue1, Blue2))
                                    else Brush.horizontalGradient(listOf(BgCard2, BgCard2))
                                )
                                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { selectedDay = day }
                                .padding(horizontal = 18.dp, vertical = 9.dp)
                        ) {
                            Text(
                                label,
                                color      = if (isSelected) Color.White else Color.White.copy(alpha = 0.45f),
                                fontSize   = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            // Stats
            item {
                val totalMl = records.sumOf { it.waterIntakeMl }
                val avgTemp = if (records.isNotEmpty()) records.map { it.temperatureC }.average().toInt() else 0
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    HistoryStatCard(Modifier.weight(1f), "💧", "Total",   "${totalMl}mL",   Blue1)
                    HistoryStatCard(Modifier.weight(1f), "📋", "Records", "${records.size}", Green1)
                    HistoryStatCard(Modifier.weight(1f), "🌡️", "Avg Temp","${avgTemp}°C",   Warm1)
                }
            }

            // Bar chart
            if (records.isNotEmpty()) {
                item { IntakeMiniChart(records = records) }
            }

            // Records header
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Records", color = Color.White.copy(alpha = 0.9f), fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    if (records.isNotEmpty()) Text("${records.size} entries", color = Blue1.copy(alpha = 0.7f), fontSize = 12.sp)
                }
            }

            if (records.isEmpty()) {
                item { HistoryEmptyState() }
            } else {
                itemsIndexed(records) { index, record ->
                    HistoryRecordCard(index = index + 1, record = record)
                }
                item {
                    Spacer(Modifier.height(4.dp))
                    Button(
                        onClick = { viewModel.confirmAcquisition(selectedDay) },
                        shape   = RoundedCornerShape(24.dp),
                        colors  = ButtonDefaults.buttonColors(containerColor = Green1.copy(alpha = 0.14f), contentColor = Green1),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.CheckCircle, null, tint = Green1, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Confirm Records Received", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

// ─── Stat Card ────────────────────────────────────────────────────────────────────

@Composable
private fun HistoryStatCard(modifier: Modifier, emoji: String, label: String, value: String, accent: Color) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.linearGradient(listOf(accent.copy(alpha = 0.10f), BgCard)))
            .padding(14.dp)
    ) {
        Text(emoji, fontSize = 16.sp)
        Spacer(Modifier.height(8.dp))
        Text(label, color = Color.White.copy(alpha = 0.45f), fontSize = 10.sp)
        Spacer(Modifier.height(2.dp))
        Text(value, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
    }
}

// ─── Mini Bar Chart ────────────────────────────────────────────────────────────────

@Composable
private fun IntakeMiniChart(records: List<DrinkingRecord>) {
    val maxMl = records.maxOf { it.waterIntakeMl }.coerceAtLeast(1)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(BgCard2)
            .padding(18.dp)
    ) {
        Text("Intake Overview", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(14.dp))
        Row(
            modifier = Modifier.fillMaxWidth().height(70.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment     = Alignment.Bottom
        ) {
            records.take(24).forEachIndexed { i, record ->
                val ratio  = record.waterIntakeMl.toFloat() / maxMl
                val animH  = remember { Animatable(0f) }
                LaunchedEffect(record.waterIntakeMl) {
                    animH.animateTo(ratio, tween(600 + i * 30, easing = FastOutSlowInEasing))
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(animH.value.coerceAtLeast(0.04f))
                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                        .background(Brush.verticalGradient(listOf(Blue1, Blue2)))
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text("Each bar = one drink event", color = Color.White.copy(alpha = 0.3f), fontSize = 10.sp)
    }
}

// ─── Record Card ─────────────────────────────────────────────────────────────────

@Composable
private fun HistoryRecordCard(index: Int, record: DrinkingRecord) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(BgCard2)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(38.dp).clip(CircleShape).background(Blue1.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) { Text("$index", color = Blue1, fontSize = 14.sp, fontWeight = FontWeight.Bold) }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(record.formattedTime(), color = Color.White.copy(alpha = 0.45f), fontSize = 11.sp)
            Spacer(Modifier.height(3.dp))
            Text("💧 ${record.waterIntakeMl}mL  •  🌡️ ${record.temperatureC}°C",
                color = Color.White.copy(alpha = 0.88f), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        }
        Box(Modifier.width(4.dp).height(36.dp).clip(RoundedCornerShape(2.dp)).background(Brush.verticalGradient(listOf(Blue1, Blue2))))
    }
}

// ─── Empty State ──────────────────────────────────────────────────────────────────

@Composable
private fun HistoryEmptyState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(BgCard2)
            .padding(48.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("💧", fontSize = 44.sp)
            Spacer(Modifier.height(14.dp))
            Text("No records for this day", color = Color.White.copy(alpha = 0.45f), fontSize = 14.sp, textAlign = TextAlign.Center)
        }
    }
}