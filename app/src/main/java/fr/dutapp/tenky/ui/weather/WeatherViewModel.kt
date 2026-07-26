package fr.dutapp.tenky.ui.weather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import fr.dutapp.tenky.TenkyApplication
import fr.dutapp.tenky.data.WeatherRepository
import fr.dutapp.tenky.data.local.SavedCitiesRepository
import fr.dutapp.tenky.data.local.SelectedPlaceRepository
import fr.dutapp.tenky.data.local.SettingsRepository
import fr.dutapp.tenky.domain.model.AppLanguage
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

/**
 * One page of the weather pager.
 *
 * A null [city] is the device's own location, which is always the first page.
 */
data class WeatherPlace(val city: SavedCity? = null) {
    val key: String
        get() = city?.let { "${it.latitude},${it.longitude}" } ?: DEVICE_KEY

    companion object {
        const val DEVICE_KEY = "device"
        val DeviceLocation = WeatherPlace()
    }
}

/** What one page is showing. */
data class PageState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val snapshot: WeatherSnapshot? = null,
    val error: WeatherError? = null,
    /** When [snapshot] was fetched, for the "updated N minutes ago" label. */
    val fetchedAtMillis: Long? = null,
    /** True when the shown data came from the cache after a failed refresh. */
    val isStale: Boolean = false,
    val needsLocationPermission: Boolean = false,
    val locationUnavailable: Boolean = false,
)

data class WeatherUiState(
    val places: List<WeatherPlace> = listOf(WeatherPlace.DeviceLocation),
    val pages: Map<String, PageState> = emptyMap(),
    val unit: TemperatureUnit = TemperatureUnit.METRIC,
    val currentPage: Int = 0,
    /** Set when another screen picked a city, so the pager can scroll to it. */
    val scrollToPage: Int? = null,
) {
    fun pageState(index: Int): PageState =
        places.getOrNull(index)?.let { pages[it.key] } ?: PageState()
}

