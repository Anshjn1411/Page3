package dev.infa.page3.SDK.repository

actual class ConnectionRepository {
    actual fun saveLastConnectedDevice(
        address: String,
        name: String,
        deviceType: String
    ) {
    }

    actual fun getSavedDeviceInfo(): SavedDeviceInfo? {
        TODO("Not yet implemented")
    }

    actual fun hasSavedDevice(): Boolean {
        TODO("Not yet implemented")
    }

    actual fun clearSavedDevice() {
    }

    actual fun markConnectionSuccess() {
    }

    actual fun markConnectionFailed() {
    }

    actual fun getConnectionAttempts(): Int {
        TODO("Not yet implemented")
    }

    actual fun incrementConnectionAttempts(): Int {
        TODO("Not yet implemented")
    }

    actual fun resetConnectionAttempts() {
    }

    actual fun isAutoConnectEnabled(): Boolean {
        TODO("Not yet implemented")
    }

    actual fun setAutoConnectEnabled(enabled: Boolean) {
    }

    actual fun isDeviceRecent(): Boolean {
        TODO("Not yet implemented")
    }
}