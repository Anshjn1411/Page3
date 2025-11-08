#!/bin/bash

# Build the framework
./gradlew :composeApp:embedAndSignAppleFrameworkForXcode

# Open Xcode
open iosApp/iosApp.xcodeproj

echo "✅ Framework built! Now click ▶️ in Xcode to run"
