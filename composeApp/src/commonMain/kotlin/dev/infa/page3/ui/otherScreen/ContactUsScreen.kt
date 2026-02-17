package dev.infa.page3.ui.otherScreen

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ContactCard(
    title: String,
    value: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            Text(
                text = value,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = Color(0xFF444444)
            )
        }
    }
}

@Composable
fun ContactUsScreen() {
    Scaffold(
        topBar = {
            CommonTopAppBar(title = "Contact Us")
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8F8F8))
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
        ) {

            Text(
                text = "Contact Us",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "We’re here to help! If you have any questions, concerns, or feedback, feel free to reach out to us.",
                fontSize = 15.sp,
                color = Color(0xFF555555),
                modifier = Modifier.padding(bottom = 20.dp)
            )

            ContactCard(
                title = "Email Support",
                value = "support@page3life.com"
            )

            ContactCard(
                title = "Website",
                value = "www.page3life.com"
            )
            ContactCard(
                title = "Contact Number",
                value = "+91 90290 88420"
            )

            ContactCard(
                title = "Working Hours",
                value = "Monday – Saturday\n10:00 AM – 6:00 PM (IST)"
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Our support team aims to respond within 24–48 hours.",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}
