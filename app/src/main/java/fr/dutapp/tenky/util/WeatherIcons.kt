package fr.dutapp.tenky.util

import androidx.annotation.DrawableRes
import fr.dutapp.tenky.R

/**
 * Maps OpenWeather icon codes and condition ids to bundled drawables.
 *
 * Using a `when` rather than the previous `HashMap` of every code means an
 * unknown value from the API falls back to a sensible icon instead of throwing
 * a `NullPointerException` while binding a row.
 */
object WeatherIcons {

    /** Icon for a code such as `04d` or `01n`. */
    @DrawableRes
    fun forCode(iconCode: String): Int = when (iconCode) {
        "01d" -> R.drawable.ic_01d
        "01n" -> R.drawable.ic_01n
        "02d" -> R.drawable.ic_02d
        "02n" -> R.drawable.ic_02n
        "03d" -> R.drawable.ic_03d
        "03n" -> R.drawable.ic_03n
        "04d" -> R.drawable.ic_04d
        "04n" -> R.drawable.ic_04n
        "09d" -> R.drawable.ic_09d
        "09n" -> R.drawable.ic_09n
        "10d" -> R.drawable.ic_10d
        "10n" -> R.drawable.ic_10n
        "11d" -> R.drawable.ic_11d
        "11n" -> R.drawable.ic_11n
        "13d" -> R.drawable.ic_13d
        "13n" -> R.drawable.ic_13n
        "50d" -> R.drawable.ic_50d
        "50n" -> R.drawable.ic_50n
        else -> R.drawable.ic_01d
    }

    /**
     * Backdrop for a condition id.
     *
     * See https://openweathermap.org/weather-conditions — 2xx thunderstorm,
     * 3xx drizzle, 5xx rain, 6xx snow, 7xx atmosphere, 800 clear, 80x clouds.
     */
    @DrawableRes
    fun backgroundFor(conditionId: Int): Int = when {
        conditionId == CLEAR_SKY -> R.drawable.img_800
        conditionId > CLEAR_SKY -> R.drawable.img_80x
        else -> when (conditionId / 100) {
            2 -> R.drawable.img_200
            3 -> R.drawable.img_300
            5 -> R.drawable.img_500
            6 -> R.drawable.img_600
            7 -> R.drawable.img_700
            else -> R.drawable.img_800
        }
    }

    private const val CLEAR_SKY = 800
}
