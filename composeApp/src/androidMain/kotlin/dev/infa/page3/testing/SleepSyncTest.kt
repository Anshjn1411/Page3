package dev.infa.page3.testing

import android.os.Looper
import android.util.Log
import com.oudmon.ble.base.bean.SleepDisplay
import com.oudmon.ble.base.communication.ILargeDataLaunchSleepResponse
import com.oudmon.ble.base.communication.ILargeDataSleepResponse
import com.oudmon.ble.base.communication.LargeDataHandler
import com.oudmon.ble.base.communication.rsp.SleepNewProtoResp
import dev.infa.page3.connection.BleConnectionService
import java.text.SimpleDateFormat
import java.util.*

class SleepSyncTest {

    private val TAG = "SleepSyncTest"

    // ============================================
    // SIMPLEST POSSIBLE TEST - START HERE!
    // ============================================
    fun quickStartTest() {
        Log.d(TAG, "========================================")
        Log.d(TAG, "QUICK START TEST")
        Log.d(TAG, "========================================")
        try {
            BleConnectionService.suspendHealthOps(30000)
            android.os.Handler(Looper.getMainLooper()).post {
                LargeDataHandler.getInstance().syncSleepListIndianDemand(
                    0,
                    object : ILargeDataSleepResponse {
                        override fun sleepData(resp: SleepDisplay?) {
                            if (resp != null) {
                            Log.i(TAG, "✓✓✓ SUCCESS! Response received! ✓✓✓")
                            Log.i(TAG, "Sleep Time: ${resp.sleepTime}")
                            Log.i(TAG, "Wake Time: ${resp.wakeTime}")
                            Log.i(TAG, "Total Sleep: ${resp.totalSleepDuration} sec")
                            Log.i(TAG, "Deep Sleep: ${resp.deepSleepDuration} sec")
                            Log.i(TAG, "Light Sleep: ${resp.shallowSleepDuration} sec")
                            Log.i(TAG, "Awake: ${resp.awakeDuration} sec")
                            Log.i(TAG, "REM: ${resp.rapidDuration} sec")
                            Log.i(TAG, "Total Days: ${resp.totalDays}")
                            Log.i(TAG, "--- In Minutes ---")
                            Log.i(TAG, "Total Sleep: ${resp.totalSleepDuration / 60} min")
                            Log.i(TAG, "Deep Sleep: ${resp.deepSleepDuration / 60} min")
                            Log.i(TAG, "Light Sleep: ${resp.shallowSleepDuration / 60} min")
                            Log.i(TAG, "Full response: $resp")
                            } else {
                                Log.e(TAG, "✗✗✗ ERROR: Response is NULL ✗✗✗ (retrying once)")
                                android.os.Handler(Looper.getMainLooper()).postDelayed({
                                    LargeDataHandler.getInstance().syncSleepListIndianDemand(
                                        0,
                                        object : ILargeDataSleepResponse {
                                            override fun sleepData(r2: SleepDisplay?) {
                                                if (r2 != null) {
                                                    Log.i(TAG, "✓ SUCCESS on retry: Total Sleep: ${r2.totalSleepDuration / 60} min")
                                                } else {
                                                    Log.e(TAG, "✗ Still NULL after retry")
                                                }
                                            }
                                        }
                                    )
                                }, 500)
                            }
                        }
                    }
                )
            }
            Log.d(TAG, "Sleep sync request sent! Waiting for callback...")
        } catch (e: Exception) {
            Log.e(TAG, "✗✗✗ EXCEPTION: ${e.message} ✗✗✗", e)
        }
    }

