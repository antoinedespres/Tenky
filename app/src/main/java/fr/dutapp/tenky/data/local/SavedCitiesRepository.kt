package fr.dutapp.tenky.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import fr.dutapp.tenky.domain.model.SavedCity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * The user's pinned cities.
 *
 * Replaces the previous scheme of numbered `ville0`, `ville1`, ... keys plus a
 * separate `nbrCities` counter, which could not survive a removal from the
 * middle of the list and stored no coordinates.
 */
class SavedCitiesRepository(
    private val dataStore: DataStore<Preferences>,
    private val json: Json,
) {

    val cities: Flow<List<SavedCity>> = dataStore.data.map { preferences ->
        val stored = preferences[CITIES] ?: return@map emptyList()
        runCatching { json.decodeFromString<List<StoredCity>>(stored) }
            .getOrDefault(emptyList())
            .map(StoredCity::toDomain)
    }

    suspend fun add(city: SavedCity) = update { current ->
        // Coordinates identify a city; the same place typed twice is one entry.
        if (current.any { it.isSamePlaceAs(city) }) current else current + city
    }

    suspend fun remove(city: SavedCity) = update { current ->
        current.filterNot { it.isSamePlaceAs(city) }
    }

    /** Puts a removed city back where it was, for undo. */
    suspend fun insert(index: Int, city: SavedCity) = update { current ->
        if (current.any { it.isSamePlaceAs(city) }) {
            current
        } else {
            current.toMutableList().apply { add(index.coerceIn(0, size), city) }
        }
    }

    /**
     * Persists a new order.
     *
     * Written once when a drag ends rather than on every swap, which would put
     * a DataStore write behind each frame of the gesture.
     */
    suspend fun replaceAll(cities: List<SavedCity>) = update { cities }

    suspend fun clear() = update { emptyList() }

    private suspend fun update(transform: (List<SavedCity>) -> List<SavedCity>) {
        dataStore.edit { preferences ->
            val current = preferences[CITIES]
                ?.let { runCatching { json.decodeFromString<List<StoredCity>>(it) }.getOrNull() }
                ?.map(StoredCity::toDomain)
                .orEmpty()
            val updated = transform(current).map { it.toStored() }
            preferences[CITIES] = json.encodeToString(updated)
        }
    }

    private fun SavedCity.isSamePlaceAs(other: SavedCity): Boolean =
        latitude == other.latitude && longitude == other.longitude

    /** On-disk shape, kept separate so the domain model can change freely. */
    @Serializable
    private data class StoredCity(
        @SerialName("name") val name: String,
        @SerialName("lat") val latitude: Double,
        @SerialName("lon") val longitude: Double,
        @SerialName("country") val country: String? = null,
        @SerialName("state") val state: String? = null,
    ) {
        fun toDomain() = SavedCity(name, latitude, longitude, country, state)
    }

    private fun SavedCity.toStored() = StoredCity(name, latitude, longitude, country, state)

    private companion object {
        val CITIES = stringPreferencesKey("saved_cities")
    }
}
