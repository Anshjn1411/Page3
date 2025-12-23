package dev.infa.page3.ui.orderscreen.componenets

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun OrderStatusChip(status: String) {
    val (backgroundColor, textColor) = when (status.uppercase()) {
        "PENDING" -> Pair(Color(0xFFFFF3CD), Color(0xFF856404))
        "PLACED" -> Pair(Color(0xFFD1ECF1), Color(0xFF0C5460))
        "CONFIRMED" -> Pair(Color(0xFFD4EDDA), Color(0xFF155724))
        "SHIPPED" -> Pair(Color(0xFFCCE5FF), Color(0xFF004085))
        "DELIVERED" -> Pair(Color(0xFFD4EDDA), Color(0xFF155724))
        "CANCELLED" -> Pair(Color(0xFFF8D7DA), Color(0xFF721C24))
        else -> Pair(Color.LightGray, Color.Black)
    }

    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = status,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            fontWeight = FontWeight.Medium
        )
    }
}