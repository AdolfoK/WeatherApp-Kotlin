package com.example.weatherapp.data.model

data class WeatherPreferences(
    val city: City = City(
        id = 0,
        name = "Temuco",
        latitude = -38.7359,
        longitude = -72.5904,
        country = "Chile"
    ),
    val useFahrenheit: Boolean = false
)
