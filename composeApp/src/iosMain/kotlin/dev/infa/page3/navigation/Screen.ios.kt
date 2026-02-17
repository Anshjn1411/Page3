package dev.infa.page3.navigation

import dev.infa.page3.SDK.connection.ConnectionManager
import dev.infa.page3.SDK.platform.SyncManager
import dev.infa.page3.SDK.platform.InstantMeasures
import dev.infa.page3.SDK.platform.ContinuousMonitoring
import dev.infa.page3.SDK.repository.ConnectionRepository
import dev.infa.page3.SDK.viewModel.HomeManager
import dev.infa.page3.SDK.viewModel.ProfileManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

actual suspend fun initializePlatform() {
    withContext(Dispatchers.IO) {
        try {
            println("🍎 iOS Platform initialization started...")

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

            // Initialize all ViewModels
            AppViewModels.init(
                connectionManager = connectionManager,
                connectionRepository = connectionRepository,
                homeManager = homeManager,
                profileManager = profileManager,
                syncManager = syncManager,
                instantMeasures = instantMeasurement,
                continuousMonitoring = continuousMonitoring,
            )

            println("✅ iOS Platform initialized successfully")
        } catch (e: Exception) {
            println("❌ iOS Platform initialization failed: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }
}
