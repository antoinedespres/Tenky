package fr.dutapp.tenky.ui.cities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import fr.dutapp.tenky.data.WeatherRepository
import fr.dutapp.tenky.data.local.SavedCitiesRepository
import fr.dutapp.tenky.data.local.SelectedPlaceRepository
import fr.dutapp.tenky.data.local.SettingsRepository
import fr.dutapp.tenky.domain.model.DataResult
import fr.dutapp.tenky.domain.model.SavedCity
import fr.dutapp.tenky.domain.model.TemperatureUnit
import fr.dutapp.tenky.domain.model.WeatherError
import fr.dutapp.tenky.ui.weather.tenkyApplication
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** A saved city plus its current temperature, once loaded. */
data class CityRow(
    val city: SavedCity,
    val temperature: Double? = null,
    val iconCode: String? = null,
)

/** A city removed by a swipe, kept just long enough to offer an undo. */
data class RemovedCity(val city: SavedCity, val index: Int)

data class CitiesUiState(
    val rows: List<CityRow> = emptyList(),
    val lastRemoved: RemovedCity? = null,
    val unit: TemperatureUnit = TemperatureUnit.METRIC,
    val query: String = "",
    val searchResults: List<SavedCity> = emptyList(),
    val isSearching: Boolean = false,
    val searchPerformed: Boolean = false,
    val error: WeatherError? = null,
)

class CitiesViewModel(
    private val weatherRepository: WeatherRepository,
    private val savedCitiesRepository: SavedCitiesRepository,
    private val selectedPlaceRepository: SelectedPlaceRepository,
    settingsRepository: SettingsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CitiesUiState())
    val uiState: StateFlow<CitiesUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        viewModelScope.launch {
            combine(
                savedCitiesRepository.cities,
                settingsRepository.temperatureUnit,
            ) { cities, unit -> cities to unit }
                .collect { (cities, unit) ->
                    _uiState.update { state ->
                        state.copy(
                            unit = unit,
                            // Keep any temperature already loaded for a city so
                            // the list does not flash empty on every emission.
                            rows = cities.map { city ->
                                state.rows.firstOrNull { it.city.sameAs(city) }
                                    ?: CityRow(city)
                            },
                        )
                    }
                    loadTemperatures(unit)
                }
        }
    }

    /**
     * Fetches each row's temperature once per list change.
     *
     * The previous implementation issued a request from `onBindViewHolder`,
     * which re-fired on every scroll and rebind.
     */
    private fun loadTemperatures(unit: TemperatureUnit) {
        _uiState.value.rows.forEach { row ->
            viewModelScope.launch {
                val result = weatherRepository.getCurrentWeather(row.city.coordinates, unit)
                if (result is DataResult.Success) {
                    _uiState.update { state ->
                        state.copy(
                            rows = state.rows.map { existing ->
                                if (existing.city.sameAs(row.city)) {
                                    existing.copy(
                                        temperature = result.data.temperature,
                                        iconCode = result.data.iconCode,
                                    )
                                } else {
                                    existing
                                }
                            },
                        )
                    }
                }
            }
        }
    }

    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query) }
        searchJob?.cancel()
        if (query.isBlank()) {
            _uiState.update { it.copy(searchResults = emptyList(), searchPerformed = false) }
            return
        }
        searchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MILLIS)
            search(query)
        }
    }

    private suspend fun search(query: String) {
        _uiState.update { it.copy(isSearching = true, error = null) }
        when (val result = weatherRepository.searchCities(query)) {
            is DataResult.Success -> _uiState.update {
                it.copy(
                    isSearching = false,
                    searchResults = result.data,
                    searchPerformed = true,
                )
            }

            is DataResult.Failure -> _uiState.update {
                it.copy(isSearching = false, error = result.error, searchPerformed = true)
            }
        }
    }

    fun addCity(city: SavedCity) {
        viewModelScope.launch { savedCitiesRepository.add(city) }
        _uiState.update { it.copy(query = "", searchResults = emptyList(), searchPerformed = false) }
    }

    fun removeCity(city: SavedCity) {
        val index = _uiState.value.rows.indexOfFirst { it.city.sameAs(city) }
        // A dismiss gesture can report the same removal more than once. The
        // second call no longer finds the row, so it must leave the pending
        // undo alone rather than clearing it.
        if (index < 0) return

        viewModelScope.launch { savedCitiesRepository.remove(city) }
        _uiState.update { state ->
            state.copy(
                rows = state.rows.filterNot { it.city.sameAs(city) },
                // Held so the removal can be undone from a snackbar; a swipe is
                // easy to trigger by accident.
                lastRemoved = RemovedCity(city, index),
            )
        }
    }

    fun undoRemove() {
        val removed = _uiState.value.lastRemoved ?: return
        viewModelScope.launch { savedCitiesRepository.insert(removed.index, removed.city) }
        _uiState.update { it.copy(lastRemoved = null) }
    }

    fun onUndoDismissed() = _uiState.update { it.copy(lastRemoved = null) }

    /** Reorders locally while the drag is in flight; nothing is written yet. */
    fun moveCity(fromIndex: Int, toIndex: Int) {
        _uiState.update { state ->
            if (fromIndex !in state.rows.indices || toIndex !in state.rows.indices) {
                state
            } else {
                state.copy(
                    rows = state.rows.toMutableList()
                        .apply { add(toIndex, removeAt(fromIndex)) },
                )
            }
        }
    }

    /** Commits the order once the drag ends. */
    fun onReorderFinished() {
        val cities = _uiState.value.rows.map { it.city }
        viewModelScope.launch { savedCitiesRepository.replaceAll(cities) }
    }

    fun clearAll() {
        viewModelScope.launch { savedCitiesRepository.clear() }
    }

    /** Makes the weather screen show this city. */
    fun selectCity(city: SavedCity) {
        viewModelScope.launch { selectedPlaceRepository.select(city) }
    }

    private fun SavedCity.sameAs(other: SavedCity) =
        latitude == other.latitude && longitude == other.longitude

    companion object {
        private const val SEARCH_DEBOUNCE_MILLIS = 350L

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val container = tenkyApplication().container
                CitiesViewModel(
                    weatherRepository = container.weatherRepository,
                    savedCitiesRepository = container.savedCitiesRepository,
                    selectedPlaceRepository = container.selectedPlaceRepository,
                    settingsRepository = container.settingsRepository,
                )
            }
        }
    }
}
