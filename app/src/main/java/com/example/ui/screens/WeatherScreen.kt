package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.models.ForecastDay
import com.example.models.WeatherRecord
import com.example.ui.theme.AgriGreenPrimary
import com.example.ui.theme.AgriGreenPrimaryDark
import com.example.ui.theme.AgriGreenTertiary
import com.example.viewmodels.CropHubUiState
import com.example.viewmodels.WeatherUiState

@Composable
fun WeatherScreen(
    weatherUiState: WeatherUiState,
    onLocationSelected: (String) -> Unit,
    onRefresh: () -> Unit
) {
    var searchLocationText by remember { mutableStateOf("") }
    val weatherData = weatherUiState.weatherData
    val current = weatherData?.current ?: WeatherRecord()
    val forecast = weatherData?.forecast ?: emptyList()

    val popularAgriHubs = listOf("Faisalabad", "Multan", "Lahore", "Kasur", "Okara", "Sahiwal", "Bahawalpur")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("weather_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Location Search Input
        item {
            OutlinedTextField(
                value = searchLocationText,
                onValueChange = { searchLocationText = it },
                placeholder = { Text("Search agricultural district / city...") },
                leadingIcon = {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = AgriGreenPrimary)
                },
                trailingIcon = {
                    if (searchLocationText.isNotBlank()) {
                        IconButton(onClick = {
                            onLocationSelected(searchLocationText)
                            searchLocationText = ""
                        }) {
                            Icon(Icons.Default.Search, contentDescription = "Search Location", tint = AgriGreenPrimary)
                        }
                    } else {
                        IconButton(onClick = onRefresh) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh Telemetry")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("weather_location_input")
            )
        }

        // Quick City Selection Chips
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(popularAgriHubs) { city ->
                    val isSelected = current.locationName.equals(city, ignoreCase = true)
                    FilterChip(
                        selected = isSelected,
                        onClick = { onLocationSelected(city) },
                        label = { Text(city, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AgriGreenPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        // API Key & Telemetry Status Notice
        item {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (weatherUiState.isLiveApiActive) Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (weatherUiState.isLiveApiActive) Color(0xFFA5D6A7) else Color(0xFFFFCC80)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (weatherUiState.isLiveApiActive) Icons.Default.Key else Icons.Default.Sensors,
                        contentDescription = null,
                        tint = if (weatherUiState.isLiveApiActive) Color(0xFF2E7D32) else Color(0xFFE65100),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = weatherUiState.apiKeyStatusNotice,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = if (weatherUiState.isLiveApiActive) Color(0xFF1B5E20) else Color(0xFFBF360C)
                    )
                }
            }
        }

        // Main Weather Hero Card
        item {
            ElevatedCard(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = Color.Transparent),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFF00695C), Color(0xFF004D40))
                            )
                        )
                        .padding(24.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationCity,
                                contentDescription = null,
                                tint = AgriGreenTertiary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = current.locationName,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Icon(
                            imageVector = when {
                                current.rainProbabilityPct > 45 -> Icons.Default.Thunderstorm
                                current.rainProbabilityPct > 25 -> Icons.Default.CloudQueue
                                else -> Icons.Default.WbSunny
                            },
                            contentDescription = null,
                            tint = AgriGreenTertiary,
                            modifier = Modifier.size(68.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "${current.temperatureC.toInt()}°C",
                            style = MaterialTheme.typography.displayMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )

                        Text(
                            text = current.conditionText,
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        )

                        Text(
                            text = current.recordedAt,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 10.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Meteorological telemetry parameters
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            WeatherParamItem(
                                icon = Icons.Default.WaterDrop,
                                label = "Humidity",
                                value = "${current.humidityPct}%"
                            )
                            WeatherParamItem(
                                icon = Icons.Default.Umbrella,
                                label = "Rain Prob.",
                                value = "${current.rainProbabilityPct}%"
                            )
                            WeatherParamItem(
                                icon = Icons.Default.Air,
                                label = "Wind Speed",
                                value = "${current.windSpeedKph} km/h"
                            )
                        }
                    }
                }
            }
        }

        // Agricultural Advisory Card (Computed dynamically by WeatherViewModel)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = AgriGreenTertiary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Agricultural Weather Advisory",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = weatherUiState.agriculturalAdvisory.ifBlank {
                                "Favorable solar radiation and calm winds. Ideal window for tractor tillage, canal irrigation scheduling, and fertilizer application."
                            },
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 18.sp
                            )
                        )
                    }
                }
            }
        }

        // 5-Day Forecast
        item {
            Text(
                text = "5-Day Agricultural Forecast",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(forecast) { day ->
                    ForecastCard(forecast = day)
                }
            }
        }
    }
}

@Composable
fun WeatherParamItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(imageVector = icon, contentDescription = null, tint = Color.White.copy(alpha = 0.85f), modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, style = MaterialTheme.typography.labelSmall.copy(color = Color.White.copy(alpha = 0.75f), fontSize = 10.sp))
        Text(text = value, style = MaterialTheme.typography.bodyMedium.copy(color = Color.White, fontWeight = FontWeight.Bold))
    }
}

@Composable
fun ForecastCard(forecast: ForecastDay) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.width(125.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = forecast.day,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Icon(
                imageVector = if (forecast.rainChance > 40) Icons.Default.CloudQueue else Icons.Default.WbSunny,
                contentDescription = null,
                tint = if (forecast.rainChance > 40) Color(0xFF0288D1) else Color(0xFFF57F17),
                modifier = Modifier.size(30.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${forecast.tempMax}° / ${forecast.tempMin}°",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "Rain: ${forecast.rainChance}%",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}
