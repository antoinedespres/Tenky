package fr.dutapp.tenky.domain.model

import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset

/** A geographic point. */
@Serializable
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

/**
 * The eight-point compass sector a wind direction falls in.
 *
 * OpenWeather reports the direction the wind blows *from*, which is the
 * meteorological convention and what the labels below mean.
 */
enum class WindDirection {
    NORTH,
    NORTH_EAST,
    EAST,
    SOUTH_EAST,
    SOUTH,
    SOUTH_WEST,
    WEST,
    NORTH_WEST,
    ;

    companion object {
        private const val SECTOR_DEGREES = 45

        /** Rounds [degrees] to the nearest of the eight sectors. */
        fun fromDegrees(degrees: Int): WindDirection {
            val normalised = ((degrees % 360) + 360) % 360
            val sector = ((normalised + SECTOR_DEGREES / 2) / SECTOR_DEGREES) % entries.size
            return entries[sector]
        }
    }
}

/** Current conditions for a place. */
@Serializable
data class CurrentWeather(
    val placeName: String,
    val coordinates: Coordinates,
    val temperature: Double,
    val feelsLike: Double,
    val humidityPercent: Int,
    val windSpeed: Double,
    val windDirection: WindDirection? = null,
    val pressureHpa: Int = 0,
    /** Metres; the API caps this at 10 km. */
    val visibilityMetres: Int? = null,
    val description: String,
    val iconCode: String,
    val conditionId: Int,
    @Serializable(with = LocalTimeSerializer::class) val sunrise: LocalTime?,
    @Serializable(with = LocalTimeSerializer::class) val sunset: LocalTime?,
)

/** One three-hour step of the forecast. */
@Serializable
data class HourlyForecast(
    @Serializable(with = LocalTimeSerializer::class) val time: LocalTime,
    val temperature: Double,
    val iconCode: String,
    /** Probability of precipitation, 0..1. */
    val precipitationProbability: Float = 0f,
)

/** One day, aggregated from the three-hour steps that fall within it. */
@Serializable
data class DailyForecast(
    @Serializable(with = LocalDateSerializer::class) val date: LocalDate,
    val minTemperature: Double,
    val maxTemperature: Double,
    val dayTemperature: Double,
    val iconCode: String,
    val description: String,
    /** The highest probability across the day's steps, 0..1. */
    val precipitationProbability: Float = 0f,
)

/**
 * One point of the temperature trend.
 *
 * Carries the full date, unlike [HourlyForecast], because the chart marks where
 * one day ends and the next begins.
 */
@Serializable
data class TemperaturePoint(
    @Serializable(with = LocalDateTimeSerializer::class) val dateTime: LocalDateTime,
    val temperature: Double,
)

/** Everything the weather screen renders, for a single place. */
@Serializable
data class WeatherSnapshot(
    val current: CurrentWeather,
    val hourly: List<HourlyForecast>,
    val daily: List<DailyForecast>,
    /** Every three-hour step across the five days, for the trend chart. */
    val trend: List<TemperaturePoint> = emptyList(),
    /** Offset of the place from UTC, used to render its local times. */
    @Serializable(with = ZoneOffsetSerializer::class) val zoneOffset: ZoneOffset,
)
