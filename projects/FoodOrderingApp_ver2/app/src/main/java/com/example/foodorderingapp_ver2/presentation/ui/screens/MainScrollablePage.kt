// ui/screens/MainScrollablePage.kt
package com.example.foodorderingapp_ver2.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foodorderingapp_ver2.presentation.viewmodel.FoodOrderingViewModel
import com.example.foodorderingapp_ver2.domain.entities.Product
import com.example.foodorderingapp_ver2.presentation.ui.theme.LocalAppTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScrollablePage(
    viewModel: FoodOrderingViewModel,
    onBackToStartup: (() -> Unit)? = null
) {
    val theme = LocalAppTheme.current
    val uiState = viewModel.uiState
    var searchText by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf<Int?>(null) }
    var showSnackbar by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }

    // FIXED: Filter products in memory, no API calls!
    val filteredProducts = remember(searchText, selectedCategoryId, uiState.products) {
        val productsToFilter = if (selectedCategoryId != null) {
            // Filter by category in memory
            uiState.products.filter { it.categoryId == selectedCategoryId }
        } else {
            // Show all products
            uiState.products
        }

        if (searchText.isNotBlank()) {
            // Filter by search text in memory
            productsToFilter.filter {
                it.name.contains(searchText, ignoreCase = true) ||
                        it.description.contains(searchText, ignoreCase = true)
            }
        } else {
            productsToFilter
        }
    }

    // REMOVED: The LaunchedEffect that was calling API repeatedly!
    // LaunchedEffect(searchText, selectedCategoryId, uiState.products) { ... } // DELETED!

    LaunchedEffect(showSnackbar) {
        if (showSnackbar) {
            launch {
                snackbarHostState.showSnackbar(
                    message = snackbarMessage,
                    duration = SnackbarDuration.Indefinite
                )
            }
            delay(800)
            snackbarHostState.currentSnackbarData?.dismiss()
            showSnackbar = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.backgroundColor)
    ) {
        if (onBackToStartup != null) {
            TopAppBar(
                title = {
                    Text(
                        text = "Food Menu",
                        fontWeight = FontWeight.Bold,
                        color = theme.textOnPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackToStartup) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back to Startup",
                            tint = theme.textOnPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = theme.primaryColor,
                    titleContentColor = theme.textOnPrimary,
                    navigationIconContentColor = theme.textOnPrimary
                )
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(theme.backgroundColor)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp)),
                    placeholder = { Text("Search menu items...") },
                    trailingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    item {
                        CategoryButton(
                            text = "All",
                            isSelected = selectedCategoryId == null,
                            onClick = {
                                selectedCategoryId = null
                                searchText = ""
                            }
                        )
                    }

                    items(uiState.categories) { category ->
                        CategoryButton(
                            text = category.name,
                            isSelected = selectedCategoryId == category.id,
                            onClick = {
                                selectedCategoryId = category.id
                                searchText = ""
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // FIXED: Use filteredProducts directly, no API calls!
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredProducts) { product ->
                        ProductCard(
                            product = product,
                            onAddToCart = {
                                viewModel.addToCart(product)
                                snackbarMessage = "✅ ${product.name} added to cart!"
                                showSnackbar = true
                            }
                        )
                    }
                }
            }

            if (uiState.cartItemCount > 0) {
                FloatingActionButton(
                    onClick = { viewModel.showCart() },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                        .size(100.dp),
                    containerColor = theme.primaryColor
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Badge(
                            modifier = Modifier.offset(x = 12.dp, y = (-5).dp)
                                .size(32.dp)
                        ) {
                            Text(
                                text = "${uiState.cartItemCount}",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = "Shopping Cart",
                            tint = theme.textOnPrimary,
                            modifier = Modifier.size(38.dp)
                        )
                    }
                }
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp),
                snackbar = { snackbarData ->
                    Snackbar(
                        snackbarData = snackbarData,
                        containerColor = theme.successColor,
                        contentColor = Color.White,
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            )
        }
    }
}

@Composable
fun CategoryButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val theme = LocalAppTheme.current

    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) theme.primaryColor else Color.White,
            contentColor = if (isSelected) theme.textOnPrimary else theme.primaryColor
        ),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.height(40.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            fontSize = 14.sp
        )
    }
}

@Composable
fun ProductCard(
    product: Product,
    onAddToCart: () -> Unit
) {
    val theme = LocalAppTheme.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onAddToCart() },
        colors = CardDefaults.cardColors(
            containerColor = theme.cardColor
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = product.imageUrl,
                    fontSize = 30.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = product.name,
                    color = theme.textOnPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = product.description,
                    color = theme.textOnPrimary.copy(alpha = 0.9f),
                    fontSize = 12.sp
                )
                Text(
                    text = "₺${String.format("%.2f", product.price)}",
                    color = theme.textOnPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}