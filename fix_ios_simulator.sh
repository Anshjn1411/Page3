#!/bin/bash

# Backup
cp iosApp/iosApp.xcodeproj/project.pbxproj iosApp/iosApp.xcodeproj/project.pbxproj.backup

# Fix ARCHS
sed -i '' 's/ARCHS = arm64;/ARCHS = "$(ARCHS_STANDARD)";/g' iosApp/iosApp.xcodeproj/project.pbxproj

# Add SUPPORTED_PLATFORMS after SDKROOT
sed -i '' '/SDKROOT = iphoneos;/a\
				SUPPORTED_PLATFORMS = "iphoneos iphonesimulator";
' iosApp/iosApp.xcodeproj/project.pbxproj

