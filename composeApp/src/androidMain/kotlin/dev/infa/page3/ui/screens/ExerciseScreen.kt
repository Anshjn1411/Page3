package dev.infa.page3.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.with
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import dev.infa.page3.viewmodels.ExerciseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseScreen(
    viewModel: ExerciseViewModel,
    navController: NavController
) {
    val live by viewModel.liveState.collectAsState()
    val recent by viewModel.recent.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Exercise") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (live.isActive || live.countdown != null) {
                LiveExercise(viewModel)
            } else {
                ExerciseList(viewModel)
            }

            if (recent.isNotEmpty() && !(live.isActive || live.countdown != null)) {
                RecentExerciseList(
                    items = recent,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                )
            }
        }
    }
}

@Composable
private fun ExerciseList(viewModel: ExerciseViewModel) {
    val types = viewModel.availableSportTypes
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Choose an exercise", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(12.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(types) { (id, name) ->
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { viewModel.startExercise(id) },
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(name, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        Text("Start", color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

@Composable
private fun LiveExercise(viewModel: ExerciseViewModel) {
    val live by viewModel.liveState.collectAsState()
    var showMinDialog by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = sportName(live.sportType ?: 10), fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(24.dp))

        if (live.countdown != null) {
            Text(
                text = "${live.countdown}",
                fontSize = 64.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        } else {
            Text(text = formatDuration(live.elapsedSec), fontSize = 40.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            MetricsRow(live.heartRate, live.steps, live.distanceMeters, live.calories)

            Spacer(Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (live.isPaused) {
                    Button(onClick = { viewModel.resumeExercise() }) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Resume")
                    }
                } else {
                    Button(onClick = { viewModel.pauseExercise() }) {
                        Icon(Icons.Default.Pause, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Pause")
                    }
                }
                OutlinedButton(onClick = {
                    if (live.elapsedSec < 60) {
                        showMinDialog = true
                    } else {
                        viewModel.endExercise()
                    }
                }) {
                    Icon(Icons.Default.Stop, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("End")
                }
            }
            live.error?.let { err ->
                Spacer(Modifier.height(12.dp))
                Text(text = err, color = MaterialTheme.colorScheme.error)
            }
        }
    }

    if (showMinDialog) {
        AlertDialog(
            onDismissRequest = { showMinDialog = false },
            title = { Text("Keep going") },
            text = { Text("Please exercise at least 1 minute to save your session.") },
            confirmButton = {
                TextButton(onClick = { showMinDialog = false }) { Text("Continue") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showMinDialog = false
                    // End without saving to recent
                    viewModel.endExerciseDontSave()
                }) { Text("Discard") }
            }
        )
    }
}

@Composable
private fun RecentExerciseList(items: List<dev.infa.page3.viewmodels.ExerciseSummary>, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(4.dp)) {
        Column(Modifier.padding(12.dp)) {
            Text("Recent Activity", fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            items.take(5).forEach { s ->
                val pace = if (s.distanceMeters > 0) s.durationSec.toFloat() / (s.distanceMeters / 1000f) else 0f
                val paceStr = if (pace > 0f) {
                    val min = (pace / 60f).toInt(); val sec = (pace % 60f).toInt(); String.format("%d'%02d\"/km", min, sec)
                } else "--"
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(text = sportName(s.sportType), fontWeight = FontWeight.Medium)
                        Text(text = "${formatDuration(s.durationSec)} · ${String.format("%.2f km", s.distanceMeters/1000f)} · $paceStr",
                            style = MaterialTheme.typography.bodySmall)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "Avg ${s.avgHeartRate} bpm", style = MaterialTheme.typography.bodySmall)
                        Text(text = "${s.calories} kcal", style = MaterialTheme.typography.bodySmall)
                    }
                }
                Divider()
            }
        }
    }
}

@Composable
private fun MetricsRow(hr: Int, steps: Int, dist: Int, kcal: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        MetricItem(label = "BPM", value = "$hr")
        MetricItem(label = "Steps", value = "$steps")
        MetricItem(label = "Distance", value = String.format("%.2f km", dist / 1000f))
        MetricItem(label = "Cal", value = "$kcal")
    }
}

@Composable
private fun MetricItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(label, fontSize = 12.sp)
    }
}

@Composable
private fun RecentExerciseSummary(title: String, summary: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(3.dp)) {
        Column(Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            Text(summary)
        }
    }
}

private fun formatDuration(total: Int): String {
    val h = total / 3600
    val m = (total % 3600) / 60
    val s = total % 60
    return if (h > 0) String.format("%d:%02d:%02d", h, m, s) else String.format("%02d:%02d", m, s)
}

private fun sportName(type: Int): String = when (type) {
    4 -> "Walking"
    7 -> "Running"
    8 -> "Hiking"
    9 -> "Cycling"
    else -> "Others"
}


