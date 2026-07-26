package fr.dutapp.tenky.data

import fr.dutapp.tenky.data.remote.dto.CoordDto
import fr.dutapp.tenky.data.remote.dto.CurrentWeatherDto
import fr.dutapp.tenky.data.remote.dto.ForecastCityDto
import fr.dutapp.tenky.data.remote.dto.ForecastDto
import fr.dutapp.tenky.data.remote.dto.ForecastEntryDto
import fr.dutapp.tenky.data.remote.dto.MainDto
import fr.dutapp.tenky.data.remote.dto.SysDto
import fr.dutapp.tenky.data.remote.dto.WeatherDescriptionDto
import fr.dutapp.tenky.data.remote.dto.WindDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset

/** Two hours ahead of UTC, matching Paris in summer. */
private const val PARIS_OFFSET_SECONDS = 7200

/** 2026-07-26T00:00:00Z, i.e. 02:00 local time in Paris. */
private const val MIDNIGHT_UTC = 1785024000L

private const val ONE_HOUR = 3600L

class WeatherMappersTest {

    @Test
    fun `current weather maps sunrise into the local time of the place`() {
        // 04:00 UTC is 06:00 in Paris.
        val dto = currentWeatherDto(sunrise = MIDNIGHT_UTC + 4 * ONE_HOUR)

        val current = dto.toDomain()

        assertEquals(LocalTime.of(6, 0), current.sunrise)
    }

    @Test
    fun `current weather falls back to a default icon when no condition is returned`() {
        val dto = currentWeatherDto().copy(weather = emptyList())

        val current = dto.toDomain()

        assertEquals("01d", current.iconCode)
        assertEquals(0, current.conditionId)
        assertEquals("", current.description)
    }

    @Test
    fun `current weather tolerates a missing sunrise`() {
        val dto = currentWeatherDto(sunrise = null)

        assertNull(dto.toDomain().sunrise)
    }

    @Test
    fun `hourly takes at most 24 hours of three-hour steps`() {
        // 16 entries is 48 hours; only the first 24 hours should survive.
        val forecast = forecastDto(entryCount = 16)

        val hourly = forecast.toHourly()

        assertEquals(HOURLY_STEP_COUNT, hourly.size)
        assertEquals(LocalTime.of(2, 0), hourly.first().time)
    }

    @Test
    fun `hourly returns everything when fewer steps are available`() {
        val forecast = forecastDto(entryCount = 3)

        assertEquals(3, forecast.toHourly().size)
    }

    @Test
    fun `daily groups three-hour steps by the local calendar day`() {
        // 16 steps of 3h starting at 02:00 local spans 26 July and 27 July.
        val forecast = forecastDto(entryCount = 16)

        val daily = forecast.toDaily()

        assertEquals(
            listOf(LocalDate.of(2026, 7, 26), LocalDate.of(2026, 7, 27)),
            daily.map { it.date },
        )
    }

    @Test
    fun `daily takes the extremes across every step of the day`() {
        val forecast = ForecastDto(
            city = forecastCity(),
            list = listOf(
                // 02:00 local, cold.
                forecastEntry(MIDNIGHT_UTC, temp = 12.0, min = 10.0, max = 13.0),
                // 14:00 local, warm.
                forecastEntry(MIDNIGHT_UTC + 12 * ONE_HOUR, temp = 26.0, min = 24.0, max = 28.0),
            ),
        )

        val day = forecast.toDaily().single()

        assertEquals(10.0, day.minTemperature, 0.001)
        assertEquals(28.0, day.maxTemperature, 0.001)
    }

    @Test
    fun `daily picks the step closest to early afternoon to represent the day`() {
        val forecast = ForecastDto(
            city = forecastCity(),
            list = listOf(
                // 02:00 local — night icon, should not be chosen.
                forecastEntry(MIDNIGHT_UTC, temp = 12.0, icon = "01n"),
                // 14:00 local — closest to 13:00, should win.
                forecastEntry(MIDNIGHT_UTC + 12 * ONE_HOUR, temp = 26.0, icon = "01d"),
                // 23:00 local — night again.
                forecastEntry(MIDNIGHT_UTC + 21 * ONE_HOUR, temp = 15.0, icon = "02n"),
            ),
        )

        val day = forecast.toDaily().single()

        assertEquals("01d", day.iconCode)
        assertEquals(26.0, day.dayTemperature, 0.001)
    }

    @Test
    fun `daily handles a day whose only step is late in the evening`() {
        // The five-day endpoint often starts mid-day, leaving a partial day.
        val forecast = ForecastDto(
            city = forecastCity(),
            list = listOf(
                forecastEntry(MIDNIGHT_UTC + 20 * ONE_HOUR, temp = 18.0, icon = "03n"),
            ),
        )

        val day = forecast.toDaily().single()

        assertEquals("03n", day.iconCode)
        assertEquals(18.0, day.dayTemperature, 0.001)
    }

    @Test
    fun `daily is empty when the forecast carries no entries`() {
        val forecast = ForecastDto(city = forecastCity(), list = emptyList())

        assertTrue(forecast.toDaily().isEmpty())
        assertTrue(forecast.toHourly().isEmpty())
    }

    @Test
    fun `zone offset is derived from the seconds the API reports`() {
        assertEquals(ZoneOffset.ofHours(2), zoneOffsetOf(PARIS_OFFSET_SECONDS))
        assertEquals(ZoneOffset.UTC, zoneOffsetOf(0))
        assertEquals(ZoneOffset.ofHours(-5), zoneOffsetOf(-5 * 3600))
    }

    private fun currentWeatherDto(sunrise: Long? = MIDNIGHT_UTC) = CurrentWeatherDto(
        coord = CoordDto(lat = 48.86, lon = 2.34),
        weather = listOf(WeatherDescriptionDto(id = 803, description = "nuageux", icon = "04d")),
        main = MainDto(temp = 22.0, feelsLike = 21.0, humidity = 66),
        wind = WindDto(speed = 2.2),
        dt = MIDNIGHT_UTC,
        sys = SysDto(country = "FR", sunrise = sunrise, sunset = MIDNIGHT_UTC + 20 * ONE_HOUR),
        timezone = PARIS_OFFSET_SECONDS,
        name = "Paris",
    )

    private fun forecastCity() = ForecastCityDto(
        name = "Paris",
        coord = CoordDto(lat = 48.86, lon = 2.34),
        country = "FR",
        timezone = PARIS_OFFSET_SECONDS,
    )

    private fun forecastDto(entryCount: Int) = ForecastDto(
        city = forecastCity(),
        list = List(entryCount) { index ->
            forecastEntry(MIDNIGHT_UTC + index * 3 * ONE_HOUR, temp = 20.0 + index)
        },
    )

    private fun forecastEntry(
        dt: Long,
        temp: Double,
        min: Double = temp - 2,
        max: Double = temp + 2,
        icon: String = "04d",
    ) = ForecastEntryDto(
        dt = dt,
        main = MainDto(temp = temp, tempMin = min, tempMax = max, humidity = 60),
        weather = listOf(WeatherDescriptionDto(id = 803, description = "nuageux", icon = icon)),
        wind = WindDto(speed = 3.0),
    )
}
