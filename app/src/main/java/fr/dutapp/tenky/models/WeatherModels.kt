package fr.dutapp.tenky.models

import com.google.gson.annotations.SerializedName

data class WeatherResponse(
    val current: Current,
    val hourly: List<HourlyWeather>,
    val daily: List<DailyWeatherResponse>,
    val timezone: String
)

data class Current(
    val temp: Double,
    @SerializedName("feels_like") val feelsLike: Double,
    val humidity: Double,
    @SerializedName("wind_speed") val windSpeed: Double,
    val sunrise: Long,
    val sunset: Long,
    val weather: List<Weather>
)

data class HourlyWeather(
    val dt: Long,
    val temp: Double,
    val weather: List<Weather>
)

data class DailyWeatherResponse(
    val dt: Long,
    val temp: Temp,
    val weather: List<Weather>
)

data class Temp(
    val day: Double,
    val min: Double,
    val max: Double
)

data class Weather(
    val id: Int,
    val icon: String,
    val description: String
)

data class CityWeatherResponse(
    val name: String,
    val coord: Coord,
    val main: Main,
    val weather: List<Weather>
)

data class Coord(
    val lat: Double,
    val lon: Double
)

data class Main(
    val temp: Double
)
