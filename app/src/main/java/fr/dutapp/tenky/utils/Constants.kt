package fr.dutapp.tenky.utils

import android.content.Context
import android.content.SharedPreferences
import fr.dutapp.tenky.R

object Constants {

    // API Configuration
    const val API_KEY = "2d16a26b9de301ad0c8d42865664d50e"
    const val BASE_URL_ONECALL = "https://api.openweathermap.org/data/3.0/onecall"
    const val BASE_URL_WEATHER = "https://api.openweathermap.org/data/2.5/weather"
    const val BASE_URL_ONECALL_25 = "https://api.openweathermap.org/data/2.5/onecall"

    // Location updates
    const val MIN_TIME_BW_UPDATES = 300000L // 5 minutes
    const val MIN_DISTANCE_CHANGE_FOR_UPDATES = 5000f // 5 km

    // Request codes
    const val LOCATION_PERMISSION_REQUEST_CODE = 1
    const val ALL_CITIES_ACTIVITY_REQUEST_CODE = 2

    // Units
    const val UNIT_METRIC = "metric"
    const val UNIT_IMPERIAL = "imperial"

    // Splash screen delay
    const val SPLASH_SCREEN_DELAY = 800

    // Weather icon mapping
    private val iconMap: Map<String, Int> by lazy {
        mapOf(
            "ic_01d" to R.drawable.ic_01d,
            "ic_01n" to R.drawable.ic_01n,
            "ic_02d" to R.drawable.ic_02d,
            "ic_02n" to R.drawable.ic_02n,
            "ic_03d" to R.drawable.ic_03d,
            "ic_03n" to R.drawable.ic_03n,
            "ic_04d" to R.drawable.ic_04d,
            "ic_04n" to R.drawable.ic_04n,
            "ic_09d" to R.drawable.ic_09d,
            "ic_09n" to R.drawable.ic_09n,
            "ic_10d" to R.drawable.ic_10d,
            "ic_10n" to R.drawable.ic_10n,
            "ic_11d" to R.drawable.ic_11d,
            "ic_11n" to R.drawable.ic_11n,
            "ic_13d" to R.drawable.ic_13d,
            "ic_13n" to R.drawable.ic_13n,
            "ic_50d" to R.drawable.ic_50d,
            "ic_50n" to R.drawable.ic_50n
        )
    }

    private val imgMap: Map<String, Int> by lazy {
        mapOf(
            "img_200" to R.drawable.img_200,
            "img_300" to R.drawable.img_300,
            "img_500" to R.drawable.img_500,
            "img_600" to R.drawable.img_600,
            "img_700" to R.drawable.img_700,
            "img_800" to R.drawable.img_800,
            "img_80x" to R.drawable.img_80x
        )
    }

    fun getIconMap(): Map<String, Int> = iconMap
    fun getImgMap(): Map<String, Int> = imgMap

    fun getDefaultSharedPreferencesName(context: Context): String {
        return "${context.packageName}_preferences"
    }

    fun getUnits(prefs: SharedPreferences): String {
        return if (prefs.getBoolean("unitChoice", false)) UNIT_IMPERIAL else UNIT_METRIC
    }
}
