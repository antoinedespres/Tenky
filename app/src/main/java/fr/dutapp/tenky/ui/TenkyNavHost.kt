package fr.dutapp.tenky.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import fr.dutapp.tenky.ui.cities.CitiesScreen
import fr.dutapp.tenky.ui.settings.SettingsScreen
import fr.dutapp.tenky.ui.weather.WeatherScreen

private object Routes {
    const val WEATHER = "weather"
    const val CITIES = "cities"
    const val SETTINGS = "settings"
}

@Composable
fun TenkyNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = Routes.WEATHER,
        modifier = modifier,
    ) {
        composable(Routes.WEATHER) {
            WeatherScreen(
                onNavigateToCities = { navController.navigate(Routes.CITIES) },
                onNavigateToSettings = { navController.navigate(Routes.SETTINGS) },
            )
        }
        composable(Routes.CITIES) {
            CitiesScreen(
                onNavigateBack = navController::popBackStack,
                // The chosen city is persisted, so returning to the weather
                // screen is enough for it to pick the selection up.
                onCitySelected = navController::popBackStack,
            )
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(onNavigateBack = navController::popBackStack)
        }
    }
}
