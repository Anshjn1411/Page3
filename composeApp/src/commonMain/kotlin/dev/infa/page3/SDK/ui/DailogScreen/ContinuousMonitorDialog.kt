package dev.infa.page3.SDK.ui.DailogScreen

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Dialog
import dev.infa.page3.SDK.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun ContinuousMonitorDialog(
    durationSeconds: Int = 30,
    onDismiss: () -> Unit
) {
    var secondsLeft by remember { mutableStateOf(durationSeconds) }

    val offsetY by rememberInfiniteTransition().animateFloat(
        initialValue = 0f,
        targetValue = -20f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    LaunchedEffect(Unit) {
        while (secondsLeft > 0) {
            delay(1000)
            secondsLeft--
        }
        onDismiss()
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = AppShapes.Dialog,
            color = AppColors.BackgroundCard,
            modifier = Modifier.padding(AppDimensions.CardPadding.ExtraLarge)
        ) {
            Column(
                modifier = Modifier
                    .padding(AppDimensions.CardPadding.ExtraLarge)
                    .width(IntrinsicSize.Min),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(AppDimensions.IconSize.XXL)
                        .offset(y = offsetY.dp)
                        .background(AppColors.Primary, shape = CircleShape)
                )
                Spacer(modifier = Modifier.height(AppDimensions.Spacing.ExtraLarge))
                Text(
                    text = "Continuous Monitor Enabled",
                    color = AppColors.Primary,
                    style = AppTypography.HeadingExtraSmall
                )
                Spacer(modifier = Modifier.height(AppDimensions.Spacing.Medium))
                Text(
                    text = "Time left: $secondsLeft sec",
                    color = AppColors.TextPrimary,
                    style = AppTypography.BodySmall
                )
            }
        }
    }
}
