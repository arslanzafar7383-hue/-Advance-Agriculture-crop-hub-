package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Crop Hub", appName)
  }

  @Test
  fun `room cart entity creation and fields match`() {
    val item = com.example.data.local.CartItemEntity(
      productId = 1,
      productName = "Faisalabad-2008 Certified Wheat Seeds (50kg)",
      category = "Seeds",
      price = 4800.0,
      sellerName = "Punjab Seed Corporation",
      imageUrl = null,
      quantity = 2
    )
    assertEquals(1, item.productId)
    assertEquals(2, item.quantity)
    assertEquals(4800.0, item.price, 0.01)
  }

  @Test
  fun `push notification preferences and user interest tracking test`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val prefs = com.example.utils.PreferencesManager(context)

    // Verify default configuration is true
    org.junit.Assert.assertTrue(prefs.notificationsMarketplaceEnabled)
    org.junit.Assert.assertTrue(prefs.notificationsWeatherEnabled)
    org.junit.Assert.assertTrue(prefs.notificationsOrdersEnabled)

    // Verify user can toggle notification settings in profile
    prefs.notificationsMarketplaceEnabled = false
    org.junit.Assert.assertFalse(prefs.notificationsMarketplaceEnabled)
    prefs.notificationsMarketplaceEnabled = true

    // Verify interest tracking from viewed & purchased categories
    prefs.recordCategoryViewed("Hybrid Seeds")
    prefs.recordCategoryPurchased("Bio Fertilizers")
    val interests = prefs.getUserInterestCategories()
    org.junit.Assert.assertTrue(interests.contains("Hybrid Seeds"))
    org.junit.Assert.assertTrue(interests.contains("Bio Fertilizers"))
    org.junit.Assert.assertTrue(prefs.isCategoryOfInterest("Hybrid Seeds"))
  }

  @Test
  fun `notification channels creation test`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    com.example.utils.NotificationHelper.createNotificationChannels(context)

    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
    val marketplaceChannel = notificationManager.getNotificationChannel(com.example.utils.NotificationHelper.CHANNEL_MARKETPLACE)
    val weatherChannel = notificationManager.getNotificationChannel(com.example.utils.NotificationHelper.CHANNEL_WEATHER)
    val ordersChannel = notificationManager.getNotificationChannel(com.example.utils.NotificationHelper.CHANNEL_ORDERS)

    org.junit.Assert.assertNotNull(marketplaceChannel)
    org.junit.Assert.assertNotNull(weatherChannel)
    org.junit.Assert.assertNotNull(ordersChannel)
  }

  @Test
  fun `open weather data models and json deserialization test`() {
    val moshi = com.squareup.moshi.Moshi.Builder()
        .addLast(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
        .build()

    val json = """
      {
        "id": 1174872,
        "name": "Faisalabad",
        "coord": {"lat": 31.4187, "lon": 73.0791},
        "main": {
          "temp": 32.5,
          "feels_like": 34.0,
          "temp_min": 28.0,
          "temp_max": 35.0,
          "pressure": 1012,
          "humidity": 45
        },
        "weather": [
          {"id": 800, "main": "Clear", "description": "clear sky", "icon": "01d"}
        ],
        "wind": {"speed": 4.1, "deg": 120},
        "clouds": {"all": 10}
      }
    """.trimIndent()

    val adapter = moshi.adapter(com.example.models.OpenWeatherResponse::class.java)
    val response = adapter.fromJson(json)

    org.junit.Assert.assertNotNull(response)
    assertEquals("Faisalabad", response?.name)
    assertEquals(32.5, response?.main?.temp ?: 0.0, 0.01)
    assertEquals(45, response?.main?.humidity)
    assertEquals("Clear", response?.weather?.firstOrNull()?.main)
    assertEquals(4.1, response?.wind?.speed ?: 0.0, 0.01)
  }

  @Test
  fun `open weather forecast data models deserialization test`() {
    val moshi = com.squareup.moshi.Moshi.Builder()
        .addLast(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
        .build()

    val json = """
      {
        "cod": "200",
        "cnt": 1,
        "list": [
          {
            "dt": 1729850400,
            "dt_txt": "2026-10-25 12:00:00",
            "pop": 0.45,
            "main": {"temp": 29.0, "temp_min": 22.0, "temp_max": 31.0, "humidity": 60, "pressure": 1010},
            "weather": [{"id": 500, "main": "Rain", "description": "light rain", "icon": "10d"}]
          }
        ]
      }
    """.trimIndent()

    val adapter = moshi.adapter(com.example.models.OpenWeatherForecastResponse::class.java)
    val response = adapter.fromJson(json)

    org.junit.Assert.assertNotNull(response)
    assertEquals(1, response?.list?.size)
    val item = response?.list?.first()
    assertEquals(29.0, item?.main?.temp ?: 0.0, 0.01)
    assertEquals(0.45, item?.pop ?: 0.0, 0.01)
    assertEquals("Rain", item?.weather?.firstOrNull()?.main)
  }

  @Test
  fun `weather api service factory creation test`() {
    val service = com.example.api.WeatherApiService.create("test_api_key_123456789")
    org.junit.Assert.assertNotNull(service)
  }
}

