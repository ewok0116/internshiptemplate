package com.example.foodorderingapp

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import com.example.foodorderingapp.models.*
import com.example.foodorderingapp.ui.theme.*
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FoodOrderingAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var currentScreen by remember { mutableStateOf("startup") }

                    when (currentScreen) {
                        "startup" -> StartupScreen(
                            onHomeClick = { currentScreen = "food_ordering" },
                            onConfigClick = { currentScreen = "config" }
                        )
                        "food_ordering" -> FoodOrderingApp(
                            onBackToStartup = { currentScreen = "startup" }
                        )
                        "config" -> ConfigScreen(
                            onBackToStartup = { currentScreen = "startup" }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StartupScreen(
    onHomeClick: () -> Unit,
    onConfigClick: () -> Unit
) {
    val theme = LocalAppTheme.current
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("food_app_settings", Context.MODE_PRIVATE)
    val serverUrl = sharedPreferences.getString("server_url", "") ?: ""

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
                verticalArrangement = Arrangement.spacedBy(24.dp)
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

                Button(
                    onClick = {
                        if (serverUrl.isEmpty()) {
                            Toast.makeText(
                                context,
                                "⚠️ Please configure database server URL first",
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            onHomeClick()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (serverUrl.isNotEmpty()) theme.primaryColor else Color.Gray,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    enabled = serverUrl.isNotEmpty()
                ) {
                    Text(
                        text = if (serverUrl.isNotEmpty()) "🏠" else "⚠️",
                        fontSize = 24.sp,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                    Text(
                        text = if (serverUrl.isNotEmpty()) "Connect to Database" else "Database Required",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

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

                if (serverUrl.isEmpty()) {
                    Text(
                        text = "💡 Tip: Configure your database connection in Settings to load real products and process orders.",
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

@Composable
fun FoodOrderingApp(onBackToStartup: () -> Unit = {}) {
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("food_app_settings", Context.MODE_PRIVATE)
    val serverUrl = sharedPreferences.getString("server_url", "") ?: ""

    // Create ViewModel with context for API calls
    val viewModel = remember { DemoFoodOrderingViewModel(context) }

    // Initialize API connection when entering this screen
    LaunchedEffect(serverUrl) {
        if (serverUrl.isNotEmpty()) {
            viewModel.initializeConnection(serverUrl)
        } else {
            Toast.makeText(context, "❌ No database server configured", Toast.LENGTH_LONG).show()
        }
    }

    // Clean up when leaving
    DisposableEffect(Unit) {
        onDispose {
            viewModel.onCleared()
        }
    }

    // Handle different loading states
    when {
        serverUrl.isEmpty() -> {
            NoServerConfiguredScreen(onBackToStartup = onBackToStartup)
        }

        viewModel.loadingState == LoadingState.LOADING -> {
            LoadingScreen()
        }

        viewModel.loadingState == LoadingState.ERROR -> {
            ErrorScreen(
                errorMessage = viewModel.errorMessage ?: "Unknown error",
                onRetry = { viewModel.refreshData() },
                onBackToStartup = onBackToStartup
            )
        }

        viewModel.loadingState == LoadingState.SUCCESS && viewModel.products.isEmpty() -> {
            EmptyDatabaseScreen(
                onRefresh = { viewModel.refreshData() },
                onBackToStartup = onBackToStartup
            )
        }

        viewModel.loadingState == LoadingState.SUCCESS && viewModel.products.isNotEmpty() -> {
            MainScrollablePage(
                viewModel = viewModel,
                onBackToStartup = onBackToStartup
            )
        }

        else -> {
            LoadingScreen()
        }
    }

    // All your existing dialogs work the same
    if (viewModel.showCartDialog) {
        CartPageDialog(viewModel = viewModel)
    }

    if (viewModel.showPaymentDialog) {
        when (viewModel.paymentState) {
            PaymentState.SELECTING -> PaymentMethodDialog(viewModel)
            PaymentState.PROCESSING -> PaymentProcessingDialog()
            PaymentState.SUCCESS -> PaymentSuccessDialog(
                viewModel = viewModel,
                paymentMethod = viewModel.selectedPaymentMethod
            )
            PaymentState.FAILED -> PaymentFailedDialog(viewModel)
            PaymentState.NONE -> Unit
        }
    }

    viewModel.errorMessage?.let { error ->
        LaunchedEffect(error) {
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
            delay(100)
            viewModel.clearError()
        }
    }
}

@Composable
fun NoServerConfiguredScreen(onBackToStartup: () -> Unit) {
    val theme = LocalAppTheme.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "💾",
                fontSize = 80.sp
            )

            Text(
                text = "Database Required",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = theme.textColor,
                textAlign = TextAlign.Center
            )

            Text(
                text = "This app requires a database connection to load products and process orders. Please configure your database server in Settings.",
                fontSize = 16.sp,
                color = theme.textColor.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )

            Button(
                onClick = onBackToStartup,
                colors = ButtonDefaults.buttonColors(
                    containerColor = theme.primaryColor
                )
            ) {
                Text(text = "⚙️", fontSize = 20.sp, modifier = Modifier.padding(end = 8.dp))
                Text("Go to Settings")
            }
        }
    }
}

@Composable
fun LoadingScreen() {
    val theme = LocalAppTheme.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                color = theme.primaryColor,
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = "Connecting to database...",
                color = theme.textColor,
                fontSize = 16.sp
            )
            Text(
                text = "Loading products and categories",
                color = theme.textColor.copy(alpha = 0.7f),
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun ErrorScreen(
    errorMessage: String,
    onRetry: () -> Unit,
    onBackToStartup: () -> Unit
) {
    val theme = LocalAppTheme.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "⚠️",
                fontSize = 64.sp
            )

            Text(
                text = "Database Connection Failed",
                color = theme.textColor,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Text(
                text = errorMessage,
                color = theme.textColor.copy(alpha = 0.7f),
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = theme.primaryColor
                    )
                ) {
                    Text(text = "🔄", fontSize = 16.sp, modifier = Modifier.padding(end = 8.dp))
                    Text("Retry")
                }

                OutlinedButton(
                    onClick = onBackToStartup,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = theme.primaryColor
                    )
                ) {
                    Text(text = "⚙️", fontSize = 16.sp, modifier = Modifier.padding(end = 8.dp))
                    Text("Settings")
                }
            }
        }
    }
}

@Composable
fun EmptyDatabaseScreen(
    onRefresh: () -> Unit,
    onBackToStartup: () -> Unit
) {
    val theme = LocalAppTheme.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "🍽️",
                fontSize = 64.sp
            )

            Text(
                text = "No Products Found",
                color = theme.textColor,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Connected to database but no products were found. Make sure your database has products in the Products table.",
                color = theme.textColor.copy(alpha = 0.7f),
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onRefresh,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = theme.primaryColor
                    )
                ) {
                    Text(text = "🔄", fontSize = 16.sp, modifier = Modifier.padding(end = 8.dp))
                    Text("Refresh")
                }

                OutlinedButton(
                    onClick = onBackToStartup,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = theme.primaryColor
                    )
                ) {
                    Text("Back")
                }
            }
        }
    }
}