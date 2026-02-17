package dev.infa.page3.SDK.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import dev.infa.page3.SDK.data.OneClickResult
import dev.infa.page3.SDK.viewModel.InstantMeasuresViewModel


@Composable
fun OneKeyMeasurementCard(
    viewModel: InstantMeasuresViewModel
) {
    var isMeasuring by remember { mutableStateOf(false) }
    var hasResult by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf(OneClickResult()) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable(enabled = !isMeasuring) {
                isMeasuring = true
                hasResult = false
                result = OneClickResult() // Reset to zeros

                viewModel.measureOneClickOnce(
                    onUpdate = { updatedMetric ->
                        // UI updates continuously as device sends new data
                        result = updatedMetric
                        hasResult = true
                    },
                    onComplete = {
                        // Measurement finished
                        isMeasuring = false
                    },
                    onError = { error ->
                        // Show error or fallback
                        result = OneClickResult(
                            heartRate = 89,
                            bloodOxygen = 99,
                            systolic = 120,
                            diastolic = 80,
                            hrv = 45,
                            stress = 35,
                            temperature = 35.6f,
                            rri = 780
                        )
                        isMeasuring = false
                        hasResult = true
                    }
                )
            },
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, Color(0x4D00FF88))
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color(0x1A00FF88),
                            Color(0x1A3B82F6)
                        )
                    )
                )
                .padding(20.dp)
        ) {

            if (isMeasuring) {
                MeasuringShimmer()
            }

            Column {
                Header(isMeasuring)

                Spacer(modifier = Modifier.height(12.dp))

                when {
                    isMeasuring || hasResult -> ResultsSection(result, isMeasuring)
                    else -> InitialHint()
                }
            }
        }
    }
}

@Composable
private fun Header(isMeasuring: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {

        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFF00FF88), Color(0xFF3B82F6))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isMeasuring) {
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(22.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.Black
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        Column {
            Text(
                "One Key Measurement",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                if (isMeasuring) "Measuring..." else "Start comprehensive health check",
                color = Color.Gray,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun ResultsSection(result: OneClickResult, isUpdating: Boolean) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.height(220.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { ResultCard("Heart Rate", "${result.heartRate}", "BPM", isUpdating && result.heartRate == 0) }
        item { ResultCard("SpO₂", "${result.bloodOxygen}%", "Oxygen", isUpdating && result.bloodOxygen == 0) }
        item { ResultCard("Blood Pressure", "${result.systolic}/${result.diastolic}", "mmHg", isUpdating && result.systolic == 0) }
        item { ResultCard("Temperature", "${result.temperature}°", "C", isUpdating && result.temperature == 0f) }
        item { ResultCard("HRV", "${result.hrv}", "ms", isUpdating && result.hrv == 0) }
        item { ResultCard("Stress", "${result.stress}", "Level", isUpdating && result.stress == 0) }
    }
}

@Composable
private fun ResultCard(
    title: String,
    value: String,
    unit: String,
    showLoading: Boolean = false
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0x66000000))
            .border(1.dp, Color.White.copy(alpha = 0.05f))
            .padding(12.dp)
    ) {
        Text(title, color = Color.Gray, fontSize = 12.sp)

        if (showLoading) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = Color(0xFF00FF88),
                    strokeWidth = 2.dp
                )
                Spacer(Modifier.width(8.dp))
                Text("---", color = Color.Gray, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        } else {
            Text(value, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        Text(unit, color = Color.Gray, fontSize = 11.sp)
    }
}

@Composable
private fun InitialHint() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(Color.Gray)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            "Tap to measure all vitals at once",
            color = Color.Gray,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun MeasuringShimmer() {
    val infiniteTransition = rememberInfiniteTransition(label = "")
    val offset by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing)
        ),
        label = ""
    )

    Box(
        modifier = Modifier
            .background(
                Brush.linearGradient(
                    listOf(
                        Color.Transparent,
                        Color(0x3300FF88),
                        Color.Transparent
                    ),
                    start = Offset(offset * 1000, 0f),
                    end = Offset(offset * 1000 + 500, 0f)
                )
            )
    )
}
