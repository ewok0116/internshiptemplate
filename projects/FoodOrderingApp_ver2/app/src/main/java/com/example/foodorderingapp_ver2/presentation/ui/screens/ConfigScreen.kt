// presentation/ui/screens/ConfigScreen.kt - With Encrypted Storage
package com.example.foodorderingapp_ver2.presentation.ui.screens
//Burda Kaldin
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foodorderingapp_ver2.presentation.ui.theme.LocalAppTheme
import com.example.foodorderingapp_ver2.data.repositories.ConnectionRepositoryImpl
import com.example.foodorderingapp_ver2.domain.usecases.TestConnectionUseCase
import com.example.foodorderingapp_ver2.domain.usecases.InitializeConnectionUseCase
import com.example.foodorderingapp_ver2.domain.common.Result
import com.example.foodorderingapp_ver2.data.preferences.ConfigHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigScreen(
    onBackToStartup: () -> Unit,
    onConnectionEstablished: () -> Unit = {}
) {
    val context = LocalContext.current
    val theme = LocalAppTheme.current
    val scope = rememberCoroutineScope()

    // Use ConfigHelper for encrypted storage
    val configHelper = remember { ConfigHelper.getInstance(context) }

    // Migrate any existing unencrypted data
    LaunchedEffect(Unit) {
        configHelper.migrateToEncryptedStorage(context)
    }

    // Check if this is first time (no previous connection established)
    val hasEstablishedConnection = configHelper.hasEstablishedConnectionOnce()

    // State for saving process
    var isSaving by remember { mutableStateOf(false) }
    var urlText by remember {
        mutableStateOf(configHelper.getServerUrl() ?: "")
    }
    var passwordText by remember {
        mutableStateOf(configHelper.getAppPassword() ?: "")
    }
    var reenterPasswordText by remember {
        mutableStateOf(configHelper.getAppPassword() ?: "")
    }
    var showSuccessMessage by remember { mutableStateOf(false) }
    var isTestingConnection by remember { mutableStateOf(false) }
    var connectionTestResult by remember { mutableStateOf<String?>(null) }
    var isConnectionSuccessful by remember { mutableStateOf(false) }

    // Password visibility states
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

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
        // Top App Bar with Back Button - Only show if connection has been established before
        if (hasEstablishedConnection) {
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
        } else {
            // Show title without back button for first time
            TopAppBar(
                title = {
                    Text(
                        text = "🔐 Secure Setup",
                        fontWeight = FontWeight.Bold,
                        color = theme.textOnPrimary
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = theme.primaryColor
                )
            )
        }

        // Scrollable Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // First time setup message with security info
            if (!hasEstablishedConnection) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = theme.primaryColor.copy(alpha = 0.1f)
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
                            text = "🔐",
                            fontSize = 20.sp,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                        Column {
                            Text(
                                text = "Secure Configuration",
                                color = theme.textColor,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Your credentials will be encrypted and stored securely on this device.",
                                color = theme.textColor.copy(alpha = 0.7f),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

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
                            text = "Configuration saved securely!",
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
                            // Clear previous test results when URL changes, but keep the URL value
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
                            unfocusedLabelColor = theme.textColor.copy(alpha = 0.7f),
                            cursorColor = theme.primaryColor
                        )
                    )

                    Text(
                        text = "Enter the server URL for food ordering service. Connection will be tested when you save.",
                        fontSize = 12.sp,
                        color = theme.textColor.copy(alpha = 0.6f)
                    )
                }
            }

            // Box 2: Password (Encrypted)
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🔒 Password",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = theme.textColor
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "🔐 Encrypted",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = theme.primaryColor,
                            modifier = Modifier
                                .background(
                                    theme.primaryColor.copy(alpha = 0.1f),
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    OutlinedTextField(
                        value = passwordText,
                        onValueChange = { passwordText = it },
                        label = { Text("Enter Password") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                    tint = theme.textColor.copy(alpha = 0.6f)
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = theme.primaryColor,
                            focusedLabelColor = theme.primaryColor,
                            focusedTextColor = theme.textColor,
                            unfocusedTextColor = theme.textColor,
                            unfocusedBorderColor = theme.textColor.copy(alpha = 0.5f),
                            unfocusedLabelColor = theme.textColor.copy(alpha = 0.7f),
                            cursorColor = theme.primaryColor
                        )
                    )

                    Text(
                        text = "Password for authentication (encrypted storage)",
                        fontSize = 12.sp,
                        color = theme.textColor.copy(alpha = 0.6f)
                    )
                }
            }

            // Box 3: Re-enter Password (Encrypted)
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🔑 Confirm Password",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = theme.textColor
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "🔐 Encrypted",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = theme.primaryColor,
                            modifier = Modifier
                                .background(
                                    theme.primaryColor.copy(alpha = 0.1f),
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    OutlinedTextField(
                        value = reenterPasswordText,
                        onValueChange = { reenterPasswordText = it },
                        label = { Text("Re-enter Password") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                Icon(
                                    imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password",
                                    tint = theme.textColor.copy(alpha = 0.6f)
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = theme.primaryColor,
                            focusedLabelColor = theme.primaryColor,
                            focusedTextColor = theme.textColor,
                            unfocusedTextColor = theme.textColor,
                            unfocusedBorderColor = theme.textColor.copy(alpha = 0.5f),
                            unfocusedLabelColor = theme.textColor.copy(alpha = 0.7f),
                            cursorColor = theme.primaryColor
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

            // Add some bottom spacing before the save button
            Spacer(modifier = Modifier.height(20.dp))

            // Save Configuration Button - Does Everything in One Step
            Button(
                onClick = {
                    when {
                        urlText.trim().isEmpty() -> {
                            Toast.makeText(context, "Please enter server URL", Toast.LENGTH_SHORT).show()
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
                            // Do everything in one step: Initialize connection + Save configuration securely
                            scope.launch {
                                isSaving = true
                                connectionTestResult = null

                                try {
                                    // Step 1: Initialize connection
                                    when (val initResult = initializeConnectionUseCase(urlText.trim())) {
                                        is Result.Success -> {
                                            // Step 2: Test the connection
                                            when (val testResult = testConnectionUseCase()) {
                                                is Result.Success -> {
                                                    // Step 3: Save everything if connection works (using encrypted storage)
                                                    // This saves the password that ConfigPasswordDialog will use
                                                    configHelper.saveCompleteConfiguration(
                                                        serverUrl = urlText.trim(),
                                                        password = passwordText
                                                    )

                                                    connectionTestResult = "Connection Established"
                                                    isConnectionSuccessful = true
                                                    showSuccessMessage = true
                                                    Toast.makeText(context, "✅ Configuration saved securely!", Toast.LENGTH_LONG).show()

                                                    // Don't clear the fields - keep URL and password visible
                                                    // Trigger connection established callback
                                                    onConnectionEstablished()
                                                }
                                                is Result.Error -> {
                                                    connectionTestResult = "❌ Connection test failed: ${testResult.message}"
                                                    isConnectionSuccessful = false
                                                    Toast.makeText(context, "❌ Connection failed: ${testResult.message}", Toast.LENGTH_LONG).show()
                                                    // Keep URL and password fields filled even on failure
                                                }
                                                Result.Loading -> {}
                                            }
                                        }
                                        is Result.Error -> {
                                            connectionTestResult = "❌ Failed to initialize connection: ${initResult.message}"
                                            isConnectionSuccessful = false
                                            Toast.makeText(context, "❌ Connection failed: ${initResult.message}", Toast.LENGTH_LONG).show()
                                            // Keep URL and password fields filled even on failure
                                        }
                                        Result.Loading -> {}
                                    }
                                } catch (e: Exception) {
                                    connectionTestResult = "❌ Connection error: ${e.message}"
                                    isConnectionSuccessful = false
                                    Toast.makeText(context, "❌ Connection error: ${e.message}", Toast.LENGTH_LONG).show()
                                    // Keep URL and password fields filled even on error
                                } finally {
                                    isSaving = false
                                }
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (urlText.trim().isNotEmpty() && passwordText.isNotEmpty() && reenterPasswordText.isNotEmpty() && passwordText == reenterPasswordText)
                        theme.primaryColor
                    else
                        theme.primaryColor.copy(alpha = 0.5f),
                    contentColor = theme.textOnPrimary
                ),
                shape = RoundedCornerShape(12.dp),
                enabled = !isSaving
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Text(
                        text = " Saving & Testing Connection...",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                } else {
                    Text(
                        text = if (!hasEstablishedConnection) "🔐 Setup & Launch App" else "🔐 Save Secure Configuration",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Add bottom padding to ensure button is always visible
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}