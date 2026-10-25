package com.example.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.models.User
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("crop_hub_prefs", Context.MODE_PRIVATE)
    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val userAdapter = moshi.adapter(User::class.java)

    companion object {
        private const val KEY_BASE_URL = "base_url"
        private const val KEY_USER = "current_user"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_SERVER_CONNECTED = "server_connected"

        // Push Notifications Config Keys
        private const val KEY_NOTIF_MARKETPLACE = "notifications_marketplace_enabled"
        private const val KEY_NOTIF_WEATHER = "notifications_weather_enabled"
        private const val KEY_NOTIF_ORDERS = "notifications_orders_enabled"
        private const val KEY_FCM_TOKEN = "fcm_token"

        // User Interests (Viewed and Purchased categories)
        private const val KEY_VIEWED_CATEGORIES = "user_viewed_categories"
        private const val KEY_PURCHASED_CATEGORIES = "user_purchased_categories"

        // Deduplication & Tracking
        private const val KEY_NOTIFIED_PRODUCTS = "notified_product_ids"
        private const val KEY_NOTIFIED_ORDERS = "notified_order_statuses"
        private const val KEY_LAST_WEATHER_ALERT = "last_weather_alert_time"
    }

    var baseUrl: String
        get() = prefs.getString(KEY_BASE_URL, Constants.DEFAULT_BASE_URL) ?: Constants.DEFAULT_BASE_URL
        set(value) {
            val formatted = if (value.endsWith("/")) value else "$value/"
            prefs.edit().putString(KEY_BASE_URL, formatted).apply()
        }

    var currentUser: User?
        get() {
            val json = prefs.getString(KEY_USER, null) ?: return null
            return try {
                userAdapter.fromJson(json)
            } catch (e: Exception) {
                null
            }
        }
        set(value) {
            if (value == null) {
                prefs.edit().remove(KEY_USER).putBoolean(KEY_IS_LOGGED_IN, false).apply()
            } else {
                val json = userAdapter.toJson(value)
                prefs.edit().putString(KEY_USER, json).putBoolean(KEY_IS_LOGGED_IN, true).apply()
            }
        }

    var isLoggedIn: Boolean
        get() = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
        set(value) = prefs.edit().putBoolean(KEY_IS_LOGGED_IN, value).apply()

    var isServerConnected: Boolean
        get() = prefs.getBoolean(KEY_SERVER_CONNECTED, false)
        set(value) = prefs.edit().putBoolean(KEY_SERVER_CONNECTED, value).apply()

    // Push Notification Settings (Configurable by user in Profile)
    var notificationsMarketplaceEnabled: Boolean
        get() = prefs.getBoolean(KEY_NOTIF_MARKETPLACE, true)
        set(value) = prefs.edit().putBoolean(KEY_NOTIF_MARKETPLACE, value).apply()

    var notificationsWeatherEnabled: Boolean
        get() = prefs.getBoolean(KEY_NOTIF_WEATHER, true)
        set(value) = prefs.edit().putBoolean(KEY_NOTIF_WEATHER, value).apply()

    var notificationsOrdersEnabled: Boolean
        get() = prefs.getBoolean(KEY_NOTIF_ORDERS, true)
        set(value) = prefs.edit().putBoolean(KEY_NOTIF_ORDERS, value).apply()

    var fcmToken: String?
        get() = prefs.getString(KEY_FCM_TOKEN, null)
        set(value) = prefs.edit().putString(KEY_FCM_TOKEN, value).apply()

    // User Interests: Viewed & Purchased Categories
    fun recordCategoryViewed(category: String) {
        if (category.isBlank() || category.equals("All", ignoreCase = true)) return
        val current = prefs.getStringSet(KEY_VIEWED_CATEGORIES, emptySet())?.toMutableSet() ?: mutableSetOf()
        current.add(category)
        prefs.edit().putStringSet(KEY_VIEWED_CATEGORIES, current).apply()
    }

    fun recordCategoryPurchased(category: String) {
        if (category.isBlank()) return
        val current = prefs.getStringSet(KEY_PURCHASED_CATEGORIES, emptySet())?.toMutableSet() ?: mutableSetOf()
        current.add(category)
        prefs.edit().putStringSet(KEY_PURCHASED_CATEGORIES, current).apply()
    }

    fun getUserInterestCategories(): Set<String> {
        val viewed = prefs.getStringSet(KEY_VIEWED_CATEGORIES, emptySet()) ?: emptySet()
        val purchased = prefs.getStringSet(KEY_PURCHASED_CATEGORIES, emptySet()) ?: emptySet()
        val combined = (viewed + purchased).toMutableSet()
        // Provide intelligent defaults if user hasn't browsed yet
        if (combined.isEmpty()) {
            combined.addAll(listOf("Seeds", "Fertilizers", "Pesticides"))
        }
        return combined
    }

    fun isCategoryOfInterest(category: String?): Boolean {
        if (category.isNullOrBlank()) return false
        val interests = getUserInterestCategories()
        return interests.any { it.equals(category, ignoreCase = true) }
    }

    // Product Notification Deduplication
    fun isProductNotified(productId: Int): Boolean {
        val notified = prefs.getStringSet(KEY_NOTIFIED_PRODUCTS, emptySet()) ?: emptySet()
        return notified.contains(productId.toString())
    }

    fun markProductNotified(productId: Int) {
        val notified = prefs.getStringSet(KEY_NOTIFIED_PRODUCTS, emptySet())?.toMutableSet() ?: mutableSetOf()
        notified.add(productId.toString())
        prefs.edit().putStringSet(KEY_NOTIFIED_PRODUCTS, notified).apply()
    }

    // Order Status Notification Tracking (id + status)
    fun isOrderStatusNotified(orderId: Int, status: String): Boolean {
        val key = "${orderId}_$status"
        val notified = prefs.getStringSet(KEY_NOTIFIED_ORDERS, emptySet()) ?: emptySet()
        return notified.contains(key)
    }

    fun markOrderStatusNotified(orderId: Int, status: String) {
        val key = "${orderId}_$status"
        val notified = prefs.getStringSet(KEY_NOTIFIED_ORDERS, emptySet())?.toMutableSet() ?: mutableSetOf()
        notified.add(key)
        prefs.edit().putStringSet(KEY_NOTIFIED_ORDERS, notified).apply()
    }

    // Weather Alert Throttle (avoid re-alerting within 1 hour for the same condition)
    var lastWeatherAlertTime: Long
        get() = prefs.getLong(KEY_LAST_WEATHER_ALERT, 0L)
        set(value) = prefs.edit().putLong(KEY_LAST_WEATHER_ALERT, value).apply()

    fun logout() {
        prefs.edit()
            .remove(KEY_USER)
            .putBoolean(KEY_IS_LOGGED_IN, false)
            .apply()
    }
}
