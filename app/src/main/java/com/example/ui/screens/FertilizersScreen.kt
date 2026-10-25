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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.models.Fertilizer
import com.example.ui.theme.AgriGreenPrimary
import com.example.utils.Constants
import com.example.viewmodels.CropHubUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FertilizersScreen(
    uiState: CropHubUiState,
    onSearchChange: (String) -> Unit,
    onTypeChange: (String) -> Unit,
    onAddFertilizer: (Fertilizer) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }

    if (showAddDialog) {
        AddFertilizerDialog(
            onDismiss = { showAddDialog = false },
            onSave = { fert ->
                onAddFertilizer(fert)
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
                modifier = Modifier.testTag("add_fertilizer_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Fertilizer")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("fertilizers_screen")
        ) {
            // Search Input
            OutlinedTextField(
                value = uiState.fertilizerSearchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Search fertilizers or suitable crops...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = AgriGreenPrimary) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("fertilizer_search_input")
            )

            // Category Chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(Constants.FERTILIZER_TYPES) { type ->
                    val isSelected = uiState.selectedFertilizerType == type
                    FilterChip(
                        selected = isSelected,
                        onClick = { onTypeChange(type) },
                        label = { Text(type, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AgriGreenPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            if (uiState.fertilizers.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No fertilizers found.")
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(uiState.fertilizers, key = { it.id }) { fert ->
                        FertilizerCard(fertilizer = fert)
                    }
                }
            }
        }
    }
}

@Composable
fun FertilizerCard(fertilizer: Fertilizer) {
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
                            .background(Color(0xFFE0F2F1)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Science,
                            contentDescription = null,
                            tint = Color(0xFF00796B),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = fertilizer.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Type: ${fertilizer.type}",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                }
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFE8F5E9)
                ) {
                    Text(
                        text = "Rs. ${fertilizer.pricePerUnit.toInt()}",
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
                text = "🌾 Suitable Crops: ${fertilizer.suitableCrops}",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
            )

            Text(
                text = "⚖ Recommended Dosage: ${fertilizer.recommendedQuantity}",
                style = MaterialTheme.typography.bodySmall.copy(color = AgriGreenPrimary)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = fertilizer.description,
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )
        }
    }
}

@Composable
fun AddFertilizerDialog(
    onDismiss: () -> Unit,
    onSave: (Fertilizer) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("Nitrogenous") }
    var crops by remember { mutableStateOf("") }
    var dose by remember { mutableStateOf("50 kg / acre") }
    var price by remember { mutableStateOf("4500") }
    var desc by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Fertilizer Information") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Fertilizer Name (e.g. Zinc Sulphate)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = type,
                    onValueChange = { type = it },
                    label = { Text("Type (Nitrogenous/Phosphatic/Organic)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = crops,
                    onValueChange = { crops = it },
                    label = { Text("Suitable Crops (e.g. Rice, Wheat)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = dose,
                        onValueChange = { dose = it },
                        label = { Text("Recommended Dosage") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = price,
                        onValueChange = { price = it },
                        label = { Text("Price (Rs.)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Description & Instructions") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val fert = Fertilizer(
                            name = name,
                            type = type,
                            suitableCrops = crops,
                            recommendedQuantity = dose,
                            pricePerUnit = price.toDoubleOrNull() ?: 1000.0,
                            description = desc
                        )
                        onSave(fert)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AgriGreenPrimary)
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
