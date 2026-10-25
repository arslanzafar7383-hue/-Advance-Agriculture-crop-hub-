package com.example.api

import com.example.BuildConfig
import com.example.models.OpenWeatherAirPollutionResponse
import com.example.models.OpenWeatherForecastResponse
import com.example.models.OpenWeatherResponse
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface WeatherApiService {

    /**
     * Fetch real-time weather conditions from OpenWeatherMap API by city name.
     * Uses metric units (°C, m/s).
     */
    @GET("data/2.5/weather")
    suspend fun getCurrentWeatherByCity(
        @Query("q") cityName: String,
        @Query("appid") apiKey: String = BuildConfig.WEATHER_API_KEY,
        @Query("units") units: String = "metric"
    ): Response<OpenWeatherResponse>

    /**
     * Fetch real-time weather conditions by GPS Coordinates (latitude, longitude).
     */
    @GET("data/2.5/weather")
    suspend fun getCurrentWeatherByCoordinates(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("appid") apiKey: String = BuildConfig.WEATHER_API_KEY,
        @Query("units") units: String = "metric"
    ): Response<OpenWeatherResponse>

    /**
     * Fetch 5-day / 3-hour meteorological forecast by city name.
     */
    @GET("data/2.5/forecast")
    suspend fun getForecastByCity(
        @Query("q") cityName: String,
        @Query("appid") apiKey: String = BuildConfig.WEATHER_API_KEY,
        @Query("units") units: String = "metric"
    ): Response<OpenWeatherForecastResponse>

    /**
     * Fetch 5-day / 3-hour meteorological forecast by GPS Coordinates.
     */
    @GET("data/2.5/forecast")
    suspend fun getForecastByCoordinates(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("appid") apiKey: String = BuildConfig.WEATHER_API_KEY,
        @Query("units") units: String = "metric"
    ): Response<OpenWeatherForecastResponse>

    /**
     * Fetch agro-environmental air quality and pollution levels.
     */
    @GET("data/2.5/air_pollution")
    suspend fun getAirPollution(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("appid") apiKey: String = BuildConfig.WEATHER_API_KEY
    ): Response<OpenWeatherAirPollutionResponse>

    companion object {
        const val BASE_URL = "https://api.openweathermap.org/"

        /**
         * Factory method to construct the Retrofit service instance.
         * Configures OkHttp with timeouts, logging, and an API key injection interceptor
         * that injects BuildConfig.WEATHER_API_KEY if not explicitly supplied.
         */
        fun create(apiKey: String = BuildConfig.WEATHER_API_KEY): WeatherApiService {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            // Interceptor to automatically attach appid if not already present in the request query
            val apiKeyInterceptor = Interceptor { chain ->
                val originalRequest = chain.request()
                val originalUrl = originalRequest.url

                val currentAppId = originalUrl.queryParameter("appid")
                val finalUrl = if (currentAppId.isNullOrBlank() && apiKey.isNotBlank() && !apiKey.contains("PLACEHOLDER", ignoreCase = true)) {
                    originalUrl.newBuilder()
                        .addQueryParameter("appid", apiKey)
                        .build()
                } else {
                    originalUrl
                }

                val requestBuilder = originalRequest.newBuilder().url(finalUrl)
                chain.proceed(requestBuilder.build())
            }

            val client = OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .addInterceptor(apiKeyInterceptor)
                .addInterceptor(loggingInterceptor)
                .build()

            val moshi = Moshi.Builder()
                .addLast(KotlinJsonAdapterFactory())
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(WeatherApiService::class.java)
        }
    }
}
