package dev.infa.page3.SDK.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.infa.page3.SDK.ui.theme.*
import dev.infa.page3.SDK.ui.utils.DateInfo
import dev.infa.page3.SDK.ui.utils.DateUtils

@Composable
fun DateSelector(
    selectedDate: DateInfo,
    onDateChange: (DateInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    val isToday = remember(selectedDate) {
        DateUtils.isToday(selectedDate)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(AppColors.BackgroundPrimary.copy(alpha = AppAlpha.Medium))
            .padding(
                horizontal = AppDimensions.ScreenPadding.Horizontal,
                vertical = AppDimensions.Component.DateSelectorPadding
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = {
            val newDate = DateUtils.addDays(selectedDate, -1)
            onDateChange(newDate)
        }) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "Previous Day",
                tint = AppColors.TextPrimary
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(AppDimensions.Spacing.Medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CalendarToday,
                contentDescription = null,
                tint = AppColors.Primary,
                modifier = Modifier.size(AppDimensions.IconSize.Small)
            )
            Text(
                text = DateUtils.formatDateForDisplay(selectedDate),
                color = AppColors.TextPrimary,
                style = AppTypography.BodyMedium
            )
        }

        IconButton(
            onClick = {
                if (!isToday) {
                    val newDate = DateUtils.addDays(selectedDate, 1)
                    onDateChange(newDate)
                }
            },
            enabled = !isToday
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Next Day",
                tint = if (isToday) {
                    AppColors.TextPrimary.copy(alpha = AppAlpha.Disabled)
                } else {
                    AppColors.TextPrimary
                }
            )
        }
    }
}
