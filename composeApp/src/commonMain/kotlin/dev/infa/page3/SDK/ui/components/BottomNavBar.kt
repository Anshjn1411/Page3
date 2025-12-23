package dev.infa.page3.SDK.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material.icons.outlined.Watch
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen

import dev.infa.page3.SDK.ui.navigation.ExerciseScreenSDK
import dev.infa.page3.SDK.ui.navigation.HeartRateScreenSDK
import dev.infa.page3.SDK.ui.navigation.HomeScreenSDK
import dev.infa.page3.SDK.ui.navigation.ProfileScreenSDK
import dev.infa.page3.SDK.ui.navigation.SleepScreenSDK
import dev.infa.page3.SDK.ui.navigation.StepsScreenSDK

@Composable
fun BottomNavBar(
    currentTab: BottomTab,
    onTabSelected:(BottomTab) ->Unit
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
            .background(Color.Black)
            .border(1.dp, Color(0xFF111111))

            // ✅ Push content ABOVE system navigation buttons
            .navigationBarsPadding()

            // ✅ Increase nav bar height
            .padding(vertical = 14.dp)
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
                        .clickable {
                            onTabSelected(item.tab)
                        }
                        .padding(horizontal = 10.dp)
                ) {

                    // ✅ ACTIVE INDICATOR (REACT GRADIENT STRIP)
                    if (isActive) {
                        Box(
                            modifier = Modifier
                                .offset(y = (-6).dp)
                                .height(4.dp)
                                .width(30.dp)
                                .clip(RoundedCornerShape(50))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            Color(0xFF00FF88),
                                            Color(0xFF3B82F6)
                                        )
                                    )
                                )
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(top = 6.dp)
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            tint = if (isActive) Color.White else Color(0xFF6B7280),
                            modifier = Modifier.size(20.dp)
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