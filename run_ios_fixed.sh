#!/bin/bash

# Set Java 21
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
export PATH="$JAVA_HOME/bin:$PATH"

echo "☕ Using Java version:"
java -version

# Stop old daemons
echo "🛑 Stopping Gradle daemons..."
./gradlew --stop

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
