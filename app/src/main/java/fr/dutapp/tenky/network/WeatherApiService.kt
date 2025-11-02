package fr.dutapp.tenky.network

import fr.dutapp.tenky.models.CityWeatherResponse
import fr.dutapp.tenky.models.WeatherResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {

    @GET("data/3.0/onecall")
    suspend fun getWeatherOneCall(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("units") units: String,
        @Query("lang") lang: String,
        @Query("appid") apiKey: String
    ): WeatherResponse

    @GET("data/2.5/onecall")
    suspend fun getWeatherOneCall25(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("units") units: String,
        @Query("appid") apiKey: String
    ): WeatherResponse

    @GET("data/2.5/weather")
    suspend fun getCityWeather(
        @Query("q") cityName: String,
        @Query("units") units: String,
        @Query("lang") lang: String,
        @Query("appid") apiKey: String
    ): CityWeatherResponse

    @GET("data/2.5/weather")
    suspend fun getCityWeatherByCoords(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("units") units: String,
        @Query("lang") lang: String,
        @Query("appid") apiKey: String
    ): CityWeatherResponse
}
