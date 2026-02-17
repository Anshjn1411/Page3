package dev.infa.page3.SDK.connection

actual class ConnectionManager {
    actual fun initialize() {
    }

    actual fun hasPermissions(): Boolean {
        TODO("Not yet implemented")
    }

    actual fun startScan(
        onDeviceFound: (DeviceInfo) -> Unit,
        onError: (String) -> Unit
    ) {
    }

    actual fun stopScan() {
    }

    actual fun connect(deviceName: String, deviceAddress: String) {
    }

    actual fun disconnect() {
    }

    actual fun isConnected(): Boolean {
        TODO("Not yet implemented")
    }

    actual fun getBatteryLevel(onResult: (Int?) -> Unit) {
    }

    actual fun observeConnectionState(onStateChange: (ConnectionState) -> Unit) {
    }
}