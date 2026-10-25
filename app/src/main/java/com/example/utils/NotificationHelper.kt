package com.example.utils

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R

object NotificationHelper {

    const val CHANNEL_MARKETPLACE = "crop_hub_channel_marketplace"
    const val CHANNEL_WEATHER = "crop_hub_channel_weather"
    const val CHANNEL_ORDERS = "crop_hub_channel_orders"

    const val EXTRA_DESTINATION = "extra_destination"
    const val EXTRA_ITEM_ID = "extra_item_id"

    const val DESTINATION_MARKETPLACE = "MARKETPLACE"
    const val DESTINATION_WEATHER = "WEATHER"
    const val DESTINATION_ORDERS = "ORDERS"

    private const val NOTIF_ID_MARKETPLACE_BASE = 2000
    private const val NOTIF_ID_WEATHER_BASE = 3000
    private const val NOTIF_ID_ORDERS_BASE = 4000

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Marketplace Channel
            val marketplaceChannel = NotificationChannel(
                CHANNEL_MARKETPLACE,
                "Marketplace & Farm Supply Recommendations",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Alerts for new seeds, fertilizers, and equipment matching your viewed categories and purchases."
                enableLights(true)
                lightColor = Color.GREEN
                setShowBadge(true)
            }

            // Weather Channel
            val weatherChannel = NotificationChannel(
                CHANNEL_WEATHER,
                "Critical Field Weather Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Urgent alerts for frost, storms, heat stress, and heavy rains across your registered farm fields."
                enableLights(true)
                lightColor = Color.YELLOW
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 300, 200, 300)
                setShowBadge(true)
            }

            // Orders Channel
            val ordersChannel = NotificationChannel(
                CHANNEL_ORDERS,
                "Order Status Tracking",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Real-time updates on orders placed in the marketplace (Confirmed, Dispatched, Delivered)."
                enableLights(true)
                lightColor = Color.CYAN
                enableVibration(true)
                setShowBadge(true)
            }

            notificationManager.createNotificationChannels(
                listOf(marketplaceChannel, weatherChannel, ordersChannel)
            )
        }
    }

    private fun canPostNotification(context: Context): Boolean {
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            return false
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        }
        return true
    }

    private fun createTapIntent(context: Context, destination: String, itemId: Int = 0): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_DESTINATION, destination)
            putExtra(EXTRA_ITEM_ID, itemId)
        }
        return PendingIntent.getActivity(
            context,
            destination.hashCode() + itemId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun sendMarketplaceNotification(
        context: Context,
        title: String,
        body: String,
        productId: Int = 0,
        category: String = ""
    ) {
        val prefs = PreferencesManager(context)
        if (!prefs.notificationsMarketplaceEnabled) return
        if (!canPostNotification(context)) return

        createNotificationChannels(context)

        val pendingIntent = createTapIntent(context, DESTINATION_MARKETPLACE, productId)

        val subtitle = if (category.isNotBlank()) "Category: $category" else "New Agricultural Listing"

        val notification = NotificationCompat.Builder(context, CHANNEL_MARKETPLACE)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setSubText(subtitle)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(body)
                    .setSummaryText(subtitle)
            )
            .setColor(0xFF2E7D32.toInt()) // AgriGreenPrimary
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        val notificationId = NOTIF_ID_MARKETPLACE_BASE + (productId % 500)
        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (_: SecurityException) {}
    }

    fun sendWeatherAlertNotification(
        context: Context,
        title: String,
        body: String,
        fieldLocation: String = "",
        isSevere: Boolean = true
    ) {
        val prefs = PreferencesManager(context)
        if (!prefs.notificationsWeatherEnabled) return
        if (!canPostNotification(context)) return

        createNotificationChannels(context)

        val pendingIntent = createTapIntent(context, DESTINATION_WEATHER)

        val summary = if (fieldLocation.isNotBlank()) "Field: $fieldLocation" else "Agri Advisory"

        val notification = NotificationCompat.Builder(context, CHANNEL_WEATHER)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setSubText(summary)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(body)
                    .setSummaryText(summary)
            )
            .setColor(if (isSevere) 0xFFD32F2F.toInt() else 0xFFF57C00.toInt())
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .build()

        val notificationId = NOTIF_ID_WEATHER_BASE + (fieldLocation.hashCode() % 100).coerceAtLeast(0)
        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (_: SecurityException) {}
    }

    fun sendOrderStatusNotification(
        context: Context,
        title: String,
        body: String,
        orderId: Int,
        status: String
    ) {
        val prefs = PreferencesManager(context)
        if (!prefs.notificationsOrdersEnabled) return
        if (!canPostNotification(context)) return

        createNotificationChannels(context)

        val pendingIntent = createTapIntent(context, DESTINATION_ORDERS, orderId)

        val notification = NotificationCompat.Builder(context, CHANNEL_ORDERS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setSubText("Order #$orderId • $status")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(body)
                    .setSummaryText("Status: $status")
            )
            .setColor(0xFF1976D2.toInt())
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        val notificationId = NOTIF_ID_ORDERS_BASE + (orderId % 500)
        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (_: SecurityException) {}
    }
}
