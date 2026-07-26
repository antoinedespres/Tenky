package fr.dutapp.tenky.ui

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import fr.dutapp.tenky.R
import fr.dutapp.tenky.domain.model.TemperatureUnit
import fr.dutapp.tenky.domain.model.WeatherError
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
