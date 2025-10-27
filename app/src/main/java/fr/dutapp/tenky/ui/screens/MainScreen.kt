package fr.dutapp.tenky.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.location.LocationServices
import fr.dutapp.tenky.R
import fr.dutapp.tenky.models.DailyWeather
import fr.dutapp.tenky.models.HourlyWeather
import fr.dutapp.tenky.utils.Constants
import fr.dutapp.tenky.viewmodel.WeatherViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: WeatherViewModel = viewModel(),
    onNavigateToSettings: () -> Unit,
    onNavigateToCities: () -> Unit,
    selectedLatitude: Double? = null,
    selectedLongitude: Double? = null
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    val sharedPreferences = remember {
        context.getSharedPreferences(Constants.getDefaultSharedPreferencesName(context), Context.MODE_PRIVATE)
    }

    val units = remember(sharedPreferences) { Constants.getUnits(sharedPreferences) }
    val locale = if (Locale.getDefault().language == "fr") "fr" else "en"

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    LaunchedEffect(selectedLatitude, selectedLongitude, units) {
        if (selectedLatitude != null && selectedLongitude != null) {
            viewModel.loadWeather(selectedLatitude, selectedLongitude, units, locale)
        } else if (hasLocationPermission) {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    viewModel.loadWeather(location.latitude, location.longitude, units, locale)
                }
            }
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tenky") },
                actions = {
                    IconButton(onClick = onNavigateToCities) {
                        Icon(Icons.Default.LocationCity, contentDescription = "Cities")
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (uiState.weatherCode != 0) {
                val backgroundRes = getBackgroundImage(uiState.weatherCode)
                Image(
                    painter = painterResource(id = backgroundRes),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                uiState.error != null -> {
                    Text(
                        text = "Error: ${uiState.error}",
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.error
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        item {
                            WeatherMainInfo(
                                cityName = uiState.cityName,
                                temperature = uiState.temperature,
                                description = uiState.description,
                                weatherIcon = uiState.weatherIcon
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            WeatherDetailsCard(
                                feelsLike = uiState.feelsLike,
                                humidity = uiState.humidity,
                                windSpeed = uiState.windSpeed,
                                sunrise = uiState.sunrise,
                                sunset = uiState.sunset
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Hourly Forecast",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            HourlyWeatherList(hourlyWeather = uiState.hourlyWeather)
                        }

                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "7-Day Forecast",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        items(uiState.dailyWeather) { day ->
                            DailyWeatherItem(day)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WeatherMainInfo(
    cityName: String,
    temperature: String,
    description: String,
    weatherIcon: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = cityName,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = temperature,
                style = MaterialTheme.typography.displayLarge,
                fontSize = 72.sp,
                fontWeight = FontWeight.Bold
            )
            if (weatherIcon.isNotEmpty()) {
                val iconRes = getWeatherIconResource(weatherIcon)
                if (iconRes != 0) {
                    Image(
                        painter = painterResource(id = iconRes),
                        contentDescription = description,
                        modifier = Modifier.size(80.dp)
                    )
                }
            }
            Text(
                text = description,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
fun WeatherDetailsCard(
    feelsLike: String,
    humidity: String,
    windSpeed: String,
    sunrise: String,
    sunset: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                WeatherDetail("Feels like", feelsLike)
                WeatherDetail("Humidity", humidity)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                WeatherDetail("Wind", windSpeed)
                Column {
                    WeatherDetail("Sunrise", sunrise)
                    Spacer(modifier = Modifier.height(8.dp))
                    WeatherDetail("Sunset", sunset)
                }
            }
        }
    }
}

@Composable
fun WeatherDetail(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun HourlyWeatherList(hourlyWeather: List<HourlyWeather>) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(hourlyWeather) { hour ->
            HourlyWeatherItem(hour)
        }
    }
}

@Composable
fun HourlyWeatherItem(hourlyWeather: HourlyWeather) {
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val time = timeFormat.format(Date(hourlyWeather.dt * 1000))
    val iconKey = "ic_${hourlyWeather.weather.firstOrNull()?.icon ?: ""}"

    Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
        shape = MaterialTheme.shapes.small
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = time, style = MaterialTheme.typography.bodySmall)
            val iconRes = getWeatherIconResource(iconKey)
            if (iconRes != 0) {
                Image(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp)
                )
            }
            Text(
                text = "${hourlyWeather.temp.toInt()}°",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun DailyWeatherItem(dailyWeather: DailyWeather) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = dailyWeather.dayName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )

            val iconRes = getWeatherIconResource(dailyWeather.weatherIcon)
            if (iconRes != 0) {
                Image(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = "${dailyWeather.temperature}°",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = dailyWeather.minMaxTemp,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

private fun getWeatherIconResource(iconKey: String): Int {
    val iconMap = Constants.getIconMap()
    return iconMap[iconKey] ?: 0
}

private fun getBackgroundImage(weatherCode: Int): Int {
    val imgMap = Constants.getImgMap()
    return when {
        weatherCode == 800 -> imgMap["img_800"] ?: R.drawable.img_800
        weatherCode > 800 -> imgMap["img_80x"] ?: R.drawable.img_80x
        else -> {
            val code = (weatherCode / 100) * 100
            imgMap["img_$code"] ?: R.drawable.img_800
        }
    }
}
