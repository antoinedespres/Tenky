package fr.dutapp.tenky.models

import com.google.gson.annotations.SerializedName

data class WeatherResponse(
    val lat: Double,
    val lon: Double,
    val timezone: String,
    @SerializedName("timezone_offset") val timezoneOffset: Int,
    val current: Current?,
    val minutely: List<MinutelyWeather>?,
    val hourly: List<HourlyWeather>?,
    val daily: List<DailyWeatherResponse>?,
    val alerts: List<WeatherAlert>?
)

data class Current(
    val dt: Long,
    val sunrise: Long?,
    val sunset: Long?,
    val temp: Double,
    @SerializedName("feels_like") val feelsLike: Double,
    val pressure: Int,
    val humidity: Int,
    @SerializedName("dew_point") val dewPoint: Double,
    val uvi: Double,
    val clouds: Int,
    val visibility: Int,
    @SerializedName("wind_speed") val windSpeed: Double,
    @SerializedName("wind_deg") val windDeg: Int?,
    @SerializedName("wind_gust") val windGust: Double?,
    val weather: List<Weather>
)

data class HourlyWeather(
    val dt: Long,
    val temp: Double,
    @SerializedName("feels_like") val feelsLike: Double,
    val pressure: Int,
    val humidity: Int,
    @SerializedName("dew_point") val dewPoint: Double,
    val uvi: Double,
    val clouds: Int,
    val visibility: Int,
    @SerializedName("wind_speed") val windSpeed: Double,
    @SerializedName("wind_deg") val windDeg: Int?,
    @SerializedName("wind_gust") val windGust: Double?,
    val weather: List<Weather>,
    val pop: Double
)

data class DailyWeatherResponse(
    val dt: Long,
    val sunrise: Long?,
    val sunset: Long?,
    val moonrise: Long?,
    val moonset: Long?,
    @SerializedName("moon_phase") val moonPhase: Double?,
    val summary: String?,
    val temp: Temp,
    @SerializedName("feels_like") val feelsLike: FeelsLike?,
    val pressure: Int,
    val humidity: Int,
    @SerializedName("dew_point") val dewPoint: Double,
    @SerializedName("wind_speed") val windSpeed: Double,
    @SerializedName("wind_deg") val windDeg: Int?,
    @SerializedName("wind_gust") val windGust: Double?,
    val weather: List<Weather>,
    val clouds: Int,
    val pop: Double,
    val rain: Double?,
    val uvi: Double
)

data class Temp(
    val day: Double,
    val min: Double,
    val max: Double,
    val night: Double?,
    val eve: Double?,
    val morn: Double?
)

data class FeelsLike(
    val day: Double,
    val night: Double,
    val eve: Double,
    val morn: Double
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

data class MinutelyWeather(
    val dt: Long,
    val precipitation: Double
)

data class WeatherAlert(
    @SerializedName("sender_name") val senderName: String,
    val event: String,
    val start: Long,
    val end: Long,
    val description: String,
    val tags: List<String>
)
