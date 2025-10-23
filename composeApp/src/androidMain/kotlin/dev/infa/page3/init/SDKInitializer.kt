package dev.infa.page3.init

import android.app.Application
import android.util.Log
import com.oudmon.ble.base.bluetooth.BleOperateManager
import com.oudmon.ble.base.bluetooth.DeviceManager
import com.oudmon.ble.base.communication.CommandHandle
import com.oudmon.ble.base.communication.Constants
import com.oudmon.ble.base.communication.ICommandResponse
import com.oudmon.ble.base.communication.rsp.BaseRspCmd
import com.oudmon.ble.base.scan.BleScannerHelper
import dev.infa.page3.models.BluetoothEvent
import org.greenrobot.eventbus.EventBus

class SDKInitializer(
    private val application: Application,
    private val addLog: (String) -> Unit
)
{
    companion object {
        const val TAG = "SDKInitializer"
    }

    private var bleOperateManager: BleOperateManager? = null
    private var deviceManager: DeviceManager? = null
    private var commandHandle: CommandHandle? = null
    private var bleScannerHelper: BleScannerHelper? = null
    private var isInitialized: Boolean = false

    data class SDKComponents(
        val bleOperateManager: BleOperateManager?,
        val deviceManager: DeviceManager?,
        val commandHandle: CommandHandle?,
        val bleScannerHelper: BleScannerHelper?
    )

    fun initializeSDK(): SDKComponents {
        if (isInitialized && bleOperateManager != null && deviceManager != null && commandHandle != null && bleScannerHelper != null) {
            addLog("SDKInitializer: already initialized, returning existing components")
            return SDKComponents(
                bleOperateManager = bleOperateManager,
                deviceManager = deviceManager,
                commandHandle = commandHandle,
                bleScannerHelper = bleScannerHelper
            )
        }
        try {
            addLog("Initializing QC SDK with enhanced connectivity...")

            // Initialize BleOperateManager with context
            bleOperateManager = try {
                BleOperateManager.getInstance(application).apply {
                    init()
                    // Set up connection monitoring
                    setNeedConnect(true)
                    setBluetoothTurnOff(true)
                }
                addLog("BleOperateManager initialized with auto-reconnect")
                BleOperateManager.getInstance()
            } catch (e: Exception) {
                addLog("ERROR: BleOperateManager initialization failed: ${e.message}")
                Log.e(TAG, "BleOperateManager initialization error", e)
                null
            }

            // Initialize other managers
            deviceManager = try {
                DeviceManager.getInstance().also {
                    addLog("DeviceManager initialized successfully")
                    addLog("Current device name: ${it.deviceName}")
                }
            } catch (e: Exception) {
                addLog("ERROR: DeviceManager.getInstance() failed: ${e.message}")
                Log.e(TAG, "DeviceManager initialization error", e)
                null
            }

            commandHandle = try {
                CommandHandle.getInstance().also {
                    addLog("CommandHandle initialized successfully")
                }
            } catch (e: Exception) {
                addLog("ERROR: CommandHandle.getInstance() failed: ${e.message}")
                Log.e(TAG, "CommandHandle initialization error", e)
                null
            }

            bleScannerHelper = try {
                BleScannerHelper.getInstance().also {
                    addLog("BleScannerHelper initialized successfully")
                }
            } catch (e: Exception) {
                addLog("ERROR: BleScannerHelper.getInstance() failed: ${e.message}")
                Log.e(TAG, "BleScannerHelper initialization error", e)
                null
            }

            // Set up connection event listeners
            setupConnectionListeners()

            // Check connection status
            val isCurrentlyConnected = bleOperateManager?.isConnected ?: false
            if (isCurrentlyConnected) {
                val currentDeviceName = deviceManager?.deviceName ?: ""
                val currentDeviceAddress = deviceManager?.deviceAddress ?: ""
                addLog("Already connected to device: $currentDeviceName")

                // Notify about existing connection
                EventBus.getDefault().post(
                    BluetoothEvent(
                        connect = true,
                        deviceName = currentDeviceName,
                        deviceAddress = currentDeviceAddress
                    )
                )
            }

            addLog("QC SDK initialization completed with enhanced features")

        } catch (e: Exception) {
            addLog("ERROR: Exception during SDK initialization: ${e.message}")
            Log.e(TAG, "SDK initialization error", e)
        }

        isInitialized = bleOperateManager != null && deviceManager != null && commandHandle != null && bleScannerHelper != null
        return SDKComponents(
            bleOperateManager = bleOperateManager,
            deviceManager = deviceManager,
            commandHandle = commandHandle,
            bleScannerHelper = bleScannerHelper
        )
    }

    fun getComponents(): SDKComponents = SDKComponents(
        bleOperateManager = bleOperateManager,
        deviceManager = deviceManager,
        commandHandle = commandHandle,
        bleScannerHelper = bleScannerHelper
    )

    private fun setupConnectionListeners() {
        try {
            // Set up various connection event listeners
            bleOperateManager?.let { manager ->
                // Add connection state listeners
                addLog("Setting up enhanced connection monitoring...")

                // Add notification listeners for connection events
                manager.addNotifyListener(
                    Constants.CMD_DEVICE_REVISION.toInt(),
                    object : ICommandResponse<BaseRspCmd> {
                        override fun onDataResponse(resultEntity: BaseRspCmd?) {
                            addLog("Device connect notification received")
                            if (resultEntity?.status == BaseRspCmd.RESULT_OK) {
                                addLog("Connection confirmed by SDK")
                                val deviceName = deviceManager?.deviceName ?: ""
                                val deviceAddress = deviceManager?.deviceAddress ?: ""
                                EventBus.getDefault().post(
                                    BluetoothEvent(
                                        connect = true,
                                        deviceName = deviceName,
                                        deviceAddress = deviceAddress
                                    )
                                )
                            }
                        }
                    })

                manager.addNotifyListener(
                    Constants.CMD_DEVICE_NOTIFY.toInt(),
                    object : ICommandResponse<BaseRspCmd> {
                        override fun onDataResponse(resultEntity: BaseRspCmd?) {
                            addLog("Device disconnect notification received")
                            EventBus.getDefault().post(BluetoothEvent(connect = false))
                        }
                    })
            }
        } catch (e: Exception) {
            addLog("ERROR: Failed to setup connection listeners: ${e.message}")
            Log.e(TAG, "Connection listeners setup error", e)
        }
    }

    fun reinitializeSDK(): SDKComponents {
        addLog("Reinitializing SDK components...")

        // Clean up existing instances if needed
        try {
            bleOperateManager?.let {
                if (it.isConnected) {
                    it.unBindDevice()
                    it.disconnect()
                }
            }
        } catch (e: Exception) {
            addLog("Warning: Error during cleanup before reinit: ${e.message}")
        }

        return initializeSDK()
    }

    fun isSDKInitialized(): Boolean {
        return bleOperateManager != null &&
                deviceManager != null &&
                commandHandle != null &&
                bleScannerHelper != null
    }
}