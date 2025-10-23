package dev.infa.page3.init

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat

class PermissionHandler(
    private val activity: ComponentActivity,
    private val onPermissionsGranted: () -> Unit,
    private val onPermissionsDenied: (List<String>) -> Unit,
    private val addLog: (String) -> Unit
)
{

    private var showPermissionDialog by mutableStateOf(false)
    private var deniedPermissionsList by mutableStateOf<List<String>>(emptyList())

    // Permission launcher for multiple permissions
    private val permissionLauncher: ActivityResultLauncher<Array<String>> =
        activity.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            handlePermissionResult(permissions)
        }

    fun getRequiredPermissions(): List<String> {
        val permissions = mutableListOf<String>()

        // Location permissions (CRITICAL for BLE scanning)
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION)

        // Android 12+ (API 31+) Bluetooth permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_SCAN)
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
            permissions.add(Manifest.permission.BLUETOOTH_ADVERTISE)
        } else {
            // Legacy Bluetooth permissions for Android < 12
            permissions.add(Manifest.permission.BLUETOOTH)
            permissions.add(Manifest.permission.BLUETOOTH_ADMIN)
        }

        return permissions
    }

    fun checkAndRequestPermissions() {
        val requiredPermissions = getRequiredPermissions()

        val missingPermissions = requiredPermissions.filter { permission ->
            ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            addLog("Missing permissions: ${missingPermissions.joinToString()}")
            addLog("Requesting permissions...")
            permissionLauncher.launch(missingPermissions.toTypedArray())
        } else {
            addLog("All required permissions already granted")
            onPermissionsGranted()
        }
    }

    fun arePermissionsGranted(): Boolean {
        val requiredPermissions = getRequiredPermissions()

        val missingPermissions = requiredPermissions.filter { permission ->
            ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            addLog("Missing permissions: ${missingPermissions.joinToString()}")
            return false
        }

        return true
    }

    private fun handlePermissionResult(permissions: Map<String, Boolean>) {
        val granted = mutableListOf<String>()
        val denied = mutableListOf<String>()

        permissions.entries.forEach { entry ->
            if (entry.value) {
                granted.add(entry.key)
            } else {
                denied.add(entry.key)
            }
        }

        if (granted.isNotEmpty()) {
            addLog("Granted permissions: ${granted.joinToString()}")
        }

        if (denied.isNotEmpty()) {
            addLog("Denied permissions: ${denied.joinToString()}")
            val criticalPermissions = denied.filter { permission ->
                when (permission) {
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION -> true

                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT -> Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

                    else -> false
                }
            }

            if (criticalPermissions.isNotEmpty()) {
                deniedPermissionsList = criticalPermissions
                showPermissionDialog = true
                onPermissionsDenied(criticalPermissions)
            } else {
                onPermissionsGranted()
            }
        } else {
            addLog("All permissions granted successfully")
            onPermissionsGranted()
        }
    }

    @Composable
    fun PermissionDialog() {
        if (showPermissionDialog) {
            PermissionExplanationDialog(
                deniedPermissions = deniedPermissionsList,
                onOpenSettings = {
                    showPermissionDialog = false
                    openAppSettings()
                },
                onDismiss = {
                    showPermissionDialog = false
                    addLog("User cancelled permission request")
                }
            )
        }
    }

    @Composable
    private fun PermissionExplanationDialog(
        deniedPermissions: List<String>,
        onOpenSettings: () -> Unit,
        onDismiss: () -> Unit
    ) {
        AlertDialog(
            onDismissRequest = { /* Don't allow dismiss by clicking outside */ },
            title = {
                Text("Permissions Required")
            },
            text = {
                val message = buildString {
                    append("This app requires the following permissions to function properly:\n\n")

                    deniedPermissions.forEach { permission ->
                        when (permission) {
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION -> {
                                append("• Location: Required for Bluetooth Low Energy device scanning\n")
                            }

                            Manifest.permission.BLUETOOTH_SCAN -> {
                                append("• Bluetooth Scan: Required to discover nearby devices\n")
                            }

                            Manifest.permission.BLUETOOTH_CONNECT -> {
                                append("• Bluetooth Connect: Required to connect to devices\n")
                            }
                        }
                    }

                    append("\nWould you like to grant these permissions in Settings?")
                }

                Text(message)
            },
            confirmButton = {
                TextButton(
                    onClick = onOpenSettings
                ) {
                    Text("Open Settings")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onDismiss
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    private fun openAppSettings() {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", activity.packageName, null)
            intent.data = uri
            activity.startActivity(intent)
            addLog("Opened app settings for manual permission grant")
        } catch (e: Exception) {
            addLog("ERROR: Could not open app settings: ${e.message}")
            Toast.makeText(
                activity,
                "Please enable permissions manually in Settings",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
