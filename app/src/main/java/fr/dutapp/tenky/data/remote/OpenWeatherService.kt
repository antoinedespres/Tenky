package fr.dutapp.tenky.data.remote

import fr.dutapp.tenky.data.remote.dto.CurrentWeatherDto
import fr.dutapp.tenky.data.remote.dto.ForecastDto
import fr.dutapp.tenky.data.remote.dto.GeocodingResultDto
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * The free OpenWeather endpoints.
 *
 * The app deliberately does not use One Call: version 2.5 of that endpoint was
 * switched off in June 2024 and version 3.0 needs a paid subscription. Current
 * conditions and the five-day forecast below are free.
 *
 * The API key is attached by [ApiKeyInterceptor], so it never appears in a
 * method signature or call site.
 */
interface OpenWeatherService {

    @GET("data/2.5/weather")
    suspend fun getCurrentWeather(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("units") units: String,
        @Query("lang") language: String,
    ): CurrentWeatherDto

    @GET("data/2.5/forecast")
    suspend fun getForecast(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("units") units: String,
        @Query("lang") language: String,
    ): ForecastDto

    @GET("geo/1.0/direct")
    suspend fun searchCities(
        @Query("q") query: String,
        @Query("limit") limit: Int = SEARCH_RESULT_LIMIT,
    ): List<GeocodingResultDto>

    private companion object {
        const val SEARCH_RESULT_LIMIT = 5
    }
}
