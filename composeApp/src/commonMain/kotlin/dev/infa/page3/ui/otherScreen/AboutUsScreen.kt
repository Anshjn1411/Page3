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
fun AboutUsScreen() {

    val sections = listOf(

        AboutSection(
            title = "Who We Are",
            content = listOf(
                "Page3 is a modern lifestyle and fashion brand built for individuals who value style, quality, and innovation.",
                "We focus on delivering trendy, reliable, and carefully curated products that fit seamlessly into everyday life."
            )
        ),

        AboutSection(
            title = "What We Do",
            content = listOf(
                "We offer a wide range of fashion, accessories, and gadgets designed to match modern lifestyles.",
                "Our platform brings together quality products, seamless shopping, and customer-first service."
            )
        ),

        AboutSection(
            title = "Our Vision",
            content = listOf(
                "To become a trusted lifestyle brand that empowers people to express themselves confidently.",
                "We aim to blend affordability, quality, and style into every product we deliver."
            )
        ),

        AboutSection(
            title = "Our Mission",
            content = listOf(
                "To continuously innovate and improve our product offerings.",
                "To ensure a smooth, transparent, and enjoyable shopping experience for every customer.",
                "To build long-term trust through quality and customer satisfaction."
            )
        ),

        AboutSection(
            title = "Why Choose Page3",
            content = listOf(
                "Carefully selected high-quality products",
                "Customer-centric approach",
                "Secure payments and reliable delivery",
                "Dedicated support team"
            )
        )
    )

    Scaffold(
        topBar = {
            CommonTopAppBar(title = "About Screen")
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8F8F8))
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
        ) {

            // Page Title
            Text(
                text = "About Us",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Short Brand Intro
            Text(
                text = "Welcome to Page3 — where style meets simplicity.",
                fontSize = 15.sp,
                color = Color(0xFF555555),
                modifier = Modifier.padding(bottom = 20.dp)
            )

            sections.forEach { section ->
                AboutSectionCard(section)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Thank you for being a part of Page3.",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Gray,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
fun AboutSectionCard(section: AboutSection) {
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

            section.content.forEach { text ->
                Row(
                    modifier = Modifier.padding(bottom = 6.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text("• ", fontSize = 14.sp)
                    Text(
                        text = text,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = Color(0xFF444444)
                    )
                }
            }
        }
    }
}

data class AboutSection(
    val title: String,
    val content: List<String>
)
