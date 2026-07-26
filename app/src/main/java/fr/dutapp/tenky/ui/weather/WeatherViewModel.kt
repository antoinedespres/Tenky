package fr.dutapp.tenky.ui.weather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import fr.dutapp.tenky.TenkyApplication
import fr.dutapp.tenky.data.WeatherRepository
import fr.dutapp.tenky.data.local.SelectedPlaceRepository
import fr.dutapp.tenky.data.local.SettingsRepository
import fr.dutapp.tenky.domain.model.Coordinates
import fr.dutapp.tenky.domain.model.DataResult
import fr.dutapp.tenky.domain.model.SavedCity
import fr.dutapp.tenky.domain.model.TemperatureUnit
import fr.dutapp.tenky.domain.model.WeatherError
import fr.dutapp.tenky.domain.model.WeatherSnapshot
import fr.dutapp.tenky.location.LocationProvider
import fr.dutapp.tenky.location.LocationResult
import fr.dutapp.tenky.ui.settings.AppLanguageController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Everything the weather screen needs to render. */
data class WeatherUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val snapshot: WeatherSnapshot? = null,
    val unit: TemperatureUnit = TemperatureUnit.METRIC,
    val error: WeatherError? = null,
    /** When [snapshot] was fetched, for the "updated N minutes ago" label. */
    val fetchedAtMillis: Long? = null,
    /** True when the shown data came from the cache after a failed refresh. */
    val isStale: Boolean = false,
    /** Set when location is needed but not granted, so the UI can prompt. */
    val needsLocationPermission: Boolean = false,
    /** Set when permission was granted but no fix could be obtained. */
    val locationUnavailable: Boolean = false,
    /** `null` while following the device location. */
    val selectedCity: SavedCity? = null,
)

class WeatherViewModel(
    private val weatherRepository: WeatherRepository,
    private val settingsRepository: SettingsRepository,
    private val selectedPlaceRepository: SelectedPlaceRepository,
    private val locationProvider: LocationProvider,
) : ViewModel() {

    private val _uiState = MutableStateFlow(WeatherUiState())
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    init {
        // Reloads whenever the chosen place, the unit system or the language
        // changes. Language matters because the condition text ("Overcast
        // clouds") is written by the API, not by a string resource, so
        // switching languages has to refetch rather than just recompose.
        viewModelScope.launch {
            combine(
                selectedPlaceRepository.selectedCity,
                settingsRepository.temperatureUnit,
                AppLanguageController.language,
            ) { city, unit, language -> Triple(city, unit, language) }
                .distinctUntilChanged()
                .collect { (city, unit, _) ->
                    _uiState.update { it.copy(selectedCity = city, unit = unit) }
                    load(city, unit, isRefresh = false)
                }
        }
    }

    fun refresh() {
        val state = _uiState.value
        viewModelScope.launch { load(state.selectedCity, state.unit, isRefresh = true) }
    }

    /** Called after the permission dialog resolves. */
    fun onLocationPermissionResult(granted: Boolean) {
        if (!granted) {
            _uiState.update { it.copy(isLoading = false, needsLocationPermission = true) }
            return
        }
        val state = _uiState.value
        viewModelScope.launch { load(state.selectedCity, state.unit, isRefresh = false) }
    }

    private suspend fun load(city: SavedCity?, unit: TemperatureUnit, isRefresh: Boolean) {
        _uiState.update {
            it.copy(
                isLoading = !isRefresh && it.snapshot == null,
                isRefreshing = isRefresh,
                error = null,
                needsLocationPermission = false,
                locationUnavailable = false,
            )
        }

        val coordinates = city?.coordinates ?: when (val result = locationProvider.currentLocation()) {
            is LocationResult.Available -> result.coordinates
            LocationResult.PermissionDenied -> {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        needsLocationPermission = true,
                    )
                }
                return
            }

            LocationResult.Unavailable -> {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        locationUnavailable = true,
                    )
                }
                return
            }
        }

        fetch(coordinates, unit)
    }

    private suspend fun fetch(coordinates: Coordinates, unit: TemperatureUnit) {
        when (val result = weatherRepository.getWeather(coordinates, unit)) {
            is DataResult.Success -> _uiState.update {
                it.copy(
                    isLoading = false,
                    isRefreshing = false,
                    snapshot = result.data.snapshot,
                    fetchedAtMillis = result.data.fetchedAtMillis,
                    isStale = result.data.isFromCache,
                    // Carries the reason the refresh failed, so cached data can
                    // be shown with an explanation rather than silently.
                    error = result.data.refreshError,
                )
            }

            is DataResult.Failure -> _uiState.update {
                it.copy(isLoading = false, isRefreshing = false, error = result.error)
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val container = tenkyApplication().container
                WeatherViewModel(
                    weatherRepository = container.weatherRepository,
                    settingsRepository = container.settingsRepository,
                    selectedPlaceRepository = container.selectedPlaceRepository,
                    locationProvider = container.locationProvider,
                )
            }
        }
    }
}

/** Shared helper for the `viewModelFactory` initializers in this package. */
internal fun androidx.lifecycle.viewmodel.CreationExtras.tenkyApplication(): TenkyApplication =
    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as TenkyApplication
