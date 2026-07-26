package fr.dutapp.tenky.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

class AppLanguageTest {

    @Test
    fun `korean maps to the code OpenWeather actually expects`() {
        // OpenWeather uses "kr" for Korean rather than the ISO 639-1 "ko".
        // Sending "ko" is not an error — the API silently answers in English,
        // which is exactly the kind of bug that survives unnoticed.
        assertEquals("kr", Locale.KOREAN.toOpenWeatherLanguage())
        assertEquals("kr", Locale.KOREA.toOpenWeatherLanguage())
    }

    @Test
    fun `languages whose code matches ISO are passed through`() {
        assertEquals("en", Locale.ENGLISH.toOpenWeatherLanguage())
        assertEquals("fr", Locale.FRENCH.toOpenWeatherLanguage())
        assertEquals("fr", Locale.FRANCE.toOpenWeatherLanguage())
    }

    @Test
    fun `the api code ignores region and case`() {
        assertEquals("fr", Locale.CANADA_FRENCH.toOpenWeatherLanguage())
        assertEquals("en", Locale.forLanguageTag("EN-GB").toOpenWeatherLanguage())
    }

    @Test
    fun `a stored tag resolves back to the matching language`() {
        assertEquals(AppLanguage.ENGLISH, AppLanguage.fromLanguageTag("en"))
        assertEquals(AppLanguage.FRENCH, AppLanguage.fromLanguageTag("fr"))
        assertEquals(AppLanguage.KOREAN, AppLanguage.fromLanguageTag("ko"))
    }

    @Test
    fun `a stored tag carrying a region still resolves`() {
        // AppCompat may hand back "fr-FR" or "ko-KR" rather than a bare code.
        assertEquals(AppLanguage.FRENCH, AppLanguage.fromLanguageTag("fr-FR"))
        assertEquals(AppLanguage.KOREAN, AppLanguage.fromLanguageTag("ko-KR"))
        assertEquals(AppLanguage.ENGLISH, AppLanguage.fromLanguageTag("en-US"))
    }

    @Test
    fun `an absent or unsupported tag falls back to following the system`() {
        assertEquals(AppLanguage.SYSTEM, AppLanguage.fromLanguageTag(null))
        assertEquals(AppLanguage.SYSTEM, AppLanguage.fromLanguageTag(""))
        assertEquals(AppLanguage.SYSTEM, AppLanguage.fromLanguageTag("   "))
        assertEquals(AppLanguage.SYSTEM, AppLanguage.fromLanguageTag("de"))
    }

    @Test
    fun `system carries no tag so it cannot be applied as a locale`() {
        assertEquals("", AppLanguage.SYSTEM.languageTag)
    }
}
