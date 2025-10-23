package dev.infa.page3.viewmodels

import com.oudmon.ble.base.bluetooth.BleOperateManager
import com.oudmon.ble.base.bluetooth.ListenerKey
import com.oudmon.ble.base.communication.CommandHandle
import com.oudmon.ble.base.communication.Constants
import com.oudmon.ble.base.communication.ICommandResponse
import com.oudmon.ble.base.communication.req.SetANCSReq
import com.oudmon.ble.base.communication.responseImpl.DeviceNotifyListener
import com.oudmon.ble.base.communication.responseImpl.DeviceSportNotifyListener
import com.oudmon.ble.base.communication.rsp.BaseRspCmd
import com.oudmon.ble.base.communication.rsp.CameraNotifyRsp
import com.oudmon.ble.base.communication.rsp.DeviceNotifyRsp
import com.oudmon.ble.base.communication.utils.BLEDataFormatUtils
import com.oudmon.ble.base.util.MessPushUtil
import kotlinx.coroutines.CoroutineScope

class NotificationsAndListeners(
    private val commandHandle: CommandHandle?,
    private val coroutineScope: CoroutineScope,
    private val addLog: (String) -> Unit,
    private val batteryLevelUpdater: (Int) -> Unit
)
{
    companion object {
        const val TAG = "NotificationsAndListeners"
    }

    // Message Push Functions
    fun enableMessagePush() {
        if (commandHandle == null) {
            addLog("ERROR: CommandHandle not initialized")
            return
        }

        addLog("Enabling message push...")
        try {
            commandHandle.executeReqCmd(SetANCSReq(), null)
            addLog("Message push enabled")
        } catch (e: Exception) {
            addLog("ERROR: Exception enabling message push: ${e.message}")
        }
    }

    fun pushMessage(type: Int, message: String) {
        addLog("Pushing message - Type: $type, Message: $message")
        try {
            MessPushUtil.pushMsg(type, message)
            addLog("Message pushed successfully")
        } catch (e: Exception) {
            addLog("ERROR: Exception pushing message: ${e.message}")
        }
    }

    // Specific Message Push Functions
    fun pushCallReminder(message: String) {
        pushMessage(0x00, message) // Call reminder
    }

    fun pushSMSReminder(message: String) {
        pushMessage(0x01, message) // SMS reminder
    }

    fun pushQQReminder(message: String) {
        pushMessage(0x02, message) // QQ reminder
    }

    fun pushWeChatReminder(message: String) {
        pushMessage(0x03, message) // WeChat reminder
    }

    fun pushIncomingCallAction(message: String) {
        pushMessage(0x04, message) // Incoming call to answer or hang up
    }

    fun pushFacebookReminder(message: String) {
        pushMessage(0x05, message) // Facebook reminder
    }

    fun pushWhatsAppReminder(message: String) {
        pushMessage(0x06, message) // WhatsApp reminder
    }

    fun pushTwitterReminder(message: String) {
        pushMessage(0x07, message) // Twitter reminder
    }

    fun pushSkypeReminder(message: String) {
        pushMessage(0x08, message) // Skype reminder
    }

    fun pushLineReminder(message: String) {
        pushMessage(0x09, message) // Line reminder
    }

    fun pushLinkedInReminder(message: String) {
        pushMessage(0x0a, message) // LinkedIn reminder
    }

    fun pushInstagramReminder(message: String) {
        pushMessage(0x0b, message) // Instagram reminder
    }

    fun pushTIMReminder(message: String) {
        pushMessage(0x0c, message) // TIM message
    }

    fun pushSnapchatReminder(message: String) {
        pushMessage(0x0d, message) // Snapchat reminder
    }

    fun pushOtherNotification(message: String) {
        pushMessage(0x0e, message) // Other notifications
    }

    // Device Data Listeners
    fun addDeviceDataListener() {
        addLog("Adding device data listeners...")
        try {
            val deviceNotifyListener = object : DeviceNotifyListener() {
                override fun onDataResponse(resultEntity: DeviceNotifyRsp?) {
                    resultEntity?.let { entity ->
                        if (entity.status == BaseRspCmd.RESULT_OK) {
                            when (entity.dataType) {
                                1 -> addLog("Device notification: Heart rate test changed")
                                2 -> addLog("Device notification: Blood pressure test changed")
                                3 -> addLog("Device notification: Blood oxygen test changed")
                                4 -> addLog("Device notification: Step counting details changed")
                                5 -> addLog("Device notification: Body temperature changed")
                                7 -> addLog("Device notification: New exercise record generated")
                                0x0c -> {
                                    val charging = BLEDataFormatUtils.bytes2Int(byteArrayOf(entity.loadData[2]))
                                    if (charging > 0) {
                                        addLog("Device notification: Charging")
                                    } else {
                                        val battery = BLEDataFormatUtils.bytes2Int(byteArrayOf(entity.loadData[1]))
                                        batteryLevelUpdater(battery)
                                        addLog("Device notification: Battery level = $battery%")
                                    }
                                }
                                0x2d -> addLog("Device notification: Custom function button triggered")
                                0x12 -> {
                                    val step = BLEDataFormatUtils.bytes2Int(
                                        byteArrayOf(entity.loadData[1], entity.loadData[2], entity.loadData[3])
                                    )
                                    val calorie = BLEDataFormatUtils.bytes2Int(
                                        byteArrayOf(entity.loadData[4], entity.loadData[5], entity.loadData[6])
                                    )
                                    val distance = BLEDataFormatUtils.bytes2Int(
                                        byteArrayOf(entity.loadData[7], entity.loadData[8], entity.loadData[9])
                                    )
                                    addLog("Device notification: Steps=$step, Calories=$calorie, Distance=$distance")
                                }
                                0x25 -> {
                                    // Muslim ring click real-time data
                                    val count = BLEDataFormatUtils.bytes2Int(
                                        byteArrayOf(
                                            entity.loadData[1],
                                            entity.loadData[2],
                                            entity.loadData[3],
                                            entity.loadData[4]
                                        )
                                    )
                                    addLog("Device notification: Muslim ring click count = $count")
                                }
                            }
                        }
                    }
                }
            }

            BleOperateManager.getInstance().addOutDeviceListener(ListenerKey.All, deviceNotifyListener)
            addLog("Device data listeners added successfully")
        } catch (e: Exception) {
            addLog("ERROR: Exception adding device listeners: ${e.message}")
        }
    }

    fun removeDeviceDataListener() {
        try {
            BleOperateManager.getInstance().removeNotifyListener(ListenerKey.All)
            addLog("Device data listeners removed")
        } catch (e: Exception) {
            addLog("ERROR: Exception removing device listeners: ${e.message}")
        }
    }

    // Sport Exercise Monitoring Functions
    fun addSportDataListener() {
        addLog("Adding sport data listener...")
        try {
            val sportNotifyListener = object : DeviceSportNotifyListener() {
                override fun onDataResponse(resultEntity: DeviceNotifyRsp?) {
                    super.onDataResponse(resultEntity)
                    resultEntity?.let { entity ->
                        if (entity.status == BaseRspCmd.RESULT_OK) {
                            val sportTime = BLEDataFormatUtils.bytes2Int(
                                byteArrayOf(entity.loadData[2], entity.loadData[3])
                            )
                            val heart = BLEDataFormatUtils.bytes2Int(byteArrayOf(entity.loadData[4]))
                            val step = BLEDataFormatUtils.bytes2Int(
                                byteArrayOf(entity.loadData[5], entity.loadData[6], entity.loadData[7])
                            )
                            val distance = BLEDataFormatUtils.bytes2Int(
                                byteArrayOf(entity.loadData[8], entity.loadData[9], entity.loadData[10])
                            )
                            val calorie = BLEDataFormatUtils.bytes2Int(
                                byteArrayOf(entity.loadData[11], entity.loadData[12], entity.loadData[13])
                            )
                            val status = BLEDataFormatUtils.bytes2Int(byteArrayOf(entity.loadData[1]))
                            val sportType = BLEDataFormatUtils.bytes2Int(byteArrayOf(entity.loadData[0]))

                            addLog("Sport Data - Type: $sportType, Duration: ${sportTime}s")
                            addLog("Heart Rate: $heart BPM, Steps: $step, Distance: ${distance}m, Calories: $calorie")

                            if (status == 0x03) {
                                addLog("WARNING: Device not being worn properly")
                            }
                        }
                    }
                }
            }

            BleOperateManager.getInstance().addSportDeviceListener(0x78, sportNotifyListener)
            addLog("Sport data listener added successfully")
        } catch (e: Exception) {
            addLog("ERROR: Exception adding sport listener: ${e.message}")
        }
    }

    fun removeSportDataListener() {
        try {
            BleOperateManager.getInstance().removeSportDeviceListener(0x78)
            addLog("Sport data listener removed")
        } catch (e: Exception) {
            addLog("ERROR: Exception removing sport listener: ${e.message}")
        }
    }

    // Camera Photo Monitoring
    fun addCameraPhotoListener() {
        addLog("Adding camera photo listener...")
        try {
            BleOperateManager.getInstance().addNotifyListener(
                Constants.CMD_TAKING_PICTURE.toInt(),
                object : ICommandResponse<CameraNotifyRsp> {
                    override fun onDataResponse(resultEntity: CameraNotifyRsp) {
                        when (resultEntity.action) {
                            CameraNotifyRsp.ACTION_FINISH -> {
                                addLog("Camera notification: Watch exited camera interface")
                            }
                            CameraNotifyRsp.ACTION_TAKE_PHOTO -> {
                                addLog("Camera notification: Watch clicked photo button")
                            }
                        }
                    }
                }
            )
            addLog("Camera photo listener added")
        } catch (e: Exception) {
            addLog("ERROR: Exception adding camera listener: ${e.message}")
        }
    }

    fun removeCameraPhotoListener() {
        try {
            BleOperateManager.getInstance().removeNotifyListener(Constants.CMD_TAKING_PICTURE.toInt())
            addLog("Camera photo listener removed")
        } catch (e: Exception) {
            addLog("ERROR: Exception removing camera listener: ${e.message}")
        }
    }

    // Specific Device Listeners for Individual Health Metrics
    fun addHeartRateListener() {
        try {
            BleOperateManager.getInstance().addOutDeviceListener(ListenerKey.Heart, object : DeviceNotifyListener() {
                override fun onDataResponse(resultEntity: DeviceNotifyRsp?) {
                    resultEntity?.let { entity ->
                        if (entity.status == BaseRspCmd.RESULT_OK && entity.dataType == 1) {
                            addLog("Heart rate changed on device")
                        }
                    }
                }
            })
            addLog("Heart rate listener added")
        } catch (e: Exception) {
            addLog("ERROR: Exception adding heart rate listener: ${e.message}")
        }
    }

    fun addBloodPressureListener() {
        try {
            BleOperateManager.getInstance().addOutDeviceListener(ListenerKey.BloodPressure, object : DeviceNotifyListener() {
                override fun onDataResponse(resultEntity: DeviceNotifyRsp?) {
                    resultEntity?.let { entity ->
                        if (entity.status == BaseRspCmd.RESULT_OK && entity.dataType == 2) {
                            addLog("Blood pressure changed on device")
                        }
                    }
                }
            })
            addLog("Blood pressure listener added")
        } catch (e: Exception) {
            addLog("ERROR: Exception adding blood pressure listener: ${e.message}")
        }
    }

    fun addBloodOxygenListener() {
        try {
            BleOperateManager.getInstance().addOutDeviceListener(ListenerKey.BloodOxygen, object : DeviceNotifyListener() {
                override fun onDataResponse(resultEntity: DeviceNotifyRsp?) {
                    resultEntity?.let { entity ->
                        if (entity.status == BaseRspCmd.RESULT_OK && entity.dataType == 3) {
                            addLog("Blood oxygen changed on device")
                        }
                    }
                }
            })
            addLog("Blood oxygen listener added")
        } catch (e: Exception) {
            addLog("ERROR: Exception adding blood oxygen listener: ${e.message}")
        }
    }

    fun addTemperatureListener() {
        try {
            BleOperateManager.getInstance().addOutDeviceListener(ListenerKey.Temperature, object : DeviceNotifyListener() {
                override fun onDataResponse(resultEntity: DeviceNotifyRsp?) {
                    resultEntity?.let { entity ->
                        if (entity.status == BaseRspCmd.RESULT_OK && entity.dataType == 5) {
                            addLog("Temperature changed on device")
                        }
                    }
                }
            })
            addLog("Temperature listener added")
        } catch (e: Exception) {
            addLog("ERROR: Exception adding temperature listener: ${e.message}")
        }
    }

    fun addSportRecordListener() {
        try {
            BleOperateManager.getInstance().addOutDeviceListener(ListenerKey.SportRecord, object : DeviceNotifyListener() {
                override fun onDataResponse(resultEntity: DeviceNotifyRsp?) {
                    resultEntity?.let { entity ->
                        if (entity.status == BaseRspCmd.RESULT_OK && entity.dataType == 7) {
                            addLog("New sport record generated on device")
                        }
                    }
                }
            })
            addLog("Sport record listener added")
        } catch (e: Exception) {
            addLog("ERROR: Exception adding sport record listener: ${e.message}")
        }
    }

    // Remove specific listeners
    fun removeHeartRateListener() {
        try {
            BleOperateManager.getInstance().removeNotifyListener(ListenerKey.Heart)
            addLog("Heart rate listener removed")
        } catch (e: Exception) {
            addLog("ERROR: Exception removing heart rate listener: ${e.message}")
        }
    }

    fun removeBloodPressureListener() {
        try {
            BleOperateManager.getInstance().removeNotifyListener(ListenerKey.BloodPressure)
            addLog("Blood pressure listener removed")
        } catch (e: Exception) {
            addLog("ERROR: Exception removing blood pressure listener: ${e.message}")
        }
    }

    fun removeBloodOxygenListener() {
        try {
            BleOperateManager.getInstance().removeNotifyListener(ListenerKey.BloodOxygen)
            addLog("Blood oxygen listener removed")
        } catch (e: Exception) {
            addLog("ERROR: Exception removing blood oxygen listener: ${e.message}")
        }
    }

    fun removeTemperatureListener() {
        try {
            BleOperateManager.getInstance().removeNotifyListener(ListenerKey.Temperature)
            addLog("Temperature listener removed")
        } catch (e: Exception) {
            addLog("ERROR: Exception removing temperature listener: ${e.message}")
        }
    }

    fun removeSportRecordListener() {
        try {
            BleOperateManager.getInstance().removeNotifyListener(ListenerKey.SportRecord)
            addLog("Sport record listener removed")
        } catch (e: Exception) {
            addLog("ERROR: Exception removing sport record listener: ${e.message}")
        }
    }

    // Comprehensive listener management
    fun addAllListeners() {
        addLog("Adding all device listeners...")
        addDeviceDataListener()
        addCameraPhotoListener()
        addSportDataListener()
        addLog("All listeners added successfully")
    }

    fun removeAllListeners() {
        addLog("Removing all device listeners...")
        removeDeviceDataListener()
        removeCameraPhotoListener()
        removeSportDataListener()
        removeHeartRateListener()
        removeBloodPressureListener()
        removeBloodOxygenListener()
        removeTemperatureListener()
        removeSportRecordListener()
        addLog("All listeners removed successfully")
    }
}