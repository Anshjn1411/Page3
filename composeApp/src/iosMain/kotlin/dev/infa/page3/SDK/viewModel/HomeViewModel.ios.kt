package dev.infa.page3.SDK.viewModel

import dev.infa.page3.SDK.data.DeviceCapabilities
import dev.infa.page3.SDK.data.ExerciseData
import dev.infa.page3.SDK.data.ExerciseSummary

actual class HomeManager {
    actual suspend fun getBatteryLevel(): Int? {
        TODO("Not yet implemented")
    }

    actual suspend fun fetchDeviceCapabilities(): DeviceCapabilities? {
        TODO("Not yet implemented")
    }

    actual suspend fun setSportsGoals(
        stepGoal: Int,
        calorieGoal: Int,
        distanceGoal: Int,
        sportMinuteGoal: Int,
        sleepMinuteGoal: Int
    ): Boolean {
        TODO("Not yet implemented")
    }

    actual fun startExercise(
        sportType: Int,
        onUpdate: (ExerciseData) -> Unit,
        onEnd: (ExerciseSummary) -> Unit,
        onError: (String) -> Unit
    ) {
    }

    actual fun pauseExercise() {
    }

    actual fun resumeExercise() {
    }

    actual fun endExercise() {
    }

    actual fun isExercising(): Boolean {
        TODO("Not yet implemented")
    }

    actual fun isPaused(): Boolean {
        TODO("Not yet implemented")
    }

    actual fun cleanup() {
    }
}