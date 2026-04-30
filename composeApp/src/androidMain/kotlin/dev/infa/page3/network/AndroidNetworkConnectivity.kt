package dev.infa.page3.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

class AndroidNetworkConnectivity(
    private val context: Context
) : NetworkConnectivity {
    override fun isConnected(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return false
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
}
