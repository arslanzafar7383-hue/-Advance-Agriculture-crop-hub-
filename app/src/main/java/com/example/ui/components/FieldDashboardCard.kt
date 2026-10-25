package com.example.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.models.FieldEntity
import com.example.ui.theme.AgriGreenPrimary
import com.example.ui.theme.AgriGreenPrimaryDark
import com.example.ui.theme.AgriGreenSecondary

enum class CropGrowthStage(
    val label: String,
    val progress: Float,
    val containerColor: Color,
    val contentColor: Color,
    val icon: ImageVector
) {
    ACTIVE_GROWING("Growing", 0.65f, Color(0xFFE8F5E9), Color(0xFF2E7D32), Icons.Default.Eco),
    HARVEST_READY("Harvest Ready", 0.95f, Color(0xFFFFF8E1), Color(0xFFF57F17), Icons.Default.Agriculture),
    SOWING_STAGE("Germinating", 0.25f, Color(0xFFE1F5FE), Color(0xFF0277BD), Icons.Default.Grass),
    FALLOW("Fallow / Idle", 0.05f, Color(0xFFF5F5F5), Color(0xFF616161), Icons.Default.Spa);

    companion object {
        fun fromCropName(cropName: String?): CropGrowthStage {
            if (cropName.isNullOrBlank() || cropName.contains("fallow", ignoreCase = true)) {
                return FALLOW
            }
            return when {
                cropName.contains("rice", ignoreCase = true) || cropName.contains("dhan", ignoreCase = true) -> HARVEST_READY
                cropName.contains("wheat", ignoreCase = true) || cropName.contains("gandum", ignoreCase = true) -> ACTIVE_GROWING
                cropName.contains("cotton", ignoreCase = true) || cropName.contains("kapas", ignoreCase = true) -> ACTIVE_GROWING
                cropName.contains("maize", ignoreCase = true) -> SOWING_STAGE
                else -> ACTIVE_GROWING
            }
        }
    }
}

/**
 * A modern Material 3 Dashboard Card component specifically designed to display
 * agricultural Field information, including real-time location telemetry, crop status,
 * acreage, soil chemistry, irrigation method, and growth cycle progression.
 */
@Composable
fun FieldDashboardCard(
    field: FieldEntity,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    showActions: Boolean = true
) {
    val growthStage = remember(field.currentCropName) {
        CropGrowthStage.fromCropName(field.currentCropName)
    }

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("field_dashboard_card_${field.id}")
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else Modifier
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 3.dp,
            pressedElevation = 6.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize()
        ) {
            // 1. Top Decorative Gradient Header with Field Name, Badge & Acreage
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                AgriGreenPrimaryDark,
                                AgriGreenPrimary,
                                AgriGreenSecondary
                            )
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Landscape,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = field.fieldName,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    letterSpacing = 0.3.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.85f),
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = field.farmerName ?: "Assigned Farmer",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color.White.copy(alpha = 0.9f),
                                        fontWeight = FontWeight.Medium
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    // Acreage Badge
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.95f),
                        shadowElevation = 2.dp
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Straighten,
                                contentDescription = null,
                                tint = AgriGreenPrimaryDark,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${field.areaAcres} Acres",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = AgriGreenPrimaryDark
                                )
                            )
                        }
                    }
                }
            }

            // 2. Card Body Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Location Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location",
                        tint = Color(0xFFD32F2F),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = field.location.ifBlank { "GPS Coordinates Recorded" },
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = AgriGreenPrimary.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "Geo-Mapped",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = AgriGreenPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            ),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                // Crop Status Section with Growth Progress
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = growthStage.containerColor,
                    border = androidx.compose.foundation.BorderStroke(1.dp, growthStage.contentColor.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(growthStage.contentColor.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = growthStage.icon,
                                        contentDescription = null,
                                        tint = growthStage.contentColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Current Crop",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = growthStage.contentColor.copy(alpha = 0.85f),
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                    Text(
                                        text = field.currentCropName ?: "Fallow / Soil Prep",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = growthStage.contentColor
                                        )
                                    )
                                }
                            }

                            // Status Tag
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = growthStage.contentColor
                            ) {
                                Text(
                                    text = growthStage.label,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        // Growth Timeline Bar
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Vegetative Season Progress",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        color = growthStage.contentColor.copy(alpha = 0.85f)
                                    )
                                )
                                Text(
                                    text = "${(growthStage.progress * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = growthStage.contentColor
                                    )
                                )
                            }
                            LinearProgressIndicator(
                                progress = { growthStage.progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = growthStage.contentColor,
                                trackColor = growthStage.contentColor.copy(alpha = 0.2f)
                            )
                        }
                    }
                }

                // Field Agronomy Attributes Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FieldAttributeTile(
                        icon = Icons.Default.Terrain,
                        label = "Soil Type",
                        value = field.soilType.ifBlank { "Loam" },
                        tint = Color(0xFF795548),
                        modifier = Modifier.weight(1f)
                    )
                    FieldAttributeTile(
                        icon = Icons.Default.WaterDrop,
                        label = "Irrigation",
                        value = field.irrigationType.ifBlank { "Canal" },
                        tint = Color(0xFF0288D1),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Bottom Action Footer
                if (showActions) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Plot ID: #FLD-${field.id}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (onDelete != null) {
                                IconButton(
                                    onClick = onDelete,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "Delete Field",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            if (onClick != null) {
                                TextButton(
                                    onClick = onClick,
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "View Details",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            color = AgriGreenPrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = null,
                                        tint = AgriGreenPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FieldAttributeTile(
    icon: ImageVector,
    label: String,
    value: String,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(tint.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
