package dev.infa.page3.SDK.platform

import dev.infa.page3.SDK.data.DayStepData
import dev.infa.page3.SDK.data.HeartRateData
import dev.infa.page3.SDK.data.HrvData
import dev.infa.page3.SDK.data.OneClickResult
import dev.infa.page3.SDK.data.PressureData
import dev.infa.page3.SDK.data.RawDataResult
import dev.infa.page3.SDK.data.SpO2Data
import dev.infa.page3.SDK.data.StartEndTimeEntity
import dev.infa.page3.SDK.data.TemperatureData
import dev.infa.page3.SDK.viewModel.IContinuousMonitoring
import dev.infa.page3.SDK.viewModel.IInstantMeasures
import dev.infa.page3.SDK.viewModel.ISyncManager

actual class SyncManager : ISyncManager {
    actual override suspend fun syncTodaySteps(): Int {
        TODO("Not yet implemented")
    }

    actual override suspend fun syncStepsByOffset(offset: Int): DayStepData {
        TODO("Not yet implemented")
    }

    actual override suspend fun syncHeartRate(offset: Int): HeartRateData {
        TODO("Not yet implemented")
    }

    actual override suspend fun syncSpO2(offset: Int): SpO2Data {
        TODO("Not yet implemented")
    }

    actual override suspend fun confirmBloodPressureSync() {
        TODO("Not yet implemented")
    }

    actual override suspend fun syncHrv(offset: Int): HrvData {
        TODO("Not yet implemented")
    }

    actual override suspend fun syncPressure(offset: Int): PressureData {
        TODO("Not yet implemented")
    }

    actual override suspend fun syncAutoTemperature(offset: Int): TemperatureData {
        TODO("Not yet implemented")
    }
}

actual class InstantMeasures : IInstantMeasures {
    actual override suspend fun measureHeartRate(): String {
        TODO("Not yet implemented")
    }

    actual override suspend fun measureSpO2(): String {
        TODO("Not yet implemented")
    }

    actual override suspend fun measureBloodPressure(): String {
        TODO("Not yet implemented")
    }

    actual override suspend fun measureHrv(): String {
        TODO("Not yet implemented")
    }

    actual override suspend fun measurePressure(): String {
        TODO("Not yet implemented")
    }

    actual override suspend fun measureTemperature(): String {
        TODO("Not yet implemented")
    }

    actual override suspend fun measureOneClick(): OneClickResult {
        TODO("Not yet implemented")
    }

    actual override suspend fun measureHeartRateRawData(seconds: Int): RawDataResult {
        TODO("Not yet implemented")
    }

    actual override suspend fun measureBloodOxygenRawData(seconds: Int): RawDataResult {
        TODO("Not yet implemented")
    }
}

actual class ContinuousMonitoring : IContinuousMonitoring {
    actual override suspend fun toggleHeartRateMonitoring(
        enabled: Boolean,
        interval: Int
    ): String {
        TODO("Not yet implemented")
    }

    actual override suspend fun toggleHrvMonitoring(
        enabled: Boolean,
        interval: Int
    ): String {
        TODO("Not yet implemented")
    }

    actual override suspend fun toggleSpO2Monitoring(
        enabled: Boolean,
        interval: Int
    ): String {
        TODO("Not yet implemented")
    }

    actual override suspend fun toggleIntervalSpO2Monitoring(
        enabled: Boolean,
        interval: Int
    ): String {
        TODO("Not yet implemented")
    }

    actual override suspend fun toggleBloodPressureMonitoring(
        enabled: Boolean,
        startEndTime: StartEndTimeEntity,
        interval: Int
    ): String {
        TODO("Not yet implemented")
    }

    actual override suspend fun togglePressureMonitoring(enabled: Boolean): String {
        TODO("Not yet implemented")
    }

    actual override suspend fun toggleTemperatureMonitoring(
        enabled: Boolean,
        interval: Int
    ): String {
        TODO("Not yet implemented")
    }
}