class WeatherViewModel(
    private val weatherRepository: WeatherRepository,
    private val settingsRepository: SettingsRepository,
    private val savedCitiesRepository: SavedCitiesRepository,
    private val selectedPlaceRepository: SelectedPlaceRepository,
    private val locationProvider: LocationProvider,
) : ViewModel() {

    private val _uiState = MutableStateFlow(WeatherUiState())
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    /** Cached responses are per unit and language, so both invalidate the pages. */
    private var loadedUnit: TemperatureUnit? = null
    private var loadedLanguage: AppLanguage? = null

    init {
        viewModelScope.launch {
            combine(
                savedCitiesRepository.cities,
                settingsRepository.temperatureUnit,
                AppLanguageController.language,
                selectedPlaceRepository.selectedCity,
            ) { cities, unit, language, selected ->
                Snapshot(cities, unit, language, selected)
            }
                .distinctUntilChanged()
                .collect(::onInputsChanged)
        }
    }

    private data class Snapshot(
        val cities: List<SavedCity>,
        val unit: TemperatureUnit,
        val language: AppLanguage,
        val selected: SavedCity?,
    )

    private fun onInputsChanged(inputs: Snapshot) {
        val places = listOf(WeatherPlace.DeviceLocation) + inputs.cities.map(::WeatherPlace)
        // Condition text comes from the API, so a language change has to
        // refetch rather than just recompose. Same for a unit change, which the
        // API converts server-side.
        val invalidated = loadedUnit != inputs.unit || loadedLanguage != inputs.language
        loadedUnit = inputs.unit
        loadedLanguage = inputs.language

        val selectedIndex = inputs.selected
            ?.let { selected -> places.indexOfFirst { it.city?.sameAs(selected) == true } }
            ?.takeIf { it >= 0 }

        _uiState.update { state ->
            state.copy(
                places = places,
                unit = inputs.unit,
                // Drop pages whose place is gone, and everything on invalidation.
                pages = if (invalidated) {
                    emptyMap()
                } else {
                    state.pages.filterKeys { key -> places.any { it.key == key } }
                },
                currentPage = state.currentPage.coerceIn(0, places.lastIndex),
                scrollToPage = selectedIndex,
            )
        }
        ensureLoaded(_uiState.value.currentPage)
    }

    /** The pager settled on a page; load it if it has no data yet. */
    fun onPageChanged(index: Int) {
        _uiState.update { it.copy(currentPage = index) }
        ensureLoaded(index)
    }

    /** Consumed by the screen once it has scrolled, so it does not repeat. */
    fun onScrollHandled() = _uiState.update { it.copy(scrollToPage = null) }

    fun refresh() {
        val state = _uiState.value
        val place = state.places.getOrNull(state.currentPage) ?: return
        viewModelScope.launch { load(place, state.unit, isRefresh = true) }
    }

    fun onLocationPermissionResult(granted: Boolean) {
        val state = _uiState.value
        if (!granted) {
            updatePage(WeatherPlace.DEVICE_KEY) {
                it.copy(isLoading = false, needsLocationPermission = true)
            }
            return
        }
        viewModelScope.launch { load(WeatherPlace.DeviceLocation, state.unit, isRefresh = false) }
    }

    /**
     * Keys with a load in flight.
     *
     * The page's own `isLoading` cannot serve this: it defaults to true before
     * anything has started, so guarding on it would block the first load. Left
     * unguarded, the initial emission and the pager settling both started a
     * load for the same place — which on the device page meant two concurrent
     * location requests, each holding its own listeners.
     */
    private val inFlight = mutableSetOf<String>()

    private fun ensureLoaded(index: Int) {
        val state = _uiState.value
        val place = state.places.getOrNull(index) ?: return
        if (state.pages[place.key]?.snapshot != null) return
        if (!inFlight.add(place.key)) return
        viewModelScope.launch {
            try {
                load(place, state.unit, isRefresh = false)
            } finally {
                inFlight.remove(place.key)
            }
        }
    }

    private suspend fun load(place: WeatherPlace, unit: TemperatureUnit, isRefresh: Boolean) {
        updatePage(place.key) {
            it.copy(
                isLoading = !isRefresh && it.snapshot == null,
                isRefreshing = isRefresh,
                error = null,
                needsLocationPermission = false,
                locationUnavailable = false,
            )
        }

        val coordinates = place.city?.coordinates ?: when (val result = locationProvider.currentLocation()) {
            is LocationResult.Available -> result.coordinates
            LocationResult.PermissionDenied -> {
                updatePage(place.key) {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        needsLocationPermission = true,
                    )
                }
                return
            }

            LocationResult.Unavailable -> {
                updatePage(place.key) {
                    it.copy(isLoading = false, isRefreshing = false, locationUnavailable = true)
                }
                return
            }
        }

        fetch(place.key, coordinates, unit)
    }

    private suspend fun fetch(key: String, coordinates: Coordinates, unit: TemperatureUnit) {
        when (val result = weatherRepository.getWeather(coordinates, unit)) {
            is DataResult.Success -> updatePage(key) {
                it.copy(
                    isLoading = false,
                    isRefreshing = false,
                    snapshot = result.data.snapshot,
                    fetchedAtMillis = result.data.fetchedAtMillis,
                    isStale = result.data.isFromCache,
                    // Carries the reason a refresh failed, so cached data can be
                    // shown with an explanation rather than silently.
                    error = result.data.refreshError,
                )
            }

            is DataResult.Failure -> updatePage(key) {
                it.copy(isLoading = false, isRefreshing = false, error = result.error)
            }
        }
    }

    private fun updatePage(key: String, transform: (PageState) -> PageState) {
        _uiState.update { state ->
            val current = state.pages[key] ?: PageState()
            state.copy(pages = state.pages + (key to transform(current)))
        }
    }

    private fun SavedCity.sameAs(other: SavedCity) =
        latitude == other.latitude && longitude == other.longitude

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val container = tenkyApplication().container
                WeatherViewModel(
                    weatherRepository = container.weatherRepository,
                    settingsRepository = container.settingsRepository,
                    savedCitiesRepository = container.savedCitiesRepository,
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
