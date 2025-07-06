// presentation/ui/screens/StartupScreen.kt - With Encrypted Storage
package com.example.foodorderingapp_ver2.presentation.ui.screens
//Burda kaldin
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foodorderingapp_ver2.presentation.ui.theme.LocalAppTheme
import com.example.foodorderingapp_ver2.data.preferences.ConfigHelper

@Composable
fun StartupScreen(
    onHomeClick: () -> Unit,
    onConfigClick: () -> Unit,
    isFirstTime: Boolean = false,
    onConnectionEstablished: () -> Unit = {}
) {
    val theme = LocalAppTheme.current
    val context = LocalContext.current

    // Use ConfigHelper for encrypted storage
    val configHelper = remember { ConfigHelper.getInstance(context) }

    // Get configuration status using ConfigHelper
    val serverUrl = configHelper.getServerUrl() ?: ""
    val connectionInitialized = configHelper.isConnectionInitialized()
    val appPassword = configHelper.getAppPassword() ?: ""

    // Connection is ready if all required fields are configured
    val isConnectionReady = configHelper.isConnectionReady()

    // Auto-navigate to food ordering if first time and connection is ready
    LaunchedEffect(isConnectionReady) {
        if (isFirstTime && isConnectionReady) {
            onConnectionEstablished()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = theme.cardColor
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(
                    text = "Food Ordering App",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.textColor,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = if (isFirstTime) {
                        if (isConnectionReady) {
                            "Connection established! Loading app..."
                        } else {
                            "Welcome! Please configure your database connection to get started."
                        }
                    } else {
                        if (isConnectionReady) {
                            "Ready to start ordering delicious food!"
                        } else {
                            "Configure your database connection to get started."
                        }
                    },
                    fontSize = 16.sp,
                    color = theme.textColor,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Server URL Status
                if (serverUrl.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = theme.successColor.copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "✅",
                                fontSize = 20.sp,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Column {
                                Text(
                                    text = "Server Configured",
                                    color = theme.textColor,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${serverUrl.take(40)}${if (serverUrl.length > 40) "..." else ""}",
                                    color = theme.textColor.copy(alpha = 0.7f),
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.Red.copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "⚠️",
                                fontSize = 20.sp,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Column {
                                Text(
                                    text = "Database Required",
                                    color = Color.Red,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Configure server URL to continue",
                                    color = Color.Red.copy(alpha = 0.8f),
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }

                // Connection Status Card
                if (isConnectionReady) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = theme.successColor.copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🔗",
                                fontSize = 20.sp,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Column {
                                Text(
                                    text = "Connection Ready",
                                    color = theme.textColor,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (isFirstTime)
                                        "Launching Food Ordering App..."
                                    else
                                        "Database connected • Password secured • Ready to go!",
                                    color = theme.textColor.copy(alpha = 0.7f),
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }

                // Show loading indicator if first time and connection is ready
                if (isFirstTime && isConnectionReady) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = theme.primaryColor,
                        strokeWidth = 3.dp
                    )
                }

                // MAIN ACTION BUTTON - Only show if NOT first time OR connection is not ready
                if (!isFirstTime && isConnectionReady) {
                    // Show Food Ordering App button when everything is configured and not first time
                    Button(
                        onClick = onHomeClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = theme.primaryColor,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "🚀",
                            fontSize = 24.sp,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                        Text(
                            text = "Open Food Ordering App",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Configure Database Button - Always show unless first time with ready connection
                if (!(isFirstTime && isConnectionReady)) {
                    Button(
                        onClick = onConfigClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (serverUrl.isEmpty()) theme.primaryColor else theme.primaryColor.copy(alpha = 0.7f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "⚙️",
                            fontSize = 24.sp,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                        Text(
                            text = if (serverUrl.isEmpty()) "Configure Database" else "Database Settings",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Debug Info (temporary - remove in production) - Don't show on first time with ready connection
                if (!isConnectionReady && !(isFirstTime && isConnectionReady)) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.Gray.copy(alpha = 0.1f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp)
                        ) {
                            Text(
                                text = "Configuration Status:",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = theme.textColor
                            )
                            Text(
                                text = "Server URL: ${if (serverUrl.isNotEmpty()) "✅" else "❌"}",
                                fontSize = 10.sp,
                                color = theme.textColor
                            )
                            Text(
                                text = "Connection Initialized: ${if (connectionInitialized) "✅" else "❌"}",
                                fontSize = 10.sp,
                                color = theme.textColor
                            )
                            Text(
                                text = "Password Saved: ${if (appPassword.isNotEmpty()) "✅" else "❌"}",
                                fontSize = 10.sp,
                                color = theme.textColor
                            )
                        }
                    }
                }

                // Tips section
                if (!(isFirstTime && isConnectionReady)) {
                    when {
                        serverUrl.isEmpty() -> {
                            Text(
                                text = "💡 Tip: Configure your database connection in Settings to load real products and process orders.",
                                fontSize = 12.sp,
                                color = theme.textColor.copy(alpha = 0.6f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                        !isConnectionReady -> {
                            Text(
                                text = "💡 Tip: Complete your database configuration to start using the app.",
                                fontSize = 12.sp,
                                color = theme.textColor.copy(alpha = 0.6f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                        !isFirstTime -> {
                            Text(
                                text = "🎉 Everything is set up! You're ready to start ordering food.",
                                fontSize = 12.sp,
                                color = theme.successColor,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}