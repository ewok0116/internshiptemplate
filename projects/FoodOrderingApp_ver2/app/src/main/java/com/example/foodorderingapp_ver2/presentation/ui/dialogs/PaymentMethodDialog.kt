
// ui/dialogs/PaymentDialogs.kt
package com.example.foodorderingapp.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.foodorderingapp.presentation.viewmodel.FoodOrderingViewModel
import com.example.foodorderingapp.domain.entities.PaymentMethod
import com.example.foodorderingapp.ui.theme.LocalAppTheme

@Composable
fun PaymentMethodDialog(viewModel: FoodOrderingViewModel) {
    val theme = LocalAppTheme.current
    val uiState = viewModel.uiState
    var selectedPaymentMethod by remember { mutableStateOf(PaymentMethod.CREDIT_CARD) }

    Dialog(
        onDismissRequest = { /* Non-cancellable */ },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(theme.primaryColor)
                .padding(20.dp)
        ) {
            Column {
                // Header with Back Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            viewModel.hidePayment()
                            viewModel.showCart()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White.copy(alpha = 0.2f)
                        )
                    ) {
                        Text(
                            text = "⬅️ Back",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }

                    Text(
                        text = "Payment Method",
                        color = theme.textOnPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.width(80.dp))
                }

                Text(
                    text = "Price: ${String.format("%.2f", uiState.cartTotal)} TL",
                    color = theme.textOnPrimary,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Payment Methods
                PaymentMethodOption(
                    emoji = "💳",
                    text = "5168 **** Credit Card",
                    isSelected = selectedPaymentMethod == PaymentMethod.CREDIT_CARD,
                    onClick = { selectedPaymentMethod = PaymentMethod.CREDIT_CARD }
                )

                Spacer(modifier = Modifier.height(12.dp))

                PaymentMethodOption(
                    emoji = "💵",
                    text = "Cash",
                    isSelected = selectedPaymentMethod == PaymentMethod.CASH,
                    onClick = { selectedPaymentMethod = PaymentMethod.CASH }
                )

                Spacer(modifier = Modifier.height(12.dp))

                PaymentMethodOption(
                    emoji = "🎫",
                    text = "Coupon",
                    isSelected = selectedPaymentMethod == PaymentMethod.COUPON,
                    onClick = { selectedPaymentMethod = PaymentMethod.COUPON }
                )

                Spacer(modifier = Modifier.height(12.dp))

                PaymentMethodOption(
                    emoji = "💳",
                    text = "Sodexo",
                    isSelected = selectedPaymentMethod == PaymentMethod.SODEXO,
                    onClick = { selectedPaymentMethod = PaymentMethod.SODEXO }
                )

                Spacer(modifier = Modifier.height(12.dp))

                PaymentMethodOption(
                    emoji = "💳",
                    text = "Multinet",
                    isSelected = selectedPaymentMethod == PaymentMethod.MULTINET,
                    onClick = { selectedPaymentMethod = PaymentMethod.MULTINET }
                )

                Spacer(modifier = Modifier.height(12.dp))

                PaymentMethodOption(
                    emoji = "💳",
                    text = "Edenred",
                    isSelected = selectedPaymentMethod == PaymentMethod.EDENRED,
                    onClick = { selectedPaymentMethod = PaymentMethod.EDENRED }
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Pay Now Button
                Button(
                    onClick = { viewModel.processPayment(selectedPaymentMethod) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = theme.primaryColor
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "💰 Pay Now",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
fun PaymentMethodOption(
    emoji: String,
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) Color.White.copy(alpha = 0.2f) else Color.Transparent)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = Color.White,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = { onClick() },
            colors = RadioButtonDefaults.colors(
                selectedColor = Color.White,
                unselectedColor = Color.White
            )
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = emoji,
            fontSize = 24.sp,
            modifier = Modifier.padding(end = 8.dp)
        )

        Text(
            text = text,
            color = Color.White,
            fontSize = 16.sp
        )
    }
}

@Composable
fun PaymentProcessingDialog() {
    val theme = LocalAppTheme.current

    Dialog(
        onDismissRequest = { },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(theme.primaryColor)
                .padding(40.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(60.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "⏳ Processing Payment...",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun PaymentFailedDialog(viewModel: FoodOrderingViewModel) {
    val theme = LocalAppTheme.current

    Dialog(
        onDismissRequest = { },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(theme.primaryColor)
                .padding(20.dp)
        ) {
            Column {
                Text(
                    text = "❌ Payment Failed",
                    color = theme.textOnPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White)
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "⚠️",
                            fontSize = 60.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Failed Payment Try Again",
                            color = Color.Black,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = { viewModel.retryPayment() },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = theme.primaryColor
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "🔄 Retry",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PaymentSuccessDialog(
    viewModel: FoodOrderingViewModel,
    paymentMethod: PaymentMethod = PaymentMethod.CREDIT_CARD
) {
    val theme = LocalAppTheme.current
    var showReceipt by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = { },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(theme.primaryColor)
                .padding(20.dp)
        ) {
            Column {
                Text(
                    text = "✅ Payment Successful",
                    color = theme.textOnPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.2f))
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🎉 Payment Successful ✅",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Your order has been confirmed!",
                            color = Color.White,
                            fontSize = 14.sp
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = { showReceipt = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = theme.primaryColor
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "🧾 Show Receipt",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                viewModel.clearCartAndHidePayment()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White.copy(alpha = 0.2f),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "🏠 Back to Home",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }

    if (showReceipt) {
        ReceiptDialog(
            viewModel = viewModel,
            paymentMethod = paymentMethod,
            onDismiss = { showReceipt = false }
        )
    }
}