package fr.dutapp.tenky.data

import fr.dutapp.tenky.data.remote.OpenWeatherService
import fr.dutapp.tenky.domain.model.Coordinates
import fr.dutapp.tenky.domain.model.DataResult
import fr.dutapp.tenky.domain.model.SavedCity
import fr.dutapp.tenky.domain.model.TemperatureUnit
import fr.dutapp.tenky.domain.model.WeatherError
import fr.dutapp.tenky.domain.model.WeatherSnapshot
import fr.dutapp.tenky.domain.model.toOpenWeatherLanguage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import java.net.HttpURLConnection
import java.util.Locale

/**
 * Single entry point for weather data.
 *
 * Every call is wrapped so callers get a [DataResult] rather than an exception,
 * which is what lets the UI show a specific message instead of the previous
 * behaviour of printing a stack trace and leaving the screen blank.
 */
class WeatherRepository(
    private val service: OpenWeatherService,
    private val apiKeyProvider: () -> String,
    private val ioDispatcher: CoroutineDispatcher,
    private val localeProvider: () -> Locale = Locale::getDefault,
) {

    /**
     * Loads current conditions and the forecast together.
     *
     * The two calls are independent, so they run concurrently; if either fails
     * the whole snapshot fails, because a screen showing one without the other
     * would be misleading.
     */
    suspend fun getWeather(
        coordinates: Coordinates,
        unit: TemperatureUnit,
    ): DataResult<WeatherSnapshot> = safeCall {
        coroutineScope {
            val language = localeProvider().toOpenWeatherLanguage()
            val currentDeferred = async {
                service.getCurrentWeather(
                    latitude = coordinates.latitude,
                    longitude = coordinates.longitude,
                    units = unit.apiValue,
                    language = language,
                )
            }
            val forecastDeferred = async {
                service.getForecast(
                    latitude = coordinates.latitude,
                    longitude = coordinates.longitude,
                    units = unit.apiValue,
                    language = language,
                )
            }

            val current = currentDeferred.await()
            val forecast = forecastDeferred.await()

            WeatherSnapshot(
                current = current.toDomain(),
                hourly = forecast.toHourly(),
                daily = forecast.toDaily(),
                zoneOffset = zoneOffsetOf(current.timezone),
            )
        }
    }

    /** Current conditions only — used for the rows of the saved-cities list. */
    suspend fun getCurrentWeather(
        coordinates: Coordinates,
        unit: TemperatureUnit,
    ) = safeCall {
        service.getCurrentWeather(
            latitude = coordinates.latitude,
            longitude = coordinates.longitude,
            units = unit.apiValue,
            language = localeProvider().toOpenWeatherLanguage(),
        ).toDomain()
    }

    /** Resolves a typed city name to candidate places. */
    suspend fun searchCities(query: String): DataResult<List<SavedCity>> = safeCall {
        service.searchCities(query.trim()).map { it.toSavedCity() }
    }

    private suspend fun <T> safeCall(block: suspend () -> T): DataResult<T> {
        if (apiKeyProvider().isBlank()) {
            return DataResult.Failure(WeatherError.MissingApiKey)
        }
        return withContext(ioDispatcher) {
            try {
                DataResult.Success(block())
            } catch (e: HttpException) {
                DataResult.Failure(e.toWeatherError())
            } catch (_: IOException) {
                DataResult.Failure(WeatherError.Network)
            } catch (_: Exception) {
                // Covers malformed payloads from kotlinx.serialization.
                DataResult.Failure(WeatherError.Unknown)
            }
        }
    }

    private fun HttpException.toWeatherError() = when (code()) {
        HttpURLConnection.HTTP_UNAUTHORIZED -> WeatherError.InvalidApiKey
        HttpURLConnection.HTTP_NOT_FOUND -> WeatherError.NotFound
        TOO_MANY_REQUESTS -> WeatherError.RateLimited
        else -> WeatherError.Unknown
    }

    private companion object {
        const val TOO_MANY_REQUESTS = 429
    }
}
