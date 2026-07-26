package fr.dutapp.tenky.data

import fr.dutapp.tenky.data.remote.dto.CurrentWeatherDto
import fr.dutapp.tenky.data.remote.dto.ForecastDto
import fr.dutapp.tenky.data.remote.dto.ForecastEntryDto
import fr.dutapp.tenky.data.remote.dto.GeocodingResultDto
import fr.dutapp.tenky.domain.model.Coordinates
import fr.dutapp.tenky.domain.model.CurrentWeather
import fr.dutapp.tenky.domain.model.DailyForecast
import fr.dutapp.tenky.domain.model.HourlyForecast
import fr.dutapp.tenky.domain.model.SavedCity
import fr.dutapp.tenky.domain.model.TemperaturePoint
import fr.dutapp.tenky.domain.model.WindDirection
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import kotlin.math.abs

/** Fallback icon when a response carries no `weather` entry. */
private const val FALLBACK_ICON = "01d"

/** 8 steps of three hours covers the next 24 hours. */
internal const val HOURLY_STEP_COUNT = 8

/**
 * Hour used to pick the icon and "day" temperature that represent a whole day.
 * Early afternoon avoids selecting a night icon for a daytime summary.
 */
private val REPRESENTATIVE_HOUR = LocalTime.of(13, 0)

/** Converts an epoch second into the local time at the place being displayed. */
private fun Long.atZone(offset: ZoneOffset) = Instant.ofEpochSecond(this).atOffset(offset)

internal fun zoneOffsetOf(timezoneSeconds: Int): ZoneOffset =
    ZoneOffset.ofTotalSeconds(timezoneSeconds)

fun CurrentWeatherDto.toDomain(): CurrentWeather {
    val offset = zoneOffsetOf(timezone)
    val condition = weather.firstOrNull()
    return CurrentWeather(
        placeName = name,
        coordinates = Coordinates(coord.lat, coord.lon),
        temperature = main.temp,
        feelsLike = main.feelsLike,
        humidityPercent = main.humidity,
        windSpeed = wind.speed,
        windDirection = wind.deg?.let(WindDirection::fromDegrees),
        pressureHpa = main.pressure,
        visibilityMetres = visibility,
        description = condition?.description.orEmpty(),
        iconCode = condition?.icon ?: FALLBACK_ICON,
        conditionId = condition?.id ?: 0,
        sunrise = sys.sunrise?.atZone(offset)?.toLocalTime(),
        sunset = sys.sunset?.atZone(offset)?.toLocalTime(),
    )
}

/** Takes the next 24 hours of three-hour steps. */
fun ForecastDto.toHourly(): List<HourlyForecast> {
    val offset = zoneOffsetOf(city.timezone)
    return list.take(HOURLY_STEP_COUNT).map { entry ->
        HourlyForecast(
            time = entry.dt.atZone(offset).toLocalTime(),
            temperature = entry.main.temp,
            iconCode = entry.weather.firstOrNull()?.icon ?: FALLBACK_ICON,
            precipitationProbability = entry.pop.toFloat(),
        )
    }
}

/** Every three-hour step, in the local time of the city, for the trend chart. */
fun ForecastDto.toTrend(): List<TemperaturePoint> {
    val offset = zoneOffsetOf(city.timezone)
    return list.map { entry ->
        TemperaturePoint(
            dateTime = entry.dt.atZone(offset).toLocalDateTime(),
            temperature = entry.main.temp,
        )
    }
}

/**
 * Collapses the three-hour steps into one entry per calendar day, in the local
 * time of the city.
 *
 * The five-day endpoint returns 40 steps, so the first and last day are usually
 * partial — their min/max reflect only the hours actually covered.
 */
fun ForecastDto.toDaily(): List<DailyForecast> {
    val offset = zoneOffsetOf(city.timezone)
    return list
        .groupBy { it.dt.atZone(offset).toLocalDate() }
        .map { (date, entries) -> entries.toDailyForecast(date, offset) }
}

private fun List<ForecastEntryDto>.toDailyForecast(
    date: LocalDate,
    offset: ZoneOffset,
): DailyForecast {
    val representative = minByOrNull { entry ->
        val time = entry.dt.atZone(offset).toLocalTime()
        abs(time.toSecondOfDay() - REPRESENTATIVE_HOUR.toSecondOfDay())
    }
    val condition = representative?.weather?.firstOrNull()
    return DailyForecast(
        date = date,
        minTemperature = minOf { it.main.tempMin },
        maxTemperature = maxOf { it.main.tempMax },
        dayTemperature = representative?.main?.temp ?: 0.0,
        iconCode = condition?.icon ?: FALLBACK_ICON,
        description = condition?.description.orEmpty(),
        // The wettest step decides the day: a 70% chance for one afternoon
        // step matters more to someone planning than the day's average.
        precipitationProbability = maxOf { it.pop }.toFloat(),
    )
}

fun GeocodingResultDto.toSavedCity() = SavedCity(
    name = name,
    latitude = lat,
    longitude = lon,
    country = country,
    state = state,
)
