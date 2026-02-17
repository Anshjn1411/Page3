package dev.infa.page3.ui.otherScreen


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

@Composable
fun TermsAndConditionsScreen() {
    Scaffold(
        topBar = {
            CommonTopAppBar(title = "Term and Condition")
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
                text = "Terms & Conditions",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            Text(
                text = "Effective Date: March 2026",
                fontSize = 13.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            SectionText(
                "Welcome to Page3Life. By using our app or website, you agree to comply with the following terms and conditions."
            )

            TermsSection(
                "1. Order Confirmation & Modifications",
                listOf(
                    "Orders cannot be modified or cancelled once dispatched.",
                    "We reserve the right to cancel orders due to errors or suspected fraud."
                )
            )

            TermsSection(
                "2. Pricing & Payment",
                listOf(
                    "All prices are in INR (₹).",
                    "Payment must be completed before shipment.",
                    "Failed payments result in automatic cancellation."
                )
            )

            TermsSection(
                "3. Shipping, Delivery & Risk",
                listOf(
                    "Delivery timelines are estimates only.",
                    "Risk transfers once the order is handed to the courier."
                )
            )

            TermsSection(
                "4. Product Information & Availability",
                listOf(
                    "Product visuals may vary across devices.",
                    "We may change pricing or availability without notice."
                )
            )

            TermsSection(
                "5. Returns & Refunds",
                listOf(
                    "Refer to our Returns & Refund Policy for details.",
                    "Discounted items may have different eligibility."
                )
            )

            TermsSection(
                "6. Intellectual Property",
                listOf(
                    "All content belongs to Page3Life.",
                    "Unauthorized reproduction is prohibited."
                )
            )

            TermsSection(
                "7. Governing Law",
                listOf(
                    "These terms are governed by Indian law.",
                    "Disputes are subject to Indian courts."
                )
            )

            SectionText(
                "This application is owned and operated by Page3Life, an Indian ecommerce brand specializing in gym and activewear clothing."
            )
            TermsSection(
                "Business Information",
                listOf(
                    "Registered Business Name: Page3Life",
                    "Business Type: Ecommerce – Gym & Activewear Clothing",
                    "Operating Country: India",
                    "Customer Support Email: support@page3life.com"
                )
            )



            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "For questions, contact us at support@page3life.com",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
fun TermsSection(title: String, points: List<String>) {
    Column(modifier = Modifier.padding(bottom = 16.dp)) {

        Text(
            text = title,
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        points.forEach {
            Text(
                text = "• $it",
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = Color(0xFF444444),
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
    }
}

@Composable
fun SectionText(text: String) {
    Text(
        text = text,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        color = Color(0xFF444444),
        modifier = Modifier.padding(bottom = 16.dp)
    )
}
