package dev.infa.page3.SDK.viewModel

import dev.infa.page3.SDK.data.DeviceCapabilities
import dev.infa.page3.SDK.data.ExerciseData
import dev.infa.page3.SDK.data.ExerciseSummary

/**
 * iOS stub for HomeManager.
 * SDK disabled – all methods return safe defaults.
 */
actual class HomeManager {
    actual suspend fun getBatteryLevel(): Int? {
        return null
    }

    actual suspend fun fetchDeviceCapabilities(): DeviceCapabilities? {
        return null
    }

    actual suspend fun setSportsGoals(
        stepGoal: Int,
        calorieGoal: Int,
        distanceGoal: Int,
        sportMinuteGoal: Int,
        sleepMinuteGoal: Int
    ): Boolean {
        return false
    }

    actual fun startExercise(
        sportType: Int,
        onUpdate: (ExerciseData) -> Unit,
        onEnd: (ExerciseSummary) -> Unit,
        onError: (String) -> Unit
    ) {
        onError("SDK disabled on iOS")
    }

    actual fun pauseExercise() {
    }

    actual fun resumeExercise() {
    }

    actual fun endExercise() {
    }

    actual fun isExercising(): Boolean {
        return false
    }

    actual fun isPaused(): Boolean {
        return false
    }

    actual fun cleanup() {
    }
}