    // ============================================
    // TEST WITH BOTH PROTOCOLS (3-parameter method)
    // ============================================
    fun testBothProtocols(offset: Int = 0) {
        Log.d(TAG, "========================================")
        Log.d(TAG, "TEST: Both Protocols (Old + New + Lunch)")
        Log.d(TAG, "========================================")
        try {
            BleConnectionService.suspendHealthOps(30000)
            android.os.Handler(Looper.getMainLooper()).post {
                LargeDataHandler.getInstance().syncSleepList(
                offset,
                object : ILargeDataSleepResponse {
                    override fun sleepData(sleepDisplay: SleepDisplay?) {
                        Log.d(TAG, "========================================")
                        Log.d(TAG, "OLD PROTOCOL CALLBACK")
                        Log.d(TAG, "========================================")
                        if (sleepDisplay != null) {
                            Log.d(TAG, "✓ Old Protocol: Data received")
                            Log.d(TAG, "  Total Sleep: ${sleepDisplay.totalSleepDuration / 60} min")
                            Log.d(TAG, "  Deep: ${sleepDisplay.deepSleepDuration / 60} min")
                            Log.d(TAG, "  Light: ${sleepDisplay.shallowSleepDuration / 60} min")
                            Log.d(TAG, "  REM: ${sleepDisplay.rapidDuration / 60} min")
                            Log.d(TAG, "  Awake: ${sleepDisplay.awakeDuration / 60} min")
                            val list = sleepDisplay.list
                            if (list != null && list.isNotEmpty()) {
                                Log.d(TAG, "  Sleep Stages: ${list.size} entries")
                                list.take(5).forEachIndexed { index, bean ->
                                    val typeStr = when (bean.type) {
                                        1 -> "Deep Sleep"
                                        2 -> "Light Sleep"
                                        3 -> "Awake"
                                        4 -> "REM Sleep"
                                        5 -> "Device Off"
                                        else -> "Unknown (${bean.type})"
                                    }
                                    Log.d(TAG, "    [$index] $typeStr: ${formatTimestamp(bean.sleepStart)} to ${formatTimestamp(bean.sleepEnd)}")
                                }
                                if (list.size > 5) {
                                    Log.d(TAG, "    ... and ${list.size - 5} more stages")
                                }
                            }
                        } else {
                            Log.w(TAG, "✗ Old Protocol: NULL response")
                        }
                    }
                },
                object : ILargeDataLaunchSleepResponse {
                    override fun sleepData(newProto: SleepNewProtoResp?) {
                        Log.d(TAG, "========================================")
                        Log.d(TAG, "NEW PROTOCOL CALLBACK (with Lunch support)")
                        Log.d(TAG, "========================================")
                        if (newProto != null) {
                            Log.d(TAG, "✓ New Protocol: Data received!")
                            Log.d(TAG, "  Sleep Start (st): ${formatTimestamp(newProto.st.toLong())}")
                            Log.d(TAG, "  Sleep End (et): ${formatTimestamp(newProto.et.toLong())}")
                            Log.d(TAG, "  Has Lunch Break: ${newProto.isLunchBreak}")
                            if (newProto.isLunchBreak) {
                                Log.d(TAG, "  Lunch Start: ${formatTimestamp(newProto.lunchSt.toLong())}")
                                Log.d(TAG, "  Lunch End: ${formatTimestamp(newProto.lunchEt.toLong())}")
                            }
                            val list = newProto.list
                            if (list != null && list.isNotEmpty()) {
                                Log.d(TAG, "  Sleep Stages: ${list.size} entries")
                                var totalDeep = 0
                                var totalLight = 0
                                var totalREM = 0
                                var totalAwake = 0
                                var totalRemoved = 0
                                list.forEachIndexed { index, bean ->
                                    val typeStr = when (bean.t) {
                                        0 -> "Not Sleeping"
                                        1 -> "Device Removed"
                                        2 -> "Light Sleep"
                                        3 -> "Deep Sleep"
                                        4 -> "REM Sleep"
                                        5 -> "Awake"
                                        else -> "Unknown (${bean.t})"
                                    }
                                    when (bean.t) {
                                        2 -> totalLight += bean.d
                                        3 -> totalDeep += bean.d
                                        4 -> totalREM += bean.d
                                        5 -> totalAwake += bean.d
                                        1 -> totalRemoved += bean.d
                                    }
                                    if (index < 10) {
                                        Log.d(TAG, "    [$index] $typeStr: ${bean.d} min")
                                    }
                                }
                                if (list.size > 10) {
                                    Log.d(TAG, "    ... and ${list.size - 10} more stages")
                                }
                                Log.d(TAG, "  === SUMMARY ===")
                                Log.d(TAG, "    Deep Sleep: $totalDeep min")
                                Log.d(TAG, "    Light Sleep: $totalLight min")
                                Log.d(TAG, "    REM Sleep: $totalREM min")
                                Log.d(TAG, "    Awake: $totalAwake min")
                                Log.d(TAG, "    Device Removed: $totalRemoved min")
                                Log.d(TAG, "    Total Sleep Time: ${totalDeep + totalLight + totalREM} min")
                            } else {
                                Log.d(TAG, "  No detailed stage data")
                            }
                        } else {
                            Log.w(TAG, "✗ New Protocol: NULL response")
                            Log.w(TAG, "  Device may not support new protocol or lunch break feature")
                        }
                    }
                }
            )
            }
            Log.d(TAG, "Both protocols request sent! Waiting for callbacks...")
        } catch (e: Exception) {
            Log.e(TAG, "✗ EXCEPTION: ${e.message}", e)
        }
    }

