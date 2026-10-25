package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.models.CartItem
import com.example.models.User
import com.example.ui.theme.AgriGreenPrimary
import com.example.ui.theme.AgriGreenSecondary
import com.example.ui.theme.AgriGreenTertiary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartReviewScreen(
    cartItems: List<CartItem>,
    currentUser: User?,
    onNavigateBack: () -> Unit,
    onQuantityChange: (Int, Int) -> Unit,
    onRemoveItem: (Int) -> Unit,
    onClearCart: () -> Unit,
    onCheckout: (customerName: String, customerPhone: String, deliveryAddress: String) -> Unit
) {
    var customerName by remember { mutableStateOf(currentUser?.fullName ?: "") }
    var customerPhone by remember { mutableStateOf(currentUser?.phone ?: "") }
    var deliveryAddress by remember { mutableStateOf("Chak 42-JB, Agriculture Area, Faisalabad") }
    var notes by remember { mutableStateOf("Please deliver directly to the farm storage shed.") }
    var showConfirmOrderDialog by remember { mutableStateOf(false) }

    val subtotal = cartItems.sumOf { it.product.price * it.quantity }
    val deliveryFee = if (cartItems.isEmpty()) 0.0 else 500.0 // Standard agricultural transit fee
    val grandTotal = subtotal + deliveryFee

    if (showConfirmOrderDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmOrderDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Verified, contentDescription = null, tint = AgriGreenPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Confirm Order Placement")
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("You are placing an order for ${cartItems.sumOf { it.quantity }} items.")
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Recipient: $customerName", fontWeight = FontWeight.SemiBold)
                            Text("Phone: $customerPhone")
                            Text("Address: $deliveryAddress")
                            Text("Total Payable: Rs. ${grandTotal.toInt()} (Cash on Delivery)", color = AgriGreenPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                    Text("Payment will be collected upon field delivery.", style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmOrderDialog = false
                        onCheckout(customerName, customerPhone, deliveryAddress)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AgriGreenPrimary),
                    modifier = Modifier.testTag("dialog_confirm_order_btn")
                ) {
                    Text("Submit Order to Backend")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmOrderDialog = false }) {
                    Text("Back to Review")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Review Cart & Checkout",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Persisted in Local SQLite • Backed by PHP API",
                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("cart_review_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to Marketplace")
                    }
                },
                actions = {
                    if (cartItems.isNotEmpty()) {
                        IconButton(
                            onClick = onClearCart,
                            modifier = Modifier.testTag("clear_cart_button")
                        ) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = "Clear All Items", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            if (cartItems.isNotEmpty()) {
                Surface(
                    tonalElevation = 8.dp,
                    shadowElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Total (${cartItems.sumOf { it.quantity }} items):",
                                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                                Text(
                                    text = "Rs. ${grandTotal.toInt()}",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = AgriGreenPrimary
                                    )
                                )
                            }
                            Button(
                                onClick = {
                                    if (customerName.isNotBlank() && customerPhone.isNotBlank() && deliveryAddress.isNotBlank()) {
                                        showConfirmOrderDialog = true
                                    }
                                },
                                enabled = customerName.isNotBlank() && customerPhone.isNotBlank() && deliveryAddress.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(containerColor = AgriGreenPrimary),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .height(48.dp)
                                    .testTag("review_place_order_button")
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Place Order (COD)")
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        if (cartItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .testTag("empty_cart_container"),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = AgriGreenPrimary.copy(alpha = 0.12f),
                        modifier = Modifier.size(72.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.RemoveShoppingCart, contentDescription = null, tint = AgriGreenPrimary, modifier = Modifier.size(36.dp))
                        }
                    }
                    Text("Your cart is currently empty", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Text("Browse the agricultural marketplace to add seeds, fertilizers, or tools.", style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = onNavigateBack,
                        colors = ButtonDefaults.buttonColors(containerColor = AgriGreenPrimary)
                    ) {
                        Icon(Icons.Default.Storefront, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Explore Marketplace")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .testTag("cart_review_list"),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header badge
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Storage, contentDescription = null, tint = AgriGreenPrimary)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Local SQLite Cart Persistence is active. Items remain safely stored across sessions.",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                            )
                        }
                    }
                }

                // Cart Line Items Card
                item {
                    Text(
                        text = "1. Selected Agricultural Supplies (${cartItems.size} items)",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }

                items(cartItems, key = { it.product.id }) { item ->
                    ElevatedCard(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("cart_review_item_${item.product.id}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = AgriGreenPrimary.copy(alpha = 0.15f),
                                modifier = Modifier.size(52.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = when (item.product.category) {
                                            "Seeds" -> Icons.Default.Spa
                                            "Fertilizers" -> Icons.Default.Science
                                            "Farming Tools" -> Icons.Default.Build
                                            "Irrigation Equipment" -> Icons.Default.WaterDrop
                                            "Pesticides" -> Icons.Default.BugReport
                                            else -> Icons.Default.Agriculture
                                        },
                                        contentDescription = null,
                                        tint = AgriGreenPrimary,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.product.name,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    maxLines = 2
                                )
                                Text(
                                    text = "Category: ${item.product.category}",
                                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                                Text(
                                    text = "Rs. ${item.product.price.toInt()} each • Total: Rs. ${(item.product.price * item.quantity).toInt()}",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = AgriGreenPrimary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }

                            // Stepper Controls
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                IconButton(
                                    onClick = { onQuantityChange(item.product.id, -1) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(16.dp))
                                }
                                Text(
                                    text = "${item.quantity}",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    modifier = Modifier.padding(horizontal = 6.dp)
                                )
                                IconButton(
                                    onClick = { onQuantityChange(item.product.id, 1) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Increase", modifier = Modifier.size(16.dp))
                                }
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            IconButton(
                                onClick = { onRemoveItem(item.product.id) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }

                // Shipping Details Form
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "2. Farmer & Delivery Coordinates",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }

                item {
                    ElevatedCard(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = customerName,
                                onValueChange = { customerName = it },
                                label = { Text("Customer / Farmer Full Name") },
                                singleLine = true,
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = AgriGreenPrimary) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("checkout_name_input")
                            )

                            OutlinedTextField(
                                value = customerPhone,
                                onValueChange = { customerPhone = it },
                                label = { Text("Contact Phone (for delivery SMS)") },
                                singleLine = true,
                                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = AgriGreenPrimary) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("checkout_phone_input")
                            )

                            OutlinedTextField(
                                value = deliveryAddress,
                                onValueChange = { deliveryAddress = it },
                                label = { Text("Farm Parcel / Delivery Destination") },
                                maxLines = 2,
                                leadingIcon = { Icon(Icons.Default.Place, contentDescription = null, tint = AgriGreenPrimary) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("checkout_address_input")
                            )

                            OutlinedTextField(
                                value = notes,
                                onValueChange = { notes = it },
                                label = { Text("Special Farm Delivery Instructions (Optional)") },
                                maxLines = 2,
                                leadingIcon = { Icon(Icons.Default.Description, contentDescription = null, tint = AgriGreenPrimary) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // Order Price Breakdown Summary
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "3. Payment & Pricing Breakdown",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }

                item {
                    ElevatedCard(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Items Subtotal:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("Rs. ${subtotal.toInt()}", fontWeight = FontWeight.SemiBold)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Agri Logistics & Transit:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("Rs. ${deliveryFee.toInt()}", fontWeight = FontWeight.SemiBold)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Payment Method:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = AgriGreenTertiary.copy(alpha = 0.3f)
                                ) {
                                    Text(
                                        text = "Cash on Delivery",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Grand Total:", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                Text(
                                    "Rs. ${grandTotal.toInt()}",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = AgriGreenPrimary
                                    )
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(60.dp))
                }
            }
        }
    }
}
