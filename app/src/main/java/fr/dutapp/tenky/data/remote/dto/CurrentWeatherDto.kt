package fr.dutapp.tenky.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Response of `GET /data/2.5/weather`. */
@Serializable
data class CurrentWeatherDto(
    val coord: CoordDto,
    val weather: List<WeatherDescriptionDto> = emptyList(),
    val main: MainDto,
    val wind: WindDto = WindDto(),
    /** Metres, capped at 10000 by the API. */
    val visibility: Int? = null,
    val dt: Long,
    val sys: SysDto = SysDto(),
    /** Shift in seconds from UTC for the requested location. */
    val timezone: Int = 0,
    val name: String = "",
)

@Serializable
data class CoordDto(
    val lat: Double,
    val lon: Double,
)

@Serializable
data class WeatherDescriptionDto(
    val id: Int,
    val description: String = "",
    val icon: String = "",
)

@Serializable
data class MainDto(
    val temp: Double,
    @SerialName("feels_like") val feelsLike: Double = temp,
    @SerialName("temp_min") val tempMin: Double = temp,
    @SerialName("temp_max") val tempMax: Double = temp,
    val humidity: Int = 0,
    /** hPa at sea level. */
    val pressure: Int = 0,
)

@Serializable
data class WindDto(
    val speed: Double = 0.0,
    /** Meteorological degrees: the direction the wind blows *from*. */
    val deg: Int? = null,
)

@Serializable
data class SysDto(
    val country: String? = null,
    val sunrise: Long? = null,
    val sunset: Long? = null,
)
