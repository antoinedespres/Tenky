package fr.dutapp.tenky.domain.model

/**
 * A snapshot plus where it came from.
 *
 * Serving stale data silently would be dishonest, so the origin travels with
 * the data: the UI can show when it was fetched and, if a refresh failed while
 * cached data was available, say so without hiding the data.
 */
data class WeatherLoad(
    val snapshot: WeatherSnapshot,
    /** Epoch millis of the fetch that produced [snapshot]. */
    val fetchedAtMillis: Long,
    /** True when the network failed and this came from the cache instead. */
    val isFromCache: Boolean,
    /** Set when a refresh failed but cached data was shown in its place. */
    val refreshError: WeatherError? = null,
)
