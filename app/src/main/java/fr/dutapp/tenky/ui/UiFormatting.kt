package fr.dutapp.tenky.ui

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import fr.dutapp.tenky.R
import fr.dutapp.tenky.domain.model.TemperatureUnit
import fr.dutapp.tenky.domain.model.WeatherError
import fr.dutapp.tenky.domain.model.WindDirection
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import kotlin.math.roundToInt

/** Maps a failure to the message shown to the user. */
@StringRes
fun WeatherError.messageRes(): Int = when (this) {
    WeatherError.MissingApiKey -> R.string.error_missing_api_key
    WeatherError.InvalidApiKey -> R.string.error_invalid_api_key
    WeatherError.Network -> R.string.error_network
    WeatherError.RateLimited -> R.string.error_rate_limited
    WeatherError.NotFound -> R.string.error_not_found
    WeatherError.Unknown -> R.string.error_unknown
}

/** "21°" — temperatures are always shown as whole degrees. */
@Composable
fun formatTemperature(value: Double): String =
    stringResource(R.string.temperature_degrees, value.roundToInt())

/** "24° / 17°" for a daily high and low. */
@Composable
fun formatTemperatureRange(max: Double, min: Double): String =
    stringResource(R.string.temperature_range, max.roundToInt(), min.roundToInt())

/**
 * Wind speed with the unit that matches the request.
 *
 * OpenWeather returns metres per second for `metric`, not km/h — the previous
 * version of the app labelled this value "km/h", which was wrong.
 */
@Composable
fun formatWindSpeed(value: Double, unit: TemperatureUnit): String = stringResource(
    when (unit) {
        TemperatureUnit.METRIC -> R.string.wind_speed_metric
        TemperatureUnit.IMPERIAL -> R.string.wind_speed_imperial
    },
    value.roundToInt(),
)

/** Wind speed followed by the compass sector it blows from, when known. */
@Composable
fun formatWind(
    speed: Double,
    unit: TemperatureUnit,
    direction: WindDirection?,
): String {
    val value = formatWindSpeed(speed, unit)
    return if (direction == null) {
        value
    } else {
        stringResource(R.string.wind_with_direction, value, stringResource(direction.labelRes()))
    }
}

@StringRes
private fun WindDirection.labelRes(): Int = when (this) {
    WindDirection.NORTH -> R.string.wind_direction_n
    WindDirection.NORTH_EAST -> R.string.wind_direction_ne
    WindDirection.EAST -> R.string.wind_direction_e
    WindDirection.SOUTH_EAST -> R.string.wind_direction_se
    WindDirection.SOUTH -> R.string.wind_direction_s
    WindDirection.SOUTH_WEST -> R.string.wind_direction_sw
    WindDirection.WEST -> R.string.wind_direction_w
    WindDirection.NORTH_WEST -> R.string.wind_direction_nw
}

@Composable
fun formatPressure(hectopascals: Int): String =
    stringResource(R.string.pressure_value, hectopascals)

/**
 * Visibility in kilometres once past 1 km, metres below that.
 *
 * The API caps this at 10 km, so "10 km" means "10 or more".
 */
@Composable
fun formatVisibility(metres: Int): String = if (metres >= METRES_PER_KILOMETRE) {
    val kilometres = metres.toDouble() / METRES_PER_KILOMETRE
    stringResource(
        R.string.visibility_kilometres,
        String.format(Locale.getDefault(), if (kilometres % 1.0 == 0.0) "%.0f" else "%.1f", kilometres),
    )
} else {
    stringResource(R.string.visibility_metres, metres)
}

/** Probability given as 0..1, shown as a whole percentage. */
@Composable
fun formatProbability(probability: Float): String =
    stringResource(R.string.percent_value, (probability * 100).roundToInt())

private const val METRES_PER_KILOMETRE = 1000

/**
 * "Updated 5 minutes ago", coarsened to the largest sensible unit.
 *
 * Anything under a minute reads as "just now" rather than counting seconds,
 * which would only draw attention to a number nobody acts on.
 */
@Composable
fun formatDataAge(fetchedAtMillis: Long, nowMillis: Long): String {
    val elapsed = (nowMillis - fetchedAtMillis).coerceAtLeast(0)
    val minutes = elapsed / MILLIS_PER_MINUTE
    val hours = minutes / MINUTES_PER_HOUR
    val days = hours / HOURS_PER_DAY

    return when {
        minutes < 1 -> stringResource(R.string.updated_just_now)
        hours < 1 -> pluralStringResource(
            R.plurals.updated_minutes_ago,
            minutes.toInt(),
            minutes.toInt(),
        )

        days < 1 -> pluralStringResource(R.plurals.updated_hours_ago, hours.toInt(), hours.toInt())
        else -> pluralStringResource(R.plurals.updated_days_ago, days.toInt(), days.toInt())
    }
}

private const val MILLIS_PER_MINUTE = 60_000L
private const val MINUTES_PER_HOUR = 60L
private const val HOURS_PER_DAY = 24L

private val timeFormatter: DateTimeFormatter =
    DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)

/** Formats a time using the device's 12h/24h convention. */
fun formatTime(time: LocalTime, locale: Locale = Locale.getDefault()): String =
    time.format(timeFormatter.withLocale(locale))

/** "Today", "Tomorrow", then the weekday name. */
fun formatDayLabel(
    date: LocalDate,
    today: LocalDate,
    todayLabel: String,
    tomorrowLabel: String,
    locale: Locale = Locale.getDefault(),
): String = when (date) {
    today -> todayLabel
    today.plusDays(1) -> tomorrowLabel
    else -> date.format(DateTimeFormatter.ofPattern("EEEE", locale))
        .replaceFirstChar { it.titlecase(locale) }
}

/** OpenWeather returns lowercase descriptions such as "nuageux". */
fun String.sentenceCase(locale: Locale = Locale.getDefault()): String =
    replaceFirstChar { it.titlecase(locale) }
