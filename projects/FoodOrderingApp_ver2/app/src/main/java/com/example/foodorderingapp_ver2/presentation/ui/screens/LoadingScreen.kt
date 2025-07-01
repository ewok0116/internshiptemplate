
// ui/screens/LoadingScreens.kt
package com.example.foodorderingapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foodorderingapp.ui.theme.LocalAppTheme

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
