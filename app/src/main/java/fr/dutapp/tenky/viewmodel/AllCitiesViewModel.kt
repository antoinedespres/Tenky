package fr.dutapp.tenky.viewmodel

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.dutapp.tenky.repository.WeatherRepository
import fr.dutapp.tenky.utils.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

data class CityData(
    val name: String,
    val temperature: String,
    val weatherIcon: String,
    val latitude: Double,
    val longitude: Double
)

class AllCitiesViewModel : ViewModel() {

    private val repository = WeatherRepository()

    private val _cities = MutableStateFlow<List<CityData>>(emptyList())
    val cities: StateFlow<List<CityData>> = _cities.asStateFlow()

    fun loadCities(sharedPreferences: SharedPreferences) {
        viewModelScope.launch {
            val cityNames = mutableListOf<String>()
            val cityCount = sharedPreferences.getInt("nbrCities", 0)

            for (i in 0 until cityCount) {
                val cityName = sharedPreferences.getString("ville$i", "")
                if (!cityName.isNullOrEmpty()) {
                    cityNames.add(cityName)
                }
            }

            val units = Constants.getUnits(sharedPreferences)
            val locale = if (Locale.getDefault().language == "fr") "fr" else "en"

            val citiesData = mutableListOf<CityData>()
            cityNames.forEach { cityName ->
                val result = repository.getCityWeather(cityName, units, locale)
                result.onSuccess { cityWeather ->
                    citiesData.add(
                        CityData(
                            name = cityWeather.name,
                            temperature = "${cityWeather.main.temp.toInt()}°",
                            weatherIcon = "ic_${cityWeather.weather.firstOrNull()?.icon ?: ""}",
                            latitude = cityWeather.coord.lat,
                            longitude = cityWeather.coord.lon
                        )
                    )
                }.onFailure { error ->
                    Log.e("AllCitiesViewModel", "Error loading city: $cityName", error)
                }
            }

            _cities.value = citiesData
        }
    }

    fun addCity(cityName: String, sharedPreferences: SharedPreferences) {
        val editor = sharedPreferences.edit()
        val currentCount = sharedPreferences.getInt("nbrCities", 0)
        editor.putString("ville$currentCount", cityName)
        editor.putInt("nbrCities", currentCount + 1)
        editor.apply()

        loadCities(sharedPreferences)
    }

    fun clearAllCities(sharedPreferences: SharedPreferences) {
        val editor = sharedPreferences.edit()
        val cityCount = sharedPreferences.getInt("nbrCities", 0)

        for (i in 0 until cityCount) {
            editor.remove("ville$i")
        }
        editor.putInt("nbrCities", 0)
        editor.apply()

        _cities.value = emptyList()
    }
}
