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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.models.Crop
import com.example.ui.theme.AgriGreenPrimary
import com.example.ui.theme.AgriGreenTertiary
import com.example.utils.Constants
import com.example.viewmodels.CropHubUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropsScreen(
    uiState: CropHubUiState,
    onSearchChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onAddCrop: (Crop) -> Unit,
    onUpdateCrop: (Crop) -> Unit,
    onDeleteCrop: (Int) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var cropToEdit by remember { mutableStateOf<Crop?>(null) }
    var cropDetailToShow by remember { mutableStateOf<Crop?>(null) }

    if (showAddDialog || cropToEdit != null) {
        CropFormDialog(
            initialCrop = cropToEdit,
            onDismiss = {
                showAddDialog = false
                cropToEdit = null
            },
            onSave = { crop ->
                if (cropToEdit != null) {
                    onUpdateCrop(crop)
                } else {
                    onAddCrop(crop)
                }
                showAddDialog = false
                cropToEdit = null
            }
        )
    }

    if (cropDetailToShow != null) {
        CropDetailDialog(
            crop = cropDetailToShow!!,
            onDismiss = { cropDetailToShow = null },
            onEdit = {
                val c = cropDetailToShow
                cropDetailToShow = null
                cropToEdit = c
            },
            onDelete = {
                onDeleteCrop(cropDetailToShow!!.id)
                cropDetailToShow = null
            }
        )
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = AgriGreenPrimary,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_crop_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Crop")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("crops_screen")
        ) {
            // Search Input
            OutlinedTextField(
                value = uiState.cropSearchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Search by crop or scientific name...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = AgriGreenPrimary) },
                trailingIcon = {
                    if (uiState.cropSearchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("crop_search_input")
            )

            // Category Chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(Constants.CROP_CATEGORIES) { cat ->
                    val isSelected = uiState.selectedCropCategory == cat
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

            // Crop List
            if (uiState.crops.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Grass,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No crops match your filter",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Tap + below to register a new crop variety.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(uiState.crops, key = { it.id }) { crop ->
                        CropCard(
                            crop = crop,
                            onClick = { cropDetailToShow = crop },
                            onEdit = { cropToEdit = crop },
                            onDelete = { onDeleteCrop(crop.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CropCard(
    crop: Crop,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(AgriGreenPrimary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Spa,
                            contentDescription = null,
                            tint = AgriGreenPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = crop.cropName,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = crop.scientificName,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFE8F5E9)
                ) {
                    Text(
                        text = crop.category,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = AgriGreenPrimary
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CropBadge(label = "Season", value = crop.season, modifier = Modifier.weight(1f))
                CropBadge(label = "Water", value = crop.waterRequirement, modifier = Modifier.weight(1f))
                CropBadge(label = "Yield/Acre", value = crop.expectedYieldPerAcre, modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = crop.description,
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Crop", tint = AgriGreenPrimary)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Crop", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun CropBadge(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun CropFormDialog(
    initialCrop: Crop?,
    onDismiss: () -> Unit,
    onSave: (Crop) -> Unit
) {
    var name by remember { mutableStateOf(initialCrop?.cropName ?: "") }
    var category by remember { mutableStateOf(initialCrop?.category ?: "Cereals") }
    var scientific by remember { mutableStateOf(initialCrop?.scientificName ?: "") }
    var season by remember { mutableStateOf(initialCrop?.season ?: "Rabi") }
    var soil by remember { mutableStateOf(initialCrop?.soilType ?: "Loamy") }
    var water by remember { mutableStateOf(initialCrop?.waterRequirement ?: "Medium") }
    var yieldPerAcre by remember { mutableStateOf(initialCrop?.expectedYieldPerAcre ?: "2000 kg") }
    var description by remember { mutableStateOf(initialCrop?.description ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialCrop != null) "Edit Crop Details" else "Add New Agricultural Crop") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Crop Name (e.g. Wheat)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = scientific,
                    onValueChange = { scientific = it },
                    label = { Text("Scientific Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Category") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = season,
                        onValueChange = { season = it },
                        label = { Text("Season") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = soil,
                        onValueChange = { soil = it },
                        label = { Text("Soil Type") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = water,
                        onValueChange = { water = it },
                        label = { Text("Water Req.") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = yieldPerAcre,
                    onValueChange = { yieldPerAcre = it },
                    label = { Text("Expected Yield / Acre") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description & Cultivation Advice") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val newCrop = (initialCrop ?: Crop()).copy(
                            cropName = name,
                            category = category,
                            scientificName = scientific,
                            season = season,
                            soilType = soil,
                            waterRequirement = water,
                            expectedYieldPerAcre = yieldPerAcre,
                            description = description
                        )
                        onSave(newCrop)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AgriGreenPrimary)
            ) {
                Text("Save Crop")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun CropDetailDialog(
    crop: Crop,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Grass, contentDescription = null, tint = AgriGreenPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(crop.cropName)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Scientific Name: ${crop.scientificName}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                HorizontalDivider()
                Text("Category: ${crop.category}")
                Text("Season: ${crop.season}")
                Text("Soil Requirement: ${crop.soilType}")
                Text("Irrigation / Water Requirement: ${crop.waterRequirement}")
                Text("Estimated Yield: ${crop.expectedYieldPerAcre}")
                HorizontalDivider()
                Text(
                    text = "Agronomic Notes:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = crop.description,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onEdit,
                colors = ButtonDefaults.buttonColors(containerColor = AgriGreenPrimary)
            ) {
                Text("Edit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}
