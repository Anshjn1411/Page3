package dev.infa.page3.ui.components

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.PlayerView

@Composable
actual fun LoopingVideoBackground(modifier: Modifier , videoUrl: String?  ) {
    val context = LocalContext.current
    var url = ""

    if(videoUrl==null) {
        url = "https://raw.githubusercontent.com/Anshjn1411/medicCodec/main/WhatsApp%20Video%202025-12-08%20at%2017.09.02.mp4"
    }else{
        url = videoUrl
    }
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val mediaItem = MediaItem.fromUri(
                Uri.parse(url)
            )
            setMediaItem(mediaItem)
            repeatMode = Player.REPEAT_MODE_ONE
            playWhenReady = true
            volume = 0f
            prepare()
        }
    }

    AndroidView(
        factory = {
            PlayerView(it).apply {
                player = exoPlayer
                useController = false
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
            }
        },
        modifier = modifier
    )

    DisposableEffect(Unit) {
        onDispose { exoPlayer.release() }
    }
}
//"https://raw.githubusercontent.com/Anshjn1411/medicCodec/main/WhatsApp%20Video%202025-12-08%20at%2017.09.02.mp4"