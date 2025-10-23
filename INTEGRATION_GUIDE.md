# BLE SDK Integration Guide

## 🏗️ Integration Overview

This guide explains how the BLE SDK has been integrated into your Compose Multiplatform e-commerce app. The integration follows a clean architecture pattern where:

- **Common UI** (shared code) handles the user interface
- **Android-specific code** handles all BLE SDK functionality
- **Platform-specific interface** bridges the two layers

## 📁 File Structure

```
composeApp/
├── src/
│   ├── commonMain/kotlin/dev/infa/page3/
│   │   ├── platform/
│   │   │   └── DeviceSDKLauncher.kt          # Platform interface
│   │   └── ui/MainScreen.kt                  # Updated with "My Devices" button
│   └── androidMain/kotlin/dev/infa/page3/
│       ├── platform/
│       │   └── DeviceSDKLauncher.kt          # Android implementation
│       ├── DeviceSDKActivity.kt              # Main SDK Activity
│       ├── SDKInitializer.kt                 # SDK initialization
│       ├── HealthMonitor.kt                  # Health data management
│       ├── DeviceScanner.kt                  # BLE device scanning
│       ├── PermissionHandler.kt              # Permission management
│       ├── ConnectionPersistenceManager.kt   # Connection persistence
│       ├── MainViewModel.kt                  # ViewModel for UI state
│       ├── AppNavigation.kt                  # SDK UI composables
│       └── DataClasses.kt                    # Data models
└── build.gradle.kts                          # Updated dependencies
```

## 🚀 How It Works

### 1. User Interaction Flow

1. **User clicks "My Devices" button** in the main app
2. **Common UI calls** `deviceSDKLauncher.openDeviceManager()`
3. **Android implementation** launches `DeviceSDKActivity`
4. **SDK Activity** handles all BLE operations and health data

### 2. Platform-Specific Interface

```kotlin
// Common code (expect)
expect class DeviceSDKLauncher {
    fun openDeviceManager()
    fun isSDKAvailable(): Boolean
}

// Android implementation (actual)
actual class DeviceSDKLauncher(private val context: Context) {
    actual fun openDeviceManager() {
        val intent = Intent(context, DeviceSDKActivity::class.java)
        context.startActivity(intent)
    }
    
    actual fun isSDKAvailable(): Boolean {
        // Check if SDK Activity can be launched
    }
}
```

### 3. SDK Activity Features

The `DeviceSDKActivity` includes all the functionality from your original SDK app:

- ✅ **BLE Device Scanning** - Find nearby QC devices
- ✅ **Device Connection** - Connect to selected devices
- ✅ **Health Data Reading** - Heart rate, SpO2, temperature, etc.
- ✅ **Permission Management** - Handle all required permissions
- ✅ **Connection Persistence** - Auto-reconnect functionality
- ✅ **Real-time UI Updates** - Live health data display

## 🔧 Configuration

### Gradle Dependencies

The `.aar` file is already configured in your `build.gradle.kts`:

```kotlin
androidMain.dependencies {
    implementation(files("libs/qring_sdk_20250516.aar"))
    implementation("org.greenrobot:eventbus:3.2.0")
    // ... other dependencies
}
```

### AndroidManifest.xml

All necessary permissions and activities are registered:

```xml
<!-- Bluetooth permissions -->
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
<uses-permission android:name="android.permission.BLUETOOTH_SCAN" />
<uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />

<!-- Location permissions (required for BLE scanning) -->
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />

<!-- Activities -->
<activity android:name=".DeviceSDKActivity" />
<service android:name=".BleConnectionService" />
```

## 📱 Usage Examples

### Basic Usage

```kotlin
// In your common UI code
@Composable
fun MyScreen() {
    val context = LocalContext.current
    val deviceSDKLauncher = remember { DeviceSDKLauncher(context) }
    
    Button(
        onClick = { 
            if (deviceSDKLauncher.isSDKAvailable()) {
                deviceSDKLauncher.openDeviceManager()
            }
        }
    ) {
        Text("Open Device Manager")
    }
}
```

