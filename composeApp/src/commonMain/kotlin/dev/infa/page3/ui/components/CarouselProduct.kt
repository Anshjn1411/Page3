package dev.infa.page3.ui.productscreen.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.compose.foundation.layout.Arrangement
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.round
import kotlin.math.sin

// ─── Extended CarouselProduct with optional SDK route ────────────────────────

data class CarouselProduct(
    val id: Int,
    val imageUrl: String,
    val gifUrl: String,
    val videoUrl: String,
    val websiteUrl: String,
    val label: String,
    val sdkRoute: SdkRoute? = null
)

sealed class SdkRoute {
    object Ring   : SdkRoute()
    object Bottle : SdkRoute()
    object VBand  : SdkRoute()
}

// ─── Default products for AppStyleProductCarousel ────────────────────────────

val defaultCarouselProducts = listOf(
    CarouselProduct(
        id = 1,
        label = "Suitcase",
        imageUrl   = "https://static.vecteezy.com/system/resources/previews/047/241/779/non_2x/3d-suitcase-isolated-on-transparent-background-free-png.png",
        gifUrl     = "https://motion.inventiko.com/paze3/suitcase/assets/suitcase.gif",
        videoUrl   = "https://motion.inventiko.com/paze3/suitcase/assets/suitcase.mp4",
        websiteUrl = "https://motion.inventiko.com/paze3/suitcase/index-suitcase.html"
    ),
    CarouselProduct(
        id = 5,
        label = "Round Headphone",
        imageUrl   = "https://motion.inventiko.com/paze3/suitcase/assets/round_headphone.png",
        gifUrl     = "https://motion.inventiko.com/paze3/suitcase/assets/round_headphone.gif",
        videoUrl   = "https://motion.inventiko.com/paze3/suitcase/assets/headphone.mp4",
        websiteUrl = "https://motion.inventiko.com/paze3/headphone/"
    ),
    CarouselProduct(
        id = 6,
        label = "Square Headphone",
        imageUrl   = "https://motion.inventiko.com/paze3/suitcase/assets/square_headphone.png",
        gifUrl     = "https://motion.inventiko.com/paze3/suitcase/assets/square_headphone.gif",
        videoUrl   = "https://motion.inventiko.com/paze3/suitcase/assets/headphone.mp4",
        websiteUrl = "https://motion.inventiko.com/paze3/headphone/"
    ),
    CarouselProduct(
        id = 7,
        label = "Earbuds",
        imageUrl   = "https://motion.inventiko.com/paze3/suitcase/assets/earbuds.png",
        gifUrl     = "https://motion.inventiko.com/paze3/suitcase/assets/earbuds.gif",
        videoUrl   = "https://motion.inventiko.com/paze3/suitcase/assets/earbuds.mp4",
        websiteUrl = "https://motion.inventiko.com/paze3/suitcase/index-suitcase.html"
    ),
    CarouselProduct(
        id = 8,
        label = "Measuring Tape",
        imageUrl   = "https://motion.inventiko.com/paze3/suitcase/assets/measuring_tape.png",
        gifUrl     = "https://motion.inventiko.com/paze3/suitcase/assets/measuring_tape.gif",
        videoUrl   = "https://motion.inventiko.com/paze3/suitcase/assets/measuring_tape.mp4",
        websiteUrl = "https://motion.inventiko.com/paze3/suitcase/index-suitcase.html"
    )
)

// ─── Default products for ConnectToPage3Section ───────────────────────────────

val defaultConnectProducts = listOf(
    CarouselProduct(
        id = 101,
        label      = "Smart Ring",
        imageUrl   = "https://static.vecteezy.com/system/resources/previews/047/241/779/non_2x/3d-suitcase-isolated-on-transparent-background-free-png.png",
        gifUrl     = "https://motion.inventiko.com/paze3/suitcase/assets/suitcase.gif",
        videoUrl   = "https://motion.inventiko.com/paze3/suitcase/assets/suitcase.mp4",
        websiteUrl = "",
        sdkRoute   = SdkRoute.Ring
    ),
    CarouselProduct(
        id = 102,
        label      = "Smart Bottle",
        imageUrl   = "https://motion.inventiko.com/paze3/suitcase/assets/round_headphone.png",
        gifUrl     = "https://motion.inventiko.com/paze3/suitcase/assets/round_headphone.gif",
        videoUrl   = "https://motion.inventiko.com/paze3/suitcase/assets/headphone.mp4",
        websiteUrl = "",
        sdkRoute   = SdkRoute.Bottle
    ),
    CarouselProduct(
        id = 103,
        label      = "V-Band",
        imageUrl   = "https://motion.inventiko.com/paze3/suitcase/assets/square_headphone.png",
        gifUrl     = "https://motion.inventiko.com/paze3/suitcase/assets/square_headphone.gif",
        videoUrl   = "https://motion.inventiko.com/paze3/suitcase/assets/headphone.mp4",
        websiteUrl = "",
        sdkRoute   = SdkRoute.VBand
    )
)

// ─── Helper: degrees → radians (replaces Java's Math.toRadians) ──────────────

private fun Double.toRadians(): Double = this * PI / 180.0

// ─── Public entry-point (with title) ─────────────────────────────────────────

@Composable
fun AppStyleProductCarousel(
    modifier: Modifier = Modifier,
    products: List<CarouselProduct> = defaultCarouselProducts,
    title: String = "Featured Products",
    initialIndex: Float = 0f,
    onIndexChanged: (Float) -> Unit = {},
    onItemClick: (product: CarouselProduct, index: Int) -> Unit = { _, _ -> }
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        AppStyleProductCarousel3D(
            products = products,
            initialIndex = initialIndex,
            onIndexChanged = onIndexChanged,
            onItemClick = onItemClick
        )
    }
}

