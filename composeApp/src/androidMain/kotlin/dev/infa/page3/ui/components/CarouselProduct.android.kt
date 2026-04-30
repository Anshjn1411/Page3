package dev.infa.page3.ui.productscreen.components

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.rememberAsyncImagePainter
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import android.os.Build
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import coil.ImageLoader
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.PlayerView



// ─── Video Player ─────────────────────────────────────────────────────────────

@Composable
actual fun CarouselVideoPlayer(
    videoUrl: String,
    modifier: Modifier,
    onReady: () -> Unit,
    onError: () -> Unit
) {
    val context = LocalContext.current
    var isVideoReady by remember { mutableStateOf(false) }

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            repeatMode = Player.REPEAT_MODE_ALL
            playWhenReady = true
            volume = 0f
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_READY && !isVideoReady) {
                        isVideoReady = true
                        onReady()
                    }
                }

                override fun onPlayerError(error: PlaybackException) {
                    onError()
                }
            })
        }
    }

    LaunchedEffect(videoUrl) {
        isVideoReady = false
        exoPlayer.setMediaItem(MediaItem.fromUri(videoUrl))
        exoPlayer.prepare()
    }

    DisposableEffect(Unit) {
        onDispose { exoPlayer.release() }
    }

    val videoAlpha by animateFloatAsState(
        targetValue = if (isVideoReady) 1f else 0f,
        animationSpec = tween(400),
        label = "carouselVideoFade"
    )

    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                player = exoPlayer
                useController = false
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                setShutterBackgroundColor(android.graphics.Color.TRANSPARENT)
            }
        },
        modifier = modifier.graphicsLayer { alpha = videoAlpha }
    )
}

// ─── GIF Image ────────────────────────────────────────────────────────────────

@Composable
actual fun CarouselGifImage(
    gifUrl: String,
    contentDescription: String,
    modifier: Modifier,
    onSuccess: () -> Unit,
    onError: () -> Unit
) {
    val context = LocalContext.current

    // GIF-capable ImageLoader (created once per composition context)
    val gifImageLoader = remember(context) {
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

    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(context)
            .data(gifUrl)
            .crossfade(true)
            .listener(
                onSuccess = { _, _ -> onSuccess() },
                onError = { _, _ -> onError() }
            )
            .build(),
        imageLoader = gifImageLoader
    )

    Image(
        painter = painter,
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = ContentScale.Fit
    )
}

// ─── Static Network Image ─────────────────────────────────────────────────────

@Composable
actual fun CarouselNetworkImage(
    imageUrl: String,
    contentDescription: String,
    modifier: Modifier
) {
    val context = LocalContext.current

    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(context)
            .data(imageUrl)
            .crossfade(true)
            .build()
    )

    Image(
        painter = painter,
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = ContentScale.Fit
    )
}