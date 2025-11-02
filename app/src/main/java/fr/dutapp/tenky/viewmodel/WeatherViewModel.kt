package fr.dutapp.tenky.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.dutapp.tenky.models.Current
import fr.dutapp.tenky.models.DailyWeather
import fr.dutapp.tenky.models.HourlyWeather
import fr.dutapp.tenky.repository.WeatherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class WeatherUiState(
    val isLoading: Boolean = true,
    val cityName: String = "",
    val temperature: String = "",
    val feelsLike: String = "",
    val humidity: String = "",
    val windSpeed: String = "",
    val description: String = "",
    val weatherIcon: String = "",
    val weatherCode: Int = 0,
    val sunrise: String = "",
    val sunset: String = "",
    val hourlyWeather: List<HourlyWeather> = emptyList(),
    val dailyWeather: List<DailyWeather> = emptyList(),
    val error: String? = null
)

class WeatherViewModel : ViewModel() {

    private val repository = WeatherRepository()

    private val _uiState = MutableStateFlow(WeatherUiState())
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    fun loadWeather(latitude: Double, longitude: Double, units: String, lang: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val weatherResult = repository.getWeather(latitude, longitude, units, lang)
            val cityNameResult = repository.getCityName(latitude, longitude, units, lang)

            weatherResult.onSuccess { weatherResponse ->
                val current = weatherResponse.current
                val daily = weatherResponse.daily
                val hourly = weatherResponse.hourly

                if (current == null || daily == null) {
                    Log.e("WeatherViewModel", "Missing required weather data in response")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Invalid weather data received"
                    )
                    return@onSuccess
                }

                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                val dayFormat = SimpleDateFormat("E", Locale.getDefault())

                val dailyWeatherList = daily.drop(1).take(7).map { day ->
                    val dayDate = Date(day.dt * 1000)
                    DailyWeather(
                        dayName = dayFormat.format(dayDate),
                        temperature = day.temp.day.toInt(),
                        minMaxTemp = "${day.temp.max.toInt()}°/${day.temp.min.toInt()}°",
                        weatherIcon = "ic_${day.weather.firstOrNull()?.icon ?: ""}"
                    )
                }

                val hourlyWeatherList = hourly?.take(24) ?: emptyList()

                val windSpeedUnit = if (units == "metric") " km/h" else " mph"

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    temperature = "${current.temp.toInt()}°",
                    feelsLike = "${current.feelsLike.toInt()}°",
                    humidity = "${current.humidity} %",
                    windSpeed = "${current.windSpeed.toInt()}$windSpeedUnit",
                    description = current.weather.firstOrNull()?.description ?: "",
                    weatherIcon = "ic_${current.weather.firstOrNull()?.icon ?: ""}",
                    weatherCode = current.weather.firstOrNull()?.id ?: 0,
                    sunrise = timeFormat.format(Date((current.sunrise ?: 0) * 1000)),
                    sunset = timeFormat.format(Date((current.sunset ?: 0) * 1000)),
                    dailyWeather = dailyWeatherList,
                    hourlyWeather = hourlyWeatherList
                )
            }.onFailure { error ->
                Log.e("WeatherViewModel", "Error loading weather", error)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = error.message
                )
            }

            cityNameResult.onSuccess { cityName ->
                _uiState.value = _uiState.value.copy(cityName = cityName)
            }
        }
    }
}
