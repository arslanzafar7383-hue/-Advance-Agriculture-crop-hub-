package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.example.models.User
import com.example.ui.components.FieldDashboardCard
import com.example.ui.components.StatCard
import com.example.ui.theme.AgriGreenPrimary
import com.example.ui.theme.AgriGreenPrimaryDark
import com.example.ui.theme.AgriGreenTertiary
import com.example.viewmodels.CropHubUiState

@Composable
fun HomeScreen(
    uiState: CropHubUiState,
    currentUser: User?,
    onNavigateCrops: () -> Unit,
    onNavigateFarmers: () -> Unit,
    onNavigateFields: () -> Unit,
    onNavigateProduction: () -> Unit,
    onNavigateFertilizers: () -> Unit,
    onNavigateDiseases: () -> Unit,
    onNavigateMarketplace: () -> Unit,
    onNavigateWeather: () -> Unit,
    onNavigateReports: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("home_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Hero User Banner
        item {
            ElevatedCard(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = Color.Transparent),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(AgriGreenPrimaryDark, AgriGreenPrimary)
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Welcome back,",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color.White.copy(alpha = 0.85f)
                                    )
                                )
                                Text(
                                    text = currentUser?.fullName ?: "Agriculture Leader",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 22.sp
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = AgriGreenTertiary,
                                modifier = Modifier.padding(start = 8.dp)
                            ) {
                                Text(
                                    text = currentUser?.userType ?: "Farmer",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    ),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Centralized agricultural crop monitoring, field agronomy & farm business analytics.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White.copy(alpha = 0.9f),
                                lineHeight = 18.sp
                            )
                        )
                    }
                }
            }
        }

        // 2. Weather Mini Widget
        item {
            val weatherRec = uiState.weather?.current
            Card(
                onClick = onNavigateWeather,
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
                    .testTag("home_weather_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFFF3E0)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.WbSunny,
                                contentDescription = null,
                                tint = Color(0xFFF57F17),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "${weatherRec?.temperatureC ?: 28.5}°C • ${weatherRec?.conditionText ?: "Clear Sunny"}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "${weatherRec?.locationName ?: "Central Farm Zone"} • Rain: ${weatherRec?.rainProbabilityPct ?: 10}%",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "View Weather",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // 3. Quick Stats Grid
        item {
            Text(
                text = "Key Farm Statistics",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        item {
            val totals = uiState.reports?.totals
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "Registered Crops",
                        value = "${uiState.crops.size}",
                        subtitle = "Active seasonal crops",
                        icon = Icons.Default.Grass,
                        iconTint = AgriGreenPrimary,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateCrops
                    )
                    StatCard(
                        title = "Member Farmers",
                        value = "${uiState.farmers.size}",
                        subtitle = "Verified profiles",
                        icon = Icons.Default.People,
                        iconTint = Color(0xFF0288D1),
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateFarmers
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "Monitored Fields",
                        value = "${uiState.fields.size}",
                        subtitle = "Cultivated land plots",
                        icon = Icons.Default.Landscape,
                        iconTint = Color(0xFF7B1FA2),
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateFields
                    )
                    StatCard(
                        title = "Harvest Yield",
                        value = "${(totals?.totalProductionKg ?: 105000.0).toInt()} kg",
                        subtitle = "Logged crop yield",
                        icon = Icons.Default.Agriculture,
                        iconTint = Color(0xFFE65100),
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateProduction
                    )
                }
            }
        }

        // 3.5 Featured Field Spotlight (Dashboard Card Component)
        if (uiState.fields.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Active Field Telemetry",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    TextButton(onClick = onNavigateFields) {
                        Text(
                            text = "All Fields (${uiState.fields.size})",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = AgriGreenPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                FieldDashboardCard(
                    field = uiState.fields.first(),
                    onClick = onNavigateFields,
                    showActions = true
                )
            }
        }

        // 4. Main Agriculture Modules Grid
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Agriculture Management Modules",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ModuleCard(
                        title = "🌾 Crops Catalog",
                        desc = "Varieties, seasons & yield",
                        color = Color(0xFFE8F5E9),
                        accent = AgriGreenPrimary,
                        icon = Icons.Default.Spa,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateCrops
                    )
                    ModuleCard(
                        title = "👨🌾 Farmers",
                        desc = "Farmer directories & land",
                        color = Color(0xFFE1F5FE),
                        accent = Color(0xFF0288D1),
                        icon = Icons.Default.Person,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateFarmers
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ModuleCard(
                        title = "🚜 Fields & Soil",
                        desc = "Irrigation & soil parcels",
                        color = Color(0xFFF3E5F5),
                        accent = Color(0xFF7B1FA2),
                        icon = Icons.Default.Map,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateFields
                    )
                    ModuleCard(
                        title = "📊 Production",
                        desc = "Harvest, costs & revenue",
                        color = Color(0xFFFFF3E0),
                        accent = Color(0xFFE65100),
                        icon = Icons.Default.Timeline,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateProduction
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ModuleCard(
                        title = "🧪 Fertilizers",
                        desc = "Dosages, NPK & types",
                        color = Color(0xFFE0F2F1),
                        accent = Color(0xFF00796B),
                        icon = Icons.Default.Science,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateFertilizers
                    )
                    ModuleCard(
                        title = "🦠 Crop Diseases",
                        desc = "Symptoms & treatments",
                        color = Color(0xFFFFEBEE),
                        accent = Color(0xFFD32F2F),
                        icon = Icons.Default.BugReport,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateDiseases
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ModuleCard(
                        title = "🛒 Marketplace",
                        desc = "Seeds, tools & chemicals",
                        color = Color(0xFFFFFDE7),
                        accent = Color(0xFFF57F17),
                        icon = Icons.Default.Storefront,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateMarketplace
                    )
                    ModuleCard(
                        title = "🌦 Agro Weather",
                        desc = "Rain forecast & radar",
                        color = Color(0xFFE8EAF6),
                        accent = Color(0xFF3949AB),
                        icon = Icons.Default.Cloud,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateWeather
                    )
                }
            }
        }

        // 5. Crop Reports CTA
        item {
            ElevatedCard(
                onClick = onNavigateReports,
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = AgriGreenPrimary.copy(alpha = 0.08f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, AgriGreenPrimary.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                    .testTag("home_crop_reports_cta")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(AgriGreenPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "📊 View DBMS Analytics & Reports",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Crop-wise yields, farmer harvest rankings & profit statements",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = AgriGreenPrimary
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun ModuleCard(
    title: String,
    desc: String,
    color: Color,
    accent: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color),
        modifier = modifier
            .height(115.dp)
            .border(1.dp, accent.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B241B)
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(22.dp)
                )
            }
            Text(
                text = desc,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Color(0xFF455A48),
                    fontSize = 11.sp
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
