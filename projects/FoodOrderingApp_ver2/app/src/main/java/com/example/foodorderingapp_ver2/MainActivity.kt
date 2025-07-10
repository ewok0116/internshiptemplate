// MainActivity.kt - With Password Protection for Config Screen
package com.example.foodorderingapp_ver2
//Burda Kaldin
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
import com.example.foodorderingapp_ver2.presentation.di.FoodOrderingViewModelFactory
import com.example.foodorderingapp_ver2.presentation.ui.screens.ConfigScreen
import com.example.foodorderingapp_ver2.presentation.ui.dialogs.ConfigPasswordDialog
import com.example.foodorderingapp_ver2.data.preferences.ConfigHelper
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    private lateinit var appDependencies: AppDependencies // lateinit means "I promise to initialize this before using it"

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
                    val context = LocalContext.current

                    // Use ConfigHelper for encrypted storage
                    val configHelper = remember { ConfigHelper.getInstance(context) }

                    /*ConfigHelper.getInstance(context) creates or gets a singleton instance for managing encrypted storage
                    remember ensures this expensive object is only created once (not on every UI refresh)
                    ConfigHelper handles saving/loading encrypted configuration data (like database settings)*/

                    // Check if connection has been established before
                    val hasEstablishedConnection = configHelper.hasEstablishedConnectionOnce()

                    var currentScreen by remember {
                        mutableStateOf(
                            // If no connection established, go directly to config (first time)
                            // If connection established, go to startup (returning user)
                            if (hasEstablishedConnection) "startup" else "config"
                        )
                    }
                    var showPasswordDialog by remember { mutableStateOf(false) }

                    when (currentScreen) {
                        "startup" -> StartupScreen(
                            onHomeClick = { currentScreen = "food_ordering" },
                            onConfigClick = {
                                // Check if password exists, if yes show dialog, if no go directly
                                if (configHelper.hasPasswordBeenSet()) {
                                    showPasswordDialog = true
                                } else {
                                    currentScreen = "config"
                                }
                            },
                            isFirstTime = false,
                            onConnectionEstablished = {
                                // Already established, just update timestamp
                                configHelper.setConfigUpdatedTimestamp(System.currentTimeMillis())
                            }
                        )
                        "food_ordering" -> FoodOrderingApp(
                            appDependencies = appDependencies,
                            onBackToStartup = { currentScreen = "startup" }
                        )
                        "config" -> ConfigScreen(
                            onBackToStartup = {
                                // After config, if connection established, go to startup
                                currentScreen = if (configHelper.hasEstablishedConnectionOnce()) "startup" else "config"
                            },
                            onConnectionEstablished = {
                                // Save that connection has been established
                                configHelper.setConnectionEstablishedOnce(true)
                                // After first successful config, go to food ordering automatically
                                currentScreen = "food_ordering"
                            }
                        )
                    }

                    // Password Dialog - Only shown when accessing config from startup screen
                    // Uses the password that was set in ConfigScreen
                    if (showPasswordDialog) {
                        ConfigPasswordDialog(
                            onPasswordCorrect = {
                                showPasswordDialog = false
                                currentScreen = "config"
                            },
                            onDismiss = {
                                showPasswordDialog = false
                            }
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

    // Create ViewModel with dependencies - ApiClient should already be initialized from StartupScreen
    val viewModel: FoodOrderingViewModel = viewModel(
        factory = FoodOrderingViewModelFactory(
            getProductsUseCase = appDependencies.getProductsUseCase(),
            getCategoriesUseCase = appDependencies.getCategoriesUseCase(),
            searchProductsUseCase = appDependencies.searchProductsUseCase(),
            getProductsByCategoryUseCase = appDependencies.getProductsByCategoryUseCase(),
            createOrderUseCase = appDependencies.createOrderUseCase(),
            testConnectionUseCase = appDependencies.testConnectionUseCase(),
            initializeConnectionUseCase = appDependencies.initializeConnectionUseCase(),
            onShowToast = { message ->
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        )
    )

    val uiState = viewModel.uiState

    // Load data immediately since connection is already established and tested
    LaunchedEffect(Unit) {
        viewModel.loadDataDirectly()
    }

    // Handle different loading states
    when {
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