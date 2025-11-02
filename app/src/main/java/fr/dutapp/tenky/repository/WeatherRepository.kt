package fr.dutapp.tenky.repository

import android.util.Log
import fr.dutapp.tenky.models.CityWeatherResponse
import fr.dutapp.tenky.models.WeatherResponse
import fr.dutapp.tenky.network.RetrofitClient
import fr.dutapp.tenky.utils.Constants

class WeatherRepository {

    private val apiService = RetrofitClient.weatherApiService

    suspend fun getWeather(
        latitude: Double,
        longitude: Double,
        units: String,
        lang: String
    ): Result<WeatherResponse> {
        return try {
            val response = apiService.getWeatherOneCall(
                latitude = latitude,
                longitude = longitude,
                units = units,
                lang = lang,
                apiKey = Constants.API_KEY
            )
            Result.success(response)
        } catch (e: Exception) {
            Log.e("WeatherRepository", "Error fetching weather", e)
            Result.failure(e)
        }
    }

    suspend fun getCityName(
        latitude: Double,
        longitude: Double,
        units: String,
        lang: String
    ): Result<String> {
        return try {
            val response = apiService.getCityWeatherByCoords(
                latitude = latitude,
                longitude = longitude,
                units = units,
                lang = lang,
                apiKey = Constants.API_KEY
            )
            Result.success(response.name)
        } catch (e: Exception) {
            Log.e("WeatherRepository", "Error fetching city name", e)
            Result.failure(e)
        }
    }

    suspend fun getCityWeather(
        cityName: String,
        units: String,
        lang: String
    ): Result<CityWeatherResponse> {
        return try {
            val response = apiService.getCityWeather(
                cityName = cityName,
                units = units,
                lang = lang,
                apiKey = Constants.API_KEY
            )
            Result.success(response)
        } catch (e: Exception) {
            Log.e("WeatherRepository", "Error fetching city weather", e)
            Result.failure(e)
        }
    }

    suspend fun getHourlyWeather(
        latitude: Double,
        longitude: Double,
        units: String
    ): Result<WeatherResponse> {
        return try {
            val response = apiService.getWeatherOneCall25(
                latitude = latitude,
                longitude = longitude,
                units = units,
                apiKey = Constants.API_KEY
            )
            Result.success(response)
        } catch (e: Exception) {
            Log.e("WeatherRepository", "Error fetching hourly weather", e)
            Result.failure(e)
        }
    }
}
