package fr.dutapp.tenky.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import fr.dutapp.tenky.domain.model.TemperatureUnit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** User preferences. Backed by DataStore rather than SharedPreferences. */
class SettingsRepository(private val dataStore: DataStore<Preferences>) {

    val temperatureUnit: Flow<TemperatureUnit> = dataStore.data.map { preferences ->
        TemperatureUnit.fromImperialFlag(preferences[USE_IMPERIAL] == true)
    }

    suspend fun setTemperatureUnit(unit: TemperatureUnit) {
        dataStore.edit { preferences ->
            preferences[USE_IMPERIAL] = unit == TemperatureUnit.IMPERIAL
        }
    }

    private companion object {
        val USE_IMPERIAL = booleanPreferencesKey("use_imperial_units")
    }
}
