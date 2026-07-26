package fr.dutapp.tenky.data

import fr.dutapp.tenky.data.remote.OpenWeatherService
import fr.dutapp.tenky.data.remote.dto.CoordDto
import fr.dutapp.tenky.data.remote.dto.CurrentWeatherDto
import fr.dutapp.tenky.data.remote.dto.ForecastCityDto
import fr.dutapp.tenky.data.remote.dto.ForecastDto
import fr.dutapp.tenky.data.remote.dto.ForecastEntryDto
import fr.dutapp.tenky.data.remote.dto.GeocodingResultDto
import fr.dutapp.tenky.data.remote.dto.MainDto
import fr.dutapp.tenky.data.remote.dto.SysDto
import fr.dutapp.tenky.data.remote.dto.WeatherDescriptionDto
import fr.dutapp.tenky.domain.model.Coordinates
import fr.dutapp.tenky.domain.model.DataResult
import fr.dutapp.tenky.domain.model.TemperatureUnit
import fr.dutapp.tenky.domain.model.WeatherError
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.util.Locale

private val PARIS = Coordinates(48.86, 2.34)

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class WeatherRepositoryTest {

    @Test
    fun `missing api key is reported without hitting the network`() = runTest {
        var called = false
        val repository = repository(
            apiKey = "",
            service = object : FakeService() {
                override suspend fun getCurrentWeather(
                    latitude: Double,
                    longitude: Double,
                    units: String,
                    language: String,
                ): CurrentWeatherDto {
                    called = true
                    return currentWeatherDto()
                }
            },
        )

        val result = repository.getWeather(PARIS, TemperatureUnit.METRIC)

        assertEquals(DataResult.Failure(WeatherError.MissingApiKey), result)
        assertEquals(false, called)
    }

    @Test
    fun `http 401 becomes an invalid key error`() = runTest {
        val repository = repository(service = failingService(httpException(401)))

        val result = repository.getWeather(PARIS, TemperatureUnit.METRIC)

        assertEquals(DataResult.Failure(WeatherError.InvalidApiKey), result)
    }

    @Test
    fun `http 404 becomes a not-found error`() = runTest {
        val repository = repository(service = failingService(httpException(404)))

        assertEquals(
            DataResult.Failure(WeatherError.NotFound),
            repository.getWeather(PARIS, TemperatureUnit.METRIC),
        )
    }

    @Test
    fun `http 429 becomes a rate-limit error`() = runTest {
        val repository = repository(service = failingService(httpException(429)))

        assertEquals(
            DataResult.Failure(WeatherError.RateLimited),
            repository.getWeather(PARIS, TemperatureUnit.METRIC),
        )
    }

    @Test
    fun `connectivity failures become a network error`() = runTest {
        val repository = repository(service = failingService(IOException("offline")))

        assertEquals(
            DataResult.Failure(WeatherError.Network),
            repository.getWeather(PARIS, TemperatureUnit.METRIC),
        )
    }

    @Test
    fun `unexpected failures do not escape the repository`() = runTest {
        val repository = repository(service = failingService(IllegalStateException("bad payload")))

        assertEquals(
            DataResult.Failure(WeatherError.Unknown),
            repository.getWeather(PARIS, TemperatureUnit.METRIC),
        )
    }

    @Test
    fun `a successful snapshot combines current conditions and forecast`() = runTest {
        val repository = repository(service = FakeService())

        val result = repository.getWeather(PARIS, TemperatureUnit.METRIC)

        val snapshot = (result as DataResult.Success).data
        assertEquals("Paris", snapshot.current.placeName)
        assertEquals(22.0, snapshot.current.temperature, 0.001)
        assertEquals(1, snapshot.daily.size)
    }

    @Test
    fun `the requested unit system is forwarded to the api`() = runTest {
        var requestedUnits: String? = null
        val repository = repository(
            service = object : FakeService() {
                override suspend fun getCurrentWeather(
                    latitude: Double,
                    longitude: Double,
                    units: String,
                    language: String,
                ): CurrentWeatherDto {
                    requestedUnits = units
                    return currentWeatherDto()
                }
            },
        )

        repository.getWeather(PARIS, TemperatureUnit.IMPERIAL)

        assertEquals("imperial", requestedUnits)
    }

    @Test
    fun `city search maps geocoding results to saved cities`() = runTest {
        val repository = repository(
            service = object : FakeService() {
                override suspend fun searchCities(query: String, limit: Int) = listOf(
                    GeocodingResultDto(
                        name = "Lyon",
                        lat = 45.75,
                        lon = 4.83,
                        country = "FR",
                        state = "Auvergne-Rhône-Alpes",
                    ),
                )
            },
        )

        val result = repository.searchCities(" Lyon ")

        val cities = (result as DataResult.Success).data
        assertEquals("Lyon", cities.single().name)
        assertEquals("Lyon, Auvergne-Rhône-Alpes, FR", cities.single().qualifiedName)
    }

    private fun repository(
        service: OpenWeatherService,
        apiKey: String = "test-key",
    ) = WeatherRepository(
        service = service,
        apiKeyProvider = { apiKey },
        ioDispatcher = UnconfinedTestDispatcher(),
        localeProvider = { Locale.FRANCE },
    )

    private fun httpException(code: Int) = HttpException(
        Response.error<Any>(code, "".toResponseBody("application/json".toMediaType())),
    )

    private fun failingService(error: Throwable) = object : FakeService() {
        override suspend fun getCurrentWeather(
            latitude: Double,
            longitude: Double,
            units: String,
            language: String,
        ): CurrentWeatherDto = throw error

        override suspend fun getForecast(
            latitude: Double,
            longitude: Double,
            units: String,
            language: String,
        ): ForecastDto = throw error
    }

    /** Returns valid, minimal payloads unless a test overrides a method. */
    private open class FakeService : OpenWeatherService {
        override suspend fun getCurrentWeather(
            latitude: Double,
            longitude: Double,
            units: String,
            language: String,
        ) = currentWeatherDto()

        override suspend fun getForecast(
            latitude: Double,
            longitude: Double,
            units: String,
            language: String,
        ) = ForecastDto(
            city = ForecastCityDto(
                name = "Paris",
                coord = CoordDto(48.86, 2.34),
                country = "FR",
                timezone = 7200,
            ),
            list = listOf(
                ForecastEntryDto(
                    dt = 1785024000L,
                    main = MainDto(temp = 20.0, tempMin = 18.0, tempMax = 23.0, humidity = 60),
                    weather = listOf(WeatherDescriptionDto(803, "nuageux", "04d")),
                ),
            ),
        )

        override suspend fun searchCities(query: String, limit: Int) =
            emptyList<GeocodingResultDto>()
    }

    private companion object {
        fun currentWeatherDto() = CurrentWeatherDto(
            coord = CoordDto(48.86, 2.34),
            weather = listOf(WeatherDescriptionDto(803, "nuageux", "04d")),
            main = MainDto(temp = 22.0, feelsLike = 21.0, humidity = 66),
            dt = 1785024000L,
            sys = SysDto(country = "FR", sunrise = 1785038400L, sunset = 1785096000L),
            timezone = 7200,
            name = "Paris",
        )
    }
}
