// presentation/ui/dialogs/ConfigPasswordDialog.kt
package com.example.foodorderingapp_ver2.presentation.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.foodorderingapp_ver2.presentation.ui.theme.LocalAppTheme

@Composable
fun ConfigPasswordDialog(
    onPasswordCorrect: () -> Unit,
    onDismiss: () -> Unit
) {
    val theme = LocalAppTheme.current
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }

    // Clear error when user types
    LaunchedEffect(password) {
        if (showError) {
            showError = false
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = theme.cardColor)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Enter Password",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.textColor
                )

                // Error message
                if (showError) {
                    Text(
                        text = "❌ Wrong password",
                        color = Color.Red,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                tint = theme.textColor.copy(alpha = 0.6f)
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = theme.primaryColor,
                        focusedLabelColor = theme.primaryColor,
                        focusedTextColor = theme.textColor,
                        unfocusedTextColor = theme.textColor,
                        unfocusedBorderColor = theme.textColor.copy(alpha = 0.5f),
                        unfocusedLabelColor = theme.textColor.copy(alpha = 0.7f),
                        cursorColor = theme.primaryColor,
                        errorBorderColor = Color.Red,
                        errorLabelColor = Color.Red
                    ),
                    isError = showError
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = theme.textColor.copy(alpha = 0.1f),
                            contentColor = theme.textColor
                        )
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            if (password == "1234") {
                                onPasswordCorrect()
                            } else {
                                showError = true
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = theme.primaryColor
                        )
                    ) {
                        Text("OK")
                    }
                }
            }
        }
    }
}