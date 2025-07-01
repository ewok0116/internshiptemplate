// MainActivity.kt - Updated for new UI structure
package com.example.foodorderingapp_ver2

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.foodorderingapp_ver2.presentation.viewmodel.*
import com.example.foodorderingapp_ver2.presentation.ui.*
import com.example.foodorderingapp_ver2.presentation.ui.screens.*
import com.example.foodorderingapp_ver2.presentation.ui.theme.*
import com.example.foodorderingapp_ver2.presentation.ui.dialogs.*
import com.example.foodorderingapp_ver2.presentation.di.AppDependencies
import com.example.foodorderingapp_ver2.presentation.di.FoodOrderingViewModelFactory  // ADD THIS IMPORT
import com.example.foodorderingapp_ver2.presentation.ui.screens.ConfigScreen

import kotlinx.coroutines.delay



class MainActivity : ComponentActivity() {
    private lateinit var appDependencies: AppDependencies

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize dependencies
        appDependencies = AppDependencies()

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
                            appDependencies = appDependencies,
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
fun FoodOrderingApp(
    appDependencies: AppDependencies,
    onBackToStartup: () -> Unit = {}
) {
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("food_app_settings", Context.MODE_PRIVATE)
    val serverUrl = sharedPreferences.getString("server_url", "") ?: ""

    // Create ViewModel with dependencies
    val viewModel: FoodOrderingViewModel = viewModel(
        factory = FoodOrderingViewModelFactory(
            getProductsUseCase = appDependencies.getProductsUseCase,
            getCategoriesUseCase = appDependencies.getCategoriesUseCase,
            searchProductsUseCase = appDependencies.searchProductsUseCase,
            getProductsByCategoryUseCase = appDependencies.getProductsByCategoryUseCase,
            createOrderUseCase = appDependencies.createOrderUseCase,
            testConnectionUseCase = appDependencies.testConnectionUseCase,
            initializeConnectionUseCase = appDependencies.initializeConnectionUseCase,
            onShowToast = { message ->
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        )
    )

    val uiState = viewModel.uiState

    // Initialize API connection when entering this screen
    LaunchedEffect(serverUrl) {
        if (serverUrl.isNotEmpty()) {
            viewModel.initializeConnection(serverUrl)
        } else {
            Toast.makeText(context, "❌ No database server configured", Toast.LENGTH_LONG).show()
        }
    }

    // Handle different loading states
    when {
        serverUrl.isEmpty() -> {
            NoServerConfiguredScreen(onBackToStartup = onBackToStartup)
        }

        uiState.loadingState == LoadingStateUi.LOADING -> {
            LoadingScreen()
        }

        uiState.loadingState == LoadingStateUi.ERROR -> {
            ErrorScreen(
                errorMessage = uiState.errorMessage ?: "Unknown error",
                onRetry = { viewModel.refreshData() },
                onBackToStartup = onBackToStartup
            )
        }

        uiState.loadingState == LoadingStateUi.SUCCESS && uiState.products.isEmpty() -> {
            EmptyDatabaseScreen(
                onRefresh = { viewModel.refreshData() },
                onBackToStartup = onBackToStartup
            )
        }

        uiState.loadingState == LoadingStateUi.SUCCESS && uiState.products.isNotEmpty() -> {
            MainScrollablePage(
                viewModel = viewModel,
                onBackToStartup = onBackToStartup
            )
        }

        else -> {
            LoadingScreen()
        }
    }

    // All your dialogs
    if (uiState.showCartDialog) {
        CartPageDialog(viewModel = viewModel)
    }

    if (uiState.showPaymentDialog) {
        when (uiState.paymentState) {
            PaymentStateUi.SELECTING -> PaymentMethodDialog(viewModel)
            PaymentStateUi.PROCESSING -> PaymentProcessingDialog()
            PaymentStateUi.SUCCESS -> PaymentSuccessDialog(
                viewModel = viewModel,
                paymentMethod = uiState.selectedPaymentMethod
            )
            PaymentStateUi.FAILED -> PaymentFailedDialog(viewModel)
            PaymentStateUi.NONE -> Unit
        }
    }

    uiState.errorMessage?.let { error ->
        LaunchedEffect(error) {
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
            delay(100)
            viewModel.clearError()
        }
    }
}