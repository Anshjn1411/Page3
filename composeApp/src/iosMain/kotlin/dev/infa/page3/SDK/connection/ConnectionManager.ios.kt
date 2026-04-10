@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class, kotlinx.cinterop.BetaInteropApi::class)
@file:Suppress("CONFLICTING_OVERLOADS")

package dev.infa.page3.SDK.connection

import cocoapods.QCBandSDK.QCSDKCmdCreator
import cocoapods.QCBandSDK.QCSDKManager
import platform.CoreBluetooth.*
import platform.Foundation.NSNumber
import platform.Foundation.NSUUID
import platform.darwin.NSObject

actual class ConnectionManager {

    private var connectionStateCallback: ((ConnectionState) -> Unit)? = null
    private var scanDeviceFoundCallback: ((DeviceInfo) -> Unit)? = null
    private var scanErrorCallback: ((String) -> Unit)? = null

    private var centralManager: CBCentralManager? = null
    private var connectedPeripheral: CBPeripheral? = null
    private var discoveredPeripherals = mutableMapOf<String, CBPeripheral>()
    private var isScanning = false

    // CBCentralManager delegate
    private val centralDelegate = object : NSObject(), CBCentralManagerDelegateProtocol {

        override fun centralManagerDidUpdateState(central: CBCentralManager) {
            println("🍎 CBCentralManager state: ${central.state}")
            when (central.state) {
                CBCentralManagerStatePoweredOn -> {
                    println("🍎 Bluetooth powered on")
                }
                CBCentralManagerStatePoweredOff -> {
                    println("🍎 Bluetooth powered off")
                    connectionStateCallback?.invoke(ConnectionState.DISCONNECTED)
                }
                CBCentralManagerStateUnauthorized -> {
                    println("🍎 Bluetooth unauthorized")
                    scanErrorCallback?.invoke("Bluetooth permission denied")
                }
                else -> {
                    println("🍎 Bluetooth state: ${central.state}")
                }
            }
        }

        override fun centralManager(
            central: CBCentralManager,
            didDiscoverPeripheral: CBPeripheral,
            advertisementData: Map<Any?, *>,
            RSSI: NSNumber
        ) {
            val name = didDiscoverPeripheral.name ?: return
            if (name.isEmpty()) return

            val address = didDiscoverPeripheral.identifier.UUIDString
            val rssiValue = RSSI.intValue

            // Store peripheral for later connection
            discoveredPeripherals[address] = didDiscoverPeripheral

            val deviceInfo = DeviceInfo(
                name = name,
                address = address,
                rssi = rssiValue
            )
            scanDeviceFoundCallback?.invoke(deviceInfo)
        }

        @kotlinx.cinterop.ObjCSignatureOverride
        override fun centralManager(
            central: CBCentralManager,
            didConnectPeripheral: CBPeripheral
        ) {
            println("🍎 Connected to: ${didConnectPeripheral.name}")
            connectedPeripheral = didConnectPeripheral

            // Register peripheral with QCSDKManager
            QCSDKManager.shareInstance().addPeripheral(didConnectPeripheral) { success ->
                if (success) {
                    println("🍎 Peripheral registered with SDK")
                } else {
                    println("🍎 Failed to register peripheral with SDK")
                }
            }
            connectionStateCallback?.invoke(ConnectionState.CONNECTED)
        }

        @kotlinx.cinterop.ObjCSignatureOverride
        override fun centralManager(
            central: CBCentralManager,
            didDisconnectPeripheral: CBPeripheral,
            error: platform.Foundation.NSError?
        ) {
            println("🍎 Disconnected from: ${didDisconnectPeripheral.name}")
            connectedPeripheral = null
            connectionStateCallback?.invoke(ConnectionState.DISCONNECTED)
        }

        @kotlinx.cinterop.ObjCSignatureOverride
        override fun centralManager(
            central: CBCentralManager,
            didFailToConnectPeripheral: CBPeripheral,
            error: platform.Foundation.NSError?
        ) {
            println("🍎 Failed to connect: ${didFailToConnectPeripheral.name}, error: ${error?.localizedDescription}")
            connectionStateCallback?.invoke(ConnectionState.ERROR)
        }
    }

    actual fun initialize() {
        println("🍎 Initializing iOS ConnectionManager...")
        centralManager = CBCentralManager(delegate = centralDelegate, queue = null)
        QCSDKManager.shareInstance().debug = true
        println("✅ iOS ConnectionManager initialized")
    }

    actual fun hasPermissions(): Boolean {
        return CBCentralManager.authorization == CBManagerAuthorizationAllowedAlways
    }

    actual fun startScan(
        onDeviceFound: (DeviceInfo) -> Unit,
        onError: (String) -> Unit
    ) {
        scanDeviceFoundCallback = onDeviceFound
        scanErrorCallback = onError
        discoveredPeripherals.clear()

        println("🍎 Starting BLE scan...")

        val cm = centralManager
        if (cm == null) {
            onError("CentralManager not initialized. Call initialize() first.")
            return
        }

        if (cm.state != CBCentralManagerStatePoweredOn) {
            onError("Bluetooth is not powered on (state: ${cm.state})")
            return
        }

        isScanning = true
        // Scan for peripherals with the QCBandSDK service UUIDs
        cm.scanForPeripheralsWithServices(null, options = null)
    }

    actual fun stopScan() {
        println("🍎 Stopping BLE scan...")
        isScanning = false
        centralManager?.stopScan()
        scanDeviceFoundCallback = null
        scanErrorCallback = null
    }

    actual fun connect(deviceName: String, deviceAddress: String) {
        println("🍎 Connecting to: $deviceName ($deviceAddress)")
        connectionStateCallback?.invoke(ConnectionState.CONNECTING)

        val peripheral = discoveredPeripherals[deviceAddress]
        if (peripheral != null) {
            centralManager?.connectPeripheral(peripheral, options = null)
        } else {
            // Try to retrieve by UUID
            val uuid = NSUUID(uUIDString = deviceAddress)
            @Suppress("UNCHECKED_CAST")
            val peripherals = centralManager?.retrievePeripheralsWithIdentifiers(listOf(uuid)) as? List<CBPeripheral>
            val found = peripherals?.firstOrNull()
            if (found != null) {
                discoveredPeripherals[deviceAddress] = found
                centralManager?.connectPeripheral(found, options = null)
            } else {
                println("🍎 Peripheral not found for address: $deviceAddress")
                connectionStateCallback?.invoke(ConnectionState.ERROR)
            }
        }
    }

    actual fun disconnect() {
        println("🍎 Disconnecting...")
        val peripheral = connectedPeripheral
        if (peripheral != null) {
            QCSDKManager.shareInstance().removePeripheral(peripheral)
            centralManager?.cancelPeripheralConnection(peripheral)
        }
        connectedPeripheral = null
    }

    actual fun isConnected(): Boolean {
        return connectedPeripheral?.state == CBPeripheralStateConnected
    }

    actual fun getBatteryLevel(onResult: (Int?) -> Unit) {
        QCSDKCmdCreator.readBatterySuccess({ battery, _ ->
            onResult(battery?.toInt())
        }, failed = {
            println("🍎 Failed to read battery")
            onResult(null)
        })
    }

    actual fun observeConnectionState(onStateChange: (ConnectionState) -> Unit) {
        connectionStateCallback = onStateChange
    }
}