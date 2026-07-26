package fr.dutapp.tenky.widget

import android.content.Context
import fr.dutapp.tenky.AppContainer
import fr.dutapp.tenky.TenkyApplication
import fr.dutapp.tenky.data.local.CachedWeather
import fr.dutapp.tenky.domain.model.SavedCity
import fr.dutapp.tenky.domain.model.TemperatureUnit
import kotlinx.coroutines.flow.first

internal val Context.container: AppContainer
    get() = (applicationContext as TenkyApplication).container

/** What the widget should show, resolved from what the app already knows. */
internal data class WidgetData(
    val city: SavedCity,
    val unit: TemperatureUnit,
    val cached: CachedWeather?,
)

/**
 * Picks the place the widget follows.
 *
 * The device location is deliberately not used: a widget updates in the
 * background, where a location fix is unreliable and expensive. It follows the
 * city selected in the app, falling back to the first saved one.
 */
internal suspend fun Context.resolveWidgetData(): WidgetData? {
    val container = container
    val unit = container.settingsRepository.temperatureUnit.first()
    val city = container.selectedPlaceRepository.selectedCity.first()
        ?: container.savedCitiesRepository.cities.first().firstOrNull()
        ?: return null

    return WidgetData(
        city = city,
        unit = unit,
        cached = container.weatherCache.read(city.coordinates, unit),
    )
}
