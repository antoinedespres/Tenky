package fr.dutapp.tenky.ui.settings

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import fr.dutapp.tenky.domain.model.AppLanguage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

/**
 * Reads and applies the per-app language.
 *
 * `AppCompatDelegate` is the source of truth for the value itself. On Android
 * 13 and above it delegates to the platform, so the choice also shows up under
 * Settings → Apps → Tenky → Language; below that, AppCompat persists it because
 * the manifest declares `autoStoreLocales`. Keeping a second copy in DataStore
 * would only create something to drift out of sync.
 *
 * [language] exists on top of that because `AppCompatDelegate` is a plain
 * getter with no change notification, and screens showing API-provided text
 * need to refetch it when the language changes.
 */
object AppLanguageController {

    private val _language = MutableStateFlow(readCurrent())

    /** Emits whenever the language is changed through [apply]. */
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    val current: AppLanguage get() = _language.value

    /**
     * The locale used for API requests and date formatting.
     *
     * Falls back to the device default while the app follows the system.
     */
    val currentLocale: Locale
        get() = AppCompatDelegate.getApplicationLocales()[0] ?: Locale.getDefault()

    /** Applies [language]; AppCompat then recreates the running activities. */
    fun apply(language: AppLanguage) {
        if (language == _language.value) return
        // Updated before the delegate call, so collectors observe the change
        // even though that call tears the Activity down and rebuilds it.
        _language.value = language
        AppCompatDelegate.setApplicationLocales(
            if (language == AppLanguage.SYSTEM) {
                LocaleListCompat.getEmptyLocaleList()
            } else {
                LocaleListCompat.forLanguageTags(language.languageTag)
            },
        )
    }

    private fun readCurrent(): AppLanguage = AppLanguage.fromLanguageTag(
        AppCompatDelegate.getApplicationLocales().toLanguageTags().takeIf { it.isNotBlank() },
    )
}
