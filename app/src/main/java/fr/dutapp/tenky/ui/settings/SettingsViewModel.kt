package fr.dutapp.tenky.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import fr.dutapp.tenky.data.local.SettingsRepository
import fr.dutapp.tenky.domain.model.AppLanguage
import fr.dutapp.tenky.domain.model.TemperatureUnit
import fr.dutapp.tenky.ui.weather.tenkyApplication
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    val useImperialUnits: StateFlow<Boolean> = settingsRepository.temperatureUnit
        .map { it == TemperatureUnit.IMPERIAL }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = false,
        )

    /** Held by AppCompat rather than DataStore — see [AppLanguageController]. */
    val language: StateFlow<AppLanguage> = AppLanguageController.language

    fun setUseImperialUnits(useImperial: Boolean) {
        viewModelScope.launch {
            settingsRepository.setTemperatureUnit(TemperatureUnit.fromImperialFlag(useImperial))
        }
    }

    /** Applying a language recreates the Activity, which re-reads resources. */
    fun setLanguage(language: AppLanguage) = AppLanguageController.apply(language)

    companion object {
        private const val STOP_TIMEOUT_MILLIS = 5_000L

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                SettingsViewModel(tenkyApplication().container.settingsRepository)
            }
        }
    }
}
