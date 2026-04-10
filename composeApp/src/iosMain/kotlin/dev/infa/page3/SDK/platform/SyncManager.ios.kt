@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class, kotlinx.cinterop.BetaInteropApi::class)

package dev.infa.page3.SDK.platform

import cocoapods.QCBandSDK.QCBloodOxygenModel
import cocoapods.QCBandSDK.QCBloodGlucoseHeartRateRawModel
import cocoapods.QCBandSDK.QCHRVModel
import cocoapods.QCBandSDK.QCMeasuringTypeBloodOxygen
import cocoapods.QCBandSDK.QCMeasuringTypeBloodOxygenRaw
import cocoapods.QCBandSDK.QCMeasuringTypeBloodPressue
import cocoapods.QCBandSDK.QCMeasuringTypeBodyTemperature
import cocoapods.QCBandSDK.QCMeasuringTypeHRV
import cocoapods.QCBandSDK.QCMeasuringTypeHeartRate
import cocoapods.QCBandSDK.QCMeasuringTypeHeartRateRaw
import cocoapods.QCBandSDK.QCMeasuringTypeOneKeyMeasure
import cocoapods.QCBandSDK.QCMeasuringTypeStress
import cocoapods.QCBandSDK.QCRealOneKeyMeasureHeartRateModel
import cocoapods.QCBandSDK.QCSchedualHeartRateModel
import cocoapods.QCBandSDK.QCSDKCmdCreator
import cocoapods.QCBandSDK.QCSDKManager
import cocoapods.QCBandSDK.QCSportModel
import cocoapods.QCBandSDK.QCStressModel
import cocoapods.QCBandSDK.QCTemperatureModel
import cocoapods.QCBandSDK.SchedualInfoTypeBodyTemperator
import dev.infa.page3.SDK.data.*
import dev.infa.page3.SDK.viewModel.IContinuousMonitoring
import dev.infa.page3.SDK.viewModel.IInstantMeasures
import dev.infa.page3.SDK.viewModel.ISyncManager
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout
import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSNumber
import platform.Foundation.timeIntervalSince1970
import kotlin.coroutines.resume

// ======================== SYNC MANAGER ========================

