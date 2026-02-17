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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
fun FaqScreen() {

    val faqList = listOf(
        FaqItem(
            "What types of products does Page3Life offer?",
            "Page3Life offers premium men’s and women’s gym wear and activewear designed for workouts, training, and fitness lifestyles."
        ),

        FaqItem(
            "How do I choose the right size for gym clothing?",
            "Each product page includes a detailed size chart to help you select the correct fit. If you are between sizes, we recommend choosing the larger size for comfort."
        ),

        FaqItem(
            "What is your return and exchange policy?",
            "We offer returns or exchanges within 7 days of delivery, provided the product is unused, unwashed, and in its original packaging with tags intact."
        ),

        FaqItem(
            "How long does delivery take?",
            "Orders are processed within 2–3 business days and delivered within 5–7 business days after dispatch. The total delivery time is usually 7–10 business days."
        ),

        FaqItem(
            "Do you accept Cash on Delivery (COD)?",
            "Currently, we support prepaid payment methods only. All available payment options are shown at checkout."
        )
    )

    Scaffold(
        topBar = {
            CommonTopAppBar(title = "FAQs")
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8F8F8))
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {

            faqList.forEach { faq ->
                FaqItemView(faq)
            }
        }
    }
}

@Composable
fun FaqItemView(item: FaqItem) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.question,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )

                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier.rotate(if (expanded) 180f else 0f)
                )
            }

            AnimatedVisibility(visible = expanded) {
                Text(
                    text = item.answer,
                    fontSize = 14.sp,
                    color = Color(0xFF555555),
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
        }
    }
}


data class FaqItem(
    val question: String,
    val answer: String
)
