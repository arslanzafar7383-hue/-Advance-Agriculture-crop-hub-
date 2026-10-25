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
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.example.models.FieldEntity
import com.example.ui.components.FieldDashboardCard
import com.example.ui.components.SeedQrScannerDialog
import com.example.ui.theme.AgriGreenPrimary
import com.example.ui.theme.AgriGreenTertiary
import com.example.viewmodels.CropHubUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FieldsScreen(
    uiState: CropHubUiState,
    onAddField: (FieldEntity) -> Unit,
    onDeleteField: (Int) -> Unit
) {
    val context = LocalContext.current
    var showAddDialog by remember { mutableStateOf(false) }
    var showQrScanner by remember { mutableStateOf(false) }
    var prefilledCropId by remember { mutableStateOf<Int?>(null) }
    var prefilledCropName by remember { mutableStateOf<String?>(null) }
    var prefilledSoil by remember { mutableStateOf<String?>(null) }
    var prefilledIrrigation by remember { mutableStateOf<String?>(null) }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    if (showQrScanner) {
        SeedQrScannerDialog(
            availableCrops = uiState.crops,
            hasCameraPermission = hasCameraPermission,
            onRequestCameraPermission = {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            },
            onDismiss = { showQrScanner = false },
            onCropScanned = { cropId, cropName, soilHint, irrigationHint ->
                prefilledCropId = cropId
                prefilledCropName = cropName
                prefilledSoil = soilHint
                prefilledIrrigation = irrigationHint
                showQrScanner = false
                showAddDialog = true
            }
        )
    }

    if (showAddDialog) {
        AddFieldDialog(
            farmers = uiState.farmers,
            crops = uiState.crops,
            initialCropId = prefilledCropId,
            initialCropName = prefilledCropName,
            initialSoil = prefilledSoil,
            initialIrrigation = prefilledIrrigation,
            onOpenScanner = {
                showAddDialog = false
                showQrScanner = true
            },
            onDismiss = {
                showAddDialog = false
                prefilledCropId = null
                prefilledCropName = null
                prefilledSoil = null
                prefilledIrrigation = null
            },
            onSave = { field ->
                onAddField(field)
                showAddDialog = false
                prefilledCropId = null
                prefilledCropName = null
                prefilledSoil = null
                prefilledIrrigation = null
            }
        )
    }

    Scaffold(
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SmallFloatingActionButton(
                    onClick = {
                        if (!hasCameraPermission) {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                        showQrScanner = true
                    },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = AgriGreenPrimary,
                    modifier = Modifier.testTag("scan_seed_qr_fab")
                ) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan Seed Package QR")
                }

                ExtendedFloatingActionButton(
                    onClick = {
                        prefilledCropId = null
                        prefilledCropName = null
                        showAddDialog = true
                    },
                    containerColor = AgriGreenPrimary,
                    contentColor = Color.White,
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Add Field") },
                    modifier = Modifier.testTag("add_field_fab")
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("fields_screen")
        ) {
            Text(
                text = "Field Parcels & Soil Management",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(16.dp)
            )

            if (uiState.fields.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No fields recorded. Tap + to add a field.")
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.fields, key = { it.id }) { field ->
                        FieldDashboardCard(
                            field = field,
                            onDelete = { onDeleteField(field.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FieldCard(
    field: FieldEntity,
    onDelete: () -> Unit
) {
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
                            .background(Color(0xFFF3E5F5)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Landscape,
                            contentDescription = null,
                            tint = Color(0xFF7B1FA2),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = field.fieldName,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Farmer: ${field.farmerName ?: "Assigned Farmer"}",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                }
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFE8F5E9)
                ) {
                    Text(
                        text = "${field.areaAcres} Acres",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
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
                FieldInfoBadge("Location", field.location, Modifier.weight(1f))
                FieldInfoBadge("Soil Type", field.soilType, Modifier.weight(1f))
                FieldInfoBadge("Irrigation", field.irrigationType, Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🌱 Current Crop: ${field.currentCropName ?: "Fallow / Preparing"}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = AgriGreenPrimary
                    )
                )
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Field", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun FieldInfoBadge(label: String, value: String, modifier: Modifier = Modifier) {
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
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold, fontSize = 11.sp),
                maxLines = 1
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFieldDialog(
    farmers: List<com.example.models.Farmer>,
    crops: List<com.example.models.Crop>,
    initialCropId: Int? = null,
    initialCropName: String? = null,
    initialSoil: String? = null,
    initialIrrigation: String? = null,
    onOpenScanner: () -> Unit = {},
    onDismiss: () -> Unit,
    onSave: (FieldEntity) -> Unit
) {
    var name by remember { mutableStateOf(if (initialCropName != null) "$initialCropName Plot" else "") }
    var location by remember { mutableStateOf("Sector 4 North Canal Side") }
    var area by remember { mutableStateOf("12.0") }
    var soil by remember { mutableStateOf(initialSoil ?: "Loamy Soil") }
    var irrigation by remember { mutableStateOf(initialIrrigation ?: "Canal & Tubewell") }
    var selectedFarmerId by remember { mutableStateOf(farmers.firstOrNull()?.id ?: 1) }
    var selectedCropId by remember { mutableStateOf(initialCropId ?: crops.firstOrNull()?.id) }

    var cropDropdownExpanded by remember { mutableStateOf(false) }
    val currentSelectedCrop = crops.find { it.id == selectedCropId }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Register New Field")
                IconButton(
                    onClick = onOpenScanner,
                    modifier = Modifier.testTag("dialog_scan_qr_icon_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = "Scan Seed Package QR",
                        tint = AgriGreenPrimary
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Banner if prefilled from seed QR code
                if (initialCropName != null) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = AgriGreenTertiary.copy(alpha = 0.25f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = AgriGreenPrimary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Auto-tagged from Seed QR: $initialCropName",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = AgriGreenPrimary)
                            )
                        }
                    }
                } else {
                    OutlinedButton(
                        onClick = onOpenScanner,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dialog_open_camera_scanner_btn"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AgriGreenPrimary)
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Scan Seed Package QR to Auto-Fill")
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Field Name (e.g. North Plot)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("field_name_input")
                )

                // Crop Selection Dropdown / Selector
                ExposedDropdownMenuBox(
                    expanded = cropDropdownExpanded,
                    onExpandedChange = { cropDropdownExpanded = !cropDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = currentSelectedCrop?.cropName ?: initialCropName ?: "No Crop Assigned",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Assigned Seed / Crop Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = cropDropdownExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                            .testTag("field_crop_selector")
                    )
                    ExposedDropdownMenu(
                        expanded = cropDropdownExpanded,
                        onDismissRequest = { cropDropdownExpanded = false }
                    ) {
                        crops.forEach { crop ->
                            DropdownMenuItem(
                                text = { Text(crop.cropName) },
                                onClick = {
                                    selectedCropId = crop.id
                                    cropDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location / Sector Coordinates") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = area,
                    onValueChange = { area = it },
                    label = { Text("Area in Acres") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = soil,
                        onValueChange = { soil = it },
                        label = { Text("Soil Type") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = irrigation,
                        onValueChange = { irrigation = it },
                        label = { Text("Irrigation") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val f = FieldEntity(
                            fieldName = name,
                            farmerId = selectedFarmerId,
                            location = location,
                            areaAcres = area.toDoubleOrNull() ?: 5.0,
                            soilType = soil,
                            irrigationType = irrigation,
                            currentCropId = selectedCropId
                        )
                        onSave(f)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AgriGreenPrimary),
                modifier = Modifier.testTag("save_field_confirm_btn")
            ) {
                Text("Save Field")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
