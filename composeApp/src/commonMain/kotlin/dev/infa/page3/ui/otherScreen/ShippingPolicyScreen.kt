package dev.infa.page3.ui.otherScreen

import androidx.compose.material3.ExperimentalMaterial3Api

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.*
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShippingPolicyScreen() {

    Scaffold(
        topBar = {
            CommonTopAppBar(title = "Shipping Policy")
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8F8F8))
                .padding(innerPadding) // 🔥 handles top app bar spacing
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {

            Text(
                text = "Shipping Policy",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "Home » Shipping Policy",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            PolicyText(
                "All orders are processed within 2–3 business days after payment confirmation."
            )

            PolicyText(
                "Once dispatched, delivery typically takes 5–7 business days depending on the delivery location."
            )

            PolicyText(
                "The total estimated delivery time is 7–10 business days."
            )

            PolicyText(
                "Shipping charges, if applicable, are shown clearly at checkout before payment."
            )

        }
    }
}

@Composable
fun PolicyText(text: String) {
    Text(
        text = text,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        modifier = Modifier.padding(bottom = 12.dp),
        color = Color.DarkGray
    )
}
