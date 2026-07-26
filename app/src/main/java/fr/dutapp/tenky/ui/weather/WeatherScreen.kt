package fr.dutapp.tenky.ui.weather

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.dutapp.tenky.R
import fr.dutapp.tenky.domain.model.CurrentWeather
import fr.dutapp.tenky.domain.model.DailyForecast
import fr.dutapp.tenky.domain.model.HourlyForecast
import fr.dutapp.tenky.domain.model.TemperatureUnit
import fr.dutapp.tenky.location.LocationProvider
import fr.dutapp.tenky.ui.components.ErrorState
import fr.dutapp.tenky.ui.components.LoadingState
import fr.dutapp.tenky.ui.components.MessageState
import fr.dutapp.tenky.ui.formatDayLabel
import fr.dutapp.tenky.ui.formatTemperature
import fr.dutapp.tenky.ui.formatTemperatureRange
import fr.dutapp.tenky.ui.formatTime
import fr.dutapp.tenky.ui.formatWindSpeed
import fr.dutapp.tenky.ui.messageRes
import fr.dutapp.tenky.ui.sentenceCase
import fr.dutapp.tenky.util.WeatherIcons
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(
    onNavigateToCities: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WeatherViewModel = viewModel(factory = WeatherViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { granted ->
        viewModel.onLocationPermissionResult(granted.values.any { it })
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = state.snapshot?.current?.placeName
                            ?: stringResource(R.string.title_main),
                    )
                },
                actions = {
                    IconButton(onClick = viewModel::refresh) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = stringResource(R.string.action_refresh),
                        )
                    }
                    IconButton(onClick = onNavigateToCities) {
                        Icon(
                            imageVector = Icons.Default.LocationCity,
                            contentDescription = stringResource(R.string.title_all_cities),
                        )
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.title_settings),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                ),
            )
        },
        containerColor = Color.Transparent,
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            state.snapshot?.let { snapshot ->
                // Backdrop matching the current condition, behind the content.
                Image(
                    painter = painterResource(
                        WeatherIcons.backgroundFor(snapshot.current.conditionId),
                    ),
                    contentDescription = stringResource(R.string.bg_description),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = SCRIM_ALPHA)),
                )
            }

            WeatherContent(
                state = state,
                contentPadding = padding,
                onRetry = viewModel::refresh,
                onRequestPermission = {
                    permissionLauncher.launch(LocationProvider.LOCATION_PERMISSIONS)
                },
                onPickCity = onNavigateToCities,
            )
        }
    }
}

@Composable
private fun WeatherContent(
    state: WeatherUiState,
    contentPadding: PaddingValues,
    onRetry: () -> Unit,
    onRequestPermission: () -> Unit,
    onPickCity: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val contentModifier = modifier
        .fillMaxSize()
        .padding(contentPadding)

    when {
        state.needsLocationPermission -> MessageState(
            title = stringResource(R.string.location_permission_title),
            body = stringResource(R.string.location_permission_body),
            actionLabel = stringResource(R.string.location_permission_action),
            onAction = onRequestPermission,
            modifier = contentModifier,
        )

        state.locationUnavailable -> MessageState(
            title = stringResource(R.string.location_unavailable),
            actionLabel = stringResource(R.string.title_all_cities),
            onAction = onPickCity,
            modifier = contentModifier,
        )

        state.error != null && state.snapshot == null -> ErrorState(
            message = stringResource(state.error.messageRes()),
            onRetry = onRetry,
            modifier = contentModifier,
        )

        state.isLoading -> LoadingState(modifier = contentModifier)

        state.snapshot != null -> Column(
            modifier = contentModifier.verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CurrentConditions(current = state.snapshot.current, unit = state.unit)
            HourlyStrip(hourly = state.snapshot.hourly)
            DailyList(daily = state.snapshot.daily)
        }
    }
}

@Composable
private fun CurrentConditions(
    current: CurrentWeather,
    unit: TemperatureUnit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Image(
            painter = painterResource(WeatherIcons.forCode(current.iconCode)),
            contentDescription = stringResource(R.string.weather_icon_description),
            modifier = Modifier.size(96.dp),
        )
        Text(
            text = formatTemperature(current.temperature),
            style = MaterialTheme.typography.displayLarge,
        )
        Text(
            text = current.description.sentenceCase(),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = CARD_ALPHA),
            ),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                DetailRow(
                    label = stringResource(R.string.title_feels_like),
                    value = formatTemperature(current.feelsLike),
                )
                DetailRow(
                    label = stringResource(R.string.title_humidity),
                    value = stringResource(R.string.humidity_value, current.humidityPercent),
                )
                DetailRow(
                    label = stringResource(R.string.title_wind_speed),
                    value = formatWindSpeed(current.windSpeed, unit),
                )
                current.sunrise?.let {
                    DetailRow(
                        label = stringResource(R.string.title_sunrise),
                        value = formatTime(it),
                    )
                }
                current.sunset?.let {
                    DetailRow(
                        label = stringResource(R.string.title_sunset),
                        value = formatTime(it),
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(text = value, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun HourlyStrip(hourly: List<HourlyForecast>, modifier: Modifier = Modifier) {
    if (hourly.isEmpty()) return

    SectionCard(title = stringResource(R.string.title_hourly), modifier = modifier) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            items(hourly) { entry ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = formatTime(entry.time),
                        style = MaterialTheme.typography.labelMedium,
                    )
                    Image(
                        painter = painterResource(WeatherIcons.forCode(entry.iconCode)),
                        contentDescription = null,
                        modifier = Modifier.size(36.dp),
                    )
                    Text(
                        text = formatTemperature(entry.temperature),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }
    }
}

@Composable
private fun DailyList(daily: List<DailyForecast>, modifier: Modifier = Modifier) {
    if (daily.isEmpty()) return

    val today = LocalDate.now()
    val todayLabel = stringResource(R.string.label_today)
    val tomorrowLabel = stringResource(R.string.label_tomorrow)

    SectionCard(title = stringResource(R.string.title_daily), modifier = modifier) {
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            daily.forEachIndexed { index, day ->
                if (index > 0) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = formatDayLabel(day.date, today, todayLabel, tomorrowLabel),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f),
                    )
                    Image(
                        painter = painterResource(WeatherIcons.forCode(day.iconCode)),
                        contentDescription = day.description.sentenceCase(),
                        modifier = Modifier.size(32.dp),
                    )
                    Text(
                        text = formatTemperatureRange(day.maxTemperature, day.minTemperature),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 16.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = CARD_ALPHA),
        ),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(start = 16.dp, top = 12.dp),
        )
        content()
    }
}

/** Keeps the photographic backdrop readable behind text. */
private const val SCRIM_ALPHA = 0.55f
private const val CARD_ALPHA = 0.75f
