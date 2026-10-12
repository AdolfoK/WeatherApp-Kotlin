package com.example.weatherapp.data.repository

import com.example.weatherapp.data.model.WeatherResponse
import com.example.weatherapp.data.remote.RetrofitInstance

class WeatherRepository {

    suspend fun getWeather(
        latitude: Double,
        longitude: Double
    ): WeatherResponse {

        return RetrofitInstance.api.getCurrentWeather(
            latitude = latitude,
            longitude = longitude
        )
    }
}
