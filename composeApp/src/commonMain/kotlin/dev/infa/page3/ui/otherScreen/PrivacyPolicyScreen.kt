package dev.infa.page3.ui.otherScreen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*

@Composable
fun PrivacyPolicyScreen(
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            CommonTopAppBar(title = "Policy")
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF8F8F8))
        ) {

            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }

                Text(
                    text = "Privacy Policy",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Divider()

            // Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {

                PolicyParagraph(
                    "This website is owned and operated by Page3Life. " +
                            "We value your privacy and are committed to protecting your personal information. " +
                            "This Privacy Policy explains how we collect, use, and safeguard your information " +
                            "when you visit https://www.page3life.com or make a purchase from us."
                )

                PrivacySection(
                    title = "1. Information We Collect",
                    points = listOf(
                        "Personal Information: Name, email address, shipping address, phone number, and payment details.",
                        "Non-Personal Information: Browser type, IP address, device details, and browsing behavior."
                    )
                )

                PrivacySection(
                    title = "2. How We Use Your Information",
                    points = listOf(
                        "To process, confirm, and deliver your orders.",
                        "To communicate order updates, offers, and customer support.",
                        "To improve website performance and user experience."
                    )
                )

                PrivacySection(
                    title = "3. Information Sharing",
                    points = listOf(
                        "We may share information with trusted third-party service providers.",
                        "Information may be disclosed if required by law or to protect our rights."
                    )
                )

                PrivacySection(
                    title = "4. Data Security",
                    points = listOf(
                        "We use reasonable security measures to protect your data.",
                        "No online system is 100% secure, and absolute protection cannot be guaranteed."
                    )
                )

                PrivacySection(
                    title = "5. Your Rights",
                    points = listOf(
                        "You can access and update your personal information anytime.",
                        "You can opt out of promotional emails using the unsubscribe link."
                    )
                )

                PrivacySection(
                    title = "6. Policy Updates",
                    points = listOf(
                        "This Privacy Policy may be updated from time to time.",
                        "Changes will be reflected on this page."
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "© Page3Life",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
@Composable
fun PrivacySection(
    title: String,
    points: List<String>
) {
    Spacer(modifier = Modifier.height(20.dp))

    Text(
        text = title,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = Color.Black
    )

    Spacer(modifier = Modifier.height(8.dp))

    points.forEach {
        Text(
            text = "• $it",
            fontSize = 14.sp,
            color = Color(0xFF444444),
            modifier = Modifier.padding(bottom = 6.dp)
        )
    }
}

@Composable
fun PolicyParagraph(text: String) {
    Text(
        text = text,
        fontSize = 14.sp,
        color = Color(0xFF444444),
        lineHeight = 20.sp
    )
}
