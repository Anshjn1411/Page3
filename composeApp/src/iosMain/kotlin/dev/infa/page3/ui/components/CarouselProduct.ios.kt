package dev.infa.page3.ui.productscreen.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.viewinterop.UIKitView
import com.seiko.imageloader.asImageBitmap
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ColorInfo
import org.jetbrains.skia.ColorSpace
import org.jetbrains.skia.ColorType
import org.jetbrains.skia.ImageInfo
import platform.AVFoundation.AVPlayerItem
import platform.AVFoundation.AVPlayerItemDidPlayToEndTimeNotification
import platform.AVFoundation.AVPlayerItemStatusFailed
import platform.AVFoundation.AVPlayerItemStatusReadyToPlay
import platform.AVFoundation.AVPlayerLayer
import platform.AVFoundation.AVPlayerLooper
import platform.AVFoundation.AVQueuePlayer
import platform.AVFoundation.currentItem
import platform.AVFoundation.pause
import platform.AVFoundation.play
import platform.AVFoundation.volume
import platform.CoreFoundation.CFDataRef
import platform.CoreGraphics.CGBitmapContextCreate
import platform.CoreGraphics.CGColorSpaceCreateDeviceRGB
import platform.CoreGraphics.CGContextDrawImage
import platform.CoreGraphics.CGImageAlphaInfo
import platform.CoreGraphics.CGImageGetHeight
import platform.CoreGraphics.CGImageGetWidth
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.kCGBitmapByteOrder32Big
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue
import platform.Foundation.NSTimer
import platform.Foundation.NSURL
import platform.Foundation.NSURLSession
import platform.Foundation.dataTaskWithURL
import platform.ImageIO.CGImageSourceCreateImageAtIndex
import platform.ImageIO.CGImageSourceCreateWithData
import platform.ImageIO.CGImageSourceGetCount
import platform.QuartzCore.CATransaction
import platform.UIKit.UIImage
import platform.UIKit.UIImageView
import platform.UIKit.UIView
import platform.UIKit.UIViewContentMode

// ─── Video Player ─────────────────────────────────────────────────────────────

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun CarouselVideoPlayer(
    videoUrl: String,
    modifier: Modifier,
    onReady: () -> Unit,
    onError: () -> Unit
) {
    var isReady by remember { mutableStateOf(false) }

    // Holder class so we can reference everything in DisposableEffect
    class PlayerHolder(
        val player: AVQueuePlayer,
        val playerLayer: AVPlayerLayer,
        val looper: AVPlayerLooper,
        val item: AVPlayerItem
    )

    val holder = remember(videoUrl) {
        val url  = NSURL.URLWithString(videoUrl) ?: return@remember null
        val item = AVPlayerItem.playerItemWithURL(url)
        val qp   = AVQueuePlayer.queuePlayerWithItems(listOf(item))
        val loop = AVPlayerLooper.playerLooperWithPlayer(qp, item)
        val layer = AVPlayerLayer()
        layer.player = qp
        qp.volume = 0f
        qp.play()
        PlayerHolder(qp, layer, loop, item)
    }

    DisposableEffect(videoUrl) {
        val h = holder ?: return@DisposableEffect onDispose {}

        // ✅ Poll item.status instead of KVO — avoids the "overrides nothing" error
        var timer: NSTimer? = null
        timer = NSTimer.scheduledTimerWithTimeInterval(
            interval = 0.2,
            repeats  = true,
            block    = { _ ->
                when (h.item.status) {
                    AVPlayerItemStatusReadyToPlay -> {
                        if (!isReady) {
                            isReady = true
                            onReady()
                        }
                        timer?.invalidate()
                    }
                    AVPlayerItemStatusFailed -> {
                        onError()
                        timer?.invalidate()
                    }
                    else -> Unit
                }
            }
        )

        onDispose {
            timer.invalidate()
            h.player.pause()
            h.playerLayer.removeFromSuperlayer()
        }
    }

    val videoAlpha by animateFloatAsState(
        targetValue = if (isReady) 1f else 0f,
        animationSpec = tween(400),
        label = "carouselVideoFadeIOS"
    )

    if (holder != null) {
        UIKitView(
            factory = {
                val container = UIView()
                CATransaction.begin()
                CATransaction.setDisableActions(true)
                holder.playerLayer.frame = container.bounds
                container.layer.addSublayer(holder.playerLayer)
                CATransaction.commit()
                container
            },
            update = { container ->
                holder.playerLayer.frame = container.bounds
            },
            modifier = modifier.graphicsLayer { alpha = videoAlpha }
        )
    }
}

