package dev.infa.page3.SDK.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.*
import dev.infa.page3.SDK.ui.theme.*

@Composable
fun CircularProgressRing(
    value: Int,
    max: Int,
    label: String,
    color: Color,
    size: Dp = AppDimensions.ProgressRing.Default,
    icon: ImageVector? = null,
    onClick: (() -> Unit)? = null
) {
    val percentage = (value / max.toFloat()).coerceAtMost(1f)

    val animatedProgress by animateFloatAsState(
        targetValue = percentage,
        animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
        label = "progress"
    )

    val infiniteGlow = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteGlow.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    val clickableModifier = if (onClick != null) {
        Modifier.clickable { onClick() }
    } else Modifier

    Column(
        modifier = clickableModifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(size),
            contentAlignment = Alignment.Center
        ) {
            // Glow background
            Box(
                modifier = Modifier
                    .size(size)
                    .background(color.copy(alpha = glowAlpha), CircleShape)
                    .blur(AppDimensions.ProgressRing.BlurRadius)
            )

            // Canvas Ring
            Canvas(modifier = Modifier.size(size)) {
                val strokeWidth = AppDimensions.ProgressRing.Stroke.toPx()
                val radius = (size.toPx() - strokeWidth) / 2

                // Background ring
                drawCircle(
                    color = AppColors.OverlayMedium,
                    radius = radius,
                    style = Stroke(width = strokeWidth)
                )

                // Progress ring
                drawArc(
                    color = color,
                    startAngle = -90f,
                    sweepAngle = 360f * animatedProgress,
                    useCenter = false,
                    style = Stroke(
                        width = strokeWidth,
                        cap = StrokeCap.Round
                    )
                )
            }

            // Center Content
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(AppDimensions.IconSize.Medium)
                    )
                }

                Text(
                    text = "${(percentage * 100).toInt()}%",
                    color = AppColors.TextPrimary,
                    style = AppTypography.ValueSmall
                )
            }
        }

        Spacer(modifier = Modifier.height(AppDimensions.Spacing.Small))

        Text(
            text = label,
            style = AppTypography.LabelMedium,
            color = AppColors.TextSecondary
        )
    }
}
