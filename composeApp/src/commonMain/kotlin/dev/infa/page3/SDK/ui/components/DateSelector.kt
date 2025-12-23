package dev.infa.page3.SDK.ui.components


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.infa.page3.SDK.ui.utils.DateInfo
import dev.infa.page3.SDK.ui.utils.DateUtils

@Composable
fun DateSelector(
    selectedDate: DateInfo,
    onDateChange: (DateInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    val isToday = remember(selectedDate) {
        DateUtils.isToday(selectedDate)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.5f))
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = {
            val newDate = DateUtils.addDays(selectedDate, -1)
            onDateChange(newDate)
        }) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "Previous Day",
                tint = Color.White
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CalendarToday,
                contentDescription = null,
                tint = Color(0xFF00FF88),
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = DateUtils.formatDateForDisplay(selectedDate),
                color = Color.White,
                fontSize = 16.sp
            )
        }

        IconButton(
            onClick = {
                if (!isToday) {
                    val newDate = DateUtils.addDays(selectedDate, 1)
                    onDateChange(newDate)
                }
            },
            enabled = !isToday
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Next Day",
                tint = if (isToday) Color.White.copy(alpha = 0.3f) else Color.White
            )
        }
    }
}

