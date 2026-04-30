// COMMENTED OUT: QCBandSDK disabled for iOS build
// Original implementation used cocoapods.QCBandSDK.* and CoreBluetooth
// This is a dummy stub that returns safe defaults

package dev.infa.page3.SDK.connection

actual class ConnectionManager {

    private var connectionStateCallback: ((ConnectionState) -> Unit)? = null

    actual fun initialize() {
        println("🍎 [STUB] iOS ConnectionManager initialized (SDK disabled)")
    }

    actual fun hasPermissions(): Boolean {
        return true // Return true so permission checks don't block
    }

    actual fun startScan(
        onDeviceFound: (DeviceInfo) -> Unit,
        onError: (String) -> Unit
    ) {
        println("🍎 [STUB] startScan called (SDK disabled)")
        onError("SDK is currently disabled on iOS")
    }

    actual fun stopScan() {
        println("🍎 [STUB] stopScan called (SDK disabled)")
    }

    actual fun connect(deviceName: String, deviceAddress: String) {
        println("🍎 [STUB] connect called (SDK disabled)")
        connectionStateCallback?.invoke(ConnectionState.DISCONNECTED)
    }

    actual fun disconnect() {
        println("🍎 [STUB] disconnect called (SDK disabled)")
    }

    actual fun isConnected(): Boolean {
        return false
    }

    actual fun getBatteryLevel(onResult: (Int?) -> Unit) {
        onResult(null)
    }

    actual fun observeConnectionState(onStateChange: (ConnectionState) -> Unit) {
        connectionStateCallback = onStateChange
    }
}