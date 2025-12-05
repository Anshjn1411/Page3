package dev.infa.page3.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.infa.page3.models.DeviceCapabilities
import dev.infa.page3.viewmodels.ConnectionViewModel

@Composable
fun DeviceCapabilityScreen(
    connectionViewModel : ConnectionViewModel
) {
    val uiState by connectionViewModel.uiState.collectAsState()
    val isConnected = uiState.isConnected
    val deviceName = uiState.connectedDevice?.deviceName ?: ""
    val deviceAddress = uiState.connectedDevice?.deviceAddress ?: ""
    val batteryLevel = uiState.batteryLevel ?: 0
    val capabilities = uiState.deviceCapabilities ?: DeviceCapabilities()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(20.dp)
    ) {

        // ✅ TITLE
        Text(
            text = "$deviceName Capabilities",
            color = Color(0xFF00FF88),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(20.dp))

        // ✅ CAPABILITY LIST
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(capabilities.toList()) { item ->
                CapabilityItem(
                    title = item.first,
                    isAvailable = item.second
                )
            }
        }
    }
}

@Composable
fun CapabilityItem(
    title: String,
    isAvailable: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF111111))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(
            text = title,
            color = Color.White,
            fontSize = 16.sp,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = if (isAvailable) "Available" else "Not Available",
            color = if (isAvailable) Color(0xFF00FF88) else Color.Red,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}