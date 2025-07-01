// di/AppDependencies.kt - Complete DI Container
package com.example.foodorderingapp_ver2.presentation.di

import com.example.foodorderingapp_ver2.data.remote.ApiClient
import com.example.foodorderingapp_ver2.data.repositories.*
import com.example.foodorderingapp_ver2.domain.repositories.*
import com.example.foodorderingapp_ver2.domain.usecases.*

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


    val testConnectionUseCase: TestConnectionUseCase by lazy {
        TestConnectionUseCase(connectionRepository)
    }

    val initializeConnectionUseCase: InitializeConnectionUseCase by lazy {
        InitializeConnectionUseCase(connectionRepository)
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