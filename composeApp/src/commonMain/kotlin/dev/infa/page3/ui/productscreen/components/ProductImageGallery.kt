package dev.infa.page3.ui.productscreen.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.seiko.imageloader.rememberImagePainter
import dev.infa.page3.data.model.WcImage

private fun normalizeProductImageUrl(url: String?): String {
    if (url.isNullOrBlank()) return ""
    return when {
        url.startsWith("//") -> "https:$url"
        url.startsWith("/") -> "https://www.page3life.com$url"
        else -> url
    }
}

/** Ordered URLs for a product grid/card (prefers WooCommerce `thumbnail` when present). */
fun productGalleryUrlsForCard(images: List<WcImage>): List<String> =
    images.mapNotNull { img ->
        val raw = img.thumbnail?.takeIf { it.isNotBlank() } ?: img.src
        normalizeProductImageUrl(raw).takeIf { it.isNotBlank() }
    }

/** Full-size gallery URLs for product detail. */
fun productGalleryUrlsForDetail(images: List<WcImage>): List<String> =
    images.mapNotNull { img ->
        normalizeProductImageUrl(img.src).takeIf { it.isNotBlank() }
    }

/**
 * Shows one image at a time with previous/next controls when [imageUrls] has more than one entry.
 */
@Composable
fun ProductImageGallery(
    galleryKey: Any,
    imageUrls: List<String>,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    onImageAreaClick: (() -> Unit)? = null
) {
    val urls = remember(imageUrls) { imageUrls.filter { it.isNotBlank() } }
    var index by remember(galleryKey) { mutableIntStateOf(0) }

    if (urls.isEmpty()) {
        Box(
            modifier = modifier
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )
        return
    }

    LaunchedEffect(galleryKey, urls.size) {
        index = index.coerceIn(0, urls.lastIndex)
    }

    val safeIndex = index.coerceIn(0, urls.lastIndex)
    val painter = rememberImagePainter(urls[safeIndex])

    Box(modifier = modifier) {
        Image(
            painter = painter,
            contentDescription = contentDescription,
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (onImageAreaClick != null) {
                        Modifier.clickable { onImageAreaClick() }
                    } else {
                        Modifier
                    }
                ),
            contentScale = contentScale
        )

        if (urls.size > 1) {
            if (safeIndex > 0) {
                IconButton(
                    onClick = { index = safeIndex - 1 },
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(4.dp)
                        .background(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.75f),
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Previous image",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            if (safeIndex < urls.lastIndex) {
                IconButton(
                    onClick = { index = safeIndex + 1 },
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(4.dp)
                        .background(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.75f),
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Next image",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
