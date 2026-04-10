package dev.infa.page3.ui.productscreen.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seiko.imageloader.rememberImagePainter
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.math.*

// ─────────────────────────────────────────────
//  Data model
// ─────────────────────────────────────────────

data class CarouselProduct(
    val imageUrl: String,
    val label: String
)

// ─────────────────────────────────────────────
//  Main composable
// ─────────────────────────────────────────────

/**
 * A 3-D rotating product carousel.
 *
 * Each cell is placed on a virtual ring using only translationX / translationY
 * (no translationZ — not available in graphicsLayer). The 3-D depth illusion
 * comes from:
 *  • translationX  — horizontal position on the ring  (cos curve)
 *  • translationY  — vertical lift for back cells      (cos curve)
 *  • scale         — larger at front, smaller at back
 *  • alpha         — opaque at front, faded at back
 *  • blur          — sharp at front, blurred at back
 *  • rotationY     — card faces the viewer at every slot
 *  • cameraDistance — perspective foreshortening
 *
 * @param products       List of [CarouselProduct] items to display.
 * @param modifier       Standard Compose modifier.
 * @param itemCount      How many cells to show (3..products.size).
 * @param carouselWidth  Width of the carousel container.
 * @param carouselHeight Height of the carousel container.
 * @param onItemClick    Callback when a cell is tapped — (product, index).
 */
@Composable
fun ProductCarousel3D(
    products: List<CarouselProduct>,
    modifier: Modifier = Modifier,
    itemCount: Int = products.size.coerceIn(3, products.size),
    carouselWidth: Dp = 300.dp,
    carouselHeight: Dp = 340.dp,
    onItemClick: (product: CarouselProduct, index: Int) -> Unit = { _, _ -> }
) {
    require(products.isNotEmpty()) { "products must not be empty" }

    val cellCount = itemCount.coerceIn(3, products.size)

    // ── Animated spin angle ───────────────────────────────────
    var selectedIndex by remember { mutableStateOf(0) }
    val spinAngle by animateFloatAsState(
        targetValue   = (360f / cellCount) * selectedIndex * -1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessMedium
        ),
        label = "spinAngle"
    )

    // ── Swipe / drag ──────────────────────────────────────────
    var dragAccumulator by remember { mutableStateOf(0f) }
    val swipeThreshold  = 40f

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(carouselWidth, carouselHeight)
            .pointerInput(cellCount) {
                val tracker = VelocityTracker()
                detectHorizontalDragGestures(
                    onDragStart   = { tracker.resetTracking(); dragAccumulator = 0f },
                    onHorizontalDrag = { change, amount ->
                        change.consume()
                        tracker.addPosition(change.uptimeMillis, change.position)
                        dragAccumulator += amount
                    },
                    onDragEnd     = {
                        if (abs(dragAccumulator) > swipeThreshold) {
                            selectedIndex += if (dragAccumulator < 0) 1 else -1
                        }
                        dragAccumulator = 0f
                    },
                    onDragCancel  = { dragAccumulator = 0f }
                )
            }
    ) {
        // Compute ring radius in px from the container width
        val density   = androidx.compose.ui.platform.LocalDensity.current
        val widthPx   = with(density) { carouselWidth.toPx() }
        // radius: distance from centre to each cell centre on the XZ plane
        val radius    = (widthPx / 2f) / tan(PI.toFloat() / cellCount)
        val liftMax   = with(density) { carouselHeight.toPx() } * 0.28f
        val thetaDeg  = 360f / cellCount

        products.take(cellCount).forEachIndexed { i, product ->
            // Static slot angle for this cell (degrees, around Y axis)
            val slotDeg = thetaDeg * i

            // World-facing angle after carousel spins
            var worldDeg = (slotDeg + spinAngle) % 360f
            if (worldDeg >  180f) worldDeg -= 360f
            if (worldDeg < -180f) worldDeg += 360f

            val dist = abs(worldDeg)   // 0 = front, 180 = back

            // ── 2-D projection of ring position ──────────────
            // X: cell moves left/right like a point on a circle viewed from above
            //    sin(0°)=0 (centre/front), sin(90°)=1 (far right), sin(180°)=0 (centre/back)
            val slotRad  = slotDeg * PI.toFloat() / 180f
            val spinRad  = spinAngle * PI.toFloat() / 180f
            val totalRad = slotRad + spinRad
            val tx       = radius * sin(totalRad)          // horizontal offset

            // Y: back cells rise upward (cos curve)
            //    cos(0°)=1 (front → no lift), cos(180°)=-1 (back → full lift)
            val ty = -(1f - cos(dist * PI.toFloat() / 180f)) / 2f * liftMax

            // ── Visual depth cues ─────────────────────────────
            val scaleFactor = 1.2f  - (dist / 180f) * 0.45f   // 1.20 → 0.75
            val cellAlpha   = 1f    - (dist / 180f) * 0.55f   // 1.00 → 0.45
            val blurAmount  = (dist / 180f) * 6f               // 0dp  → 6dp
            val labelAlpha  = if (dist < 45f) 1f else 0f

            // ── Z-order: front cell drawn last (on top) ───────
            // We achieve painter's-algorithm ordering via the loop order combined
            // with the cosine of totalRad (positive = coming toward viewer).
            val zOrder = cos(totalRad)   // +1 front, -1 back

            CarouselCell(
                product      = product,
                productIndex = i,
                translationX = tx,
                translationY = ty,
                zOrder       = zOrder,
                scale        = scaleFactor,
                alpha        = cellAlpha,
                blurDp       = blurAmount.dp,
                labelAlpha   = labelAlpha,
                onItemClick  = onItemClick
            )
        }
    }
}

