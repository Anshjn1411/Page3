package dev.infa.page3.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

import androidx.compose.runtime.*
import androidx.compose.ui.interop.UIKitView
import io.ktor.client.request.invoke
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.*
import platform.CoreMedia.CMTimeMakeWithSeconds
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSURL
import platform.QuartzCore.CALayer
import platform.UIKit.*

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun LoopingVideoBackground(modifier: Modifier ) {
    val player = remember {
        val url = NSURL(string = "https://raw.githubusercontent.com/Anshjn1411/medicCodec/main/WhatsApp%20Video%202025-12-08%20at%2017.09.02.mp4")
        val item = AVPlayerItem(uRL = url!!)
        AVPlayer(playerItem = item).apply {
            actionAtItemEnd = AVPlayerActionAtItemEndNone
            volume = 0f
            play()
        }
    }

    // Loop playback
    DisposableEffect(Unit) {
        val observer = NSNotificationCenter.defaultCenter.addObserverForName(
            name = AVPlayerItemDidPlayToEndTimeNotification,
            `object` = player.currentItem,
            queue = null
        ) {
            player.seekToTime(CMTimeMakeWithSeconds(0.0, preferredTimescale = 1))
            player.play()
        }

        onDispose {
            NSNotificationCenter.defaultCenter.removeObserver(observer)
            player.pause()
        }
    }

    UIKitView(
        modifier = modifier,
        factory = {
            // ✅ Create a layer manually (Kotlin/Native only supports default constructor)
            val playerLayer = AVPlayerLayer().apply {
                this.player = player
                this.videoGravity = AVLayerVideoGravityResizeAspectFill
            }

            val view = UIView(frame = UIScreen.mainScreen.bounds).apply {
                backgroundColor = UIColor.blackColor
                layer.addSublayer(playerLayer)
            }

            playerLayer.frame = view.layer.bounds
            view
        },
        update = { view ->
            (view.layer.sublayers?.firstOrNull() as? AVPlayerLayer)?.frame = view.layer.bounds
        }
    )
}