package com.example.paze3

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.paze3.ui.theme.*

data class Product(
    val id: Int,
    val name: String,
    val price: Double,
    val rating: String,
    val bgColor: Color,
    val category: String,
    val description: String,
    val specifications: List<Pair<String, String>>,
    val imageUrl: String,
    val gifUrl: String,
    val videoUrl: String,
    val buyUrl: String,
    var isWishlisted: Boolean = false,
    val showInCarousel: Boolean = true
)

data class NavItem(val label: String, val icon: ImageVector, val activeIcon: ImageVector)

val allProducts = listOf(
    Product(
        1, "Suitcase", 89.00, "4.8", PastelYellow, "Travel",
        "A durable and lightweight suitcase for your travels.",
        listOf("Material" to "Polycarbonate", "Weight" to "3.2kg", "Capacity" to "65L"),
        "https://static.vecteezy.com/system/resources/previews/047/241/779/non_2x/3d-suitcase-isolated-on-transparent-background-free-png.png",
        "https://motion.inventiko.com/paze3/suitcase/assets/suitcase.gif",
        "https://motion.inventiko.com/paze3/suitcase/assets/suitcase.mp4",
        "https://motion.inventiko.com/paze3/suitcase/index-suitcase.html",
        isWishlisted = false,
        showInCarousel = true
    ),
    Product(
        2, "Anxiety Killer", 49.00, "4.6", PastelPeach, "Electronics",
        "Track your fitness and health with this advanced smart band.",
        listOf("Display" to "AMOLED", "Battery" to "14 Days", "Waterproof" to "5 ATM"),
        "https://motion.inventiko.com/paze3/suitcase/assets/anxiety_killer.png",
        "https://motion.inventiko.com/paze3/suitcase/assets/anxiety_killer.gif",
        "https://motion.inventiko.com/paze3/suitcase/assets/anxiety_killer.mp4",
        "https://motion.inventiko.com/paze3/suitcase/index-suitcase.html",
        isWishlisted = false,
        showInCarousel = false
    ),
    Product(
        3, "Smart Bottle", 35.00, "4.7", PastelGreen, "Lifestyle",
        "Self-cleaning water bottle with UV-C LED technology.",
        listOf("Material" to "Stainless Steel", "Capacity" to "500ml", "Battery" to "1 Month"),
        "https://www.navinmart.com/cdn/shop/files/0003_85383606-d8f7-491a-b472-8bc34e1d1d73.png?v=1765346742",
        "https://motion.inventiko.com/paze3/suitcase/assets/bottle.gif",
        "https://motion.inventiko.com/paze3/suitcase/assets/bottle.mp4",
        "https://motion.inventiko.com/paze3/bottle/index-bottle.html",
        isWishlisted = false,
        showInCarousel = false
    ),
    Product(
        4, "Smart Ring", 199.00, "4.9", PastelLavender, "Electronics",
        "The future of wearable technology on your finger.",
        listOf("Material" to "Titanium", "Sensors" to "HRV, SpO2", "Weight" to "4g"),
        "https://merlin-digital.com/cdn/shop/files/smartringnew2.png?v=1711540047&width=1946",
        "https://motion.inventiko.com/paze3/suitcase/assets/ring.gif",
        "https://motion.inventiko.com/paze3/suitcase/assets/ring.mp4",
        "https://motion.inventiko.com/paze3/suitcase/index-suitcase.html",
        isWishlisted = false,
        showInCarousel = false
    ),
    Product(
        5, "Round Headphone", 128.00, "4.8", PastelSalmon, "Electronics",
        "Immersive sound quality with active noise cancellation.",
        listOf("Type" to "Over-ear", "Battery" to "40 Hours", "Bluetooth" to "5.2"),
        "https://motion.inventiko.com/paze3/suitcase/assets/round_headphone.png",
        "https://motion.inventiko.com/paze3/suitcase/assets/round_headphone.gif",
        "https://motion.inventiko.com/paze3/suitcase/assets/headphone.mp4",
        "https://motion.inventiko.com/paze3/headphone/",
        isWishlisted = false,
        showInCarousel = true
    ),
    Product(
        6, "Square Headphone", 128.00, "4.8", PastelSalmon, "Electronics",
        "Immersive sound quality with active noise cancellation.",
        listOf("Type" to "Over-ear", "Battery" to "40 Hours", "Bluetooth" to "5.2"),
        "https://motion.inventiko.com/paze3/suitcase/assets/square_headphone.png",
        "https://motion.inventiko.com/paze3/suitcase/assets/square_headphone.gif",
        "https://motion.inventiko.com/paze3/suitcase/assets/headphone.mp4",
        "https://motion.inventiko.com/paze3/headphone/",
        isWishlisted = false,
        showInCarousel = true
    ),
    Product(
        7, "Earbuds", 79.00, "4.7", PastelBlue, "Electronics",
        "Truly wireless earbuds with crystal clear audio.",
        listOf("Model" to "TWS-X", "Charging" to "USB-C", "Water Resistance" to "IPX4"),
        "https://motion.inventiko.com/paze3/suitcase/assets/earbuds.png",
        "https://motion.inventiko.com/paze3/suitcase/assets/earbuds.gif",
        "https://motion.inventiko.com/paze3/suitcase/assets/earbuds.mp4",
        "https://motion.inventiko.com/paze3/suitcase/index-suitcase.html",
        isWishlisted = false,
        showInCarousel = true
    ),
    Product(
        8, "Measuring Tap", 79.00, "4.7", PastelBlue, "Electronics",
        "Truly wireless earbuds with crystal clear audio.",
        listOf("Model" to "TWS-X", "Charging" to "USB-C", "Water Resistance" to "IPX4"),
        "https://motion.inventiko.com/paze3/suitcase/assets/measuring_tape.png",
        "https://motion.inventiko.com/paze3/suitcase/assets/measuring_tape.gif",
        "https://motion.inventiko.com/paze3/suitcase/assets/measuring_tape.mp4",
        "https://motion.inventiko.com/paze3/suitcase/index-suitcase.html",
        isWishlisted = false,
        showInCarousel = true
    )
)
