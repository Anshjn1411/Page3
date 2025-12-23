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
import dev.infa.page3.data.remote.CacheManager

expect class SyncManager: ISyncManager {
    override suspend fun syncTodaySteps(): Int
    override suspend fun syncStepsByOffset(offset: Int): DayStepData
    override suspend fun syncHeartRate(offset: Int): HeartRateData
    override suspend fun syncSpO2(offset: Int): SpO2Data
    override suspend fun confirmBloodPressureSync()
    override suspend fun syncHrv(offset: Int): HrvData
    override suspend fun syncPressure(offset: Int): PressureData
    override suspend fun syncAutoTemperature(offset: Int): TemperatureData
}

expect class InstantMeasures: IInstantMeasures {
    override suspend fun measureHeartRate(): String
    override suspend fun measureSpO2(): String
    override suspend fun measureBloodPressure(): String
    override suspend fun measureHrv(): String
    override suspend fun measurePressure(): String
    override suspend fun measureTemperature(): String
    override suspend fun measureOneClick(): OneClickResult
    override suspend fun measureHeartRateRawData(seconds: Int): RawDataResult
    override suspend fun measureBloodOxygenRawData(seconds: Int): RawDataResult
}

expect class ContinuousMonitoring: IContinuousMonitoring {
    override suspend fun toggleHeartRateMonitoring(
        enabled: Boolean,
        interval: Int
    ): String

    override suspend fun toggleHrvMonitoring(
        enabled: Boolean,
        interval: Int
    ): String

    override suspend fun toggleSpO2Monitoring(
        enabled: Boolean,
        interval: Int
    ): String

    override suspend fun toggleIntervalSpO2Monitoring(
        enabled: Boolean,
        interval: Int
    ): String

    override suspend fun toggleBloodPressureMonitoring(
        enabled: Boolean,
        startEndTime: StartEndTimeEntity,
        interval: Int
    ): String

    override suspend fun togglePressureMonitoring(enabled: Boolean): String
    override suspend fun toggleTemperatureMonitoring(
        enabled: Boolean,
        interval: Int
    ): String
}