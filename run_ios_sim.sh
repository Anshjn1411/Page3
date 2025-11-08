#!/bin/bash
echo "🔨 Building iOS framework..."
./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64

if [ $? -eq 0 ]; then
    echo "✅ Framework built successfully!"
    echo "📱 Opening Xcode..."
    open iosApp/iosApp.xcodeproj
    echo ""
    echo "👉 Now click the ▶️ button in Xcode to run!"
else
    echo "❌ Build failed!"
fi
