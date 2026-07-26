package fr.dutapp.tenky.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColors = lightColorScheme(
    primary = CeladonBlue,
    onPrimary = Color.White,
    primaryContainer = CarolinaBlue,
    onPrimaryContainer = Color.White,
    secondary = SapphireBlue,
    onSecondary = Color.White,
    background = AzureWeb,
    onBackground = PrussianBlue,
    surface = Color.White,
    onSurface = PrussianBlue,
    surfaceVariant = AzureWeb,
    onSurfaceVariant = SapphireBlue,
)

private val DarkColors = darkColorScheme(
    primary = SkyLight,
    onPrimary = PrussianBlue,
    primaryContainer = SapphireBlue,
    onPrimaryContainer = Color.White,
    secondary = CarolinaBlue,
    onSecondary = PrussianBlue,
    background = NightSurface,
    onBackground = AzureWeb,
    surface = NightSurface,
    onSurface = AzureWeb,
    surfaceVariant = NightSurfaceVariant,
    onSurfaceVariant = SkyLight,
)

@Composable
fun TenkyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    /** Material You colours, available from Android 12 onwards. */
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = TenkyTypography,
        content = content,
    )
}
