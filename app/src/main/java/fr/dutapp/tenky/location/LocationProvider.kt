package fr.dutapp.tenky.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import androidx.core.content.ContextCompat
import fr.dutapp.tenky.domain.model.Coordinates
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

/** Outcome of asking the device where it is. */
sealed interface LocationResult {
    data class Available(val coordinates: Coordinates) : LocationResult

    /** The user has not granted a location permission. */
    data object PermissionDenied : LocationResult

    /** Permission is granted but no provider produced a fix. */
    data object Unavailable : LocationResult
}

/**
 * Resolves the device's position using the platform [LocationManager].
 *
 * This deliberately avoids Play Services' fused provider: the app targets
 * devices without Google Play Services, where the fused client never returns.
 */
class LocationProvider(private val context: Context) {

    private val locationManager: LocationManager?
        get() = ContextCompat.getSystemService(context, LocationManager::class.java)

    fun hasPermission(): Boolean = LOCATION_PERMISSIONS.any { permission ->
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Returns the current position, preferring a recent cached fix and falling
     * back to a single live update.
     */
    suspend fun currentLocation(): LocationResult {
        if (!hasPermission()) return LocationResult.PermissionDenied
        val manager = locationManager ?: return LocationResult.Unavailable

        manager.freshestCachedLocation()?.let { cached ->
            return LocationResult.Available(cached.toCoordinates())
        }

        val live = withTimeoutOrNull(LIVE_FIX_TIMEOUT_MILLIS) {
            manager.awaitSingleUpdate()
        }
        return live?.let { LocationResult.Available(it.toCoordinates()) }
            ?: LocationResult.Unavailable
    }

    @SuppressLint("MissingPermission") // Guarded by hasPermission() above.
    private fun LocationManager.freshestCachedLocation(): Location? = PROVIDERS
        .filter { provider -> isProviderEnabledSafely(provider) }
        .mapNotNull { provider -> runCatching { getLastKnownLocation(provider) }.getOrNull() }
        .filter { location ->
            System.currentTimeMillis() - location.time <= MAX_CACHED_FIX_AGE_MILLIS
        }
        .maxByOrNull(Location::getTime)

    @SuppressLint("MissingPermission") // Guarded by hasPermission() above.
    private suspend fun LocationManager.awaitSingleUpdate(): Location? {
        val provider = PROVIDERS.firstOrNull { isProviderEnabledSafely(it) } ?: return null

        return suspendCancellableCoroutine { continuation ->
            // android.location.LocationListener only gained default methods in
            // API 30, so every method is implemented for older devices.
            val listener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    removeUpdates(this)
                    if (continuation.isActive) continuation.resume(location)
                }

                override fun onProviderDisabled(provider: String) = Unit
                override fun onProviderEnabled(provider: String) = Unit

                @Deprecated("Required for API < 30", ReplaceWith(""))
                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) = Unit
            }

            continuation.invokeOnCancellation { removeUpdates(listener) }

            runCatching {
                requestLocationUpdates(provider, 0L, 0f, listener, Looper.getMainLooper())
            }.onFailure {
                if (continuation.isActive) continuation.resume(null)
            }
        }
    }

    private fun LocationManager.isProviderEnabledSafely(provider: String): Boolean =
        runCatching { isProviderEnabled(provider) }.getOrDefault(false)

    private fun Location.toCoordinates() = Coordinates(latitude, longitude)

    companion object {
        val LOCATION_PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        )

        /** Network first: it resolves indoors and far faster than GPS. */
        private val PROVIDERS = listOf(
            LocationManager.NETWORK_PROVIDER,
            LocationManager.GPS_PROVIDER,
            LocationManager.PASSIVE_PROVIDER,
        )

        private const val MAX_CACHED_FIX_AGE_MILLIS = 10 * 60 * 1000L
        private const val LIVE_FIX_TIMEOUT_MILLIS = 15_000L
    }
}
