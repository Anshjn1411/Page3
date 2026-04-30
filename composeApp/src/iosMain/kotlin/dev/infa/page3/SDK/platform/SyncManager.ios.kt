// COMMENTED OUT: QCBandSDK disabled for iOS build
// Original implementation used cocoapods.QCBandSDK.* for BLE health data sync
// This is a dummy stub that returns empty/default data

package dev.infa.page3.SDK.platform

import dev.infa.page3.SDK.data.*
import dev.infa.page3.SDK.viewModel.IContinuousMonitoring
import dev.infa.page3.SDK.viewModel.IInstantMeasures
import dev.infa.page3.SDK.viewModel.ISyncManager
import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.timeIntervalSince1970

// ======================== SYNC MANAGER (STUB) ========================

actual class SyncManager : ISyncManager {

    actual override suspend fun syncTodaySteps(): Int {
        println("🍎 [STUB] syncTodaySteps (SDK disabled)")
        return 0
    }

    actual override suspend fun syncStepsByOffset(offset: Int): DayStepData {
        println("🍎 [STUB] syncStepsByOffset offset=$offset (SDK disabled)")
        return DayStepData(date = dateStringForOffset(offset))
    }

    actual override suspend fun syncHeartRate(offset: Int): HeartRateData {
        println("🍎 [STUB] syncHeartRate offset=$offset (SDK disabled)")
        return HeartRateData(date = dateStringForOffset(offset))
    }

    actual override suspend fun syncSpO2(offset: Int): SpO2Data {
        println("🍎 [STUB] syncSpO2 offset=$offset (SDK disabled)")
        return SpO2Data(date = dateStringForOffset(offset))
    }

    actual override suspend fun confirmBloodPressureSync() {
        println("🍎 [STUB] confirmBloodPressureSync (SDK disabled)")
    }

    actual override suspend fun syncHrv(offset: Int): HrvData {
        println("🍎 [STUB] syncHrv offset=$offset (SDK disabled)")
        return HrvData(date = dateStringForOffset(offset))
    }

    actual override suspend fun syncPressure(offset: Int): PressureData {
        println("🍎 [STUB] syncPressure offset=$offset (SDK disabled)")
        return PressureData(date = dateStringForOffset(offset))
    }

    actual override suspend fun syncAutoTemperature(offset: Int): TemperatureData {
        println("🍎 [STUB] syncAutoTemperature offset=$offset (SDK disabled)")
        return TemperatureData(date = dateStringForOffset(offset))
    }

    private fun currentTimeMillis(): Long {
        return (NSDate().timeIntervalSince1970 * 1000).toLong()
    }

    private fun dateStringForOffset(offset: Int): String {
        val timestamp = currentTimeMillis() - (offset * 24L * 60 * 60 * 1000)
        val date = NSDate(timeIntervalSinceReferenceDate = (timestamp / 1000.0) - 978307200.0)
        val formatter = NSDateFormatter()
        formatter.dateFormat = "yyyy-MM-dd"
        return formatter.stringFromDate(date)
    }
}

// ======================== INSTANT MEASURES (STUB) ========================

actual class InstantMeasures : IInstantMeasures {

    actual override suspend fun measureHeartRate(): String {
        println("🍎 [STUB] measureHeartRate (SDK disabled)")
        return "SDK disabled on iOS"
    }

    actual override suspend fun measureSpO2(): String {
        println("🍎 [STUB] measureSpO2 (SDK disabled)")
        return "SDK disabled on iOS"
    }

    actual override suspend fun measureBloodPressure(): String {
        println("🍎 [STUB] measureBloodPressure (SDK disabled)")
        return "SDK disabled on iOS"
    }

    actual override suspend fun measureHrv(): String {
        println("🍎 [STUB] measureHrv (SDK disabled)")
        return "SDK disabled on iOS"
    }

    actual override suspend fun measurePressure(): String {
        println("🍎 [STUB] measurePressure (SDK disabled)")
        return "SDK disabled on iOS"
    }

    actual override suspend fun measureTemperature(): String {
        println("🍎 [STUB] measureTemperature (SDK disabled)")
        return "SDK disabled on iOS"
    }

    actual override suspend fun measureOneClick(): OneClickResult {
        println("🍎 [STUB] measureOneClick (SDK disabled)")
        return OneClickResult()
    }

    actual override suspend fun measureHeartRateRawData(seconds: Int): RawDataResult {
        println("🍎 [STUB] measureHeartRateRawData (SDK disabled)")
        return RawDataResult()
    }

    actual override suspend fun measureBloodOxygenRawData(seconds: Int): RawDataResult {
        println("🍎 [STUB] measureBloodOxygenRawData (SDK disabled)")
        return RawDataResult()
    }

    actual override suspend fun measureOneClickContinuous(
        onUpdate: (OneClickResult) -> Unit,
        onComplete: () -> Unit
    ) {
        println("🍎 [STUB] measureOneClickContinuous (SDK disabled)")
        onComplete()
    }
}

// ======================== CONTINUOUS MONITORING (STUB) ========================

actual class ContinuousMonitoring : IContinuousMonitoring {

    actual override suspend fun toggleHeartRateMonitoring(
        enabled: Boolean,
        interval: Int
    ): String {
        println("🍎 [STUB] toggleHeartRateMonitoring (SDK disabled)")
        return "SDK disabled on iOS"
    }

    actual override suspend fun toggleHrvMonitoring(
        enabled: Boolean,
        interval: Int
    ): String {
        println("🍎 [STUB] toggleHrvMonitoring (SDK disabled)")
        return "SDK disabled on iOS"
    }

    actual override suspend fun toggleSpO2Monitoring(
        enabled: Boolean,
        interval: Int
    ): String {
        println("🍎 [STUB] toggleSpO2Monitoring (SDK disabled)")
        return "SDK disabled on iOS"
    }

    actual override suspend fun toggleIntervalSpO2Monitoring(
        enabled: Boolean,
        interval: Int
    ): String {
        println("🍎 [STUB] toggleIntervalSpO2Monitoring (SDK disabled)")
        return "SDK disabled on iOS"
    }

    actual override suspend fun toggleBloodPressureMonitoring(
        enabled: Boolean,
        startEndTime: StartEndTimeEntity,
        interval: Int
    ): String {
        println("🍎 [STUB] toggleBloodPressureMonitoring (SDK disabled)")
        return "SDK disabled on iOS"
    }

    actual override suspend fun togglePressureMonitoring(enabled: Boolean): String {
        println("🍎 [STUB] togglePressureMonitoring (SDK disabled)")
        return "SDK disabled on iOS"
    }

    actual override suspend fun toggleTemperatureMonitoring(
        enabled: Boolean,
        interval: Int
    ): String {
        println("🍎 [STUB] toggleTemperatureMonitoring (SDK disabled)")
        return "SDK disabled on iOS"
    }
}