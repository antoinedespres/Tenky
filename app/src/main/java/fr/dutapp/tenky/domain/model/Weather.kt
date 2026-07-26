package fr.dutapp.tenky.domain.model

import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset

/** A geographic point. */
data class Coordinates(
    val latitude: Double,
    val longitude: Double,
)

/**
 * Temperature/wind unit system.
 *
 * [apiValue] is what OpenWeather expects in the `units` query parameter. Note
 * that `metric` returns wind speed in metres per second, not km/h.
 */
enum class TemperatureUnit(val apiValue: String) {
    METRIC("metric"),
    IMPERIAL("imperial"),
    ;

    companion object {
        fun fromImperialFlag(isImperial: Boolean) = if (isImperial) IMPERIAL else METRIC
    }
}

/** Current conditions for a place. */
data class CurrentWeather(
    val placeName: String,
    val coordinates: Coordinates,
    val temperature: Double,
    val feelsLike: Double,
    val humidityPercent: Int,
    val windSpeed: Double,
    val description: String,
    val iconCode: String,
    val conditionId: Int,
    val sunrise: LocalTime?,
    val sunset: LocalTime?,
)

/** One three-hour step of the forecast. */
data class HourlyForecast(
    val time: LocalTime,
    val temperature: Double,
    val iconCode: String,
)

/** One day, aggregated from the three-hour steps that fall within it. */
data class DailyForecast(
    val date: LocalDate,
    val minTemperature: Double,
    val maxTemperature: Double,
    val dayTemperature: Double,
    val iconCode: String,
    val description: String,
)

/** Everything the weather screen renders, for a single place. */
data class WeatherSnapshot(
    val current: CurrentWeather,
    val hourly: List<HourlyForecast>,
    val daily: List<DailyForecast>,
    /** Offset of the place from UTC, used to render its local times. */
    val zoneOffset: ZoneOffset,
)
