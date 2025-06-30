// di/AppDependencies.kt - Complete DI Container
package com.example.foodorderingapp.di

import com.example.foodorderingapp.data.remote.ApiClient
import com.example.foodorderingapp.data.repositories.*
import com.example.foodorderingapp.domain.repositories.*
import com.example.foodorderingapp.domain.usecases.*

class AppDependencies {

    // Lazy initialization ensures repositories are created only when needed
    // and after ApiClient is initialized

    // Repositories
    private val productRepository: ProductRepository by lazy {
        ProductRepositoryImpl(ApiClient.getApiService())
    }

    private val categoryRepository: CategoryRepository by lazy {
        CategoryRepositoryImpl(ApiClient.getApiService())
    }

    private val orderRepository: OrderRepository by lazy {
        OrderRepositoryImpl(ApiClient.getApiService())
    }

    private val userRepository: UserRepository by lazy {
        UserRepositoryImpl(ApiClient.getApiService())
    }

    private val connectionRepository: ConnectionRepository by lazy {
        ConnectionRepositoryImpl()
    }

    // Use Cases
    val getProductsUseCase: GetProductsUseCase by lazy {
        GetProductsUseCase(productRepository)
    }

    val getCategoriesUseCase: GetCategoriesUseCase by lazy {
        GetCategoriesUseCase(categoryRepository)
    }

    val searchProductsUseCase: SearchProductsUseCase by lazy {
        SearchProductsUseCase(productRepository)
    }

    val getProductsByCategoryUseCase: GetProductsByCategoryUseCase by lazy {
        GetProductsByCategoryUseCase(productRepository)
    }

    val createOrderUseCase: CreateOrderUseCase by lazy {
        CreateOrderUseCase(orderRepository)
    }

    val updateOrderStatusUseCase: UpdateOrderStatusUseCase by lazy {
        UpdateOrderStatusUseCase(orderRepository)
    }

    val testConnectionUseCase: TestConnectionUseCase by lazy {
        TestConnectionUseCase(connectionRepository)
    }

    val initializeConnectionUseCase: InitializeConnectionUseCase by lazy {
        InitializeConnectionUseCase(connectionRepository)
    }
}

// MainActivity.kt - Updated for Clean Architecture
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.foodorderingapp.presentation.viewmodel.*
import com.example.foodorderingapp.presentation.di.FoodOrderingViewModelFactory
import com.example.foodorderingapp.ui.theme.*
import com.example.foodorderingapp.di.AppDependencies
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
            updateOrderStatusUseCase = appDependencies.updateOrderStatusUseCase,
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

    // All your existing dialogs work the same
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

// Example of how to test individual layers in isolation

// test/domain/usecases/GetProductsUseCaseTest.kt
package com.example.foodorderingapp.test.domain.usecases

import com.example.foodorderingapp.domain.entities.Product
import com.example.foodorderingapp.domain.repositories.ProductRepository
import com.example.foodorderingapp.domain.usecases.GetProductsUseCase
import com.example.foodorderingapp.domain.common.Result
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.Assert.*
import org.mockito.Mockito.*

class GetProductsUseCaseTest {

    @Test
    fun `getProducts returns success when repository succeeds`() = runTest {
        // Arrange
        val mockRepository = mock(ProductRepository::class.java)
        val expectedProducts = listOf(
            Product(1, "Burger", 10.0, "Delicious burger", 1, "🍔"),
            Product(2, "Pizza", 15.0, "Tasty pizza", 1, "🍕")
        )
        `when`(mockRepository.getProducts()).thenReturn(Result.Success(expectedProducts))

        val useCase = GetProductsUseCase(mockRepository)

        // Act
        val result = useCase()

        // Assert
        assertTrue(result is Result.Success)
        assertEquals(expectedProducts, (result as Result.Success).data)
        verify(mockRepository).getProducts()
    }

