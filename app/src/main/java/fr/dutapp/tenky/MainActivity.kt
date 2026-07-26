package fr.dutapp.tenky

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.splashscreen.SplashScreenViewProvider
import androidx.interpolator.view.animation.FastOutLinearInInterpolator
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
        installSplashScreen().setOnExitAnimationListener(::animateSplashOut)
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

    /**
     * Dissolves the splash into the app instead of cutting to it.
     *
     * The icon lifts and fades slightly ahead of the background, which reads as
     * the splash opening up rather than the whole screen dimming at once.
     * Unlike the animated icon this runs on every API level, because the
     * compat library hands back a real view to animate.
     *
     * [SplashScreenViewProvider.remove] must run whatever happens, or the
     * splash stays on top of the app forever — hence `withEndAction`, which
     * still fires when animations are switched off device-wide.
     */
    private fun animateSplashOut(splashProvider: SplashScreenViewProvider) {
        splashProvider.iconView
            .animate()
            .alpha(0f)
            .scaleX(SPLASH_ICON_EXIT_SCALE)
            .scaleY(SPLASH_ICON_EXIT_SCALE)
            .setDuration(SPLASH_ICON_EXIT_MILLIS)
            .setInterpolator(FastOutLinearInInterpolator())
            .start()

        splashProvider.view
            .animate()
            .alpha(0f)
            .setDuration(SPLASH_EXIT_MILLIS)
            .setInterpolator(FastOutLinearInInterpolator())
            .withEndAction(splashProvider::remove)
            .start()
    }

    private companion object {
        const val SPLASH_ICON_EXIT_SCALE = 1.2f
        const val SPLASH_ICON_EXIT_MILLIS = 200L
        const val SPLASH_EXIT_MILLIS = 300L
    }
}
