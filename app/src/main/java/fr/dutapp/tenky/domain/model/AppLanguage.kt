package fr.dutapp.tenky.domain.model

import java.util.Locale

/**
 * The languages the app ships translations for.
 *
 * [SYSTEM] follows the device setting; the others override it for this app
 * only, which is what Android calls a per-app language.
 */
enum class AppLanguage(
    /** BCP-47 tag handed to `AppCompatDelegate`, empty for [SYSTEM]. */
    val languageTag: String,
) {
    SYSTEM(""),
    ENGLISH("en"),
    FRENCH("fr"),
    KOREAN("ko"),
    ;

    companion object {
        /** Resolves the tag stored by AppCompat back to an entry. */
        fun fromLanguageTag(tag: String?): AppLanguage {
            if (tag.isNullOrBlank()) return SYSTEM
            // Compare on language only: the stored tag may carry a region,
            // e.g. "fr-FR" or "ko-KR".
            val language = Locale.forLanguageTag(tag).language
            return entries.firstOrNull { it != SYSTEM && it.languageTag == language } ?: SYSTEM
        }
    }
}

/**
 * The value OpenWeather expects in its `lang` query parameter.
 *
 * Mostly the ISO 639-1 code, with exceptions: Korean is `kr`, not `ko`.
 * Passing an unknown code makes the API silently answer in English, so the
 * mapping matters — see https://openweathermap.org/current#multi
 */
fun Locale.toOpenWeatherLanguage(): String = when (language) {
    "ko" -> "kr"
    else -> language.lowercase(Locale.ROOT)
}
