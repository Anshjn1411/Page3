package com.example.paze3

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import coil.compose.rememberAsyncImagePainter
import android.os.Build
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.media3.common.util.UnstableApi
import androidx.annotation.OptIn
import kotlin.math.*

@OptIn(UnstableApi::class)
@Composable
fun VideoPlayer(
    videoUrl: String, 
    modifier: Modifier = Modifier, 
    onReady: () -> Unit = {},
    onError: () -> Unit
) {
    val context = LocalContext.current
    var isVideoReady by remember { mutableStateOf(false) }
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            repeatMode = Player.REPEAT_MODE_ALL
            playWhenReady = true
            volume = 0f // Mute for autoplay
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_READY) {
                        isVideoReady = true
                        onReady()
                    }
                }
                override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                    onError()
                }
            })
        }
    }

    LaunchedEffect(videoUrl) {
        exoPlayer.setMediaItem(MediaItem.fromUri(videoUrl))
        exoPlayer.prepare()
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    val videoAlpha by animateFloatAsState(
        targetValue = if (isVideoReady) 1f else 0f,
        animationSpec = tween(400),
        label = "videoFade"
    )

    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                player = exoPlayer
                useController = false
                resizeMode = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                setShutterBackgroundColor(android.graphics.Color.TRANSPARENT)
            }
        },
        modifier = modifier.graphicsLayer { 
            alpha = videoAlpha
        }
    )
}

@Composable
fun ProductCarousel3D(
    products: List<Product>,
    onProductClick: (Product) -> Unit,
    initialIndex: Float = 0f,
    onIndexChanged: (Float) -> Unit = {}
) {
    val context = LocalContext.current
    val imageLoader = remember {
        ImageLoader.Builder(context)
            .components {
                if (Build.VERSION.SDK_INT >= 28) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .build()
    }
    var selectedIndex by remember { mutableStateOf(initialIndex) }
    
    // Update local state if initialIndex changes from outside
    LaunchedEffect(initialIndex) {
        selectedIndex = initialIndex
    }

    val animatedIndex by animateFloatAsState(
        targetValue = selectedIndex,
        animationSpec = tween(durationMillis = 600),
        label = "carouselAnimation"
    )

    val count = products.size
    if (count == 0) return
    val theta = 360f / count
    
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(550.dp)
            .pointerInput(Unit) {
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
        val radius = (width / 2.1f) 

        products.forEachIndexed { index, product ->
            androidx.compose.runtime.key(product.id) {
                val slotAngle = theta * index
                val spinAngle = animatedIndex * theta * -1f
                
                // Robust angle normalization
                var worldAngle = (slotAngle + spinAngle) % 360f
                if (worldAngle > 180f) worldAngle -= 360f
                if (worldAngle < -180f) worldAngle += 360f
                
                val dist = abs(worldAngle)
                
                // Use a much more aggressive zIndex to ensure front products stay on top
                // We also add a large bias for focused items
                val sortingPriority = (180f - dist) * 10f
                
                val maxLift = 340f 
                val yOffset = -(dist / 180f).pow(1.5f) * maxLift
                val scale = 0.3f + 1.4f * (1.0f - dist / 180f).pow(3.0f)
                val opacity = 1f - (dist / 180f) * 0.4f
                val blurRadius = (dist / 180f) * 6f
                
                val isFront = dist < (theta / 2.5f)

                Box(
                    modifier = Modifier
                        .zIndex(sortingPriority) // Set zIndex first for reliable sorting
                        .size(200.dp, 260.dp)
                        .graphicsLayer {
                            translationX = (radius * sin(Math.toRadians(worldAngle.toDouble())).toFloat())
                            translationY = yOffset
                            scaleX = scale
                            scaleY = scale
                            alpha = opacity
                            rotationY = 0f 
                            cameraDistance = 8f * density
                        }
                        .blur(blurRadius.dp)
                        .clickable(enabled = isFront) { 
                            onProductClick(product)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        var isGifReady by remember(product.id) { mutableStateOf(false) }
                        var isGifError by remember(product.id) { mutableStateOf(false) }
                        var isVideoReady by remember(product.id) { mutableStateOf(false) }
                        var isVideoError by remember(product.id) { mutableStateOf(false) }

                            // Reset readiness only when product moves far away
                        LaunchedEffect(dist < theta * 1.5f) {
                            if (dist >= theta * 1.5f) {
                                isGifReady = false
                                isVideoReady = false
                            }
                        }

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .aspectRatio(1f)
                        ) {
                            // Readiness alpha - handles the "pop" when the asset finally loads
                        val readyAlphaState = animateFloatAsState(
                            targetValue = if (isGifReady || isVideoReady) 1f else 0f,
                            animationSpec = tween(300),
                            label = "readyFade"
                        )

                        // Proximity alpha - handles the transition as the user scrolls
                        // Fades in between 0.6*theta and 0.2*theta
                        val proximityAlpha = remember(dist, theta) {
                            val startFade = theta * 0.6f
                            val endFade = theta * 0.2f
                            ((startFade - dist) / (startFade - endFade)).coerceIn(0f, 1f)
                        }

                        val animationAlpha = readyAlphaState.value * proximityAlpha

                        // Base static image - fades out as animation layer takes over
                        AsyncImage(
                            model = product.imageUrl,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer { 
                                    alpha = 1f - animationAlpha 
                                }
                        )

                        // Pre-load range increased to 1.5 * theta for better caching
                        if (dist < theta * 1.5f) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer { alpha = animationAlpha }
                            ) {
                                    if (!isGifError) {
                                        // Try GIF first
                                        AsyncImage(
                                            model = ImageRequest.Builder(context)
                                                .data(product.gifUrl)
                                                .crossfade(true)
                                                .build(),
                                            imageLoader = imageLoader,
                                            contentDescription = product.name,
                                            onSuccess = { isGifReady = true },
                                            onError = { isGifError = true },
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else if (!isVideoError) {
                                        // Fallback to Video if GIF fails
                                        VideoPlayer(
                                            videoUrl = product.videoUrl,
                                            modifier = Modifier.fillMaxSize(),
                                            onReady = { isVideoReady = true },
                                            onError = { isVideoError = true }
                                        )
                                    }
                                }
                            }
                        }
                        
                        androidx.compose.animation.AnimatedVisibility(
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
                                    text = product.name.uppercase(),
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

@Composable
fun FeaturedProductsCarousel(
    modifier: Modifier = Modifier,
    products: List<Product> = allProducts.filter { it.showInCarousel },
    initialIndex: Float = 0f,
    onIndexChanged: (Float) -> Unit = {},
    onProductClick: (Product) -> Unit
) {
    Column(modifier = modifier) {
        Text(
            text = "Featured Products",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        ProductCarousel3D(
            products = products,
            onProductClick = onProductClick,
            initialIndex = initialIndex,
            onIndexChanged = onIndexChanged
        )
    }
}
