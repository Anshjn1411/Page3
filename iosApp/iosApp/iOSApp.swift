import SwiftUI
import ComposeApp

@main
struct iOSApp: App {
    // Initialize platform on app launch
    init() {
        // Initialize Kotlin platform
        initializeKotlinPlatform()

        // Request Bluetooth permissions
        requestBluetoothPermissions()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
    // Initialize Kotlin/KMP platform
    private func initializeKotlinPlatform() {
        Task {
            do {
                try await Screen_iosKt.initializePlatform()
                print("✅ Kotlin platform initialized from Swift")
            } catch {
                print("❌ Failed to initialize Kotlin platform: \(error)")
            }
        }
    }

    // Request Bluetooth permissions
    private func requestBluetoothPermissions() {
        // Bluetooth permissions are requested automatically when you use CoreBluetooth
        // But we can trigger the prompt early
        print("📱 Bluetooth permissions will be requested when needed")
    }
}
