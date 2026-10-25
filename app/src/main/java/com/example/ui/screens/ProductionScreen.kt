package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.models.CropProduction
import com.example.ui.theme.AgriGreenPrimary
import com.example.ui.theme.AgriGreenTertiary
import com.example.viewmodels.CropHubUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductionScreen(
    uiState: CropHubUiState,
    onAddProduction: (CropProduction) -> Unit,
    onDeleteProduction: (Int) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }

    if (showAddDialog) {
        AddProductionDialog(
            farmers = uiState.farmers,
            fields = uiState.fields,
            crops = uiState.crops,
            onDismiss = { showAddDialog = false },
            onSave = { record ->
                onAddProduction(record)
                showAddDialog = false
            }
        )
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = AgriGreenPrimary,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_production_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Production Record")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("production_screen")
        ) {
            // Summary Card
            val totalKg = uiState.productions.sumOf { it.quantityProducedKg }
            val totalRev = uiState.productions.sumOf { it.expectedRevenue }
            val totalCost = uiState.productions.sumOf { it.productionCost }
            val netProfit = totalRev - totalCost

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Production Harvest Summary",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Total Yield", style = MaterialTheme.typography.labelSmall)
                            Text("${totalKg.toInt()} kg", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = AgriGreenPrimary))
                        }
                        Column {
                            Text("Total Revenue", style = MaterialTheme.typography.labelSmall)
                            Text("Rs. ${totalRev.toInt()}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        }
                        Column {
                            Text("Net Profit", style = MaterialTheme.typography.labelSmall)
                            Text(
                                text = "Rs. ${netProfit.toInt()}",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (netProfit >= 0) AgriGreenPrimary else MaterialTheme.colorScheme.error
                                )
                            )
                        }
                    }
                }
            }

            if (uiState.productions.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No harvest records logged yet. Tap + to add.")
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.productions, key = { it.id }) { item ->
                        ProductionCard(record = item, onDelete = { onDeleteProduction(item.id) })
                    }
                }
            }
        }
    }
}

@Composable
fun ProductionCard(
    record: CropProduction,
    onDelete: () -> Unit
) {
    val profit = record.expectedRevenue - record.productionCost

    ElevatedCard(
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFF3E0)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Agriculture,
                            contentDescription = null,
                            tint = Color(0xFFE65100),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = record.cropName ?: "Crop Harvest",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Farmer: ${record.farmerName ?: "Farmer"} • ${record.fieldName ?: "Field"}",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                }
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFE8F5E9)
                ) {
                    Text(
                        text = "${record.quantityProducedKg} kg",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = AgriGreenPrimary
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "🗓 Planted: ${record.plantingDate} | Harvested: ${record.harvestDate}",
                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Cost: Rs. ${record.productionCost.toInt()} | Rev: Rs. ${record.expectedRevenue.toInt()}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "Net Margin: Rs. ${profit.toInt()}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (profit >= 0) AgriGreenPrimary else Color.Red
                        )
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Record", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun AddProductionDialog(
    farmers: List<com.example.models.Farmer>,
    fields: List<com.example.models.FieldEntity>,
    crops: List<com.example.models.Crop>,
    onDismiss: () -> Unit,
    onSave: (CropProduction) -> Unit
) {
    var farmerId by remember { mutableStateOf(farmers.firstOrNull()?.id ?: 1) }
    var fieldId by remember { mutableStateOf(fields.firstOrNull()?.id ?: 1) }
    var cropId by remember { mutableStateOf(crops.firstOrNull()?.id ?: 1) }
    var plantingDate by remember { mutableStateOf("2024-03-01") }
    var harvestDate by remember { mutableStateOf("2024-07-15") }
    var qty by remember { mutableStateOf("15000") }
    var cost by remember { mutableStateOf("250000") }
    var revenue by remember { mutableStateOf("450000") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Harvest & Crop Production") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = plantingDate,
                        onValueChange = { plantingDate = it },
                        label = { Text("Planting Date") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = harvestDate,
                        onValueChange = { harvestDate = it },
                        label = { Text("Harvest Date") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = qty,
                    onValueChange = { qty = it },
                    label = { Text("Produced Quantity (kg)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = cost,
                        onValueChange = { cost = it },
                        label = { Text("Cost (Rs.)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = revenue,
                        onValueChange = { revenue = it },
                        label = { Text("Revenue (Rs.)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Agronomic / Yield Notes") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val rec = CropProduction(
                        farmerId = farmerId,
                        fieldId = fieldId,
                        cropId = cropId,
                        plantingDate = plantingDate,
                        harvestDate = harvestDate,
                        quantityProducedKg = qty.toDoubleOrNull() ?: 1000.0,
                        productionCost = cost.toDoubleOrNull() ?: 0.0,
                        expectedRevenue = revenue.toDoubleOrNull() ?: 0.0,
                        notes = notes
                    )
                    onSave(rec)
                },
                colors = ButtonDefaults.buttonColors(containerColor = AgriGreenPrimary)
            ) {
                Text("Log Yield")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
