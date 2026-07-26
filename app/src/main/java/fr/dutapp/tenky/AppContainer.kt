package fr.dutapp.tenky

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import fr.dutapp.tenky.data.WeatherRepository
import fr.dutapp.tenky.data.local.SavedCitiesRepository
import fr.dutapp.tenky.data.local.SelectedPlaceRepository
import fr.dutapp.tenky.data.local.SettingsRepository
import fr.dutapp.tenky.data.remote.ApiKeyInterceptor
import fr.dutapp.tenky.data.remote.OpenWeatherService
import fr.dutapp.tenky.data.remote.installNetworkLogging
import fr.dutapp.tenky.location.LocationProvider
import fr.dutapp.tenky.ui.settings.AppLanguageController
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "tenky")

/**
 * Manual dependency container.
 *
 * The app is small enough that a hand-written container is clearer than a DI
 * framework, and it keeps annotation processing out of the build.
 */
class AppContainer(context: Context) {

    private val appContext = context.applicationContext

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(ApiKeyInterceptor(BuildConfig.OPENWEATHER_API_KEY))
        .installNetworkLogging()
        .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .build()

    private val service: OpenWeatherService = Retrofit.Builder()
        .baseUrl(BuildConfig.OPENWEATHER_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(OpenWeatherService::class.java)

    val weatherRepository = WeatherRepository(
        service = service,
        apiKeyProvider = { BuildConfig.OPENWEATHER_API_KEY },
        ioDispatcher = Dispatchers.IO,
        // Weather descriptions come back in the language the user picked in
        // Settings, not just the device language.
        localeProvider = { AppLanguageController.currentLocale },
    )

    val settingsRepository = SettingsRepository(appContext.dataStore)

    val savedCitiesRepository = SavedCitiesRepository(appContext.dataStore, json)

    val selectedPlaceRepository = SelectedPlaceRepository(appContext.dataStore, json)

    val locationProvider = LocationProvider(appContext)

    private companion object {
        const val TIMEOUT_SECONDS = 15L
    }
}
