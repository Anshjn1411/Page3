package dev.infa.page3.SDK.bottle.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.Navigator
import dev.infa.page3.SDK.bottle.viewmodel.BottleViewModel

// ─── Navigation Tabs ─────────────────────────────────────────────────────────────

enum class BottleTab(val emoji: String, val label: String) {
    HISTORY("📊", "History"),
    HOME("🍶", "Home"),
    SETTINGS("⚙️", "Settings")
}

private val BottleNavBlue = Color(0xFF4FC3F7)
private val BgDeep        = Color(0xFF0A0E1A)

// ─── Scroll-aware nav visibility state (hoisted, shared via CompositionLocal) ────

class NavVisibilityState {
    var isVisible by mutableStateOf(true)
}

val LocalNavVisibility = compositionLocalOf { NavVisibilityState() }

// ─── Main Container ───────────────────────────────────────────────────────────────

@Composable
fun BottleMainContainer(
    viewModel: BottleViewModel,
    navigator: Navigator
) {
    var selectedTab   by remember { mutableStateOf(BottleTab.HOME) }
    val navVisibility  = remember { NavVisibilityState() }

    // Reset nav to visible whenever tab changes
    LaunchedEffect(selectedTab) { navVisibility.isVisible = true }

    val navOffsetY by animateDpAsState(
        targetValue   = if (navVisibility.isVisible) 0.dp else 120.dp,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 300f),
        label = "nav_slide"
    )
    val navAlpha by animateFloatAsState(
        targetValue   = if (navVisibility.isVisible) 1f else 0f,
        animationSpec = tween(260),
        label = "nav_alpha"
    )

    CompositionLocalProvider(LocalNavVisibility provides navVisibility) {

        // ── Edge-to-edge root box — same dark background everywhere ─────────────
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BgDeep)
                // Only pad horizontal system insets (avoid clipping notch area sideways)
                .windowInsetsPadding(
                    WindowInsets.systemBars.only(WindowInsetsSides.Horizontal)
                )
        ) {

            // ── Screen content — fills the entire box, draws behind status bar ──
            // Each screen is responsible for its own top status-bar padding.
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    val dir = if (targetState.ordinal > initialState.ordinal) 1 else -1
                    (slideInHorizontally { it * dir } + fadeIn(tween(260))) togetherWith
                            (slideOutHorizontally { -it * dir } + fadeOut(tween(180)))
                },
                modifier = Modifier.fillMaxSize(),
                label = "tab_transition"
            ) { tab ->
                when (tab) {
                    BottleTab.HOME     -> BottleHomeScreen(viewModel = viewModel, navigator = navigator)
                    BottleTab.HISTORY  -> BottleDrinkingHistoryScreen(viewModel = viewModel, navigator = navigator)
                    BottleTab.SETTINGS -> BottleSettingsScreen(viewModel = viewModel, navigator = navigator)
                }
            }

            // ── Floating nav overlaid — slides off when scrolling down ───────────
            FloatingBottomNav(
                selectedTab   = selectedTab,
                onTabSelected = { selectedTab = it },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 28.dp)
                    .windowInsetsPadding(WindowInsets.navigationBars) // home-indicator safe area
                    .padding(bottom = 12.dp)
                    .offset(y = navOffsetY)
                    .graphicsLayer(alpha = navAlpha)
            )
        }
    }
}

// ─── Floating Bottom Nav Bar ─────────────────────────────────────────────────────

@Composable
private fun FloatingBottomNav(
    selectedTab: BottleTab,
    onTabSelected: (BottleTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp)
            .shadow(
                elevation   = 32.dp,
                shape       = RoundedCornerShape(36.dp),
                ambientColor = BottleNavBlue.copy(alpha = 0.35f),
                spotColor   = BottleNavBlue.copy(alpha = 0.45f)
            )
            .clip(RoundedCornerShape(36.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFF1A1A2E).copy(alpha = 0.93f),
                        Color(0xFF16213E).copy(alpha = 0.97f)
                    )
                )
            )
    ) {
        // Glassmorphic top shimmer line
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .align(Alignment.TopCenter)
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = 0.28f),
                            Color.White.copy(alpha = 0.10f),
                            Color.Transparent
                        )
                    )
                )
        )

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            BottleTab.entries.forEach { tab ->
                NavItem(
                    tab        = tab,
                    isSelected = tab == selectedTab,
                    isCenter   = tab == BottleTab.HOME,
                    onClick    = { onTabSelected(tab) }
                )
            }
        }
    }
}

// ─── Nav Item ────────────────────────────────────────────────────────────────────

@Composable
private fun NavItem(
    tab: BottleTab,
    isSelected: Boolean,
    isCenter: Boolean,
    onClick: () -> Unit
) {
    val animScale by animateFloatAsState(
        targetValue   = if (isSelected) 1f else 0.88f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "scale"
    )
    val animAlpha by animateFloatAsState(
        targetValue   = if (isSelected) 1f else 0.45f,
        animationSpec = tween(200),
        label = "alpha"
    )

    if (isCenter) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .graphicsLayer(scaleX = animScale, scaleY = animScale)
                .shadow(16.dp, CircleShape, ambientColor = BottleNavBlue.copy(0.5f))
                .clip(CircleShape)
                .background(
                    if (isSelected)
                        Brush.radialGradient(listOf(Color(0xFF81D4FA), BottleNavBlue, Color(0xFF0288D1)))
                    else
                        Brush.radialGradient(listOf(Color(0xFF2A2A4A), Color(0xFF1A1A2E)))
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication        = null,
                    onClick           = onClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(tab.emoji, fontSize = 24.sp)
        }
    } else {
        Column(
            modifier = Modifier
                .width(72.dp)
                .graphicsLayer(scaleX = animScale, scaleY = animScale, alpha = animAlpha)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication        = null,
                    onClick           = onClick
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(tab.emoji, fontSize = 20.sp)
            Spacer(Modifier.height(3.dp))
            Text(
                text       = tab.label,
                color      = if (isSelected) BottleNavBlue else Color.White.copy(alpha = 0.5f),
                fontSize   = 10.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
            AnimatedVisibility(visible = isSelected) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(Modifier.height(3.dp))
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .clip(CircleShape)
                            .background(BottleNavBlue)
                    )
                }
            }
        }
    }
}