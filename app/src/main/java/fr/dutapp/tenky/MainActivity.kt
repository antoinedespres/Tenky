package fr.dutapp.tenky

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import fr.dutapp.tenky.ui.screens.*
import fr.dutapp.tenky.ui.theme.TenkyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TenkyTheme {
                TenkyApp()
            }
        }
    }
}

@Composable
fun TenkyApp() {
    val navController = rememberNavController()
    var showSplash by remember { mutableStateOf(true) }

    if (showSplash) {
        SplashScreen(onTimeout = { showSplash = false })
    } else {
        NavHost(
            navController = navController,
            startDestination = "main"
        ) {
            composable("main?lat={lat}&lon={lon}",
                arguments = listOf(
                    navArgument("lat") {
                        type = NavType.StringType
                        nullable = true
                    },
                    navArgument("lon") {
                        type = NavType.StringType
                        nullable = true
                    }
                )
            ) { backStackEntry ->
                val lat = backStackEntry.arguments?.getString("lat")?.toDoubleOrNull()
                val lon = backStackEntry.arguments?.getString("lon")?.toDoubleOrNull()

                MainScreen(
                    onNavigateToSettings = { navController.navigate("settings") },
                    onNavigateToCities = { navController.navigate("cities") },
                    selectedLatitude = lat,
                    selectedLongitude = lon
                )
            }

            composable("settings") {
                SettingsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable("cities") {
                AllCitiesScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onCitySelected = { lat, lon ->
                        navController.navigate("main?lat=$lat&lon=$lon") {
                            popUpTo("main") { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
