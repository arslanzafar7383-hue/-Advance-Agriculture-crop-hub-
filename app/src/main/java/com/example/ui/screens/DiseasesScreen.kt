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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.models.CropDisease
import com.example.ui.components.MedicalDisclaimerBanner
import com.example.ui.theme.AgriGreenPrimary
import com.example.viewmodels.CropHubUiState

@Composable
fun DiseasesScreen(
    uiState: CropHubUiState,
    onSearchChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("diseases_screen")
    ) {
        // Disclaimer Banner
        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
            MedicalDisclaimerBanner()
        }

        // Search Bar
        OutlinedTextField(
            value = uiState.diseaseSearchQuery,
            onValueChange = onSearchChange,
            placeholder = { Text("Search crop disease or symptom...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = AgriGreenPrimary) },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .testTag("disease_search_input")
        )

        if (uiState.diseases.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No matching crop disease records found.")
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(uiState.diseases, key = { it.id }) { disease ->
                    DiseaseCard(disease = disease)
                }
            }
        }
    }
}

@Composable
fun DiseaseCard(disease: CropDisease) {
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
                            .background(Color(0xFFFFEBEE)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.BugReport,
                            contentDescription = null,
                            tint = Color(0xFFD32F2F),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = disease.diseaseName,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Affected Crops: ${disease.affectedCrops}",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                }
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFFFEBEE)
                ) {
                    Text(
                        text = "Info Guide",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFC62828)
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Symptoms
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFFFF8E1),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "🔬 Visual Symptoms:",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE65100)
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = disease.symptoms,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Prevention
            Text(
                text = "🛡 Prevention Practices:",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = AgriGreenPrimary
                )
            )
            Text(
                text = disease.prevention,
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Treatment & Control Method
            Text(
                text = "💊 Control & Treatment Methods:",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0277BD)
                )
            )
            Text(
                text = disease.treatmentMethod,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
