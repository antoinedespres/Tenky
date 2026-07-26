package fr.dutapp.tenky.ui.settings

import android.content.res.Resources
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
     * Derived from [language] rather than read back from `AppCompatDelegate`.
     * [apply] publishes the new value before asking the delegate to switch, so
     * a collector reacting to that emission would otherwise query the delegate
     * while it still reports the previous locale — and refetch the weather in
     * the language the user just moved away from.
     *
     * For [AppLanguage.SYSTEM] this reads the device configuration rather than
     * `Locale.getDefault()`, which reflects any app-level override still in
     * effect at that moment.
     */
    val currentLocale: Locale
        get() = when (val language = _language.value) {
            AppLanguage.SYSTEM -> Resources.getSystem().configuration.locales[0]
            else -> Locale.forLanguageTag(language.languageTag)
        }

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

    /**
     * Re-reads the locale AppCompat restored from storage.
     *
     * Below Android 13 AppCompat only applies the stored locale when an
     * Activity is attached, which is after `Application.onCreate` — where this
     * object is first touched. Without this the app could start showing Korean
     * while believing it was following the system.
     */
    fun sync() {
        _language.value = readCurrent()
    }

    private fun readCurrent(): AppLanguage = AppLanguage.fromLanguageTag(
        AppCompatDelegate.getApplicationLocales().toLanguageTags().takeIf { it.isNotBlank() },
    )
}
