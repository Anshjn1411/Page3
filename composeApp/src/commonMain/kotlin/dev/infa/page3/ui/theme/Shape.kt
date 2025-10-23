package dev.infa.page3.ui.theme


import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val Page3Shapes = Shapes(
    // Extra small - For small chips, badges
    extraSmall = RoundedCornerShape(4.dp),

    // Small - For small buttons, text fields
    small = RoundedCornerShape(8.dp),

    // Medium - For cards, dialogs, medium buttons
    medium = RoundedCornerShape(12.dp),

    // Large - For bottom sheets, large cards
    large = RoundedCornerShape(16.dp),

    // Extra large - For modals, full-screen sheets
    extraLarge = RoundedCornerShape(24.dp)
)

// Custom shape definitions for specific components
object Page3CustomShapes {
    // Product cards typically have subtle rounding
    val ProductCard = RoundedCornerShape(12.dp)

    // Buttons have moderate rounding for modern look
    val Button = RoundedCornerShape(8.dp)
    val ButtonLarge = RoundedCornerShape(12.dp)

    // Search bar has full rounding
    val SearchBar = RoundedCornerShape(24.dp)

    // Bottom sheet has top corners rounded only
    val BottomSheet = RoundedCornerShape(
        topStart = 24.dp,
        topEnd = 24.dp,
        bottomStart = 0.dp,
        bottomEnd = 0.dp
    )

    // Image containers with subtle rounding
    val ImageContainer = RoundedCornerShape(8.dp)

    // Chips and tags
    val Chip = RoundedCornerShape(20.dp)

    // Dialog boxes
    val Dialog = RoundedCornerShape(16.dp)

    // Text fields
    val TextField = RoundedCornerShape(8.dp)

    // Navigation bar items
    val NavItem = RoundedCornerShape(12.dp)

    // Promotion banners
    val Banner = RoundedCornerShape(12.dp)

    // Size selector buttons (circular/pill-shaped)
    val SizeButton = RoundedCornerShape(8.dp)
}