actual class SyncManager : ISyncManager {

    private val mutex = Mutex()

    actual override suspend fun syncTodaySteps(): Int = mutex.withLock {
        withTimeout(15_000L) {
            suspendCancellableCoroutine { cont ->
                QCSDKCmdCreator.getCurrentSportSucess({ sport ->
                    if (sport != null) {
                        val typedSport = sport as? QCSportModel
                        val steps = typedSport?.totalStepCount?.toInt() ?: 0
                        println("🍎 Today steps: $steps")
                        if (cont.isActive) cont.resume(steps)
                    } else {
                        if (cont.isActive) cont.resume(0)
                    }
                }, failed = {
                    println("🍎 Failed to get today steps")
                    if (cont.isActive) cont.resume(0)
                })
            }
        }
    }

    actual override suspend fun syncStepsByOffset(offset: Int): DayStepData = mutex.withLock {
        withTimeout(15_000L) {
            suspendCancellableCoroutine { cont ->
                QCSDKCmdCreator.getSportDetailDataByDay(
                    offset.toLong(),
                    sportDatas = { sports ->
                        if (sports != null) {
                            @Suppress("UNCHECKED_CAST")
                            val sportList = sports as? List<QCSportModel> ?: emptyList()
                            var totalSteps = 0
                            var totalCalories = 0
                            var totalDistance = 0
                            val hourlyData = mutableListOf<HourlyStepData>()

                            for (model in sportList) {
                                totalSteps += model.totalStepCount.toInt()
                                totalCalories += model.calories.toInt()
                                totalDistance += model.distance.toInt()
                            }

                            // Group by hour for hourly data
                            for (hour in 0..23) {
                                val hourSports = sportList.filter { sport ->
                                    sport.happenDate?.let {
                                        try {
                                            val parts = it.split(" ")
                                            if (parts.size > 1) {
                                                parts[1].split(":")[0].toInt() == hour
                                            } else false
                                        } catch (_: Exception) { false }
                                    } ?: false
                                }
                                if (hourSports.isNotEmpty()) {
                                    hourlyData.add(
                                        HourlyStepData(
                                            hour = hour,
                                            steps = hourSports.sumOf { it.totalStepCount.toInt() },
                                            calories = hourSports.sumOf { it.calories.toInt() }
                                        )
                                    )
                                }
                            }

                            println("🍎 Steps offset $offset: total=$totalSteps")
                            if (cont.isActive) {
                                cont.resume(
                                    DayStepData(
                                        date = dateStringForOffset(offset),
                                        totalSteps = totalSteps,
                                        totalCalories = totalCalories,
                                        totalDistance = totalDistance,
                                        hourlyData = hourlyData
                                    )
                                )
                            }
                        } else {
                            if (cont.isActive) cont.resume(DayStepData(date = dateStringForOffset(offset)))
                        }
                    },
                    fail = {
                        println("🍎 Failed to get steps for offset $offset")
                        if (cont.isActive) cont.resume(DayStepData(date = dateStringForOffset(offset)))
                    }
                )
            }
        }
    }

    actual override suspend fun syncHeartRate(offset: Int): HeartRateData = mutex.withLock {
        withTimeout(15_000L) {
            suspendCancellableCoroutine { cont ->
                val dayIndexes = listOf(NSNumber(int = offset))
                QCSDKCmdCreator.getSchedualHeartRateDataWithDayIndexs(dayIndexes, success = { models ->
                    if (models != null) {
                        @Suppress("UNCHECKED_CAST")
                        val hrModels = models as? List<QCSchedualHeartRateModel> ?: emptyList()

                        // heartRates is NSArray<NSNumber> of values, one per N-minute interval
                        val hrEntries = mutableListOf<HeartRateEntry>()
                        for (model in hrModels) {
                            val heartRatesArray = model.heartRates ?: continue
                            val interval = model.secondInterval
                            @Suppress("UNCHECKED_CAST")
                            val rates = heartRatesArray as? List<NSNumber> ?: continue
                            for ((i, nsNum) in rates.withIndex()) {
                                val hr = nsNum.intValue
                                if (hr > 0) {
                                    hrEntries.add(
                                        HeartRateEntry(
                                            timestamp = currentTimeMillis(),
                                            heartRate = hr,
                                            minuteOfDay = (i * interval / 60).toInt()
                                        )
                                    )
                                }
                            }
                        }

                        val validHrs = hrEntries.map { it.heartRate }.filter { it > 0 }
                        val avg = if (validHrs.isNotEmpty()) validHrs.average().toInt() else 0
                        val max = validHrs.maxOrNull() ?: 0
                        val min = validHrs.minOrNull() ?: 0

                        println("🍎 Heart rate offset $offset: ${hrEntries.size} entries, avg=$avg")
                        if (cont.isActive) {
                            cont.resume(
                                HeartRateData(
                                    date = dateStringForOffset(offset),
                                    heartRateValues = hrEntries,
                                    averageHeartRate = avg,
                                    maxHeartRate = max,
                                    minHeartRate = min
                                )
                            )
                        }
                    } else {
                        if (cont.isActive) cont.resume(HeartRateData(date = dateStringForOffset(offset)))
                    }
                }, fail = {
                    println("🍎 Failed to get heart rate for offset $offset")
                    if (cont.isActive) cont.resume(HeartRateData(date = dateStringForOffset(offset)))
                })
            }
        }
    }

    actual override suspend fun syncSpO2(offset: Int): SpO2Data = mutex.withLock {
        withTimeout(15_000L) {
            suspendCancellableCoroutine { cont ->
                QCSDKCmdCreator.getBloodOxygenDataByDayIndex(
                    offset.toLong(),
                    finished = { data, error ->
                        if (error == null && data != null) {
                            @Suppress("UNCHECKED_CAST")
                            val boModels = data as? List<QCBloodOxygenModel> ?: emptyList()

                            val spo2Entries = boModels.mapNotNull { model ->
                                // soa2 is a CGFloat
                                val value = model.soa2.toInt()
                                if (value > 0) {
                                    SpO2Entry(
                                        timestamp = currentTimeMillis(),
                                        spo2Value = value,
                                        hourOfDay = 0
                                    )
                                } else null
                            }

                            val validValues = spo2Entries.map { it.spo2Value }.filter { it > 0 }
                            val avg = if (validValues.isNotEmpty()) validValues.average().toInt() else 0
                            val max = validValues.maxOrNull() ?: 0
                            val min = validValues.minOrNull() ?: 0

                            println("🍎 SpO2 offset $offset: ${spo2Entries.size} entries")
                            if (cont.isActive) {
                                cont.resume(
                                    SpO2Data(
                                        date = dateStringForOffset(offset),
                                        spo2Values = spo2Entries,
                                        averageSpO2 = avg,
                                        maxSpO2 = max,
                                        minSpO2 = min
                                    )
                                )
                            }
                        } else {
                            if (cont.isActive) cont.resume(SpO2Data(date = dateStringForOffset(offset)))
                        }
                    }
                )
            }
        }
    }

    actual override suspend fun confirmBloodPressureSync() {
        // No-op on iOS
        println("🍎 Blood pressure sync confirmed (no-op on iOS)")
    }

    actual override suspend fun syncHrv(offset: Int): HrvData = mutex.withLock {
        withTimeout(15_000L) {
            suspendCancellableCoroutine { cont ->
                val dayIndexes = listOf(NSNumber(int = offset))
                QCSDKCmdCreator.getSchedualHRVDataWithDates(dayIndexes, finished = { data, error ->
                    if (error == null && data != null) {
                        @Suppress("UNCHECKED_CAST")
                        val hrvModels = data as? List<QCHRVModel> ?: emptyList()

                        // hrv property is NSArray<NSNumber> of values
                        val hrvEntries = mutableListOf<HrvEntry>()
                        for (model in hrvModels) {
                            val hrvArray = model.hrv() ?: continue
                            val interval = model.secondInterval
                            @Suppress("UNCHECKED_CAST")
                            val values = hrvArray as? List<NSNumber> ?: continue
                            for ((i, nsNum) in values.withIndex()) {
                                val value = nsNum.intValue
                                if (value > 0) {
                                    hrvEntries.add(
                                        HrvEntry(
                                            timestamp = currentTimeMillis(),
                                            hrvValue = value,
                                            minuteOfDay = (i * interval / 60).toInt()
                                        )
                                    )
                                }
                            }
                        }

                        val validValues = hrvEntries.map { it.hrvValue }.filter { it > 0 }
                        val avg = if (validValues.isNotEmpty()) validValues.average().toInt() else 0
                        val max = validValues.maxOrNull() ?: 0
                        val min = validValues.minOrNull() ?: 0

                        println("🍎 HRV offset $offset: ${hrvEntries.size} entries")
                        if (cont.isActive) {
                            cont.resume(
                                HrvData(
                                    date = dateStringForOffset(offset),
                                    hrvValues = hrvEntries,
                                    averageHrv = avg,
                                    maxHrv = max,
                                    minHrv = min
                                )
                            )
                        }
                    } else {
                        if (cont.isActive) cont.resume(HrvData(date = dateStringForOffset(offset)))
                    }
                })
            }
        }
    }

    actual override suspend fun syncPressure(offset: Int): PressureData = mutex.withLock {
        withTimeout(15_000L) {
            suspendCancellableCoroutine { cont ->
                val dayIndexes = listOf(NSNumber(int = offset))
                QCSDKCmdCreator.getSchedualStressDataWithDates(dayIndexes, finished = { data, error ->
                    if (error == null && data != null) {
                        @Suppress("UNCHECKED_CAST")
                        val stressModels = data as? List<QCStressModel> ?: emptyList()

                        // stresses is NSArray<NSNumber> of values
                        val pressureEntries = mutableListOf<PressureEntry>()
                        for (model in stressModels) {
                            val stressArray = model.stresses ?: continue
                            val interval = model.secondInterval
                            @Suppress("UNCHECKED_CAST")
                            val values = stressArray as? List<NSNumber> ?: continue
                            for ((i, nsNum) in values.withIndex()) {
                                val value = nsNum.floatValue
                                if (value > 0f) {
                                    pressureEntries.add(
                                        PressureEntry(
                                            minuteOfDay = (i * interval / 60).toInt(),
                                            pressureValue = value
                                        )
                                    )
                                }
                            }
                        }

                        val validValues = pressureEntries.map { it.pressureValue }.filter { it > 0f }
                        val avg = if (validValues.isNotEmpty()) validValues.average().toFloat() else 0f

                        println("🍎 Pressure offset $offset: ${pressureEntries.size} entries")
                        if (cont.isActive) {
                            cont.resume(PressureData(date = dateStringForOffset(offset), entries = pressureEntries, averagePressure = avg))
                        }
                    } else {
                        if (cont.isActive) cont.resume(PressureData(date = dateStringForOffset(offset)))
                    }
                })
            }
        }
    }

    actual override suspend fun syncAutoTemperature(offset: Int): TemperatureData = mutex.withLock {
        withTimeout(15_000L) {
            suspendCancellableCoroutine { cont ->
                QCSDKCmdCreator.getSchedualTemperatureDataByDayIndex(
                    offset.toLong(),
                    finished = { temperatureList, error ->
                        if (error == null && temperatureList != null) {
                            @Suppress("UNCHECKED_CAST")
                            val tempModels = temperatureList as? List<QCTemperatureModel> ?: emptyList()

                            val tempEntries = tempModels.mapNotNull { model ->
                                // temperature is Float32, in 0.1 degree C units
                                val value = model.temperature
                                if (value > 0f) {
                                    TemperatureEntry(
                                        minuteOfDay = 0,
                                        temperature = value / 10f
                                    )
                                } else null
                            }

                            val validValues = tempEntries.map { it.temperature }.filter { it > 0f }
                            val avg = if (validValues.isNotEmpty()) validValues.average().toFloat() else 0f
                            val max = validValues.maxOrNull() ?: 0f
                            val min = validValues.minOrNull() ?: 0f

                            println("🍎 Temperature offset $offset: ${tempEntries.size} entries")
                            if (cont.isActive) {
                                cont.resume(
                                    TemperatureData(
                                        date = dateStringForOffset(offset),
                                        entries = tempEntries,
                                        averageTemp = avg,
                                        maxTemp = max,
                                        minTemp = min
                                    )
                                )
                            }
                        } else {
                            if (cont.isActive) cont.resume(TemperatureData(date = dateStringForOffset(offset)))
                        }
                    }
                )
            }
        }
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

// ======================== INSTANT MEASURES ========================

actual class InstantMeasures : IInstantMeasures {

    private val mutex = Mutex()

    actual override suspend fun measureHeartRate(): String = mutex.withLock {
        withTimeout(60_000L) {
            suspendCancellableCoroutine { cont ->
                QCSDKManager.shareInstance().startToMeasuringWithOperateType(
                    QCMeasuringTypeHeartRate,
                    measuringHandle = { result ->
                        if (result != null) println("🍎 HR measuring: $result")
                    },
                    completedHandle = { isSuccess, result, _ ->
                        if (!cont.isActive) return@startToMeasuringWithOperateType
                        if (isSuccess) {
                            val hr = (result as? NSNumber)?.intValue ?: 0
                            cont.resume("Heart Rate: $hr bpm")
                        } else {
                            cont.resume("Heart rate measurement failed")
                        }
                    }
                )
            }
        }
    }

    actual override suspend fun measureSpO2(): String = mutex.withLock {
        withTimeout(60_000L) {
            suspendCancellableCoroutine { cont ->
                QCSDKManager.shareInstance().startToMeasuringWithOperateType(
                    QCMeasuringTypeBloodOxygen,
                    measuringHandle = { result ->
                        if (result != null) println("🍎 SpO2 measuring: $result")
                    },
                    completedHandle = { isSuccess, result, _ ->
                        if (!cont.isActive) return@startToMeasuringWithOperateType
                        if (isSuccess) {
                            val spo2 = (result as? NSNumber)?.intValue ?: 0
                            cont.resume("SpO2: $spo2%")
                        } else {
                            cont.resume("SpO2 measurement failed")
                        }
                    }
                )
            }
        }
    }

    actual override suspend fun measureBloodPressure(): String = mutex.withLock {
        withTimeout(60_000L) {
            suspendCancellableCoroutine { cont ->
                QCSDKManager.shareInstance().startToMeasuringWithOperateType(
                    QCMeasuringTypeBloodPressue,
                    measuringHandle = { result ->
                        if (result != null) println("🍎 BP measuring: $result")
                    },
                    completedHandle = { isSuccess, result, _ ->
                        if (!cont.isActive) return@startToMeasuringWithOperateType
                        if (isSuccess) {
                            cont.resume("Blood Pressure: $result")
                        } else {
                            cont.resume("Blood pressure measurement failed")
                        }
                    }
                )
            }
        }
    }

    actual override suspend fun measureHrv(): String = mutex.withLock {
        withTimeout(60_000L) {
            suspendCancellableCoroutine { cont ->
                QCSDKManager.shareInstance().startToMeasuringWithOperateType(
                    QCMeasuringTypeHRV,
                    measuringHandle = { result ->
                        if (result != null) println("🍎 HRV measuring: $result")
                    },
                    completedHandle = { isSuccess, result, _ ->
                        if (!cont.isActive) return@startToMeasuringWithOperateType
                        if (isSuccess) {
                            val hrv = (result as? NSNumber)?.intValue ?: 0
                            cont.resume("HRV: $hrv ms")
                        } else {
                            cont.resume("HRV measurement failed")
                        }
                    }
                )
            }
        }
    }

    actual override suspend fun measurePressure(): String = mutex.withLock {
        withTimeout(60_000L) {
            suspendCancellableCoroutine { cont ->
                QCSDKManager.shareInstance().startToMeasuringWithOperateType(
                    QCMeasuringTypeStress,
                    measuringHandle = { result ->
                        if (result != null) println("🍎 Stress measuring: $result")
                    },
                    completedHandle = { isSuccess, result, _ ->
                        if (!cont.isActive) return@startToMeasuringWithOperateType
                        if (isSuccess) {
                            val stress = (result as? NSNumber)?.intValue ?: 0
                            cont.resume("Stress: $stress")
                        } else {
                            cont.resume("Stress measurement failed")
                        }
                    }
                )
            }
        }
    }

    actual override suspend fun measureTemperature(): String = mutex.withLock {
        withTimeout(60_000L) {
            suspendCancellableCoroutine { cont ->
                QCSDKManager.shareInstance().startToMeasuringWithOperateType(
                    QCMeasuringTypeBodyTemperature,
                    measuringHandle = { result ->
                        if (result != null) println("🍎 Temp measuring: $result")
                    },
                    completedHandle = { isSuccess, result, _ ->
                        if (!cont.isActive) return@startToMeasuringWithOperateType
                        if (isSuccess) {
                            val temp = (result as? NSNumber)?.doubleValue ?: 0.0
                            val tempDisplay = temp / 10.0
                            // Kotlin/Native doesn't have String.format, use manual formatting
                            val tempStr = ((tempDisplay * 10).toInt() / 10.0).toString()
                            cont.resume("Temperature: ${tempStr}°C")
                        } else {
                            cont.resume("Temperature measurement failed")
                        }
                    }
                )
            }
        }
    }

    actual override suspend fun measureOneClick(): OneClickResult = mutex.withLock {
        withTimeout(120_000L) {
            suspendCancellableCoroutine { cont ->
                QCSDKManager.shareInstance().startToMeasuringWithOperateType(
                    QCMeasuringTypeOneKeyMeasure,
                    measuringHandle = { result ->
                        if (result != null) println("🍎 One-click measuring: $result")
                    },
                    completedHandle = { isSuccess, result, _ ->
                        if (!cont.isActive) return@startToMeasuringWithOperateType
                        if (isSuccess && result != null) {
                            val m = result as? QCRealOneKeyMeasureHeartRateModel
                            cont.resume(
                                OneClickResult(
                                    heartRate = m?.heartRateValue?.toInt() ?: 0,
                                    bloodOxygen = 0, // not directly available in this model
                                    systolic = m?.bloodPressureSbp?.toInt() ?: 0,
                                    diastolic = m?.bloodPressureDbp?.toInt() ?: 0,
                                    hrv = m?.heartRateHRV?.toInt() ?: 0,
                                    stress = m?.stress?.toInt() ?: 0,
                                    temperature = (m?.temp?.toFloat() ?: 0f) / 10f,
                                    rri = m?.rri?.toInt() ?: 0
                                )
                            )
                        } else {
                            cont.resume(OneClickResult())
                        }
                    }
                )
            }
        }
    }

    actual override suspend fun measureHeartRateRawData(seconds: Int): RawDataResult = mutex.withLock {
        withTimeout((seconds * 1000L) + 10_000L) {
            suspendCancellableCoroutine { cont ->
                QCSDKManager.shareInstance().startToMeasuringWithOperateType(
                    QCMeasuringTypeHeartRateRaw,
                    timeout = seconds.toLong(),
                    measuringHandle = { result ->
                        if (result != null) println("🍎 HR Raw: $result")
                    },
                    completedHandle = { isSuccess, result, _ ->
                        if (!cont.isActive) return@startToMeasuringWithOperateType
                        if (isSuccess && result != null) {
                            val m = result as? QCBloodGlucoseHeartRateRawModel
                            cont.resume(
                                RawDataResult(
                                    heartRate = m?.value?.toInt() ?: 0,
                                    bloodOxygen = 0,
                                    ppgCount = m?.ppgCount?.toInt() ?: 0,
                                    redLightPpgL = m?.redLightPpgL?.toInt() ?: 0,
                                    redLightPpgH = m?.redLightPpgH?.toInt() ?: 0,
                                    infraredPpgL = m?.InfraredPpgL?.toInt() ?: 0,
                                    infraredPpgH = m?.InfraredPpgH?.toInt() ?: 0,
                                    greenLightPpgL = 0,
                                    greenLightPpgH = 0
                                )
                            )
                        } else {
                            cont.resume(RawDataResult())
                        }
                    }
                )
            }
        }
    }

    actual override suspend fun measureBloodOxygenRawData(seconds: Int): RawDataResult = mutex.withLock {
        withTimeout((seconds * 1000L) + 10_000L) {
            suspendCancellableCoroutine { cont ->
                QCSDKManager.shareInstance().startToMeasuringWithOperateType(
                    QCMeasuringTypeBloodOxygenRaw,
                    timeout = seconds.toLong(),
                    measuringHandle = { result ->
                        if (result != null) println("🍎 BO Raw: $result")
                    },
                    completedHandle = { isSuccess, result, _ ->
                        if (!cont.isActive) return@startToMeasuringWithOperateType
                        if (isSuccess && result != null) {
                            val m = result as? QCBloodGlucoseHeartRateRawModel
                            cont.resume(
                                RawDataResult(
                                    heartRate = 0,
                                    bloodOxygen = m?.value?.toInt() ?: 0,
                                    ppgCount = m?.ppgCount?.toInt() ?: 0,
                                    redLightPpgL = m?.redLightPpgL?.toInt() ?: 0,
                                    redLightPpgH = m?.redLightPpgH?.toInt() ?: 0,
                                    infraredPpgL = m?.InfraredPpgL?.toInt() ?: 0,
                                    infraredPpgH = m?.InfraredPpgH?.toInt() ?: 0,
                                    greenLightPpgL = 0,
                                    greenLightPpgH = 0
                                )
                            )
                        } else {
                            cont.resume(RawDataResult())
                        }
                    }
                )
            }
        }
    }

    actual override suspend fun measureOneClickContinuous(
        onUpdate: (OneClickResult) -> Unit,
        onComplete: () -> Unit
    ) {
        withTimeout(120_000L) {
            suspendCancellableCoroutine { cont ->
                QCSDKManager.shareInstance().startToMeasuringWithOperateType(
                    QCMeasuringTypeOneKeyMeasure,
                    measuringHandle = { result ->
                        if (result != null) {
                            val m = result as? QCRealOneKeyMeasureHeartRateModel
                            if (m != null) {
                                onUpdate(
                                    OneClickResult(
                                        heartRate = m.heartRateValue.toInt(),
                                        bloodOxygen = 0,
                                        systolic = m.bloodPressureSbp.toInt(),
                                        diastolic = m.bloodPressureDbp.toInt(),
                                        hrv = m.heartRateHRV.toInt(),
                                        stress = m.stress.toInt(),
                                        temperature = m.temp.toFloat() / 10f,
                                        rri = m.rri.toInt()
                                    )
                                )
                            }
                        }
                    },
                    completedHandle = { _, _, _ ->
                        if (!cont.isActive) return@startToMeasuringWithOperateType
                        onComplete()
                        cont.resume(Unit)
                    }
                )
            }
        }
    }
}

// ======================== CONTINUOUS MONITORING ========================

actual class ContinuousMonitoring : IContinuousMonitoring {

    private val mutex = Mutex()

    actual override suspend fun toggleHeartRateMonitoring(
        enabled: Boolean,
        interval: Int
    ): String = mutex.withLock {
        withTimeout(15_000L) {
            suspendCancellableCoroutine { cont ->
                val validInterval = if (interval in listOf(5, 10, 15, 20, 30, 60)) interval else 5

                QCSDKCmdCreator.setSchedualHeartRateStatus(
                    enabled,
                    timeInterval = validInterval.toLong(),
                    success = {
                        if (!cont.isActive) return@setSchedualHeartRateStatus
                        val status = if (enabled) "enabled with $validInterval min interval" else "disabled"
                        cont.resume("Heart rate monitoring $status")
                    },
                    fail = {
                        if (!cont.isActive) return@setSchedualHeartRateStatus
                        cont.resume("Heart rate monitoring failed")
                    }
                )
            }
        }
    }

    actual override suspend fun toggleHrvMonitoring(
        enabled: Boolean,
        interval: Int
    ): String = mutex.withLock {
        withTimeout(15_000L) {
            suspendCancellableCoroutine { cont ->
                QCSDKCmdCreator.setSchedualHRVStatus(
                    enabled,
                    finshed = { error ->
                        if (!cont.isActive) return@setSchedualHRVStatus
                        if (error == null) {
                            val status = if (enabled) "enabled" else "disabled"
                            cont.resume("HRV monitoring $status")
                        } else {
                            cont.resume("HRV monitoring failed")
                        }
                    }
                )
            }
        }
    }

    actual override suspend fun toggleSpO2Monitoring(
        enabled: Boolean,
        interval: Int
    ): String = mutex.withLock {
        withTimeout(15_000L) {
            suspendCancellableCoroutine { cont ->
                QCSDKCmdCreator.setSchedualBOInfoOn(
                    enabled,
                    success = { _ ->
                        if (!cont.isActive) return@setSchedualBOInfoOn
                        val status = if (enabled) "enabled" else "disabled"
                        cont.resume("SpO2 monitoring $status")
                    },
                    fail = {
                        if (!cont.isActive) return@setSchedualBOInfoOn
                        cont.resume("SpO2 monitoring failed")
                    }
                )
            }
        }
    }

    actual override suspend fun toggleIntervalSpO2Monitoring(
        enabled: Boolean,
        interval: Int
    ): String = mutex.withLock {
        withTimeout(15_000L) {
            suspendCancellableCoroutine { cont ->
                QCSDKCmdCreator.setSchedualBOInfoOn(
                    enabled,
                    timeInterval = interval.toLong(),
                    success = {
                        if (!cont.isActive) return@setSchedualBOInfoOn
                        val status = if (enabled) "enabled with $interval min interval" else "disabled"
                        cont.resume("Interval SpO2 monitoring $status")
                    },
                    fail = {
                        if (!cont.isActive) return@setSchedualBOInfoOn
                        cont.resume("Interval SpO2 monitoring failed")
                    }
                )
            }
        }
    }

    actual override suspend fun toggleBloodPressureMonitoring(
        enabled: Boolean,
        startEndTime: StartEndTimeEntity,
        interval: Int
    ): String = mutex.withLock {
        withTimeout(15_000L) {
            suspendCancellableCoroutine { cont ->
                val multiple = if (interval > 0) interval else 60
                val beginTimeStr = "${padZero(startEndTime.startHour)}:${padZero(startEndTime.startMinute)}"
                val endTimeStr = "${padZero(startEndTime.endHour)}:${padZero(startEndTime.endMinute)}"

                QCSDKCmdCreator.setSchedualBPInfoOn(
                    enabled,
                    beginTime = beginTimeStr,
                    endTime = endTimeStr,
                    minuteInterval = multiple.toLong(),
                    success = { _, _, _, _ ->
                        if (!cont.isActive) return@setSchedualBPInfoOn
                        val status = if (enabled) "enabled with $multiple min interval" else "disabled"
                        cont.resume("Blood pressure monitoring $status")
                    },
                    fail = {
                        if (!cont.isActive) return@setSchedualBPInfoOn
                        cont.resume("Blood pressure monitoring failed")
                    }
                )
            }
        }
    }

    actual override suspend fun togglePressureMonitoring(enabled: Boolean): String = mutex.withLock {
        withTimeout(15_000L) {
            suspendCancellableCoroutine { cont ->
                QCSDKCmdCreator.setSchedualStressStatus(
                    enabled,
                    finshed = { error ->
                        if (!cont.isActive) return@setSchedualStressStatus
                        if (error == null) {
                            val status = if (enabled) "enabled" else "disabled"
                            cont.resume("Stress monitoring $status")
                        } else {
                            cont.resume("Stress monitoring failed")
                        }
                    }
                )
            }
        }
    }

    actual override suspend fun toggleTemperatureMonitoring(
        enabled: Boolean,
        interval: Int
    ): String = mutex.withLock {
        withTimeout(15_000L) {
            suspendCancellableCoroutine { cont ->
                val validInterval = interval.coerceIn(1, 120)

                QCSDKCmdCreator.setSchedualInfoType(
                    SchedualInfoTypeBodyTemperator,
                    featureOn = enabled,
                    calibrate = 0,
                    interval = validInterval.toLong(),
                    success = {
                        if (!cont.isActive) return@setSchedualInfoType
                        val status = if (enabled) "enabled with $validInterval min interval" else "disabled"
                        cont.resume("Temperature monitoring $status")
                    },
                    fail = {
                        if (!cont.isActive) return@setSchedualInfoType
                        cont.resume("Temperature monitoring failed")
                    }
                )
            }
        }
    }

    private fun padZero(value: Int): String {
        return if (value < 10) "0$value" else "$value"
    }
}