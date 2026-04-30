package dev.infa.page3.navigation

import dev.infa.page3.di.ecommerceModule
import dev.infa.page3.network.IosNetworkConnectivity
import dev.infa.page3.network.NetworkConnectivity
import org.koin.core.context.startKoin
import org.koin.dsl.module
import dev.infa.page3.SDK.connection.ConnectionManager
import dev.infa.page3.SDK.`V-Band`.VBandManager
import dev.infa.page3.SDK.platform.SyncManager
import dev.infa.page3.SDK.platform.InstantMeasures
import dev.infa.page3.SDK.platform.ContinuousMonitoring
import dev.infa.page3.SDK.repository.ConnectionRepository
import dev.infa.page3.SDK.viewModel.HomeManager
import dev.infa.page3.SDK.viewModel.ProfileManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

actual suspend fun initializePlatform() {
    withContext(Dispatchers.Default) {
        try {
            println("🍎 iOS Platform initialization started...")

            if (org.koin.mp.KoinPlatformTools.defaultContext().getOrNull() == null) {
                startKoin {
                    modules(
                        module {
                            single<NetworkConnectivity> { IosNetworkConnectivity() }
                        },
                        ecommerceModule()
                    )
                }
            }

            // Create platform managers (iOS versions)
            val connectionManager = ConnectionManager().apply {
                initialize()
            }
            val connectionRepository = ConnectionRepository()
            val homeManager = HomeManager()
            val profileManager = ProfileManager()
            val syncManager = SyncManager()
            val instantMeasurement = InstantMeasures()
            val continuousMonitoring = ContinuousMonitoring()
            val vBandManager = VBandManager()

            // Initialize all ViewModels
            AppViewModels.init(
                connectionManager = connectionManager,
                connectionRepository = connectionRepository,
                homeManager = homeManager,
                profileManager = profileManager,
                syncManager = syncManager,
                instantMeasures = instantMeasurement,
                continuousMonitoring = continuousMonitoring,
                vBandManager = vBandManager,
            )

            println("✅ iOS Platform initialized successfully")
        } catch (e: Exception) {
            println("❌ iOS Platform initialization failed: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }
}
