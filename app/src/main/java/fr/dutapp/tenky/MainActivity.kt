package fr.dutapp.tenky

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import fr.dutapp.tenky.ui.TenkyNavHost
import fr.dutapp.tenky.ui.theme.TenkyTheme

/**
 * The app's only Activity.
 *
 * The former splash, cities and settings Activities are now Compose
 * destinations; the splash itself is handled by the platform API, which removes
 * the artificial 800 ms delay the old SplashScreenActivity imposed on startup.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            TenkyTheme {
                TenkyNavHost()
            }
        }
    }
}
