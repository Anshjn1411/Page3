package dev.infa.page3.SDK.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.lottiefiles.dotlottie.core.compose.ui.DotLottieAnimation
import com.lottiefiles.dotlottie.core.util.DotLottieSource


@Composable
actual fun ScanningAnimation(
    modifier: Modifier
) {
    DotLottieAnimation(
        source = DotLottieSource.Url(
            "https://lottiefiles-mobile-templates.s3.amazonaws.com/ar-stickers/swag_sticker_piggy.lottie"
        ),
        autoplay = true,
        loop = true,
        speed = 1.4f,
        modifier = modifier
    )
}