// ─── GIF Image ────────────────────────────────────────────────────────────────

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun CarouselGifImage(
    gifUrl: String,
    contentDescription: String,
    modifier: Modifier,
    onSuccess: () -> Unit,
    onError: () -> Unit
) {
    UIKitView(
        factory = {
            val imageView = UIImageView()
            imageView.contentMode = UIViewContentMode.UIViewContentModeScaleAspectFit

            val url = NSURL.URLWithString(gifUrl)
            if (url != null) {
                // ✅ Named 'completionHandler' to pick the correct K/N overload
                NSURLSession.sharedSession
                    .dataTaskWithURL(url, completionHandler = { data, _, error ->
                        if (error != null || data == null) {
                            NSOperationQueue.mainQueue.addOperationWithBlock { onError() }
                            return@dataTaskWithURL
                        }

                        // Cast NSData → CFDataRef for ImageIO
                        @Suppress("UNCHECKED_CAST")
                        val cfData = data as? CFDataRef
                        if (cfData == null) {
                            NSOperationQueue.mainQueue.addOperationWithBlock { onError() }
                            return@dataTaskWithURL
                        }

                        val source = CGImageSourceCreateWithData(cfData, null)
                        if (source == null) {
                            NSOperationQueue.mainQueue.addOperationWithBlock { onError() }
                            return@dataTaskWithURL
                        }

                        val frameCount = CGImageSourceGetCount(source).toInt()
                        val frames = (0 until frameCount).mapNotNull { i ->
                            CGImageSourceCreateImageAtIndex(source, i.toULong(), null)
                                ?.let { cgImg -> UIImage.imageWithCGImage(cgImg) }
                        }

                        if (frames.isEmpty()) {
                            NSOperationQueue.mainQueue.addOperationWithBlock { onError() }
                            return@dataTaskWithURL
                        }

                        val totalDuration = frameCount * 0.1

                        NSOperationQueue.mainQueue.addOperationWithBlock {
                            imageView.animationImages = frames
                            imageView.animationDuration = totalDuration
                            imageView.animationRepeatCount = 0
                            imageView.startAnimating()
                            onSuccess()
                        }
                    })
                    .resume()
            } else {
                onError()
            }

            imageView
        },
        modifier = modifier
    )
}

// ─── Static Network Image ─────────────────────────────────────────────────────

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun CarouselNetworkImage(
    imageUrl: String,
    contentDescription: String,
    modifier: Modifier
) {
    var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }

    DisposableEffect(imageUrl) {
        val url = NSURL.URLWithString(imageUrl)
        if (url != null) {
            // ✅ Named 'completionHandler' to pick the correct K/N overload
            NSURLSession.sharedSession
                .dataTaskWithURL(url, completionHandler = { data, _, _ ->
                    if (data != null) {
                        val uiImage = UIImage.imageWithData(data)
                        if (uiImage != null) {
                            // ✅ Manual UIImage → Skia Bitmap → ImageBitmap
                            val bitmap = uiImageToImageBitmap(uiImage)
                            if (bitmap != null) {
                                NSOperationQueue.mainQueue.addOperationWithBlock {
                                    imageBitmap = bitmap
                                }
                            }
                        }
                    }
                })
                .resume()
        }
        onDispose { }
    }

    val bitmap = imageBitmap
    if (bitmap != null) {
        Image(
            bitmap = bitmap,
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = ContentScale.Fit
        )
    }
}

// ─── UIImage → Compose ImageBitmap ───────────────────────────────────────────
// CGImageRef.toComposeImageBitmap() does not exist in all K/N setups,
// so we render into a CGBitmapContext and copy raw RGBA bytes into a Skia Bitmap.

@OptIn(ExperimentalForeignApi::class)
private fun uiImageToImageBitmap(uiImage: UIImage): ImageBitmap? {
    val cgImage = uiImage.CGImage ?: return null

    val width  = CGImageGetWidth(cgImage).toInt()
    val height = CGImageGetHeight(cgImage).toInt()
    if (width == 0 || height == 0) return null

    val colorSpace  = CGColorSpaceCreateDeviceRGB() ?: return null
    val bytesPerRow = width * 4
    val rawBytes    = ByteArray(height * bytesPerRow)

    rawBytes.usePinned { pinned ->
        val ctx = CGBitmapContextCreate(
            data             = pinned.addressOf(0),
            width            = width.toULong(),
            height           = height.toULong(),
            bitsPerComponent = 8u,
            bytesPerRow      = bytesPerRow.toULong(),
            space            = colorSpace,
            bitmapInfo       = CGImageAlphaInfo.kCGImageAlphaPremultipliedLast.value or
                    kCGBitmapByteOrder32Big
        ) ?: return null

        CGContextDrawImage(
            ctx,
            CGRectMake(0.0, 0.0, width.toDouble(), height.toDouble()),
            cgImage
        )
    }

    val skiaBitmap = Bitmap()
    val imageInfo  = ImageInfo(
        colorInfo = ColorInfo(
            colorType  = ColorType.RGBA_8888,
            alphaType  = ColorAlphaType.PREMUL,
            colorSpace = ColorSpace.sRGB
        ),
        width  = width,
        height = height
    )
    skiaBitmap.allocPixels(imageInfo)
    skiaBitmap.installPixels(rawBytes)

    return skiaBitmap.asImageBitmap()
}