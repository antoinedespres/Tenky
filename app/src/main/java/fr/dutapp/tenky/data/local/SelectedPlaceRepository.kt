package fr.dutapp.tenky.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import fr.dutapp.tenky.domain.model.SavedCity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Which place the weather screen is showing.
 *
 * `null` means "follow the device location". Persisting the choice means
 * picking a city survives process death, and it is also how the cities screen
 * hands its selection back to the weather screen without passing arguments
 * through the navigation graph.
 */
class SelectedPlaceRepository(
    private val dataStore: DataStore<Preferences>,
    private val json: Json,
) {

    val selectedCity: Flow<SavedCity?> = dataStore.data.map { preferences ->
        preferences[SELECTED_CITY]?.let { stored ->
            runCatching { json.decodeFromString<StoredPlace>(stored) }.getOrNull()?.toDomain()
        }
    }

    suspend fun select(city: SavedCity) {
        dataStore.edit { preferences ->
            preferences[SELECTED_CITY] = json.encodeToString(city.toStored())
        }
    }

    /** Switches back to following the device location. */
    suspend fun clearSelection() {
        dataStore.edit { preferences -> preferences.remove(SELECTED_CITY) }
    }

    @Serializable
    private data class StoredPlace(
        val name: String,
        val lat: Double,
        val lon: Double,
        val country: String? = null,
        val state: String? = null,
    ) {
        fun toDomain() = SavedCity(name, lat, lon, country, state)
    }

    private fun SavedCity.toStored() = StoredPlace(name, latitude, longitude, country, state)

    private companion object {
        val SELECTED_CITY = stringPreferencesKey("selected_city")
    }
}
