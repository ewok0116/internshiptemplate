// presentation/viewmodel/ConnectionManager.kt
package com.example.foodorderingapp_ver2.presentation.viewmodel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import android.util.Log
import com.example.foodorderingapp_ver2.domain.usecases.*
import com.example.foodorderingapp_ver2.domain.common.Result


class ConnectionManager(
    private val initializeConnectionUseCase: InitializeConnectionUseCase,
    private val testConnectionUseCase: TestConnectionUseCase,
    private val getProductsUseCase: GetProductsUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val coroutineScope: CoroutineScope,
    private val onShowToast: (String) -> Unit,
    private val onStateUpdate: (FoodOrderingUiState) -> Unit,
    private val getCurrentState: () -> FoodOrderingUiState
) {

    // =====================================================
    // CONNECTION METHODS
    // =====================================================

    fun initializeConnection(serverUrl: String) {
        coroutineScope.launch {
            val currentState = getCurrentState()
            onStateUpdate(currentState.copy(loadingState = LoadingStateUi.LOADING))
            onShowToast("🔄 Connecting to database...")

            when (val result = initializeConnectionUseCase(serverUrl)) {
                is Result.Success -> {
                    val updatedState = getCurrentState()
                    onStateUpdate(updatedState.copy(isConnectedToDatabase = true))
                    onShowToast("✅ Connection successful")
                    loadAllData()
                }

                is Result.Error -> {
                    handleConnectionError(result.message)
                }

                Result.Loading -> {
                    // Handle loading state if needed
                }
            }
        }
    }

    suspend fun loadAllData() {
        try {
            // Load products and categories in parallel
            val productsResult = getProductsUseCase()
            val categoriesResult = getCategoriesUseCase()

            val currentState = getCurrentState()

            when (productsResult) {
                is Result.Success -> {
                    onStateUpdate(currentState.copy(products = productsResult.data))
                    Log.d("ConnectionManager", "Loaded ${productsResult.data.size} products")
                }

                is Result.Error -> {
                    Log.e("ConnectionManager", "Failed to load products: ${productsResult.message}")
                    val updatedState = getCurrentState()
                    onStateUpdate(updatedState.copy(errorMessage = productsResult.message))
                }

                Result.Loading -> {}
            }

            when (categoriesResult) {
                is Result.Success -> {
                    val updatedState = getCurrentState()
                    onStateUpdate(updatedState.copy(categories = categoriesResult.data))
                    Log.d("ConnectionManager", "Loaded ${categoriesResult.data.size} categories")
                }

                is Result.Error -> {
                    Log.e("ConnectionManager", "Failed to load categories: ${categoriesResult.message}")
                    // Categories are optional, so we don't fail the entire load
                }

                Result.Loading -> {}
            }

            val finalState = getCurrentState()
            if (finalState.products.isNotEmpty()) {
                onStateUpdate(finalState.copy(loadingState = LoadingStateUi.SUCCESS))
                onShowToast("📦 Data loaded successfully")
            } else {
                onStateUpdate(finalState.copy(
                    loadingState = LoadingStateUi.ERROR,
                    errorMessage = "No products found in database"
                ))
            }

        } catch (e: Exception) {
            Log.e("ConnectionManager", "Error loading data", e)
            val currentState = getCurrentState()
            onStateUpdate(currentState.copy(
                loadingState = LoadingStateUi.ERROR,
                errorMessage = "Failed to load data: ${e.message}"
            ))
        }
    }

    private fun handleConnectionError(message: String) {
        val currentState = getCurrentState()
        onStateUpdate(currentState.copy(
            isConnectedToDatabase = false,
            loadingState = LoadingStateUi.ERROR,
            errorMessage = message,
            products = emptyList(),
            categories = emptyList()
        ))
        onShowToast("❌ $message")
    }

    fun loadDataDirectly() {
        coroutineScope.launch {
            val currentState = getCurrentState()
            onStateUpdate(currentState.copy(
                loadingState = LoadingStateUi.LOADING,
                isConnectedToDatabase = true // We know connection is already established
            ))

            try {
                // Since connection is already established and tested, directly load data
                loadAllData()
            } catch (e: Exception) {
                Log.e("ConnectionManager", "Error loading data directly", e)
                val updatedState = getCurrentState()
                onStateUpdate(updatedState.copy(
                    loadingState = LoadingStateUi.ERROR,
                    errorMessage = "Failed to load data: ${e.message}"
                ))
            }
        }
    }

    fun refreshData() {
        coroutineScope.launch {
            val currentState = getCurrentState()
            onStateUpdate(currentState.copy(loadingState = LoadingStateUi.LOADING))

            when (val result = testConnectionUseCase()) {
                is Result.Success -> {
                    loadAllData()
                }

                is Result.Error -> {
                    handleConnectionError(result.message)
                }

                Result.Loading -> {}
            }
        }
    }

    // =====================================================
    // UTILITY METHODS// presentation/viewmodel/ConnectionManager.kt
    //package com.example.foodorderingapp_ver2.presentation.viewmodel
    //
    //import kotlinx.coroutines.CoroutineScope
    //import kotlinx.coroutines.launch
    //import android.util.Log
    //import com.example.foodorderingapp_ver2.domain.usecases.*
    //import com.example.foodorderingapp_ver2.domain.common.Result
    //
    //
    //class ConnectionManager(
    //    private val initializeConnectionUseCase: InitializeConnectionUseCase,
    //    private val testConnectionUseCase: TestConnectionUseCase,
    //    private val getProductsUseCase: GetProductsUseCase,
    //    private val getCategoriesUseCase: GetCategoriesUseCase,
    //    private val coroutineScope: CoroutineScope,
    //    private val onShowToast: (String) -> Unit,
    //    private val onStateUpdate: (FoodOrderingUiState) -> Unit,
    //    private val getCurrentState: () -> FoodOrderingUiState
    //) {
    //
    //    // =====================================================
    //    // CONNECTION METHODS
    //    // =====================================================
    //
    //    fun initializeConnection(serverUrl: String) {
    //        coroutineScope.launch {
    //            val currentState = getCurrentState()
    //            onStateUpdate(currentState.copy(loadingState = LoadingStateUi.LOADING))
    //            onShowToast("🔄 Connecting to database...")
    //
    //            when (val result = initializeConnectionUseCase(serverUrl)) {
    //                is Result.Success -> {
    //                    val updatedState = getCurrentState()
    //                    onStateUpdate(updatedState.copy(isConnectedToDatabase = true))
    //                    onShowToast("✅ Connection successful")
    //                    loadAllData()
    //                }
    //
    //                is Result.Error -> {
    //                    handleConnectionError(result.message)
    //                }
    //
    //                Result.Loading -> {
    //                    // Handle loading state if needed
    //                }
    //            }
    //        }
    //    }
    //
    //    suspend fun loadAllData() {
    //        try {
    //            // Load products and categories in parallel
    //            val productsResult = getProductsUseCase()
    //            val categoriesResult = getCategoriesUseCase()
    //
    //            val currentState = getCurrentState()
    //
    //            when (productsResult) {
    //                is Result.Success -> {
    //                    onStateUpdate(currentState.copy(products = productsResult.data))
    //                    Log.d("ConnectionManager", "Loaded ${productsResult.data.size} products")
    //                }
    //
    //                is Result.Error -> {
    //                    Log.e("ConnectionManager", "Failed to load products: ${productsResult.message}")
    //                    val updatedState = getCurrentState()
    //                    onStateUpdate(updatedState.copy(errorMessage = productsResult.message))
    //                }
    //
    //                Result.Loading -> {}
    //            }
    //
    //            when (categoriesResult) {
    //                is Result.Success -> {
    //                    val updatedState = getCurrentState()
    //                    onStateUpdate(updatedState.copy(categories = categoriesResult.data))
    //                    Log.d("ConnectionManager", "Loaded ${categoriesResult.data.size} categories")
    //                }
    //
    //                is Result.Error -> {
    //                    Log.e("ConnectionManager", "Failed to load categories: ${categoriesResult.message}")
    //                    // Categories are optional, so we don't fail the entire load
    //                }
    //
    //                Result.Loading -> {}
    //            }
    //
    //            val finalState = getCurrentState()
    //            if (finalState.products.isNotEmpty()) {
    //                onStateUpdate(finalState.copy(loadingState = LoadingStateUi.SUCCESS))
    //                onShowToast("📦 Data loaded successfully")
    //            } else {
    //                onStateUpdate(finalState.copy(
    //                    loadingState = LoadingStateUi.ERROR,
    //                    errorMessage = "No products found in database"
    //                ))
    //            }
    //
    //        } catch (e: Exception) {
    //            Log.e("ConnectionManager", "Error loading data", e)
    //            val currentState = getCurrentState()
    //            onStateUpdate(currentState.copy(
    //                loadingState = LoadingStateUi.ERROR,
    //                errorMessage = "Failed to load data: ${e.message}"
    //            ))
    //        }
    //    }
    //
    //    private fun handleConnectionError(message: String) {
    //        val currentState = getCurrentState()
    //        onStateUpdate(currentState.copy(
    //            isConnectedToDatabase = false,
    //            loadingState = LoadingStateUi.ERROR,
    //            errorMessage = message,
    //            products = emptyList(),
    //            categories = emptyList()
    //        ))
    //        onShowToast("❌ $message")
    //    }
    //
    //    fun loadDataDirectly() {
    //        coroutineScope.launch {
    //            val currentState = getCurrentState()
    //            onStateUpdate(currentState.copy(
    //                loadingState = LoadingStateUi.LOADING,
    //                isConnectedToDatabase = true // We know connection is already established
    //            ))
    //
    //            try {
    //                // Since connection is already established and tested, directly load data
    //                loadAllData()
    //            } catch (e: Exception) {
    //                Log.e("ConnectionManager", "Error loading data directly", e)
    //                val updatedState = getCurrentState()
    //                onStateUpdate(updatedState.copy(
    //                    loadingState = LoadingStateUi.ERROR,
    //                    errorMessage = "Failed to load data: ${e.message}"
    //                ))
    //            }
    //        }
    //    }
    //
    //    fun refreshData() {
    //        coroutineScope.launch {
    //            val currentState = getCurrentState()
    //            onStateUpdate(currentState.copy(loadingState = LoadingStateUi.LOADING))
    //
    //            when (val result = testConnectionUseCase()) {
    //                is Result.Success -> {
    //                    loadAllData()
    //                }
    //
    //                is Result.Error -> {
    //                    handleConnectionError(result.message)
    //                }
    //
    //                Result.Loading -> {}
    //            }
    //        }
    //    }
    //
    //    // =====================================================
    //    // UTILITY METHODS
    //    // =====================================================
    //
    //    fun isConnected(): Boolean {
    //        return getCurrentState().isConnectedToDatabase
    //    }
    //
    //    fun isDataLoaded(): Boolean {
    //        val state = getCurrentState()
    //        return state.loadingState == LoadingStateUi.SUCCESS && state.products.isNotEmpty()
    //    }
    //
    //    fun getConnectionStatus(): String {
    //        val state = getCurrentState()
    //        return when {
    //            !state.isConnectedToDatabase -> "Disconnected"
    //            state.loadingState == LoadingStateUi.LOADING -> "Loading..."
    //            state.loadingState == LoadingStateUi.SUCCESS -> "Connected & Data Loaded"
    //            state.loadingState == LoadingStateUi.ERROR -> "Error: ${state.errorMessage}"
    //            else -> "Idle"
    //        }
    //    }
    //}
    // =====================================================

    fun isConnected(): Boolean {
        return getCurrentState().isConnectedToDatabase
    }

    fun isDataLoaded(): Boolean {
        val state = getCurrentState()
        return state.loadingState == LoadingStateUi.SUCCESS && state.products.isNotEmpty()
    }

    fun getConnectionStatus(): String {
        val state = getCurrentState()
        return when {
            !state.isConnectedToDatabase -> "Disconnected"
            state.loadingState == LoadingStateUi.LOADING -> "Loading..."
            state.loadingState == LoadingStateUi.SUCCESS -> "Connected & Data Loaded"
            state.loadingState == LoadingStateUi.ERROR -> "Error: ${state.errorMessage}"
            else -> "Idle"
        }
    }
}