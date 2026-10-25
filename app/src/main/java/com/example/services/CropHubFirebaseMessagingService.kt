package com.example.services

import android.util.Log
import com.example.utils.NotificationHelper
import com.example.utils.PreferencesManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class CropHubFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "CropHubFCM"
    }

    @Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
    @Deprecated("Deprecated in FirebaseMessagingService SDK", ReplaceWith("onNewToken(token)"))
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM Token received: $token")
        val prefs = PreferencesManager(applicationContext)
        prefs.fcmToken = token
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "FCM Message received from: ${remoteMessage.from}")

        val prefs = PreferencesManager(applicationContext)
        val data = remoteMessage.data

        val type = data["type"] ?: data["category_type"] ?: ""
        val title = data["title"] ?: remoteMessage.notification?.title ?: "Crop Hub Alert"
        val body = data["body"] ?: data["message"] ?: remoteMessage.notification?.body ?: "You have a new update."

        when (type.lowercase()) {
            "marketplace", "product", "listing" -> {
                if (!prefs.notificationsMarketplaceEnabled) return

                val category = data["category"] ?: ""
                val productId = data["product_id"]?.toIntOrNull() ?: 0

                // Check if relevant to user interest based on viewed categories or past purchases
                if (category.isBlank() || prefs.isCategoryOfInterest(category)) {
                    NotificationHelper.sendMarketplaceNotification(
                        context = applicationContext,
                        title = title,
                        body = body,
                        productId = productId,
                        category = category
                    )
                }
            }

            "weather", "weather_alert" -> {
                if (!prefs.notificationsWeatherEnabled) return

                val fieldLocation = data["field_location"] ?: data["location"] ?: "Registered Field"
                val isSevere = data["severity"]?.equals("critical", ignoreCase = true) != false

                NotificationHelper.sendWeatherAlertNotification(
                    context = applicationContext,
                    title = title,
                    body = body,
                    fieldLocation = fieldLocation,
                    isSevere = isSevere
                )
            }

            "order", "order_status" -> {
                if (!prefs.notificationsOrdersEnabled) return

                val orderId = data["order_id"]?.toIntOrNull() ?: 1
                val status = data["status"] ?: "Updated"

                NotificationHelper.sendOrderStatusNotification(
                    context = applicationContext,
                    title = title,
                    body = body,
                    orderId = orderId,
                    status = status
                )
            }

            else -> {
                // If notification object exists, deliver using best match or marketplace default
                if (prefs.notificationsMarketplaceEnabled) {
                    NotificationHelper.sendMarketplaceNotification(
                        context = applicationContext,
                        title = title,
                        body = body
                    )
                }
            }
        }
    }
}
