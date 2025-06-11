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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScrollablePage(viewModel: DemoFoodOrderingViewModel) {
    var searchText by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf<Int?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFC680)) // Background color
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
                // All button
                item {
                    CategoryButton(
                        text = "All",
                        isSelected = selectedCategoryId == null,
                        onClick = {
                            selectedCategoryId = null
                            searchText = "" // Clear search when switching category
                        }
                    )
                }

                // Hamburgers button
                item {
                    CategoryButton(
                        text = "Hamburgers",
                        isSelected = selectedCategoryId == 1,
                        onClick = {
                            selectedCategoryId = 1
                            searchText = "" // Clear search when switching category
                        }
                    )
                }

                // Drinks button
                item {
                    CategoryButton(
                        text = "Drinks",
                        isSelected = selectedCategoryId == 2,
                        onClick = {
                            selectedCategoryId = 2
                            searchText = "" // Clear search when switching category
                        }
                    )
                }

                // Extras button
                item {
                    CategoryButton(
                        text = "Extras",
                        isSelected = selectedCategoryId == 3,
                        onClick = {
                            selectedCategoryId = 3
                            searchText = "" // Clear search when switching category
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
                    // Filter by category first, then by search
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
                    // Show all products, filtered by search if any
                    viewModel.searchProducts(searchText)
                }

                items(filteredProducts) { product ->
                    ProductCard(
                        product = product,
                        onAddToCart = { viewModel.addToCart(product) }
                    )
                }
            }
        }

        // Shopping Cart Button (Floating)
        if (viewModel.cartItemCount > 0) {
            FloatingActionButton(
                onClick = { viewModel.showPayment() },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                containerColor = Color(0xFFFF8C42)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${viewModel.cartItemCount}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Cart",
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Color(0xFFFF8C42) else Color.White,
            contentColor = if (isSelected) Color.White else Color(0xFFFF8C42)
        ),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .height(40.dp),
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onAddToCart() },
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFF8C42) // Orange color matching screenshot
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Product Image (Emoji)
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
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = product.description,
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 12.sp
                )
                Text(
                    text = "${product.price} TL",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}