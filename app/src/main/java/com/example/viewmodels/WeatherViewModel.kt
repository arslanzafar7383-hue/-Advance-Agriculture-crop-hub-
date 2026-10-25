package com.example.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.models.WeatherResponseData
import com.example.repositories.WeatherRepository
import com.example.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class WeatherUiState(
    val isLoading: Boolean = false,
    val weatherData: WeatherResponseData? = null,
    val selectedLocation: String = "Faisalabad",
    val isLiveApiActive: Boolean = false,
    val apiKeyStatusNotice: String = "",
    val agriculturalAdvisory: String = "",
    val errorMessage: String? = null
)

class WeatherViewModel(
    private val weatherRepository: WeatherRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WeatherUiState())
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    init {
        checkApiKeyStatus()
        fetchWeather(_uiState.value.selectedLocation)
    }

    private fun checkApiKeyStatus() {
        val hasKey = weatherRepository.hasValidExternalApiKey()
        _uiState.value = _uiState.value.copy(
            isLiveApiActive = hasKey,
            apiKeyStatusNotice = if (hasKey) {
                "🟢 Live Weather API Key Active (via Secrets Panel)"
            } else {
                "🟠 Using MySQL Backend Telemetry (Configure WEATHER_API_KEY in Secrets panel for global live weather)"
            }
        )
    }

    fun fetchWeather(location: String = _uiState.value.selectedLocation) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                selectedLocation = location,
                errorMessage = null
            )

            when (val result = weatherRepository.getWeatherData(location)) {
                is Resource.Success -> {
                    val data = result.data
                    val current = data.current
                    val advisory = computeAgronomicAdvisory(
                        temp = current?.temperatureC ?: 28.0,
                        rainChance = current?.rainProbabilityPct ?: 10,
                        windSpeed = current?.windSpeedKph ?: 12.0
                    )

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        weatherData = data,
                        agriculturalAdvisory = advisory,
                        isLiveApiActive = weatherRepository.hasValidExternalApiKey()
                    )
                }

                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }

                Resource.Loading -> {
                    _uiState.value = _uiState.value.copy(isLoading = true)
                }
            }
        }
    }

    fun setLocation(location: String) {
        if (location.isNotBlank() && location != _uiState.value.selectedLocation) {
            fetchWeather(location)
        }
    }

    fun refresh() {
        checkApiKeyStatus()
        fetchWeather(_uiState.value.selectedLocation)
    }

    private fun computeAgronomicAdvisory(temp: Double, rainChance: Int, windSpeed: Double): String {
        return when {
            rainChance >= 50 -> {
                "🌧 Rain Probability is High ($rainChance%). Delay chemical pesticide and herbicide foliar spraying to prevent rain wash-off. Ensure field drainage channels are open."
            }
            windSpeed > 25.0 -> {
                "💨 Strong Winds Detected (${windSpeed.toInt()} km/h). High drift hazard: suspend knapsack/tractor spray operations immediately. Monitor tall maize and sugarcane for lodging."
            }
            temp > 38.0 -> {
                "🔥 Heat Stress Warning ($temp°C). Irrigate during late evening or early morning to minimize evapotranspiration losses and protect delicate cotton/vegetable blossoms."
            }
            temp < 10.0 -> {
                "❄ Low Temperatures ($temp°C). Potential frost alert for young vegetable nurseries and potato crops. Light evening irrigation recommended to raise soil thermal mass."
            }
            else -> {
                "🌾 Optimal Farming Conditions ($temp°C, Rain: $rainChance%, Wind: ${windSpeed.toInt()} km/h). Excellent window for tractor tillage, canal irrigation scheduling, and fertilizer top-dressing."
            }
        }
    }
}
