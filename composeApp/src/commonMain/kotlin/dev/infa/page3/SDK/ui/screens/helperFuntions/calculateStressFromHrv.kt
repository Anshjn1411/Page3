package dev.infa.page3.SDK.ui.screens.helperFuntions

fun calculateStressFromHrv(hrv: Int): Int {
    return when {
        hrv > 80 -> 20
        hrv > 60 -> 40
        hrv > 40 -> 60
        hrv > 20 -> 80
        else -> 100
    }
}