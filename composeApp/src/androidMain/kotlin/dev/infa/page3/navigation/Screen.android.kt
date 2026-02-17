package dev.infa.page3.navigation

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import dev.infa.page3.SDK.connection.ConnectionManager
import dev.infa.page3.SDK.connection.ConnectionPlatform
import dev.infa.page3.SDK.platform.ContinuousMonitoring
import dev.infa.page3.SDK.platform.InstantMeasures
import dev.infa.page3.SDK.platform.SyncManager
import dev.infa.page3.SDK.repository.ConnectionRepository
import dev.infa.page3.SDK.ui.utils.PlatformContext
import dev.infa.page3.SDK.viewModel.HomeManager
import dev.infa.page3.SDK.viewModel.ISyncManager
import dev.infa.page3.SDK.viewModel.ProfileManager
import dev.infa.page3.data.remote.CacheManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


actual suspend fun initializePlatform() {
    withContext(Dispatchers.IO) {
        val context = PlatformContext.get()
        ConnectionPlatform.initialize(context as Context)

        // Create platform managers
        val connectionManager = ConnectionManager().apply { initialize() }
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

        println("✅ Platform initialized")
    }
}
