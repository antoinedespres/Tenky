# Tenky

A small Android weather app: current conditions, the next 24 hours, and a
five-day forecast for your location or any city you pin.

## Setup

The app needs a free OpenWeather API key. Keys are **not** committed — they are
read from `local.properties`, which is git-ignored.

1. Create a free account and generate a key at
   [openweathermap.org/api_keys](https://home.openweathermap.org/api_keys).
2. Copy `local.properties.example` to `local.properties` (Android Studio
   usually creates the file for you with just `sdk.dir`).
3. Add your key:

   ```properties
   OPENWEATHER_API_KEY=your_api_key_here
   ```

4. Build and run.

A new key can take around ten minutes to activate. Until then the API returns
401 and the app shows "The OpenWeather API key was rejected".

CI can supply the key through an `OPENWEATHER_API_KEY` environment variable
instead. If neither is set the project still builds, and the app shows a
"no API key configured" message rather than failing at compile time.

> **Note on client-side keys.** The key is injected into `BuildConfig` at build
> time, which keeps it out of version control but *not* out of the APK — anyone
> can extract it from a shipped binary. That is inherent to calling a third
> party API directly from a mobile client. Hiding it properly would require
> proxying requests through a backend you control.

## Which OpenWeather endpoints this uses

| Endpoint | Purpose |
| --- | --- |
| `data/2.5/weather` | Current conditions |
| `data/2.5/forecast` | Five-day forecast in three-hour steps |
| `geo/1.0/direct` | City search / geocoding |

All three are on the free tier. The app deliberately avoids **One Call**:
version 2.5 was switched off in June 2024, and version 3.0 requires a separate
paid "One Call by Call" subscription. Both return HTTP 401 on a free key.

Because the free forecast endpoint returns three-hour steps over five days, the
hourly strip shows three-hour intervals and the daily list covers five days,
aggregated from those steps.

## Architecture

Single-Activity Jetpack Compose app, roughly MVVM:

```
fr.dutapp.tenky
├── data
│   ├── remote      Retrofit service, DTOs, API-key interceptor
│   ├── local       DataStore: settings, saved cities, selected place
│   ├── WeatherMappers.kt    DTO → domain, three-hourly → daily aggregation
│   └── WeatherRepository.kt Returns DataResult instead of throwing
├── domain/model    Domain types and the DataResult/WeatherError pair
├── location        LocationManager wrapper (no Play Services)
├── ui              Compose screens + ViewModels, theme, formatting
└── AppContainer.kt Hand-written dependency container
```

Notes on a couple of deliberate choices:

- **No Play Services.** Location goes through the platform `LocationManager`
  so the app works on devices without Google Play Services.
- **No DI framework.** `AppContainer` is small enough to read in one sitting
  and keeps annotation processing out of the build.

## Development

```bash
./gradlew assembleDebug     # build
./gradlew testDebugUnitTest # unit tests
./gradlew assembleRelease   # minified release build
```

Requires JDK 21. Kotlin comes from AGP 9's built-in Kotlin support, so there is
no `org.jetbrains.kotlin.android` plugin in the build files.

## Languages

The app ships English, French and Korean, and offers an in-app language picker
under Settings. It uses `AppCompatDelegate.setApplicationLocales`, so on Android
13 and later the choice is also exposed in Settings → Apps → Tenky → Language.

Adding a language means touching four places: a `values-<code>/strings.xml`, the
`AppLanguage` enum, `res/xml/locales_config.xml`, and `localeFilters` in
`app/build.gradle.kts`.

Note that OpenWeather's `lang` codes are not all ISO 639-1 — Korean is `kr`, not
`ko`. An unrecognised code is not an error; the API just answers in English. See
`toOpenWeatherLanguage()`.

## Credits

Weather icon pack by Those Icons —
[flaticon.com/packs/weather-76](https://www.flaticon.com/packs/weather-76).
