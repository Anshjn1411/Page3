@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package dev.infa.page3.network

import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.SystemConfiguration.SCNetworkReachabilityCreateWithName
import platform.SystemConfiguration.SCNetworkReachabilityGetFlags
import platform.SystemConfiguration.SCNetworkReachabilityFlags
import platform.SystemConfiguration.SCNetworkReachabilityFlagsVar
import platform.SystemConfiguration.kSCNetworkReachabilityFlagsConnectionOnDemand
import platform.SystemConfiguration.kSCNetworkReachabilityFlagsConnectionOnTraffic
import platform.SystemConfiguration.kSCNetworkReachabilityFlagsConnectionRequired
import platform.SystemConfiguration.kSCNetworkReachabilityFlagsInterventionRequired
import platform.SystemConfiguration.kSCNetworkReachabilityFlagsReachable

/**
 * iOS reachability using SystemConfiguration (SCNetworkReachability).
 * Mirrors common "Reachability" logic: reachable and either ready to use or connect-on-demand without user intervention.
 */
@Suppress("DEPRECATION", "unused")
class IosNetworkConnectivity : NetworkConnectivity {

    override fun isConnected(): Boolean = memScoped {
        val ref = SCNetworkReachabilityCreateWithName(null, "apple.com")
            ?: return@memScoped false

        val flags = alloc<SCNetworkReachabilityFlagsVar>()
        if (!SCNetworkReachabilityGetFlags(ref, flags.ptr)) {
            return@memScoped false
        }

        val f: SCNetworkReachabilityFlags = flags.value
        val reachable = (f and kSCNetworkReachabilityFlagsReachable) != 0u
        if (!reachable) return@memScoped false

        val connectionRequired = (f and kSCNetworkReachabilityFlagsConnectionRequired) != 0u
        if (!connectionRequired) return@memScoped true

        val onDemand = (f and kSCNetworkReachabilityFlagsConnectionOnDemand) != 0u
        val onTraffic = (f and kSCNetworkReachabilityFlagsConnectionOnTraffic) != 0u
        val intervention = (f and kSCNetworkReachabilityFlagsInterventionRequired) != 0u

        return@memScoped (onDemand || onTraffic) && !intervention
    }
}
