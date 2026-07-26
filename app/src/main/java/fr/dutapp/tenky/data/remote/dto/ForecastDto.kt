package fr.dutapp.tenky.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * Response of `GET /data/2.5/forecast`: 40 entries at three-hour steps,
 * covering five days.
 */
@Serializable
data class ForecastDto(
    val list: List<ForecastEntryDto> = emptyList(),
    val city: ForecastCityDto,
)

@Serializable
data class ForecastEntryDto(
    val dt: Long,
    val main: MainDto,
    val weather: List<WeatherDescriptionDto> = emptyList(),
    val wind: WindDto = WindDto(),
    /** Probability of precipitation, 0..1. */
    val pop: Double = 0.0,
    val visibility: Int? = null,
)

@Serializable
data class ForecastCityDto(
    val name: String = "",
    val coord: CoordDto,
    val country: String? = null,
    /** Shift in seconds from UTC for this city. */
    val timezone: Int = 0,
    val sunrise: Long? = null,
    val sunset: Long? = null,
)
