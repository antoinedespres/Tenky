package fr.dutapp.tenky.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import fr.dutapp.tenky.domain.model.Coordinates
import fr.dutapp.tenky.domain.model.TemperatureUnit
import fr.dutapp.tenky.domain.model.WeatherSnapshot
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.math.roundToInt

/** A stored snapshot together with when it was fetched. */
@Serializable
data class CachedWeather(
    val snapshot: WeatherSnapshot,
    /** Epoch millis of the fetch that produced [snapshot]. */
    val fetchedAtMillis: Long,
    /** Cached responses are unit-specific, since the API converts server-side. */
    val unit: TemperatureUnit,
)

/**
 * Last known good weather, per place and unit system.
 *
 * Lets the app show something on a cold start with no connection, instead of an
 * error screen.
 */
interface WeatherCache {
    suspend fun read(coordinates: Coordinates, unit: TemperatureUnit): CachedWeather?

    suspend fun write(
        coordinates: Coordinates,
        unit: TemperatureUnit,
        snapshot: WeatherSnapshot,
        fetchedAtMillis: Long,
    )
}

/**
 * DataStore-backed [WeatherCache].
 *
 * Entries are keyed by rounded coordinates so the same place reached from the
 * location provider and from a saved city shares one entry.
 */
class DataStoreWeatherCache(
    private val dataStore: DataStore<Preferences>,
    private val json: Json,
) : WeatherCache {

    override suspend fun read(
        coordinates: Coordinates,
        unit: TemperatureUnit,
    ): CachedWeather? {
        val stored = dataStore.data.first()[keyFor(coordinates, unit)] ?: return null
        return runCatching { json.decodeFromString<CachedWeather>(stored) }.getOrNull()
    }

    override suspend fun write(
        coordinates: Coordinates,
        unit: TemperatureUnit,
        snapshot: WeatherSnapshot,
        fetchedAtMillis: Long,
    ) {
        val entry = CachedWeather(snapshot, fetchedAtMillis, unit)
        dataStore.edit { preferences ->
            preferences[keyFor(coordinates, unit)] = json.encodeToString(entry)
        }
    }

    /**
     * Coordinates rounded to two decimals, roughly a kilometre.
     *
     * A GPS fix never repeats exactly, so keying on the raw value would grow an
     * unbounded set of near-duplicate entries and never register a cache hit.
     */
    private fun keyFor(coordinates: Coordinates, unit: TemperatureUnit): Preferences.Key<String> {
        val latitude = (coordinates.latitude * PRECISION).roundToInt()
        val longitude = (coordinates.longitude * PRECISION).roundToInt()
        return stringPreferencesKey("weather_${latitude}_${longitude}_${unit.name}")
    }

    private companion object {
        const val PRECISION = 100
    }
}
