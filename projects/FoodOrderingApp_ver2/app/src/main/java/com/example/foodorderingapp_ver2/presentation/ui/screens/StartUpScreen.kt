// presentation/ui/screens/StartupScreen.kt - Fixed Button Logic
package com.example.foodorderingapp_ver2.presentation.ui.screens

import android.content.Context
import android.widget.Toast
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
import com.example.foodorderingapp_ver2.data.repositories.ConnectionRepositoryImpl
import com.example.foodorderingapp_ver2.domain.usecases.TestConnectionUseCase
import com.example.foodorderingapp_ver2.domain.common.Result
import kotlinx.coroutines.launch

@Composable
fun StartupScreen(
    onHomeClick: () -> Unit,
    onConfigClick: () -> Unit
) {
    val theme = LocalAppTheme.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sharedPreferences = context.getSharedPreferences("food_app_settings", Context.MODE_PRIVATE)
    val serverUrl = sharedPreferences.getString("server_url", "") ?: ""
    val connectionInitialized = sharedPreferences.getBoolean("connection_initialized", false)
    val appPassword = sharedPreferences.getString("app_password", "") ?: ""

    var isTestingConnection by remember { mutableStateOf(false) }
    var connectionTestResult by remember { mutableStateOf<String?>(null) }
    var isConnectionSuccessful by remember { mutableStateOf(false) }

    // Create use cases for connection testing
    val connectionRepository = remember { ConnectionRepositoryImpl() }
    val testConnectionUseCase = remember { TestConnectionUseCase(connectionRepository) }

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
                    text = "Connect to your database to get started.",
                    fontSize = 16.sp,
                    color = theme.textColor,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Connection Test Result
                connectionTestResult?.let { result ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isConnectionSuccessful) theme.successColor.copy(alpha = 0.1f) else Color.Red.copy(alpha = 0.1f)
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
                                text = if (isConnectionSuccessful) "✅" else "❌",
                                fontSize = 20.sp,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Column {
                                Text(
                                    text = if (isConnectionSuccessful) "Connection Successful" else "Connection Failed",
                                    color = if (isConnectionSuccessful) theme.successColor else Color.Red,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = result,
                                    color = theme.textColor.copy(alpha = 0.7f),
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }

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
                if (connectionInitialized && appPassword.isNotEmpty()) {
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
                                    text = "Connection Initialized",
                                    color = theme.textColor,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Password saved • Ready to test",
                                    color = theme.textColor.copy(alpha = 0.7f),
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }

                // Test Connection Button (show if server configured but not tested yet)
                if (serverUrl.isNotEmpty() && !isConnectionSuccessful) {
                    Button(
                        onClick = {
                            scope.launch {
                                isTestingConnection = true
                                connectionTestResult = null

                                try {
                                    when (val testResult = testConnectionUseCase()) {
                                        is Result.Success -> {
                                            connectionTestResult = "Server is responding correctly"
                                            isConnectionSuccessful = true
                                            Toast.makeText(context, "✅ Connection test successful!", Toast.LENGTH_SHORT).show()
                                        }
                                        is Result.Error -> {
                                            connectionTestResult = testResult.message
                                            isConnectionSuccessful = false
                                            Toast.makeText(context, "❌ ${testResult.message}", Toast.LENGTH_LONG).show()
                                        }
                                        Result.Loading -> {}
                                    }
                                } catch (e: Exception) {
                                    connectionTestResult = "Connection error: ${e.message}"
                                    isConnectionSuccessful = false
                                    Toast.makeText(context, "❌ Connection error: ${e.message}", Toast.LENGTH_LONG).show()
                                } finally {
                                    isTestingConnection = false
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = theme.primaryColor.copy(alpha = 0.8f),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isTestingConnection
                    ) {
                        if (isTestingConnection) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Text(
                                text = " Testing Connection...",
                                fontSize = 14.sp,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        } else {
                            Text(
                                text = "🔍",
                                fontSize = 20.sp,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                text = "Test Connection",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // MAIN BUTTON - Always show if server is configured
                if (serverUrl.isNotEmpty()) {
                    Button(
                        onClick = {
                            if (isConnectionSuccessful) {
                                onHomeClick() // Open the main scrollable page
                            } else {
                                Toast.makeText(context, "⚠️ Please test connection first", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isConnectionSuccessful) theme.primaryColor else Color.Gray,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (isConnectionSuccessful) "🚀" else "🔍",
                            fontSize = 24.sp,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                        Text(
                            text = if (isConnectionSuccessful) "Open Food Ordering App" else "Test Connection First",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Configure Database Button
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

                // Debug Info (temporary - remove in production)
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
                            text = "Debug Info:",
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
                            text = "Connection Init: ${if (connectionInitialized) "✅" else "❌"}",
                            fontSize = 10.sp,
                            color = theme.textColor
                        )
                        Text(
                            text = "Password Saved: ${if (appPassword.isNotEmpty()) "✅" else "❌"}",
                            fontSize = 10.sp,
                            color = theme.textColor
                        )
                        Text(
                            text = "Connection Test: ${if (isConnectionSuccessful) "✅" else "❌"}",
                            fontSize = 10.sp,
                            color = theme.textColor
                        )
                    }
                }

                // Tips section
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
                    !isConnectionSuccessful -> {
                        Text(
                            text = "💡 Tip: Test your connection first to ensure the app can communicate with your database.",
                            fontSize = 12.sp,
                            color = theme.textColor.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        }
    }
}