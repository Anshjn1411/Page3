package dev.infa.page3.SDK.viewModel


import android.util.Log
import androidx.core.graphics.blue
import com.oudmon.ble.base.communication.CommandHandle
import com.oudmon.ble.base.communication.ICommandResponse
import com.oudmon.ble.base.communication.req.*
import com.oudmon.ble.base.communication.rsp.*
import dev.infa.page3.SDK.data.DeviceInfo
import dev.infa.page3.SDK.data.TimeFormat
import dev.infa.page3.SDK.data.TouchSettings
import dev.infa.page3.SDK.data.UnitSystem
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

actual class ProfileManager {
    private val commandHandle = CommandHandle.getInstance()

    companion object {
        private const val TAG = "ProfileManager"
    }

    actual suspend fun findMyDevice(): Boolean {
        return try {
            commandHandle.executeReqCmdNoCallback(FindDeviceReq())
            Log.d(TAG, "Find device command sent")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to find device", e)
            false
        }
    }

    actual suspend fun getDeviceInfo(): DeviceInfo? = suspendCancellableCoroutine { continuation ->
        try {

        } catch (e: Exception) {
            Log.e(TAG, "Failed to get device info", e)
            continuation.resume(null)
        }
    }

    actual suspend fun updateTimeFormat(
        timeFormat: TimeFormat,
    ): Boolean = suspendCancellableCoroutine { continuation ->

        val is24Hour = timeFormat == TimeFormat.HOUR_24
        var resumed = false
        val metric = 0

        try {
            val req = TimeFormatReq.getWriteInstance(
                is24Hour,
                metric.toByte(),
            )

            commandHandle.executeReqCmd(
                req,
                object : ICommandResponse<TimeFormatRsp> {
                    override fun onDataResponse(resultEntity: TimeFormatRsp) {
                        if (resumed || !continuation.isActive) return
                        resumed = true

                        val success = resultEntity.status == BaseRspCmd.RESULT_OK
                        Log.d(TAG, "Time format update result: $success")

                        continuation.resume(success)
                    }
                }
            )
        } catch (e: Exception) {
            if (!resumed && continuation.isActive) {
                resumed = true
                Log.e(TAG, "Failed to update time format", e)
                continuation.resume(false)
            }
        }

        continuation.invokeOnCancellation {
            resumed = true
        }
    }

    actual suspend fun toggleLowBatteryPrompt(enabled: Boolean): Boolean {
        // This is typically a local app setting
        Log.d(TAG, "Low battery prompt: $enabled")
        return true
    }

    actual suspend fun loadTouchSettings(): TouchSettings? = suspendCancellableCoroutine { continuation ->
        try {
            commandHandle.executeReqCmd(
                TouchControlReq.getReadInstance(false),
                object : ICommandResponse<TouchControlResp> {
                    override fun onDataResponse(resultEntity: TouchControlResp) {
                        if (resultEntity.status == BaseRspCmd.RESULT_OK) {
                            val settings = TouchSettings(
                                appType = resultEntity.appType,
                                isTouch = false,
                                strength = resultEntity.strength
                            )
                            Log.d(TAG, "Touch settings loaded: $settings")
                            continuation.resume(settings)
                        } else {
                            continuation.resume(null)
                        }
                    }
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load touch settings", e)
            continuation.resume(null)
        }
    }

    actual suspend fun updateTouchSettings(
        appType: Int,
        isTouch: Boolean,
        strength: Int
    ): Boolean {
        return try {
            commandHandle.executeReqCmdNoCallback(
                TouchControlReq.getWriteInstance(appType, isTouch, strength)
            )
            Log.d(TAG, "Touch settings updated")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update touch settings", e)
            false
        }
    }

    actual fun cleanup() {
        Log.d(TAG, "ProfileManager cleanup")
    }

    actual suspend fun updateUnitSystem(
        unitSystem: UnitSystem
    ): Boolean = suspendCancellableCoroutine { continuation ->

        val metricValue = when (unitSystem) {
            UnitSystem.METRIC -> 0
            UnitSystem.IMPERIAL -> 1
        }

        var resumed = false

        try {
            val req = TimeFormatReq.getWriteInstance(
                true,               // keep current time format
                metricValue.toByte(),
            )

            commandHandle.executeReqCmd(
                req,
                object : ICommandResponse<TimeFormatRsp> {
                    override fun onDataResponse(rsp: TimeFormatRsp) {
                        if (resumed || !continuation.isActive) return
                        resumed = true
                        continuation.resume(rsp.status == BaseRspCmd.RESULT_OK)
                    }
                }
            )
        } catch (e: Exception) {
            if (!resumed && continuation.isActive) {
                resumed = true
                continuation.resume(false)
            }
        }

        continuation.invokeOnCancellation {
            resumed = true
        }
    }


}