    @Test
    fun `getProducts returns error when repository fails`() = runTest {
        // Arrange
        val mockRepository = mock(ProductRepository::class.java)
        val errorMessage = "Network error"
        `when`(mockRepository.getProducts()).thenReturn(Result.Error(errorMessage))

        val useCase = GetProductsUseCase(mockRepository)

        // Act
        val result = useCase()

        // Assert
        assertTrue(result is Result.Error)
        assertEquals(errorMessage, (result as Result.Error).message)
        verify(mockRepository).getProducts()
    }
}

// test/data/repositories/ProductRepositoryImplTest.kt
package com.example.foodorderingapp.test.data.repositories

import com.example.foodorderingapp.data.remote.FoodOrderingApiService
import com.example.foodorderingapp.data.remote.ProductDto
import com.example.foodorderingapp.data.remote.ApiResponse
import com.example.foodorderingapp.data.repositories.ProductRepositoryImpl
import com.example.foodorderingapp.domain.common.Result
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.Assert.*
import org.mockito.Mockito.*
import retrofit2.Response

class ProductRepositoryImplTest {

    @Test
    fun `getProducts returns mapped products when API succeeds`() = runTest {
        // Arrange
        val mockApiService = mock(FoodOrderingApiService::class.java)
        val productDtos = listOf(
            ProductDto(1, "Burger", "Delicious", 10.0, 1, true),
            ProductDto(2, "Pizza", "Tasty", 15.0, 1, true)
        )
        val apiResponse = ApiResponse(true, "Success", productDtos)
        val response = Response.success(apiResponse)

        `when`(mockApiService.getProducts()).thenReturn(response)

        val repository = ProductRepositoryImpl(mockApiService)

        // Act
        val result = repository.getProducts()

        // Assert
        assertTrue(result is Result.Success)
        val products = (result as Result.Success).data
        assertEquals(2, products.size)
        assertEquals("Burger", products[0].name)
        assertEquals("🍔", products[0].imageUrl) // Tests emoji mapping
        verify(mockApiService).getProducts()
    }
}

/*
MIGRATION GUIDE - How to update your existing app:

1. **Update your build.gradle (Module: app)**:
   ```kotlin
   dependencies {
       // Add if not already present
       implementation "androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0"
       
       // For testing (optional)
       testImplementation "junit:junit:4.13.2"
       testImplementation "org.mockito:mockito-core:5.1.1"
       testImplementation "org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3"
   }
   ```

2. **File Structure** - Create these directories:
   ```
   app/src/main/java/com/example/foodorderingapp/
   ├── domain/
   │   ├── entities/
   │   ├── repositories/
   │   ├── usecases/
   │   └── common/
   ├── data/
   │   ├── remote/
   │   ├── repositories/
   │   └── mappers/
   ├── presentation/
   │   ├── viewmodel/
   │   └── di/
   ├── di/
   └── ui/theme/ (keep existing)
   ```

3. **Replace Files**:
   - Delete your existing `Models.kt`
   - Delete your existing `ApiService.kt` 
   - Create new files from the artifacts above
   - Update your `MainActivity.kt`
   - Keep your existing UI theme files

4. **Key Benefits**:
   ✅ **Testable**: Each layer can be tested independently
   ✅ **Maintainable**: Changes in one layer don't affect others
   ✅ **Scalable**: Easy to add new features
   ✅ **Clean**: Clear separation of concerns
   ✅ **Professional**: Follows Android development best practices

5. **What Stays the Same**:
   - All your UI themes and styling
   - All your Composable functions (just update ViewModel usage)
   - Your API endpoints and data structures
   - App functionality - everything works exactly the same

6. **What Changes**:
   - ViewModel now uses Use Cases instead of direct API calls
   - Business logic is in the Domain layer
   - Network code is properly abstracted
   - State management is cleaner and more predictable

7. **Testing Strategy**:
   - Test Use Cases with mocked repositories
   - Test Repositories with mocked API services
   - Test ViewModels with mocked Use Cases
   - UI tests remain the same
*/