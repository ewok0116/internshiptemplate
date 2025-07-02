// presentation/ui/screens/ConfigScreen.kt - Final Version
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foodorderingapp_ver2.presentation.ui.theme.LocalAppTheme
import com.example.foodorderingapp_ver2.data.repositories.ConnectionRepositoryImpl
import com.example.foodorderingapp_ver2.domain.usecases.TestConnectionUseCase
import com.example.foodorderingapp_ver2.domain.usecases.InitializeConnectionUseCase
import com.example.foodorderingapp_ver2.domain.common.Result
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigScreen(onBackToStartup: () -> Unit) {
    val context = LocalContext.current
    val theme = LocalAppTheme.current
    val sharedPreferences = context.getSharedPreferences("food_app_settings", Context.MODE_PRIVATE)
    val scope = rememberCoroutineScope()

    // State variables
    var urlText by remember {
        mutableStateOf(sharedPreferences.getString("server_url", "") ?: "")
    }
    var passwordText by remember { mutableStateOf("") }
    var reenterPasswordText by remember { mutableStateOf("") }
    var showSuccessMessage by remember { mutableStateOf(false) }
    var isTestingConnection by remember { mutableStateOf(false) }
    var connectionTestResult by remember { mutableStateOf<String?>(null) }
    var isConnectionSuccessful by remember { mutableStateOf(false) }

    // Create use cases for connection testing
    val connectionRepository = remember { ConnectionRepositoryImpl() }
    val testConnectionUseCase = remember { TestConnectionUseCase(connectionRepository) }
    val initializeConnectionUseCase = remember { InitializeConnectionUseCase(connectionRepository) }

    // Show success message for 3 seconds
    LaunchedEffect(showSuccessMessage) {
        if (showSuccessMessage) {
            delay(3000)
            showSuccessMessage = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.backgroundColor)
    ) {
        // Top App Bar with Back Button
        TopAppBar(
            title = {
                Text(
                    text = "⚙️ App Configuration",
                    fontWeight = FontWeight.Bold,
                    color = theme.textOnPrimary
                )
            },
            navigationIcon = {
                Button(
                    onClick = onBackToStartup,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.2f)
                    )
                ) {
                    Text(
                        text = "⬅️ Back",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = theme.primaryColor
            )
        )

        // Main Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Success Message
            if (showSuccessMessage) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = theme.successColor
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "✅",
                            fontSize = 20.sp,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                        Text(
                            text = "Configuration saved successfully!",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Connection Test Result
            connectionTestResult?.let { result ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isConnectionSuccessful) theme.successColor else Color.Red
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isConnectionSuccessful) "✅" else "❌",
                            fontSize = 20.sp,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                        Text(
                            text = result,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Box 1: Server URL Configuration
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = theme.cardColor
                ),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "🌐 Server URL",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.textColor
                    )

                    OutlinedTextField(
                        value = urlText,
                        onValueChange = {
                            urlText = it
                            // Clear previous test results when URL changes
                            connectionTestResult = null
                            isConnectionSuccessful = false
                        },
                        label = { Text("Enter Server URL") },
                        placeholder = { Text("https://api.foodapp.com") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = theme.primaryColor,
                            focusedLabelColor = theme.primaryColor,
                            focusedTextColor = theme.textColor,
                            unfocusedTextColor = theme.textColor,
                            unfocusedBorderColor = theme.textColor.copy(alpha = 0.5f),
                            unfocusedLabelColor = theme.textColor.copy(alpha = 0.7f)
                        )
                    )

                    // Initialize Connection Button
                    Button(
                        onClick = {
                            if (urlText.trim().isEmpty()) {
                                Toast.makeText(context, "Please enter server URL first", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            scope.launch {
                                isTestingConnection = true
                                connectionTestResult = null

                                try {
                                    // Initialize connection first
                                    when (val initResult = initializeConnectionUseCase(urlText.trim())) {
                                        is Result.Success -> {
                                            // Test the connection to confirm it works
                                            when (val testResult = testConnectionUseCase()) {
                                                is Result.Success -> {
                                                    connectionTestResult = "Connection initialized and tested successfully!"
                                                    isConnectionSuccessful = true
                                                }
                                                is Result.Error -> {
                                                    connectionTestResult = "Initialization successful but test failed: ${testResult.message}"
                                                    isConnectionSuccessful = false
                                                }
                                                Result.Loading -> {}
                                            }
                                        }
                                        is Result.Error -> {
                                            connectionTestResult = "Failed to initialize connection: ${initResult.message}"
                                            isConnectionSuccessful = false
                                        }
                                        Result.Loading -> {}
                                    }
                                } catch (e: Exception) {
                                    connectionTestResult = "Connection error: ${e.message}"
                                    isConnectionSuccessful = false
                                } finally {
                                    isTestingConnection = false
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = theme.primaryColor.copy(alpha = 0.8f)
                        ),
                        enabled = !isTestingConnection && urlText.trim().isNotEmpty()
                    ) {
                        if (isTestingConnection) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Text(
                                text = " Initializing...",
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        } else {
                            Text("🔗 Initialize Connection")
                        }
                    }

                    Text(
                        text = "Enter the server URL for food ordering service",
                        fontSize = 12.sp,
                        color = theme.textColor.copy(alpha = 0.6f)
                    )
                }
            }

            // Box 2: Password
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = theme.cardColor
                ),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "🔒 Password",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.textColor
                    )

                    OutlinedTextField(
                        value = passwordText,
                        onValueChange = { passwordText = it },
                        label = { Text("Enter Password") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = theme.primaryColor,
                            focusedLabelColor = theme.primaryColor,
                            focusedTextColor = theme.textColor,
                            unfocusedTextColor = theme.textColor,
                            unfocusedBorderColor = theme.textColor.copy(alpha = 0.5f),
                            unfocusedLabelColor = theme.textColor.copy(alpha = 0.7f)
                        )
                    )

                    Text(
                        text = "Password for authentication",
                        fontSize = 12.sp,
                        color = theme.textColor.copy(alpha = 0.6f)
                    )
                }
            }

            // Box 3: Re-enter Password
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = theme.cardColor
                ),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "🔑 Confirm Password",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.textColor
                    )

                    OutlinedTextField(
                        value = reenterPasswordText,
                        onValueChange = { reenterPasswordText = it },
                        label = { Text("Re-enter Password") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = theme.primaryColor,
                            focusedLabelColor = theme.primaryColor,
                            focusedTextColor = theme.textColor,
                            unfocusedTextColor = theme.textColor,
                            unfocusedBorderColor = theme.textColor.copy(alpha = 0.5f),
                            unfocusedLabelColor = theme.textColor.copy(alpha = 0.7f)
                        )
                    )

                    // Password match indicator
                    if (passwordText.isNotEmpty() && reenterPasswordText.isNotEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            if (passwordText == reenterPasswordText) {
                                Text(
                                    text = "✅ Passwords match",
                                    color = theme.successColor,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            } else {
                                Text(
                                    text = "❌ Passwords don't match",
                                    color = Color.Red,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    } else {
                        Text(
                            text = "Confirm your password",
                            fontSize = 12.sp,
                            color = theme.textColor.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Save Button
            Button(
                onClick = {
                    when {
                        urlText.trim().isEmpty() -> {
                            Toast.makeText(context, "Please enter server URL", Toast.LENGTH_SHORT).show()
                        }
                        !isConnectionSuccessful -> {
                            Toast.makeText(context, "Please initialize connection first", Toast.LENGTH_SHORT).show()
                        }
                        passwordText.isEmpty() -> {
                            Toast.makeText(context, "Please enter password", Toast.LENGTH_SHORT).show()
                        }
                        reenterPasswordText.isEmpty() -> {
                            Toast.makeText(context, "Please confirm password", Toast.LENGTH_SHORT).show()
                        }
                        passwordText != reenterPasswordText -> {
                            Toast.makeText(context, "Passwords don't match", Toast.LENGTH_SHORT).show()
                        }
                        else -> {
                            // Save configuration with password
                            with(sharedPreferences.edit()) {
                                putString("server_url", urlText.trim())
                                putString("app_password", passwordText)
                                putBoolean("connection_initialized", true) // Mark connection as initialized
                                putLong("config_updated", System.currentTimeMillis())
                                apply()
                            }
                            showSuccessMessage = true
                            Toast.makeText(context, "✅ Configuration and connection saved successfully!", Toast.LENGTH_LONG).show()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isConnectionSuccessful) theme.primaryColor else theme.primaryColor.copy(alpha = 0.5f),
                    contentColor = theme.textOnPrimary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "💾 Save Configuration",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}