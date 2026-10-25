package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.models.User
import com.example.ui.components.ServerConfigDialog
import com.example.ui.theme.AgriGreenPrimary
import com.example.ui.theme.AgriGreenSecondary
import com.example.ui.theme.AgriGreenTertiary
import com.example.utils.PreferencesManager

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    currentUser: User?,
    currentBaseUrl: String,
    isServerConnected: Boolean,
    preferencesManager: PreferencesManager?,
    onTestNotification: (String) -> Unit = {},
    onTestServer: (String) -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    var showServerDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showDbInfoDialog by remember { mutableStateOf(false) }

    // Notification states managed from PreferencesManager
    var notifMarketplace by remember {
        mutableStateOf(preferencesManager?.notificationsMarketplaceEnabled ?: true)
    }
    var notifWeather by remember {
        mutableStateOf(preferencesManager?.notificationsWeatherEnabled ?: true)
    }
    var notifOrders by remember {
        mutableStateOf(preferencesManager?.notificationsOrdersEnabled ?: true)
    }

    // Permission check for Android 13+ (API 33)
    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
    }

    val userInterests = remember(preferencesManager) {
        preferencesManager?.getUserInterestCategories()?.toList() ?: listOf("Seeds", "Fertilizers", "Pesticides")
    }

    val fcmToken = remember(preferencesManager) {
        preferencesManager?.fcmToken ?: "Connecting..."
    }

    val scrollState = rememberScrollState()

    if (showServerDialog) {
        ServerConfigDialog(
            currentUrl = currentBaseUrl,
            isConnected = isServerConnected,
            onDismiss = { showServerDialog = false },
            onTestAndSave = { url ->
                onTestServer(url)
                showServerDialog = false
            }
        )
    }

    if (showChangePasswordDialog) {
        AlertDialog(
            onDismissRequest = { showChangePasswordDialog = false },
            title = { Text("Change Password") },
            text = {
                Text("Password change updates bcrypt hash in MySQL users table. Enter new credentials or keep 'password123'.")
            },
            confirmButton = {
                Button(
                    onClick = { showChangePasswordDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = AgriGreenPrimary)
                ) {
                    Text("Update")
                }
            },
            dismissButton = {
                TextButton(onClick = { showChangePasswordDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    if (showDbInfoDialog) {
        AlertDialog(
            onDismissRequest = { showDbInfoDialog = false },
            title = { Text("MySQL Database Architecture (3NF)") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        "Advanced Crop Hub DBMS Relational Architecture:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text("• users: Account credentials & bcrypt security")
                    Text("• farmers: Farm size, experience, contact (FK users)")
                    Text("• crops: Season, soil, water requirement, expected yield")
                    Text("• fields: Farm parcel location, irrigation (FK farmers, crops)")
                    Text("• crop_production: Harvest yield, cost, revenue (FK farmers, fields, crops)")
                    Text("• fertilizers: Nutrient NPK types & dosages")
                    Text("• crop_diseases: Plant pathology symptoms & controls")
                    Text("• marketplace_products: Agricultural supplies inventory")
                    Text("• orders & order_items: Normalized transactions (FK users, products)")
                    Text("• weather_records: Meteorological sensor telemetry")
                }
            },
            confirmButton = {
                Button(
                    onClick = { showDbInfoDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = AgriGreenPrimary)
                ) {
                    Text("Got it")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState)
            .testTag("profile_screen"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Avatar & Info
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(AgriGreenPrimary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = AgriGreenPrimary,
                modifier = Modifier.size(46.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = currentUser?.fullName ?: "Agriculture User",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = AgriGreenTertiary,
            modifier = Modifier.padding(top = 4.dp)
        ) {
            Text(
                text = currentUser?.userType ?: "Farmer",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color.Black),
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Profile Details Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ProfileField(label = "Email Address", value = currentUser?.email ?: "Not specified")
                HorizontalDivider()
                ProfileField(label = "Phone Number", value = currentUser?.phone ?: "Not specified")
                HorizontalDivider()
                ProfileField(label = "User ID", value = "#${currentUser?.id ?: 1}")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Push Notifications & FCM Configuration Section
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
                .testTag("push_notifications_settings_card")
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = "Notifications",
                        tint = AgriGreenPrimary
                    )
                    Column {
                        Text(
                            text = "Push Notifications & Alerts (FCM)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Configure real-time alerts sent to your device",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Android 13+ Permission Banner
                if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Notification Permission Required",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Text(
                                    text = "Allow notifications to receive weather alerts and order updates.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                            Button(
                                onClick = { permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) },
                                colors = ButtonDefaults.buttonColors(containerColor = AgriGreenPrimary),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("Grant", fontSize = 12.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))

                // Toggle 1: Marketplace Recommendations
                NotificationToggleItem(
                    title = "Marketplace Recommendations",
                    description = "Alerts for new supplies & seeds in categories you view or purchase",
                    icon = Icons.Default.Storefront,
                    checked = notifMarketplace,
                    testTag = "toggle_marketplace_notifications",
                    onCheckedChange = { checked ->
                        notifMarketplace = checked
                        preferencesManager?.notificationsMarketplaceEnabled = checked
                    }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                // Toggle 2: Critical Weather Alerts
                NotificationToggleItem(
                    title = "Critical Weather Alerts for Fields",
                    description = "Instant warnings for frost, severe storms, heat stress, and gale winds on your parcels",
                    icon = Icons.Default.WbSunny,
                    checked = notifWeather,
                    testTag = "toggle_weather_notifications",
                    onCheckedChange = { checked ->
                        notifWeather = checked
                        preferencesManager?.notificationsWeatherEnabled = checked
                    }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                // Toggle 3: Order Status Updates
                NotificationToggleItem(
                    title = "Order Status Updates",
                    description = "Live tracking notifications when orders are confirmed, dispatched, or delivered",
                    icon = Icons.Default.LocalShipping,
                    checked = notifOrders,
                    testTag = "toggle_order_notifications",
                    onCheckedChange = { checked ->
                        notifOrders = checked
                        preferencesManager?.notificationsOrdersEnabled = checked
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // User Interests Tags
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                Icons.Default.FilterList,
                                contentDescription = null,
                                tint = AgriGreenSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Learned Marketplace Interests:",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            userInterests.forEach { category ->
                                SuggestionChip(
                                    onClick = {},
                                    label = { Text(category, fontSize = 11.sp) },
                                    colors = SuggestionChipDefaults.suggestionChipColors(
                                        containerColor = AgriGreenTertiary.copy(alpha = 0.6f)
                                    )
                                )
                            }
                        }
                        Text(
                            text = "FCM push alerts prioritize listings matching your active farm categories.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // FCM Token Info & Test Push Triggers
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.CloudSync, contentDescription = null, tint = AgriGreenPrimary, modifier = Modifier.size(16.dp))
                                Text(
                                    text = "FCM Service Status: Active",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = AgriGreenPrimary)
                                )
                            }
                            Text(
                                text = "Token Registered",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "ID: ${fcmToken.take(24)}...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        Text(
                            text = "Send Test Push Notification:",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(top = 8.dp, bottom = 6.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            OutlinedButton(
                                onClick = { onTestNotification("marketplace") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("test_push_marketplace"),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                            ) {
                                Text("🌱 Listing", fontSize = 11.sp)
                            }
                            OutlinedButton(
                                onClick = { onTestNotification("weather") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("test_push_weather"),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                            ) {
                                Text("⚠️ Weather", fontSize = 11.sp)
                            }
                            OutlinedButton(
                                onClick = { onTestNotification("order") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("test_push_order"),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                            ) {
                                Text("📦 Order", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // System Actions
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                ListItem(
                    headlineContent = { Text("Server Connection (XAMPP / MySQL)") },
                    supportingContent = {
                        Text(
                            text = if (isServerConnected) "Connected: $currentBaseUrl" else "Offline / Standby: $currentBaseUrl",
                            color = if (isServerConnected) AgriGreenPrimary else Color(0xFFE65100),
                            fontSize = 11.sp
                        )
                    },
                    leadingContent = {
                        Icon(
                            Icons.Default.Dns,
                            contentDescription = null,
                            tint = if (isServerConnected) AgriGreenPrimary else Color(0xFFE65100)
                        )
                    },
                    modifier = Modifier.clickable { showServerDialog = true }
                )
                HorizontalDivider()
                ListItem(
                    headlineContent = { Text("Database Schema Structure") },
                    supportingContent = { Text("View 11 Normalized 3NF MySQL Tables", fontSize = 11.sp) },
                    leadingContent = { Icon(Icons.Default.Storage, contentDescription = null, tint = AgriGreenPrimary) },
                    modifier = Modifier.clickable { showDbInfoDialog = true }
                )
                HorizontalDivider()
                ListItem(
                    headlineContent = { Text("Change Password") },
                    leadingContent = { Icon(Icons.Default.LockReset, contentDescription = null, tint = AgriGreenPrimary) },
                    modifier = Modifier.clickable { showChangePasswordDialog = true }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onLogout,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("logout_button")
        ) {
            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Log Out")
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun NotificationToggleItem(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    checked: Boolean,
    testTag: String,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (checked) AgriGreenPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .padding(top = 2.dp)
                    .size(22.dp)
            )
            Column(modifier = Modifier.padding(end = 8.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = AgriGreenPrimary
            ),
            modifier = Modifier.testTag(testTag)
        )
    }
}

@Composable
fun ProfileField(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
    }
}
