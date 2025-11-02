package fr.dutapp.tenky.models

data class DailyWeather(
    val dayName: String,
    val temperature: Int,
    val minMaxTemp: String,
    val weatherIcon: String
)
