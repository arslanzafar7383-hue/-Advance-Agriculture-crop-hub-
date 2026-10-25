package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.models.CartItem
import com.example.models.User
import com.example.ui.theme.AgriGreenPrimary

@Composable
fun CartDialog(
    cartItems: List<CartItem>,
    currentUser: User?,
    onDismiss: () -> Unit,
    onQuantityChange: (Int, Int) -> Unit,
    onCheckout: (String, String, String) -> Unit
) {
    var customerName by remember { mutableStateOf(currentUser?.fullName ?: "") }
    var customerPhone by remember { mutableStateOf(currentUser?.phone ?: "") }
    var deliveryAddress by remember { mutableStateOf("Chak 42-JB, Agriculture Area, Faisalabad") }
    var isCheckingOut by remember { mutableStateOf(false) }

    val totalAmount = cartItems.sumOf { it.product.price * it.quantity }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = AgriGreenPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isCheckingOut) "Checkout & Place Order" else "Shopping Cart (${cartItems.size})")
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                if (!isCheckingOut) {
                    if (cartItems.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Your cart is empty. Add items from Marketplace.")
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 280.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(cartItems) { item ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = item.product.name,
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                maxLines = 1
                                            )
                                            Text(
                                                text = "Rs. ${item.product.price.toInt()} each • Subtotal: Rs. ${(item.product.price * item.quantity).toInt()}",
                                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            )
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            IconButton(
                                                onClick = { onQuantityChange(item.product.id, -1) },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(16.dp))
                                            }
                                            Text(
                                                text = "${item.quantity}",
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                modifier = Modifier.padding(horizontal = 4.dp)
                                            )
                                            IconButton(
                                                onClick = { onQuantityChange(item.product.id, 1) },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(Icons.Default.Add, contentDescription = "Increase", modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Grand Total:",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Rs. ${totalAmount.toInt()}",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = AgriGreenPrimary
                                )
                            )
                        }
                    }
                } else {
                    // Checkout inputs
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Order Total: Rs. ${totalAmount.toInt()}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = AgriGreenPrimary
                            )
                        )
                        OutlinedTextField(
                            value = customerName,
                            onValueChange = { customerName = it },
                            label = { Text("Customer Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = customerPhone,
                            onValueChange = { customerPhone = it },
                            label = { Text("Phone Number") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = deliveryAddress,
                            onValueChange = { deliveryAddress = it },
                            label = { Text("Delivery Address / Farm Location") },
                            maxLines = 2,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (!isCheckingOut) {
                if (cartItems.isNotEmpty()) {
                    Button(
                        onClick = { isCheckingOut = true },
                        colors = ButtonDefaults.buttonColors(containerColor = AgriGreenPrimary),
                        modifier = Modifier.testTag("proceed_checkout_button")
                    ) {
                        Text("Proceed to Checkout")
                    }
                }
            } else {
                Button(
                    onClick = {
                        if (customerName.isNotBlank() && customerPhone.isNotBlank() && deliveryAddress.isNotBlank()) {
                            onCheckout(customerName, customerPhone, deliveryAddress)
                            onDismiss()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AgriGreenPrimary),
                    modifier = Modifier.testTag("confirm_order_button")
                ) {
                    Text("Confirm Order (Cash on Delivery)")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    if (isCheckingOut) {
                        isCheckingOut = false
                    } else {
                        onDismiss()
                    }
                }
            ) {
                Text(if (isCheckingOut) "Back to Cart" else "Close")
            }
        }
    )
}