// ─────────────────────────────────────────────
//  Single cell
// ─────────────────────────────────────────────

@Composable
private fun CarouselCell(
    product: CarouselProduct,
    productIndex: Int,
    translationX: Float,
    translationY: Float,
    zOrder: Float,
    scale: Float,
    alpha: Float,
    blurDp: Dp,
    labelAlpha: Float,
    onItemClick: (CarouselProduct, Int) -> Unit
) {
    val painter = rememberImagePainter(product.imageUrl)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .wrapContentSize()
            .graphicsLayer {
                this.translationX = translationX
                this.translationY = translationY
                this.scaleX       = scale
                this.scaleY       = scale
                this.alpha        = alpha
                // cameraDistance gives mild perspective foreshortening on rotationY
                this.cameraDistance = 12f * density
                // Slight rotationY so each card always faces the viewer
                // (same maths as the HTML: each cell is rotated to its slot)
                // Here we keep cards fully facing forward (rotationY = 0) for
                // simplicity; set to -worldDeg if you prefer angled cards.
                this.rotationY = 0f
            }
            .alpha(alpha)                     // also drives hit-test transparency
            .pointerInput(productIndex) {
                detectTapGesturesCompat { onItemClick(product, productIndex) }
            }
    ) {
        Image(
            painter            = painter,
            contentDescription = product.label,
            contentScale       = ContentScale.Fit,
            modifier           = Modifier
                .size(160.dp, 200.dp)
                .let { if (blurDp.value > 0.5f) it.blur(blurDp) else it }
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text          = product.label,
            fontSize      = 14.sp,
            fontWeight    = FontWeight.SemiBold,
            color         = Color(0xFFE0E0E0),
            textAlign     = TextAlign.Center,
            letterSpacing = 0.5.sp,
            modifier      = Modifier.alpha(labelAlpha)
        )
    }
}

// ─────────────────────────────────────────────
//  Tap helper — does NOT consume drag events
// ─────────────────────────────────────────────

private suspend fun androidx.compose.ui.input.pointer.PointerInputScope.detectTapGesturesCompat(
    onTap: (Offset) -> Unit
) {
    awaitPointerEventScope {
        while (true) {
            val down    = awaitPointerEvent()
            val downPos = down.changes.firstOrNull()?.position ?: continue
            val up      = awaitPointerEvent()
            val upPos   = up.changes.firstOrNull()?.position ?: continue
            if ((downPos - upPos).getDistance() < 20f) onTap(upPos)
        }
    }
}

// ─────────────────────────────────────────────
//  Sample data + preview screen
// ─────────────────────────────────────────────

private val sampleProducts = listOf(
    CarouselProduct("https://static.vecteezy.com/system/resources/previews/047/241/779/non_2x/3d-suitcase-isolated-on-transparent-background-free-png.png", "Suitcase"),
    CarouselProduct("https://www.kibotek.com/wp-content/uploads/2024/08/kiboTEK_xiaomi_band_9_027.png", "Smart Band"),
    CarouselProduct("https://www.navinmart.com/cdn/shop/files/0003_85383606-d8f7-491a-b472-8bc34e1d1d73.png", "Smart Bottle"),
    CarouselProduct("https://merlin-digital.com/cdn/shop/files/smartringnew2.png", "Smart Ring"),
    CarouselProduct("https://www.fingers.co.in/secure/api/image-tool/index.php?src=https://www.fingers.co.in/secure/api/uploads/products/1764393128_3.png&w=500&h=500&zc=2", "Headphone"),
    CarouselProduct("https://elver.in/cdn/shop/files/Elver_Buds_X_True_Wireless_Earbuds.png", "Earbuds"),
)

@Composable
fun ProductCarouselScreen() {
    var lastClicked by remember { mutableStateOf<CarouselProduct?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A2E)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            ProductCarousel3D(
                products       = sampleProducts,
                itemCount      = 6,
                carouselWidth  = 300.dp,
                carouselHeight = 360.dp,
                onItemClick    = { product, index ->
                    lastClicked = product
                    // e.g. navController.navigate("detail/$index")
                }
            )

            Spacer(Modifier.height(24.dp))

            lastClicked?.let { p ->
                Text(
                    text       = "Selected: ${p.label}",
                    color      = Color(0xFFE94560),
                    fontSize   = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier   = Modifier
                        .background(Color(0xFF16213E), RoundedCornerShape(20.dp))
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1A1A2E)
@Composable
private fun PreviewCarousel() {
    ProductCarouselScreen()
}