package dev.infa.page3.SDK.ui.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import dev.infa.page3.SDK.ui.navigation.*
import dev.infa.page3.SDK.ui.theme.*

@Composable
fun BottomNavBar(
    currentTab: BottomTab,
    onTabSelected: (BottomTab) -> Unit
) {
    val navItems = listOf(
        BottomNavItem(BottomTab.HOME, "Overview", Icons.Outlined.Home),
        BottomNavItem(BottomTab.STRAIN, "Strain", Icons.Outlined.TrendingUp),
        BottomNavItem(BottomTab.RECOVERY, "Recovery", Icons.Outlined.Favorite),
        BottomNavItem(BottomTab.STEP, "Sleep", Icons.Outlined.Watch),
        BottomNavItem(BottomTab.PROFILE, "Profile", Icons.Outlined.Person)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .background(AppColors.BackgroundPrimary)
            .border(AppDimensions.Border.Thin, AppColors.BorderPrimary)
            .navigationBarsPadding()
            .padding(vertical = AppDimensions.Component.BottomNavHeight)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            navItems.forEach { item ->
                val isActive = currentTab == item.tab

                Box(
                    contentAlignment = Alignment.TopCenter,
                    modifier = Modifier
                        .clickable { onTabSelected(item.tab) }
                        .padding(horizontal = AppDimensions.Spacing.Medium)
                ) {
                    // Active Indicator
                    if (isActive) {
                        Box(
                            modifier = Modifier
                                .offset(y = (-6).dp)
                                .height(AppDimensions.Component.BottomNavIndicatorHeight)
                                .width(AppDimensions.Component.BottomNavIndicatorWidth)
                                .clip(AppShapes.BottomNavIndicator)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            AppColors.GradientStart,
                                            AppColors.GradientEnd
                                        )
                                    )
                                )
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(top = AppDimensions.Spacing.Small)
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            tint = if (isActive) AppColors.TextPrimary else AppColors.TextTertiary,
                            modifier = Modifier.size(AppDimensions.Component.BottomNavIconSize)
                        )
                    }
                }
            }
        }
    }
}

enum class BottomTab(val route: Screen) {
    HOME(HomeScreenSDK()),
    STRAIN(ExerciseScreenSDK()),
    RECOVERY(HeartRateScreenSDK()),
    STEP(StepsScreenSDK()),
    PROFILE(ProfileScreenSDK())
}

data class BottomNavItem(
    val tab: BottomTab,
    val label: String,
    val icon: ImageVector
)
