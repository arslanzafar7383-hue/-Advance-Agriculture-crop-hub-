package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AgriGreenPrimary
import com.example.ui.theme.AgriGreenPrimaryDark
import com.example.ui.theme.AgriGreenTertiary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgriTopBar(
    title: String,
    onBackClick: (() -> Unit)? = null,
    onServerClick: (() -> Unit)? = null,
    onCartClick: (() -> Unit)? = null,
    cartCount: Int = 0,
    isServerConnected: Boolean = true
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Advanced Agriculture Crop Hub",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        },
        navigationIcon = {
            if (onBackClick != null) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.testTag("top_bar_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Navigate Back"
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .padding(start = 12.dp, end = 8.dp)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(AgriGreenPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Grass,
                        contentDescription = "Agri Icon",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        },
        actions = {
            // Live Server Status Indicator Chip
            if (onServerClick != null) {
                Surface(
                    onClick = onServerClick,
                    shape = RoundedCornerShape(16.dp),
                    color = if (isServerConnected) Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isServerConnected) Color(0xFF81C784) else Color(0xFFFFB74D)
                    ),
                    modifier = Modifier
                        .padding(end = 6.dp)
                        .testTag("server_status_chip")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isServerConnected) Color(0xFF2E7D32) else Color(0xFFE65100))
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isServerConnected) "MySQL" else "Standby",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = if (isServerConnected) Color(0xFF1B5E20) else Color(0xFFBF360C)
                            )
                        )
                    }
                }
            }

            if (onCartClick != null) {
                IconButton(
                    onClick = onCartClick,
                    modifier = Modifier.testTag("top_bar_cart_button")
                ) {
                    BadgedBox(
                        badge = {
                            if (cartCount > 0) {
                                Badge(
                                    containerColor = AgriGreenTertiary,
                                    contentColor = Color.Black
                                ) {
                                    Text("$cartCount", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = "View Cart"
                        )
                    }
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

@Composable
fun ServerConfigDialog(
    currentUrl: String,
    isConnected: Boolean,
    onDismiss: () -> Unit,
    onTestAndSave: (String) -> Unit
) {
    var urlInput by remember { mutableStateOf(currentUrl) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Dns,
                    contentDescription = null,
                    tint = AgriGreenPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Backend Server (MySQL/PHP)")
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Configure your XAMPP Apache endpoint to communicate with the MySQL database.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = urlInput,
                    onValueChange = { urlInput = it },
                    label = { Text("API Base URL") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("server_url_input")
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SuggestionChip(
                        onClick = { urlInput = "http://10.0.2.2/crop_hub_api/api/" },
                        label = { Text("Emulator (10.0.2.2)", fontSize = 11.sp) }
                    )
                    SuggestionChip(
                        onClick = { urlInput = "http://localhost/crop_hub_api/api/" },
                        label = { Text("Localhost", fontSize = 11.sp) }
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isConnected) Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isConnected) Icons.Default.CheckCircle else Icons.Default.Info,
                            contentDescription = null,
                            tint = if (isConnected) Color(0xFF2E7D32) else Color(0xFFE65100),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isConnected) "Active Connection: MySQL database is online."
                            else "Notice: If XAMPP is offline, verified local database cache is active so app never fails.",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isConnected) Color(0xFF1B5E20) else Color(0xFFBF360C)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onTestAndSave(urlInput) },
                colors = ButtonDefaults.buttonColors(containerColor = AgriGreenPrimary),
                modifier = Modifier.testTag("test_server_button")
            ) {
                Text("Test & Connect")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun StatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color = AgriGreenPrimary,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    ElevatedCard(
        onClick = { onClick?.invoke() },
        enabled = onClick != null,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = modifier
            .border(
                1.dp,
                MaterialTheme.colorScheme.surfaceVariant,
                RoundedCornerShape(16.dp)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(iconTint.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun MedicalDisclaimerBanner() {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFFFF8E1),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFE082)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = Color(0xFFF57F17),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Disclaimer: Information provided is for educational & agricultural reference. Always consult certified agronomists or plant pathology laboratory services for commercial disease management.",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    color = Color(0xFF5D4037)
                )
            )
        }
    }
}
