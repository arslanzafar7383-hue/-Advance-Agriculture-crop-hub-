package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.models.MarketplaceProduct
import com.example.models.User
import com.example.ui.theme.AgriGreenPrimary
import com.example.ui.theme.AgriGreenTertiary
import com.example.utils.Constants
import com.example.viewmodels.CropHubUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketplaceScreen(
    uiState: CropHubUiState,
    currentUser: User?,
    onSearchChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onAddToCart: (MarketplaceProduct) -> Unit,
    onOpenCart: () -> Unit,
    onAddProduct: (MarketplaceProduct) -> Unit,
    onProductDetailViewed: (MarketplaceProduct) -> Unit = {}
) {
    var showAddProductDialog by remember { mutableStateOf(false) }
    var selectedProductForDetail by remember { mutableStateOf<MarketplaceProduct?>(null) }

    if (showAddProductDialog) {
        AddProductDialog(
            currentUser = currentUser,
            onDismiss = { showAddProductDialog = false },
            onSave = { product ->
                onAddProduct(product)
                showAddProductDialog = false
            }
        )
    }

    if (selectedProductForDetail != null) {
        ProductDetailDialog(
            product = selectedProductForDetail!!,
            onDismiss = { selectedProductForDetail = null },
            onAddToCart = {
                onAddToCart(selectedProductForDetail!!)
                selectedProductForDetail = null
            }
        )
    }

    Scaffold(
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (currentUser?.userType == Constants.TYPE_DEALER || currentUser?.userType == Constants.TYPE_FARMER) {
                    SmallFloatingActionButton(
                        onClick = { showAddProductDialog = true },
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = AgriGreenPrimary
                    ) {
                        Icon(Icons.Default.AddBusiness, contentDescription = "Sell Product")
                    }
                }
                ExtendedFloatingActionButton(
                    onClick = onOpenCart,
                    containerColor = AgriGreenPrimary,
                    contentColor = Color.White,
                    icon = {
                        BadgedBox(
                            badge = {
                                if (uiState.cartItems.isNotEmpty()) {
                                    Badge(containerColor = AgriGreenTertiary, contentColor = Color.Black) {
                                        Text("${uiState.cartItems.sumOf { it.quantity }}")
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = null)
                        }
                    },
                    text = { Text("Cart (${uiState.cartItems.size})") },
                    modifier = Modifier.testTag("marketplace_cart_fab")
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("marketplace_screen")
        ) {
            // Search Input
            OutlinedTextField(
                value = uiState.productSearchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Search seeds, tools, machinery, sprays...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = AgriGreenPrimary) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("product_search_input")
            )

            // Category Chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(Constants.MARKET_CATEGORIES) { cat ->
                    val isSelected = uiState.selectedMarketCategory == cat
                    FilterChip(
                        selected = isSelected,
                        onClick = { onCategoryChange(cat) },
                        label = { Text(cat, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AgriGreenPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            if (uiState.products.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No products in this category.")
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(uiState.products, key = { it.id }) { product ->
                        ProductCard(
                            product = product,
                            onClick = {
                                selectedProductForDetail = product
                                onProductDetailViewed(product)
                            },
                            onAddToCart = { onAddToCart(product) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProductCard(
    product: MarketplaceProduct,
    onClick: () -> Unit,
    onAddToCart: () -> Unit
) {
    ElevatedCard(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(getCategoryColor(product.category)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getCategoryIcon(product.category),
                            contentDescription = null,
                            tint = Color.DarkGray,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = product.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Category: ${product.category}",
                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = product.description,
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Rs. ${product.price.toInt()}",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = AgriGreenPrimary
                        )
                    )
                    Text(
                        text = "Seller: ${product.sellerName} (${product.sellerContact})",
                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }

                Button(
                    onClick = onAddToCart,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AgriGreenPrimary)
                ) {
                    Icon(Icons.Default.AddShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add to Cart")
                }
            }
        }
    }
}

fun getCategoryIcon(category: String): ImageVector {
    return when (category) {
        "Seeds" -> Icons.Default.Grass
        "Fertilizers" -> Icons.Default.Science
        "Farming Tools" -> Icons.Default.Build
        "Irrigation Equipment" -> Icons.Default.WaterDrop
        "Pesticides" -> Icons.Default.Shield
        "Agricultural Machinery" -> Icons.Default.Agriculture
        else -> Icons.Default.Storefront
    }
}

fun getCategoryColor(category: String): Color {
    return when (category) {
        "Seeds" -> Color(0xFFC8E6C9)
        "Fertilizers" -> Color(0xFFB2DFDB)
        "Farming Tools" -> Color(0xFFFFE0B2)
        "Irrigation Equipment" -> Color(0xFFB3E5FC)
        "Pesticides" -> Color(0xFFFFCDD2)
        "Agricultural Machinery" -> Color(0xFFFFF9C4)
        else -> Color(0xFFE0E0E0)
    }
}

@Composable
fun ProductDetailDialog(
    product: MarketplaceProduct,
    onDismiss: () -> Unit,
    onAddToCart: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(product.name) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Price: Rs. ${product.price.toInt()}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = AgriGreenPrimary
                    )
                )
                Text("Category: ${product.category}")
                Text("Seller Name: ${product.sellerName}")
                Text("Seller Contact: ${product.sellerContact}")
                Text("Available Stock: ${product.stockQuantity} units")
                HorizontalDivider()
                Text("Detailed Specification:", fontWeight = FontWeight.Bold)
                Text(product.description, style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = {
            Button(
                onClick = onAddToCart,
                colors = ButtonDefaults.buttonColors(containerColor = AgriGreenPrimary)
            ) {
                Text("Add to Cart")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Composable
fun AddProductDialog(
    currentUser: User?,
    onDismiss: () -> Unit,
    onSave: (MarketplaceProduct) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Seeds") }
    var price by remember { mutableStateOf("1500") }
    var seller by remember { mutableStateOf(currentUser?.fullName ?: "Local Dealer") }
    var contact by remember { mutableStateOf(currentUser?.phone ?: "+92 300 0000000") }
    var desc by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("List Product on Agriculture Marketplace") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Product Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category (Seeds/Fertilizers/Tools)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Price in PKR (Rs.)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = seller,
                        onValueChange = { seller = it },
                        label = { Text("Seller Name") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = contact,
                        onValueChange = { contact = it },
                        label = { Text("Contact Phone") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Description & Specifications") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val p = MarketplaceProduct(
                            name = name,
                            category = category,
                            price = price.toDoubleOrNull() ?: 100.0,
                            sellerName = seller,
                            sellerContact = contact,
                            description = desc,
                            stockQuantity = 20
                        )
                        onSave(p)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AgriGreenPrimary)
            ) {
                Text("Publish")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
