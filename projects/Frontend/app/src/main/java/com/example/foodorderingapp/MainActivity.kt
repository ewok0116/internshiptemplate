// MainActivity.kt - Complete with ConfigScreen
package com.example.foodorderingapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foodorderingapp.models.DemoFoodOrderingViewModel
import com.example.foodorderingapp.models.PaymentState
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
                    // State to control which screen to show
                    var currentScreen by remember { mutableStateOf("startup") }

                    when (currentScreen) {
                        "startup" -> StartupScreen(
                            onHomeClick = { currentScreen = "food_ordering" },
                            onConfigClick = { currentScreen = "config" } // UPDATED: Go to config screen
                        )
                        "food_ordering" -> FoodOrderingApp(
                            onBackToStartup = { currentScreen = "startup" }
                        )
                        "config" -> ConfigScreen( // ADDED: Config screen case
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
    val theme = LocalAppTheme.current // Get current theme
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
                // App Title
                Text(
                    text = "Food Ordering App",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.textColor,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Welcome! Choose an option to get started.",
                    fontSize = 16.sp,
                    color = theme.textColor,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Home Button - goes to your food ordering screen
                Button(
                    onClick = onHomeClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = theme.primaryColor
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Home",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Config Button - goes to config screen
                Button(
                    onClick = onConfigClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = theme.primaryColor
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Settings",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}


// UPDATED: Add onBackToStartup parameter to FoodOrderingApp
@Composable
fun FoodOrderingApp(onBackToStartup: () -> Unit = {}) { // ADD DEFAULT PARAMETER
    val viewModel = remember { DemoFoodOrderingViewModel() }

    // Main Screen (your MainScrollablePage) - PASS THE CALLBACK
    MainScrollablePage(
        viewModel = viewModel,
        onBackToStartup = onBackToStartup // ADD THIS LINE
    )

    // Cart Dialog
    if (viewModel.showCartDialog) {
        CartPageDialog(viewModel = viewModel)
    }

    // Payment Dialogs
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
}