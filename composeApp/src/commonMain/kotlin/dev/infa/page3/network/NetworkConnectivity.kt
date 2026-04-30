package dev.infa.page3.network

/**
 * Platform implementation reports whether the device can reach the internet.
 * Used by repositories before remote calls; offline surfaces [NetworkException] to the UI.
 */
interface NetworkConnectivity {
    fun isConnected(): Boolean
}

class NetworkException(
    message: String = DEFAULT_MESSAGE
) : Exception(message) {
    companion object {
        const val DEFAULT_MESSAGE =
            "No internet connection. Please connect to the internet and try again."
    }
}

fun NetworkConnectivity.requireNetwork() {
    if (!isConnected()) throw NetworkException()
}
