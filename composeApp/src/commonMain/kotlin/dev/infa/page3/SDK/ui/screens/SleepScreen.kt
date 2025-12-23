package dev.infa.page3.SDK.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import dev.infa.page3.SDK.ui.components.DateSelector
import dev.infa.page3.SDK.ui.utils.DateUtils
import kotlin.random.Random

@Composable
fun SleepScreen(
    onBack: () -> Unit
) {
    var selectedDate by remember { mutableStateOf(DateUtils.getCurrentDate()) }
    val selectedDateOffset = remember(selectedDate) {
        DateUtils.getDayOffsetFromToday(selectedDate)
    }
    val sleepStages = listOf(
        SleepStage("Deep", 2.3f, 28, Color(0xFF00FF88)),
        SleepStage("Light", 3.7f, 45, Color(0xFF3B82F6)),
        SleepStage("REM", 1.6f, 19, Color(0xFF6366F1)),
        SleepStage("Awake", 0.6f, 8, Color(0xFF6B7280)),
    )

    val totalSleep = sleepStages.sumOf { it.duration.toDouble() }.toFloat()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ✅ Header
            ScreenHeader(title = "Sleep", onBack = onBack)

            // ✅ Date Selector (Your Existing One)
            DateSelector(
                selectedDate = selectedDate,
                onDateChange ={ newDate->
                    selectedDate = newDate

                }
            )

            // ✅ Scroll Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // ✅ Sleep Score Card
                CardBlock {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Sleep Performance", color = Color.Gray, fontSize = 12.sp)

                        Text(
                            "85%",
                            fontSize = 60.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00FF88)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(Color(0x2200FF88), RoundedCornerShape(50))
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(Color(0xFF00FF88), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Optimal Recovery", color = Color(0xFF00FF88), fontSize = 12.sp)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            SmallStatCard("Total Sleep", "${totalSleep}h")
                            SmallStatCard("Efficiency", "92%")
                        }
                    }
                }

                // ✅ Sleep Stages Timeline
                CardBlock {
                    Text("Sleep Stages", color = Color.White, fontSize = 16.sp)

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(18.dp)
                            .clip(RoundedCornerShape(20))
                    ) {
                        sleepStages.forEach {
                            Box(
                                modifier = Modifier
                                    .weight(it.percentage.toFloat())
                                    .fillMaxHeight()
                                    .background(it.color)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    sleepStages.forEach { stage ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0x22000000), RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("${stage.stage} Sleep", color = Color.White)
                                Text("${stage.percentage}% of total", fontSize = 12.sp, color = Color.Gray)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("${stage.duration}h", color = Color.White)
                                Text("${(stage.duration * 60).toInt()}m", fontSize = 12.sp, color = Color.Gray)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                // ✅ Sleep Vitals
                CardBlock {
                    Text("Sleep Vitals", color = Color.White, fontSize = 16.sp)

                    Spacer(modifier = Modifier.height(12.dp))

                    GridRow(
                        "Bedtime" to "11:24 PM",
                        "Wake Time" to "7:38 AM",
                        "Sleep Latency" to "8 min",
                        "Respiratory Rate" to "15.2"
                    )
                }

                // ✅ Heart Rate Overnight
                CardBlock {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Heart Rate Overnight", color = Color.White)
                        Text("52 avg", color = Color(0xFF3B82F6))
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(90.dp),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        repeat(30) {
                            val height = Random.nextInt(20, 100)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(height.dp)
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(Color(0xFF3B82F6), Color(0x883B82F6))
                                        ),
                                        RoundedCornerShape(4.dp)
                                    )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("11 PM", fontSize = 11.sp, color = Color.Gray)
                        Text("3 AM", fontSize = 11.sp, color = Color.Gray)
                        Text("7 AM", fontSize = 11.sp, color = Color.Gray)
                    }
                }

                // ✅ 7-Day Average
                CardBlock(borderColor = Color(0x5500FF88)) {
                    Text("7-Day Average", color = Color.White, fontSize = 16.sp)

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Sleep Duration", color = Color.Gray, fontSize = 12.sp)
                            Text("7h 52m", color = Color.White, fontSize = 22.sp)
                        }
                        Column {
                            Text("Sleep Score", color = Color.Gray, fontSize = 12.sp)
                            Text("83%", color = Color.White, fontSize = 22.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(Color(0xFF00FF88), CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sleep consistency improving", color = Color.Gray, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
data class SleepStage(
    val stage: String,
    val duration: Float,
    val percentage: Int,
    val color: Color
)

@Composable
fun CardBlock(
    borderColor: Color = Color(0x22FFFFFF),
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0x11000000), RoundedCornerShape(20.dp))
            .border(1.dp, borderColor, RoundedCornerShape(20.dp))
            .padding(16.dp),
        content = content
    )
}

@Composable
fun SmallStatCard(title: String, value: String) {
    Column(
        modifier = Modifier
            .background(Color(0x22000000), RoundedCornerShape(14.dp))
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, fontSize = 18.sp, color = Color.White)
        Text(title, fontSize = 12.sp, color = Color.Gray)
    }
}
@Composable
fun GridRow(vararg items: Pair<String, String>) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {

        var i = 0
        while (i < items.size) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color(0x22000000), RoundedCornerShape(14.dp))
                        .padding(14.dp)
                ) {
                    Text(items[i].second, color = Color.White)
                    Text(items[i].first, fontSize = 12.sp, color = Color.Gray)
                }

                if (i + 1 < items.size) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .background(Color(0x22000000), RoundedCornerShape(14.dp))
                            .padding(14.dp)
                    ) {
                        Text(items[i + 1].second, color = Color.White)
                        Text(items[i + 1].first, fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }

            i += 2
        }
    }
}