// ─── 3-D Carousel ─────────────────────────────────────────────────────────────

@Composable
fun AppStyleProductCarousel3D(
    products: List<CarouselProduct> = defaultCarouselProducts,
    initialIndex: Float = 0f,
    onIndexChanged: (Float) -> Unit = {},
    onItemClick: (product: CarouselProduct, index: Int) -> Unit = { _, _ -> }
) {
    if (products.isEmpty()) return

    var selectedIndex by remember { mutableStateOf(initialIndex) }

    LaunchedEffect(initialIndex) { selectedIndex = initialIndex }

    val animatedIndex by animateFloatAsState(
        targetValue = selectedIndex,
        animationSpec = tween(durationMillis = 600),
        label = "appStyleCarouselAnimation"
    )

    val count = products.size
    val theta = 360f / count

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(550.dp)
            .pointerInput(count) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        selectedIndex = round(selectedIndex)
                        onIndexChanged(selectedIndex)
                    },
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        selectedIndex -= dragAmount / 400f
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        val width = constraints.maxWidth.toFloat()
        val radius = width / 2.1f

        products.forEachIndexed { index, product ->
            key(product.id) {
                val slotAngle = theta * index
                val spinAngle = animatedIndex * theta * -1f

                var worldAngle = (slotAngle + spinAngle) % 360f
                if (worldAngle > 180f) worldAngle -= 360f
                if (worldAngle < -180f) worldAngle += 360f

                val dist = abs(worldAngle)
                val sortingPriority = (180f - dist) * 10f
                val maxLift = 340f
                val yOffset = -(dist / 180f).pow(1.5f) * maxLift
                val scale = 0.3f + 1.4f * (1f - dist / 180f).pow(3f)
                val opacity = 1f - (dist / 180f) * 0.4f
                val blurRadius = (dist / 180f) * 6f
                val isFront = dist < (theta / 2.5f)

                Box(
                    modifier = Modifier
                        .zIndex(sortingPriority)
                        .size(200.dp, 260.dp)
                        .graphicsLayer {
                            // ✅ Use kotlin.math.sin + our extension instead of Math.toRadians
                            translationX =
                                radius * sin(worldAngle.toDouble().toRadians()).toFloat()
                            translationY = yOffset
                            scaleX = scale
                            scaleY = scale
                            alpha = opacity
                            rotationY = 0f
                            cameraDistance = 8f * density
                        }
                        .blur(blurRadius.dp)
                        .clickable(enabled = isFront) {
                            onItemClick(product, index)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        var isGifReady   by remember(product.id) { mutableStateOf(false) }
                        var isGifError   by remember(product.id) { mutableStateOf(false) }
                        var isVideoReady by remember(product.id) { mutableStateOf(false) }
                        var isVideoError by remember(product.id) { mutableStateOf(false) }

                        LaunchedEffect(dist < theta * 1.5f) {
                            if (dist >= theta * 1.5f) {
                                isGifReady   = false
                                isVideoReady = false
                            }
                        }

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .aspectRatio(1f)
                        ) {
                            val readyAlpha by animateFloatAsState(
                                targetValue = if (isGifReady || isVideoReady) 1f else 0f,
                                animationSpec = tween(300),
                                label = "carouselReadyFade"
                            )

                            val proximityAlpha = remember(dist, theta) {
                                val startFade = theta * 0.6f
                                val endFade   = theta * 0.2f
                                ((startFade - dist) / (startFade - endFade)).coerceIn(0f, 1f)
                            }

                            val animationAlpha = readyAlpha * proximityAlpha

                            // Static fallback image
                            Box(modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer { alpha = 1f - animationAlpha }
                            ) {
                                CarouselNetworkImage(
                                    imageUrl = product.imageUrl,
                                    contentDescription = product.label,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            // Animated overlay: GIF → video fallback
                            if (dist < theta * 1.5f) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .graphicsLayer { alpha = animationAlpha }
                                ) {
                                    if (!isGifError) {
                                        CarouselGifImage(
                                            gifUrl = product.gifUrl,
                                            contentDescription = product.label,
                                            modifier = Modifier.fillMaxSize(),
                                            onSuccess = { isGifReady = true },
                                            onError   = { isGifError = true }
                                        )
                                    } else if (!isVideoError) {
                                        CarouselVideoPlayer(
                                            videoUrl = product.videoUrl,
                                            modifier = Modifier.fillMaxSize(),
                                            onReady  = { isVideoReady = true },
                                            onError  = { isVideoError = true }
                                        )
                                    }
                                }
                            }
                        }

                        // Label (front item only)
                        AnimatedVisibility(
                            visible = isFront,
                            enter = fadeIn(tween(800)) + expandVertically(
                                animationSpec = tween(800),
                                expandFrom = Alignment.Top
                            ),
                            exit = fadeOut(tween(800)) + shrinkVertically(
                                animationSpec = tween(800),
                                shrinkTowards = Alignment.Top
                            )
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = product.label.uppercase(),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = Color.Black,
                                    letterSpacing = 1.2.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─── expect declarations ───────────────────────────────────────────────────────

@Composable
expect fun CarouselVideoPlayer(
    videoUrl: String,
    modifier: Modifier = Modifier,
    onReady: () -> Unit,
    onError: () -> Unit
)

@Composable
expect fun CarouselGifImage(
    gifUrl: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
    onSuccess: () -> Unit,
    onError: () -> Unit
)

@Composable
expect fun CarouselNetworkImage(
    imageUrl: String,
    contentDescription: String,
    modifier: Modifier = Modifier
)