### Advanced Usage

```kotlin
// Check SDK availability before showing UI
@Composable
fun DeviceManagementButton() {
    val context = LocalContext.current
    val deviceSDKLauncher = remember { DeviceSDKLauncher(context) }
    var isSDKAvailable by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        isSDKAvailable = deviceSDKLauncher.isSDKAvailable()
    }
    
    if (isSDKAvailable) {
        Button(onClick = { deviceSDKLauncher.openDeviceManager() }) {
            Text("My Devices")
        }
    } else {
        Text("Device SDK not available")
    }
}
```

## 🔍 Testing the Integration

### 1. Build and Run

```bash
./gradlew assembleDebug
```

### 2. Test Flow

1. **Launch the app** - You should see the "My Devices" button on the home screen
2. **Tap "My Devices"** - This should open the SDK Activity
3. **Grant permissions** - Allow location and Bluetooth permissions
4. **Scan for devices** - Tap "Scan Devices" to find nearby QC devices
5. **Connect to device** - Select a device and tap "Connect"
6. **View health data** - Once connected, you can read various health metrics

### 3. Expected Behavior

- ✅ Button appears on home screen when not searching
- ✅ SDK Activity launches with full BLE functionality
- ✅ Permissions are requested automatically
- ✅ Device scanning works
- ✅ Connection and health data reading work
- ✅ UI updates in real-time

## 🐛 Troubleshooting

### Common Issues

1. **"My Devices" button not visible**
   - Check if `searchQuery.isBlank()` condition is met
   - Ensure the button is in the correct location in the UI

2. **SDK Activity doesn't launch**
   - Verify `DeviceSDKActivity` is registered in AndroidManifest.xml
   - Check if `.aar` file is properly included in dependencies

3. **Permission errors**
   - Ensure all required permissions are in AndroidManifest.xml
   - Check if permission request flow is working

4. **BLE scanning not working**
   - Verify location permissions are granted
   - Check if Bluetooth is enabled
   - Ensure device supports BLE

### Debug Steps

1. **Check logs** - Look for "DeviceSDKActivity" and "DeviceScanner" logs
2. **Verify permissions** - Check if all permissions are granted in device settings
3. **Test BLE** - Use a BLE scanner app to verify devices are discoverable
4. **Check .aar file** - Ensure the SDK file is in the correct location

## 🎯 Next Steps

### Optional Enhancements

1. **Add device status to main UI** - Show connection status on home screen
2. **Implement data sync** - Sync health data back to your e-commerce backend
3. **Add notifications** - Notify users about health alerts
4. **Customize UI** - Style the SDK Activity to match your app theme

### Integration with E-commerce

```kotlin
// Example: Sync health data with user profile
fun syncHealthDataWithProfile(healthData: HealthData) {
    // Send to your API
    apiService.updateUserHealthData(healthData)
}
```

## 📚 API Reference

### DeviceSDKLauncher

```kotlin
class DeviceSDKLauncher(context: Context) {
    fun openDeviceManager() // Opens the SDK Activity
    fun isSDKAvailable(): Boolean // Checks if SDK is available
}
```

### Health Data Models

```kotlin
data class HealthData(
    var heartRate: Int = 0,
    var spo2: Int = 0,
    var temperature: Float = 0.0f,
    var steps: Int = 0,
    // ... more fields
)
```

## ✅ Integration Complete!

Your BLE SDK is now fully integrated into your Compose Multiplatform e-commerce app. The integration follows best practices for:

- ✅ **Clean Architecture** - Separation of concerns
- ✅ **Platform Abstraction** - Common UI, platform-specific logic
- ✅ **Permission Handling** - Proper Android permission management
- ✅ **Error Handling** - Graceful fallbacks and error states
- ✅ **User Experience** - Seamless navigation between app and SDK

The "My Devices" button will appear on your home screen, and clicking it will launch the full BLE SDK functionality in a separate Activity, exactly as requested!
