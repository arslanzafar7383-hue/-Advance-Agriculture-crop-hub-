package com.example.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Current Weather API response from OpenWeatherMap (data/2.5/weather)
 */
@JsonClass(generateAdapter = true)
data class OpenWeatherResponse(
    @Json(name = "id") val id: Long? = null,
    @Json(name = "name") val name: String? = null,
    @Json(name = "coord") val coord: OpenWeatherCoord? = null,
    @Json(name = "weather") val weather: List<OpenWeatherDescription>? = null,
    @Json(name = "base") val base: String? = null,
    @Json(name = "main") val main: OpenWeatherMain? = null,
    @Json(name = "visibility") val visibility: Int? = null,
    @Json(name = "wind") val wind: OpenWeatherWind? = null,
    @Json(name = "clouds") val clouds: OpenWeatherClouds? = null,
    @Json(name = "rain") val rain: OpenWeatherPrecipitation? = null,
    @Json(name = "snow") val snow: OpenWeatherPrecipitation? = null,
    @Json(name = "dt") val dt: Long? = null,
    @Json(name = "sys") val sys: OpenWeatherSys? = null,
    @Json(name = "timezone") val timezone: Int? = null,
    @Json(name = "cod") val cod: Int? = null
)

/**
 * 5-Day / 3-Hour Forecast API response from OpenWeatherMap (data/2.5/forecast)
 */
@JsonClass(generateAdapter = true)
data class OpenWeatherForecastResponse(
    @Json(name = "cod") val cod: String? = null,
    @Json(name = "message") val message: Int? = null,
    @Json(name = "cnt") val cnt: Int? = null,
    @Json(name = "list") val list: List<OpenWeatherForecastItem>? = null,
    @Json(name = "city") val city: OpenWeatherCity? = null
)

@JsonClass(generateAdapter = true)
data class OpenWeatherForecastItem(
    @Json(name = "dt") val dt: Long = 0L,
    @Json(name = "main") val main: OpenWeatherMain? = null,
    @Json(name = "weather") val weather: List<OpenWeatherDescription>? = null,
    @Json(name = "clouds") val clouds: OpenWeatherClouds? = null,
    @Json(name = "wind") val wind: OpenWeatherWind? = null,
    @Json(name = "visibility") val visibility: Int? = null,
    @Json(name = "pop") val pop: Double? = null, // Probability of precipitation (0.0 to 1.0)
    @Json(name = "rain") val rain: OpenWeatherPrecipitation? = null,
    @Json(name = "dt_txt") val dtTxt: String? = null
)

@JsonClass(generateAdapter = true)
data class OpenWeatherCity(
    @Json(name = "id") val id: Long? = null,
    @Json(name = "name") val name: String? = null,
    @Json(name = "coord") val coord: OpenWeatherCoord? = null,
    @Json(name = "country") val country: String? = null,
    @Json(name = "population") val population: Long? = null,
    @Json(name = "timezone") val timezone: Int? = null,
    @Json(name = "sunrise") val sunrise: Long? = null,
    @Json(name = "sunset") val sunset: Long? = null
)

@JsonClass(generateAdapter = true)
data class OpenWeatherMain(
    @Json(name = "temp") val temp: Double = 25.0,
    @Json(name = "feels_like") val feelsLike: Double = 25.0,
    @Json(name = "temp_min") val tempMin: Double = 20.0,
    @Json(name = "temp_max") val tempMax: Double = 30.0,
    @Json(name = "pressure") val pressure: Int = 1013,
    @Json(name = "humidity") val humidity: Int = 50,
    @Json(name = "sea_level") val seaLevel: Int? = null,
    @Json(name = "grnd_level") val grndLevel: Int? = null
)

@JsonClass(generateAdapter = true)
data class OpenWeatherDescription(
    @Json(name = "id") val id: Int = 800,
    @Json(name = "main") val main: String = "Clear",
    @Json(name = "description") val description: String = "clear sky",
    @Json(name = "icon") val icon: String = "01d"
)

@JsonClass(generateAdapter = true)
data class OpenWeatherWind(
    @Json(name = "speed") val speed: Double = 3.5, // m/s
    @Json(name = "deg") val deg: Int = 0,
    @Json(name = "gust") val gust: Double? = null
)

@JsonClass(generateAdapter = true)
data class OpenWeatherClouds(
    @Json(name = "all") val all: Int = 0
)

@JsonClass(generateAdapter = true)
data class OpenWeatherCoord(
    @Json(name = "lat") val lat: Double = 0.0,
    @Json(name = "lon") val lon: Double = 0.0
)

@JsonClass(generateAdapter = true)
data class OpenWeatherPrecipitation(
    @Json(name = "1h") val oneHour: Double? = null,
    @Json(name = "3h") val threeHours: Double? = null
)

@JsonClass(generateAdapter = true)
data class OpenWeatherSys(
    @Json(name = "type") val type: Int? = null,
    @Json(name = "id") val id: Long? = null,
    @Json(name = "country") val country: String? = null,
    @Json(name = "sunrise") val sunrise: Long? = null,
    @Json(name = "sunset") val sunset: Long? = null
)

/**
 * Air Pollution API response from OpenWeatherMap (data/2.5/air_pollution)
 */
@JsonClass(generateAdapter = true)
data class OpenWeatherAirPollutionResponse(
    @Json(name = "coord") val coord: OpenWeatherCoord? = null,
    @Json(name = "list") val list: List<OpenWeatherAirPollutionItem>? = null
)

@JsonClass(generateAdapter = true)
data class OpenWeatherAirPollutionItem(
    @Json(name = "dt") val dt: Long? = null,
    @Json(name = "main") val main: OpenWeatherAqi? = null,
    @Json(name = "components") val components: Map<String, Double>? = null
)

@JsonClass(generateAdapter = true)
data class OpenWeatherAqi(
    @Json(name = "aqi") val aqi: Int = 1 // 1 = Good, 2 = Fair, 3 = Moderate, 4 = Poor, 5 = Very Poor
)
