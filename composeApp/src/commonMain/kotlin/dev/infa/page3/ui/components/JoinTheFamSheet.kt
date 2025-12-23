package dev.infa.page3.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.infa.page3.navigation.AuthManager
import dev.infa.page3.presentation.uiSatateClaases.AuthUiState


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JoinTheFamSheet(
    onDismiss: () -> Unit
) {
    val authState by AuthManager.authState.collectAsState()
    ModalBottomSheet(
        onDismissRequest = { onDismiss() },
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Drag handle
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(4.dp)
                    .background(Color.LightGray, RoundedCornerShape(2.dp))
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "JOIN THE FAM",
                fontSize = 20.sp,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "A few reasons why you should sign up",
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Example Feature Items
            FeatureItem(
                emoji = "✉️",
                title = "YOUR VERY OWN INBOX",
                subtitle = "Get exclusive content and offers, just for you."
            )
            FeatureItem(
                emoji = "❤️",
                title = "SAVE TO WISHLIST",
                subtitle = "Save your favs for later."
            )
            FeatureItem(
                emoji = "🔔",
                title = "BACK IN STOCK NOTIFICATIONS",
                subtitle = "We'll let you know as soon as it's back in stock."
            )
            FeatureItem(
                emoji = "💳",
                title = "FASTER CHECKOUT",
                subtitle = "Save address and your preferred payment method."
            )
            FeatureItem(
                emoji = "📋",
                title = "MANAGE YOUR ORDERS",
                subtitle = "Check for status updates and place returns."
            )

            Spacer(modifier = Modifier.height(24.dp))

            when (authState) {
                is AuthUiState.LoggedIn -> {

                }
                else ->{

                    // Buttons
                    LoginButton(onLoginClick = { })
                    Spacer(modifier = Modifier.height(12.dp))
                    SignUpButton(onSignUpClick = { })


                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}


@Composable
fun FeatureItem(emoji: String, title: String, subtitle: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = emoji, fontSize = 20.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text(text = subtitle, fontSize = 13.sp, color = Color.Gray)
            }
        }
    }
}
