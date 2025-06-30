
// ui/dialogs/ReceiptDialog.kt
package com.example.foodorderingapp.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.foodorderingapp.presentation.viewmodel.FoodOrderingViewModel
import com.example.foodorderingapp.domain.entities.PaymentMethod
import com.example.foodorderingapp.ui.theme.LocalAppTheme
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ReceiptDialog(
    viewModel: FoodOrderingViewModel,
    paymentMethod: PaymentMethod,
    onDismiss: () -> Unit
) {
    val theme = LocalAppTheme.current
    val uiState = viewModel.uiState
    val currentDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
    val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
    val total = uiState.cartTotal

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White)
                .border(2.dp, Color.Black, RoundedCornerShape(8.dp))
                .padding(16.dp)
        ) {
            LazyColumn {
                item {
                    // Close button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.Black
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Receipt Header
                    Text(
                        text = "RECEIPT",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                        fontFamily = FontFamily.Monospace,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Date and Receipt Number Row
                    ReceiptField("Date:", "$currentDate, $currentTime")

                    Spacer(modifier = Modifier.height(12.dp))

                    ReceiptField("Amount Received:", "₺${String.format("%.2f", total)}")

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "For the Payment of:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        fontFamily = FontFamily.Monospace
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Color.Black)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = viewModel.getCartItemsSummary(),
                        fontSize = 11.sp,
                        color = Color.Black,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Payment Method checkboxes
                    Text(
                        text = "Payment Method: ",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        fontFamily = FontFamily.Monospace
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    PaymentMethodCheckbox("Cash", paymentMethod == PaymentMethod.CASH)
                    PaymentMethodCheckbox("Check", false)
                    PaymentMethodCheckbox("Credit Card", paymentMethod == PaymentMethod.CREDIT_CARD)
                    PaymentMethodCheckbox("Sodexo", paymentMethod == PaymentMethod.SODEXO)
                    PaymentMethodCheckbox("Multinet", paymentMethod == PaymentMethod.MULTINET)
                    PaymentMethodCheckbox("Edenred", paymentMethod == PaymentMethod.EDENRED)
                    PaymentMethodCheckbox("Coupon", paymentMethod == PaymentMethod.COUPON)

                    Spacer(modifier = Modifier.height(16.dp))

                    // Dotted line separator
                    Text(
                        text = "- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -",
                        fontSize = 12.sp,
                        color = Color.Black,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "ORDER DETAILS",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                        fontFamily = FontFamily.Monospace,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                }

                items(uiState.cartItems) { cartItem ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${cartItem.quantity}x ${cartItem.product.name}",
                            fontSize = 11.sp,
                            color = Color.Black,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "₺${String.format("%.2f", cartItem.subtotal)}",
                            fontSize = 11.sp,
                            color = Color.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Color.Black)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "TOTAL:",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "₺${String.format("%.2f", total)}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Thank you for your business!\n${theme.companyName}",
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                        fontFamily = FontFamily.Monospace,
                        color = Color.Black
                    )
                }
            }
        }
    }
}

@Composable
fun ReceiptField(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            fontFamily = FontFamily.Monospace
        )

        Text(
            text = value,
            fontSize = 12.sp,
            color = Color.Black,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
fun PaymentMethodCheckbox(method: String, isChecked: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Text(
            text = if (isChecked) "☑" else "☐",
            fontSize = 12.sp,
            color = Color.Black,
            fontFamily = FontFamily.Monospace
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = method,
            fontSize = 11.sp,
            color = Color.Black,
            fontFamily = FontFamily.Monospace
        )
    }
}
                