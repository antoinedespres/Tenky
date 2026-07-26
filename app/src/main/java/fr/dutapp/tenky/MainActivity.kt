package fr.dutapp.tenky

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import fr.dutapp.tenky.ui.TenkyNavHost
import fr.dutapp.tenky.ui.settings.AppLanguageController
import fr.dutapp.tenky.ui.theme.TenkyTheme

/**
 * The app's only Activity.
 *
 * The former splash, cities and settings Activities are now Compose
 * destinations; the splash itself is handled by the platform API, which removes
 * the artificial 800 ms delay the old SplashScreenActivity imposed on startup.
 *
 * It extends [AppCompatActivity] rather than `ComponentActivity` so that
 * `AppCompatDelegate.setApplicationLocales` can apply the per-app language on
 * devices below Android 13.
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Before setContent, so the first composition already sees the right
        // language and the weather is fetched once rather than twice.
        AppLanguageController.sync()

        setContent {
            TenkyTheme {
                TenkyNavHost()
            }
        }
    }
}
