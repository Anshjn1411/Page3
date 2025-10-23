package dev.infa.page3.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.Navigator
import dev.infa.page3.presentation.viewModel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(navigator: Navigator, authViewModel: AuthViewModel) {
    var pushNotifications by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Top Bar
        TopAppBar(
            title = { Text("Setting") },
            navigationIcon = {
                IconButton(onClick = { navigator.pop() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            }
        )
        // Divider just below Top Bar
        Divider(color = Color.LightGray, thickness = 1.dp)

        // Account Section
        SectionHeader("ACCOUNT")
        SettingsItem(
            title = "Account",
            subtitle = "Your name, email address and DOB",
            hasLock = true,
            onClick = {

            }
        )
        SettingsItem(
            title = "Address Book",
            subtitle = "Manage your address book",
            hasLock = true,
            onClick = {

            }
        )

        // Preferences Section
        SectionHeader("PREFERENCES")
        SettingsItem(
            title = "Push Notifications",
            subtitle = "Receive alerts on all things Gymshark including products, sales, workouts and more straight to your phone.",
            trailingContent = {
                Switch(
                    checked = pushNotifications,
                    onCheckedChange = { pushNotifications = it }
                )
            }
        )

        // About Section
        SectionHeader("ABOUT")
        SettingsItem(title = "Terms and Conditions",
            onClick = {

            })
        SettingsItem(title = "Privacy Notice",
            onClick = {

            })
        SettingsItem(title = "Modern Slavery Act",
            onClick = {

            })
    }
}


@Composable
fun SettingsItem(
    title: String,
    subtitle: String? = null,
    hasLock: Boolean = false,
    trailingContent: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(vertical = 12.dp, horizontal = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                }
            }

            if (hasLock) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Locked",
                    tint = Color(0xFFFFA000) // amber
                )
            }

            trailingContent?.invoke()
        }
    }
    Divider(color = Color.LightGray, thickness = 1.dp)
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = Color.Gray,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    )
}