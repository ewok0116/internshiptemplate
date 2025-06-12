package com.example.foodorderingapp.ui.theme

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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foodorderingapp.models.DemoFoodOrderingViewModel
import com.example.foodorderingapp.models.Product
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScrollablePage(viewModel: DemoFoodOrderingViewModel) {
    val theme = LocalAppTheme.current  // Get current theme
    var searchText by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf<Int?>(null) }
    var showSnackbar by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }

    // Show snackbar when needed
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.backgroundColor) // Use theme background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Search Bar
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

            // Category Filter Buttons
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

                item {
                    CategoryButton(
                        text = "Hamburgers",
                        isSelected = selectedCategoryId == 1,
                        onClick = {
                            selectedCategoryId = 1
                            searchText = ""
                        }
                    )
                }

                item {
                    CategoryButton(
                        text = "Drinks",
                        isSelected = selectedCategoryId == 2,
                        onClick = {
                            selectedCategoryId = 2
                            searchText = ""
                        }
                    )
                }

                item {
                    CategoryButton(
                        text = "Extras",
                        isSelected = selectedCategoryId == 3,
                        onClick = {
                            selectedCategoryId = 3
                            searchText = ""
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Products List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val filteredProducts = if (selectedCategoryId != null) {
                    val categoryProducts = viewModel.products.filter { it.cid == selectedCategoryId }
                    if (searchText.isNotBlank()) {
                        categoryProducts.filter {
                            it.pname.contains(searchText, ignoreCase = true) ||
                                    it.description.contains(searchText, ignoreCase = true)
                        }
                    } else {
                        categoryProducts
                    }
                } else {
                    viewModel.searchProducts(searchText)
                }

                items(filteredProducts) { product ->
                    ProductCard(
                        product = product,
                        onAddToCart = {
                            viewModel.addToCart(product)
                            snackbarMessage = "✅ ${product.pname} added to cart!"
                            showSnackbar = true
                        }
                    )
                }
            }
        }

        // Shopping Cart Button with theme colors
        if (viewModel.cartItemCount > 0) {
            FloatingActionButton(
                onClick = { viewModel.showCart() },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .size(100.dp),
                containerColor = theme.primaryColor // Use theme primary color
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
                            text = "${viewModel.cartItemCount}",
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

        // Snackbar Host with theme success color
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp),
            snackbar = { snackbarData ->
                Snackbar(
                    snackbarData = snackbarData,
                    containerColor = theme.successColor, // Use theme success color
                    contentColor = Color.White,
                    shape = RoundedCornerShape(8.dp)
                )
            }
        )
    }
}

@Composable
fun CategoryButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val theme = LocalAppTheme.current // Get current theme

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
    val theme = LocalAppTheme.current // Get current theme

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onAddToCart() },
        colors = CardDefaults.cardColors(
            containerColor = theme.cardColor // Use theme card color
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
                    text = product.pname,
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
                    text = "${product.price} TL",
                    color = theme.textOnPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}