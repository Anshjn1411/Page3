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
import dev.infa.page3.SDK.ui.utils.DateUtils


@Composable
fun RefundReturnPolicyScreen() {

    val sections = listOf(

        PolicySection(
            title = "Returns",
            content = listOf(
                "Our policy lasts 7 days. If 7 days have gone by since your purchase, unfortunately we can’t offer you a refund or exchange.",
                "To be eligible for a return, your item must be unused and in the same condition that you received it.",
                "It must also be in the original packaging."
            )
        ),

        PolicySection(
            title = "Non-returnable Items",
            content = listOf(
                "Innerwear, socks, and compression wear once opened",
                "Products damaged due to misuse or improper handling",
                "Items without original tags or packaging",
                "Products purchased during clearance or sale (if mentioned on product page)"
            )
        )
        ,

        PolicySection(
            title = "Refunds",
            content = listOf(
                "Once we receive and inspect the returned product, you will be notified via email or SMS within 48 hours.",
                "If approved, the refund will be initiated within 3 working days.",
                "The refunded amount will reflect in your original payment method within 5–7 working days, depending on your bank."
            )
        )
        ,

        PolicySection(
            title = "Late or Missing Refunds",
            content = listOf(
                "If you haven’t received a refund yet, first check your bank account again.",
                "Then contact your credit card company, it may take some time before your refund is officially posted.",
                "If you’ve done all of this and still have not received your refund, please contact us."
            )
        ),

        PolicySection(
            title = "Exchanges",
            content = listOf(
                "We only replace items if they are defective or damaged.",
                "If you need to exchange it for the same item, contact us with your order details."
            )
        ),

        PolicySection(
            title = "Shipping Returns",
            content = listOf(
                "You will be responsible for paying for your own shipping costs for returning your item.",
                "Shipping costs are non-refundable.",
                "If you receive a refund, the cost of return shipping will be deducted from your refund."
            )
        )
    )
    Scaffold(
        topBar = {
            CommonTopAppBar(title = "Refund Policy")
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
                text = "Refund & Return Policy",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Text(
                text = "Last updated: ${DateUtils.getCurrentDate()}",
                fontSize = 13.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            sections.forEach { section ->
                PolicySectionView(section)
            }
        }
    }
}

@Composable
fun PolicySectionView(section: PolicySection) {
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
                text = section.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            section.content.forEach { point ->
                Row(
                    modifier = Modifier.padding(bottom = 6.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text("• ", fontSize = 14.sp)
                    Text(
                        text = point,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = Color(0xFF444444)
                    )
                }
            }
        }
    }
}


data class PolicySection(
    val title: String,
    val content: List<String>
)
