import UIKit
import SwiftUI
import ComposeApp

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    @State private var isInitialized = false

    var body: some View {
        ZStack {
            if isInitialized {
                ComposeView()
                    .ignoresSafeArea(.all)
            } else {
                // Loading screen
                VStack {
                    ProgressView()
                    Text("Initializing...")
                        .padding()
                }
            }
        }
        .onAppear {
            initializePlatform()
        }
    }

    private func initializePlatform() {
        Task {
            do {
                try await Screen_iosKt.initializePlatform()
                print("✅ Platform initialized")
                await MainActor.run {
                    isInitialized = true
                }
            } catch {
                print("❌ Initialization failed: \(error)")
                // Handle error - maybe show alert
                await MainActor.run {
                    isInitialized = true // Still show app but without BLE
                }
            }
        }
    }
}