    // ============================================
    // TEST YESTERDAY'S DATA
    // ============================================
    fun testYesterday() {
        Log.d(TAG, "========================================")
        Log.d(TAG, "TEST: Yesterday's Sleep Data")
        Log.d(TAG, "========================================")
        try {
            BleConnectionService.suspendHealthOps(30000)
            android.os.Handler(Looper.getMainLooper()).post {
                LargeDataHandler.getInstance().syncSleepListIndianDemand(
                    1,
                    object : ILargeDataSleepResponse {
                        override fun sleepData(resp: SleepDisplay?) {
                            if (resp != null) {
                            Log.i(TAG, "✓ Yesterday's data received!")
                            Log.i(TAG, "Total Sleep: ${resp.totalSleepDuration / 60} min")
                            Log.i(TAG, "Deep: ${resp.deepSleepDuration / 60} min")
                            Log.i(TAG, "Light: ${resp.shallowSleepDuration / 60} min")
                            } else {
                                Log.w(TAG, "✗ No data for yesterday (retrying once)")
                                android.os.Handler(Looper.getMainLooper()).postDelayed({
                                    LargeDataHandler.getInstance().syncSleepListIndianDemand(
                                        1,
                                        object : ILargeDataSleepResponse {
                                            override fun sleepData(r2: SleepDisplay?) {
                                                if (r2 != null) {
                                                    Log.i(TAG, "✓ Yesterday's data received on retry: ${r2.totalSleepDuration / 60} min")
                                                } else {
                                                    Log.w(TAG, "✗ Still no data for yesterday after retry")
                                                }
                                            }
                                        }
                                    )
                                }, 500)
                            }
                        }
                    }
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "✗ EXCEPTION: ${e.message}")
        }
    }

    // ============================================
    // TEST MULTIPLE DAYS (using Indian demand method)
    // ============================================
    fun testMultipleDays(days: Int = 3) {
        Log.d(TAG, "========================================")
        Log.d(TAG, "TEST: Last $days Days")
        Log.d(TAG, "========================================")
        for (dayOffset in 0 until days) {
            val dateStr = getDateForOffset(dayOffset)
            try {
                BleConnectionService.suspendHealthOps(30000)
                android.os.Handler(Looper.getMainLooper()).post {
                    LargeDataHandler.getInstance().syncSleepListIndianDemand(
                    dayOffset,
                    object : ILargeDataSleepResponse {
                        override fun sleepData(resp: SleepDisplay?) {
                            if (resp != null) {
                                Log.d(TAG, "Day $dayOffset ($dateStr): ${resp.totalSleepDuration / 60} min total")
                                Log.d(TAG, "  Deep: ${resp.deepSleepDuration / 60} min, Light: ${resp.shallowSleepDuration / 60} min")
                            } else {
                                Log.w(TAG, "Day $dayOffset ($dateStr): No data")
                            }
                        }
                    }
                )
                }
                Thread.sleep(300)
            } catch (e: Exception) {
                Log.e(TAG, "Day $dayOffset ERROR: ${e.message}")
            }
        }
    }

    // ============================================
    // RUN ALL TESTS IN SEQUENCE
    // ============================================
    fun runAllSleepTests() {
        Log.d(TAG, "")
        Log.d(TAG, "################################################")
        Log.d(TAG, "STARTING COMPREHENSIVE SLEEP SYNC TESTS")
        Log.d(TAG, "################################################")
        Log.d(TAG, "")
        quickStartTest()
        android.os.Handler(Looper.getMainLooper()).postDelayed({
            testBothProtocols()
            android.os.Handler(Looper.getMainLooper()).postDelayed({
                testYesterday()
                android.os.Handler(Looper.getMainLooper()).postDelayed({
                    testMultipleDays(3)
                }, 2000)
            }, 3000)
        }, 3000)
    }

    // ============================================
    // VERY SIMPLE TEST - ABSOLUTE MINIMUM
    // ============================================
    fun verySimpleTest() {
        Log.d(TAG, "=== VERY SIMPLE TEST ===")
        BleConnectionService.suspendHealthOps(30000)
        android.os.Handler(Looper.getMainLooper()).post {
            LargeDataHandler.getInstance().syncSleepListIndianDemand(
                0,
                object : ILargeDataSleepResponse {
                    override fun sleepData(data: SleepDisplay?) {
                        Log.d(TAG, "Callback received! Data = $data")
                    }
                }
            )
        }
        Log.d(TAG, "Request sent!")
    }

    private fun formatTimestamp(timestamp: Long): String {
        if (timestamp == 0L) return "N/A"
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            sdf.format(Date(timestamp * 1000))
        } catch (e: Exception) {
            "Invalid: $timestamp"
        }
    }

    private fun getDateForOffset(offset: Int): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -offset)
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(calendar.time)
    }
}


