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
 * Everything goes through the platform API rather than the Play Services
 * location client, so the app still works on devices without Google Play
 * Services. Where the platform does expose a fused provider it is used, since
 * it is the one that resolves indoors — but its absence only costs accuracy,
 * never a crash or a hang.
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
            manager.awaitFirstFix()
        }
        return live?.let { LocationResult.Available(it.toCoordinates()) }
            ?: LocationResult.Unavailable
    }

    @SuppressLint("MissingPermission") // Guarded by hasPermission() above.
    private fun LocationManager.freshestCachedLocation(): Location? = PROVIDERS
        .mapNotNull { provider -> runCatching { getLastKnownLocation(provider) }.getOrNull() }
        .filter { location ->
            System.currentTimeMillis() - location.time <= MAX_CACHED_FIX_AGE_MILLIS
        }
        .maxByOrNull(Location::getTime)

    /**
     * Listens on every provider at once and takes the first fix.
     *
     * Providers are deliberately not filtered by `isProviderEnabled` first.
     * That reads the legacy `location_providers_allowed` setting, which only
     * ever lists gps and network — so the fused provider, the one that resolves
     * indoors, was reported as disabled and never asked, leaving a device with
     * GPS as its only listed provider waiting out the whole timeout for a cold
     * lock that rarely arrives inside.
     *
     * Registering on a provider that is switched off is harmless: it simply
     * never delivers. An unknown provider throws, and is skipped.
     */
    @SuppressLint("MissingPermission") // Guarded by hasPermission() above.
    private suspend fun LocationManager.awaitFirstFix(): Location? {
        return suspendCancellableCoroutine { continuation ->
            val listeners = mutableListOf<LocationListener>()

            fun stopListening() {
                listeners.forEach { listener -> runCatching { removeUpdates(listener) } }
                listeners.clear()
            }

            PROVIDERS.forEach { provider ->
                // android.location.LocationListener only gained default methods
                // in API 30, so every method is implemented for older devices.
                val listener = object : LocationListener {
                    override fun onLocationChanged(location: Location) {
                        stopListening()
                        if (continuation.isActive) continuation.resume(location)
                    }

                    override fun onProviderDisabled(provider: String) = Unit
                    override fun onProviderEnabled(provider: String) = Unit

                    @Deprecated("Required for API < 30", ReplaceWith(""))
                    override fun onStatusChanged(
                        provider: String?,
                        status: Int,
                        extras: Bundle?,
                    ) = Unit
                }
                runCatching {
                    requestLocationUpdates(provider, 0L, 0f, listener, Looper.getMainLooper())
                }.onSuccess { listeners += listener }
            }

            // Nothing accepted a request, so waiting out the timeout would tell
            // the user nothing they could not be told now.
            if (listeners.isEmpty() && continuation.isActive) {
                continuation.resume(null)
                return@suspendCancellableCoroutine
            }

            continuation.invokeOnCancellation { stopListening() }
        }
    }

    private fun Location.toCoordinates() = Coordinates(latitude, longitude)

    companion object {
        val LOCATION_PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        )

        /**
         * Every provider worth asking, coarse and cheap before precise and slow.
         *
         * "fused" is the platform's combined provider. It is only a public
         * constant from API 31, but the string works on older releases where
         * the device supplies one, and simply reports as disabled where it does
         * not — so this stays a plain LocationManager call with no Play
         * Services dependency.
         */
        private const val FUSED_PROVIDER = "fused"

        private val PROVIDERS = listOf(
            FUSED_PROVIDER,
            LocationManager.NETWORK_PROVIDER,
            LocationManager.GPS_PROVIDER,
            LocationManager.PASSIVE_PROVIDER,
        )

        /**
         * How stale a cached fix may be before a live one is requested.
         *
         * Generous on purpose: the weather a few kilometres from where the
         * phone last had a fix is the same weather, and waiting on a cold GPS
         * lock to refine that is a poor trade.
         */
        private const val MAX_CACHED_FIX_AGE_MILLIS = 2 * 60 * 60 * 1000L

        /** A cold GPS lock can take well over the 15s this used to allow. */
        private const val LIVE_FIX_TIMEOUT_MILLIS = 30_000L
    }
}
