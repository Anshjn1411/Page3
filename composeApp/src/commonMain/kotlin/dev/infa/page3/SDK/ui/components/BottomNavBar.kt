package dev.infa.page3.SDK.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import dev.infa.page3.SDK.ui.navigation.*
import dev.infa.page3.SDK.ui.theme.AppColors
import dev.infa.page3.SDK.ui.theme.AppDimensions
import dev.infa.page3.SDK.ui.theme.AppShapes

// ─── Scroll-aware bottom nav visibility for SDK dashboard ──────────────────────────

class SdkNavVisibilityState {
    var isVisible by mutableStateOf(true)
}

val LocalSdkNavVisibility = compositionLocalOf { SdkNavVisibilityState() }

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

    val navVisibility = LocalSdkNavVisibility.current

    val navOffsetY by animateDpAsState(
        targetValue = if (navVisibility.isVisible) 0.dp else 120.dp,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 300f),
        label = "sdk_nav_offset"
    )
    val navAlpha by animateFloatAsState(
        targetValue = if (navVisibility.isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing),
        label = "sdk_nav_alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = AppDimensions.Spacing.XXXL, vertical = AppDimensions.Spacing.Medium)
            .graphicsLayer(alpha = navAlpha)
            .shadow(
                elevation = 28.dp,
                shape = AppShapes.BottomNavIndicator,
                ambientColor = AppColors.GradientStart.copy(alpha = 0.35f),
                spotColor = AppColors.GradientEnd.copy(alpha = 0.45f)
            )
            .clip(AppShapes.BottomNavIndicator)
            .graphicsLayer { translationY = navOffsetY.toPx() },
        contentAlignment = Alignment.Center
    ) {
        Surface(
            color = Color.Transparent
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(68.dp)
                    .clip(AppShapes.BottomNavIndicator)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                AppColors.BackgroundPrimary.copy(alpha = 0.96f),
                                AppColors.OverlayDark.copy(alpha = 0.98f)
                            )
                        )
                    )
            ) {
                // Glass highlight on top
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .align(Alignment.TopCenter)
                        .padding(horizontal = AppDimensions.Spacing.Large)
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color.Transparent,
                                    Color.White.copy(alpha = 0.28f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = AppDimensions.Spacing.Large),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    navItems.forEach { item ->
                        val isActive = currentTab == item.tab

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onTabSelected(item.tab) }
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                                tint = if (isActive) AppColors.Primary else AppColors.TextTertiary,
                                modifier = Modifier.size(AppDimensions.Component.BottomNavIconSize)
                            )
                            if (isActive) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .width(AppDimensions.Component.BottomNavIndicatorWidth)
                                        .height(3.dp)
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
                        }
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
