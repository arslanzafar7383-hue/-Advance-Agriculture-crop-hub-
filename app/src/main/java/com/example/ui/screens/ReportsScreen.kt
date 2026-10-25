package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.models.CropProductionStat
import com.example.models.FarmerProductionStat
import com.example.ui.components.StatCard
import com.example.ui.theme.AgriGreenPrimary
import com.example.ui.theme.AgriGreenTertiary
import com.example.viewmodels.CropHubUiState

@Composable
fun ReportsScreen(
    uiState: CropHubUiState
) {
    val reports = uiState.reports
    val totals = reports?.totals

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("reports_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Database Analytics & Reports",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = AgriGreenPrimary
                )
            )
            Text(
                text = "Aggregated SQL query views (GROUP BY, SUM, COUNT)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Summary KPI Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "Total Farmers",
                        value = "${totals?.totalFarmers ?: uiState.farmers.size}",
                        subtitle = "DBMS farmers table",
                        icon = Icons.Default.People,
                        iconTint = Color(0xFF0288D1),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Total Crops",
                        value = "${totals?.totalCrops ?: uiState.crops.size}",
                        subtitle = "Registered varieties",
                        icon = Icons.Default.Grass,
                        iconTint = AgriGreenPrimary,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "Active Fields",
                        value = "${totals?.totalFields ?: uiState.fields.size}",
                        subtitle = "Land parcels",
                        icon = Icons.Default.Landscape,
                        iconTint = Color(0xFF7B1FA2),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Market Products",
                        value = "${totals?.totalProducts ?: uiState.products.size}",
                        subtitle = "Inventory items",
                        icon = Icons.Default.Storefront,
                        iconTint = Color(0xFFF57F17),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Financial Performance Statement Card
        item {
            val rev = totals?.totalRevenue ?: 3450000.0
            val cost = totals?.totalCost ?: 1860000.0
            val profit = totals?.netProfit ?: (rev - cost)

            ElevatedCard(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "💰 Production Financial Statement",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Total Production", style = MaterialTheme.typography.labelSmall)
                            Text(
                                "${(totals?.totalProductionKg ?: 105000.0).toInt()} kg",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        Column {
                            Text("Total Cost", style = MaterialTheme.typography.labelSmall)
                            Text(
                                "Rs. ${cost.toInt()}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        Column {
                            Text("Total Revenue", style = MaterialTheme.typography.labelSmall)
                            Text(
                                "Rs. ${rev.toInt()}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
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
                            text = "Net Agricultural Profit Margin:",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Rs. ${profit.toInt()}",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (profit >= 0) AgriGreenPrimary else Color.Red
                            )
                        )
                    }
                }
            }
        }

        // Crop-Wise Production Breakdown with Visual Bars
        item {
            Text(
                text = "🌾 Crop-Wise Harvest Yield (kg)",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        item {
            val cropStats = reports?.cropWiseProduction ?: emptyList()
            val maxKg = (cropStats.maxOfOrNull { it.totalKg } ?: 1.0).coerceAtLeast(1.0)

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (cropStats.isEmpty()) {
                        Text("No crop yields recorded yet.")
                    } else {
                        cropStats.forEach { stat ->
                            CropBarItem(stat = stat, maxKg = maxKg)
                        }
                    }
                }
            }
        }

        // Farmer-Wise Production Ranking
        item {
            Text(
                text = "👨🌾 Top Farmer Producers",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        item {
            val farmerStats = reports?.farmerWiseProduction ?: emptyList()

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (farmerStats.isEmpty()) {
                        Text("No farmer harvest logs recorded yet.")
                    } else {
                        farmerStats.forEachIndexed { index, fStat ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (index == 0) AgriGreenTertiary else MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = "#${index + 1}",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = fStat.name,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(
                                            text = "Farm Area: ${fStat.farmSizeAcres} Acres",
                                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        )
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "${fStat.totalKg.toInt()} kg",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = AgriGreenPrimary
                                        )
                                    )
                                    Text(
                                        text = "Rs. ${fStat.totalRevenue.toInt()}",
                                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    )
                                }
                            }
                            if (index < farmerStats.size - 1) {
                                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CropBarItem(stat: CropProductionStat, maxKg: Double) {
    val progress = (stat.totalKg / maxKg).coerceIn(0.05, 1.0).toFloat()

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${stat.cropName} (${stat.category})",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${stat.totalKg.toInt()} kg",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = AgriGreenPrimary
                )
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        // Progress Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = progress)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(5.dp))
                    .background(AgriGreenPrimary)
            )
        }
    }
}
