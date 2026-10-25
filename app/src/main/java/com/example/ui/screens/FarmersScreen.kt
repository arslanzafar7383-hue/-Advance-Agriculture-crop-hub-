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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.models.Farmer
import com.example.ui.theme.AgriGreenPrimary
import com.example.viewmodels.CropHubUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmersScreen(
    uiState: CropHubUiState,
    onSearchChange: (String) -> Unit,
    onAddFarmer: (Farmer) -> Unit,
    onUpdateFarmer: (Farmer) -> Unit,
    onDeleteFarmer: (Int) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var farmerToEdit by remember { mutableStateOf<Farmer?>(null) }
    var farmerDetailToShow by remember { mutableStateOf<Farmer?>(null) }

    if (showAddDialog || farmerToEdit != null) {
        FarmerFormDialog(
            initialFarmer = farmerToEdit,
            onDismiss = {
                showAddDialog = false
                farmerToEdit = null
            },
            onSave = { farmer ->
                if (farmerToEdit != null) {
                    onUpdateFarmer(farmer)
                } else {
                    onAddFarmer(farmer)
                }
                showAddDialog = false
                farmerToEdit = null
            }
        )
    }

    if (farmerDetailToShow != null) {
        FarmerDetailDialog(
            farmer = farmerDetailToShow!!,
            onDismiss = { farmerDetailToShow = null },
            onEdit = {
                val f = farmerDetailToShow
                farmerDetailToShow = null
                farmerToEdit = f
            },
            onDelete = {
                onDeleteFarmer(farmerDetailToShow!!.id)
                farmerDetailToShow = null
            }
        )
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = AgriGreenPrimary,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_farmer_fab")
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = "Add Farmer")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("farmers_screen")
        ) {
            // Search Input
            OutlinedTextField(
                value = uiState.farmerSearchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Search farmer by name, phone, or address...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = AgriGreenPrimary) },
                trailingIcon = {
                    if (uiState.farmerSearchQuery.isNotEmpty()) {
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
                    .testTag("farmer_search_input")
            )

            if (uiState.farmers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.People,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No farmers found",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Tap + to add a new farmer directory record.",
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
                    items(uiState.farmers, key = { it.id }) { farmer ->
                        FarmerCard(
                            farmer = farmer,
                            onClick = { farmerDetailToShow = farmer },
                            onEdit = { farmerToEdit = farmer },
                            onDelete = { onDeleteFarmer(farmer.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FarmerCard(
    farmer: Farmer,
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
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE1F5FE)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color(0xFF0288D1),
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = farmer.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = farmer.phone,
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFE8F5E9)
                ) {
                    Text(
                        text = "${farmer.farmSizeAcres} Acres",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = AgriGreenPrimary
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = farmer.address,
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Experience: ${farmer.experienceYears} Years • Reg: ${farmer.registeredDate}",
                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Farmer", tint = AgriGreenPrimary)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Farmer", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
fun FarmerFormDialog(
    initialFarmer: Farmer?,
    onDismiss: () -> Unit,
    onSave: (Farmer) -> Unit
) {
    var name by remember { mutableStateOf(initialFarmer?.name ?: "") }
    var phone by remember { mutableStateOf(initialFarmer?.phone ?: "") }
    var email by remember { mutableStateOf(initialFarmer?.email ?: "") }
    var address by remember { mutableStateOf(initialFarmer?.address ?: "") }
    var farmSize by remember { mutableStateOf(initialFarmer?.farmSizeAcres?.toString() ?: "10.0") }
    var experience by remember { mutableStateOf(initialFarmer?.experienceYears?.toString() ?: "5") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialFarmer != null) "Edit Farmer Information" else "Register Farmer") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Farmer Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address / Village / District") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = farmSize,
                        onValueChange = { farmSize = it },
                        label = { Text("Farm Size (Acres)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = experience,
                        onValueChange = { experience = it },
                        label = { Text("Experience (Yrs)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && phone.isNotBlank()) {
                        val f = (initialFarmer ?: Farmer()).copy(
                            name = name,
                            phone = phone,
                            email = email,
                            address = address,
                            farmSizeAcres = farmSize.toDoubleOrNull() ?: 1.0,
                            experienceYears = experience.toIntOrNull() ?: 1,
                            registeredDate = initialFarmer?.registeredDate ?: "2024-01-01"
                        )
                        onSave(f)
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

@Composable
fun FarmerDetailDialog(
    farmer: Farmer,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, contentDescription = null, tint = AgriGreenPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(farmer.name)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Phone: ${farmer.phone}")
                Text("Email: ${farmer.email.ifBlank { "Not specified" }}")
                Text("Address: ${farmer.address}")
                HorizontalDivider()
                Text("Farm Size: ${farmer.farmSizeAcres} Acres")
                Text("Farming Experience: ${farmer.experienceYears} Years")
                Text("Registered Date: ${farmer.registeredDate}")
            }
        },
        confirmButton = {
            Button(onClick = onEdit, colors = ButtonDefaults.buttonColors(containerColor = AgriGreenPrimary)) {
                Text("Edit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}
