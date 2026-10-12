package com.example.weatherapp.data.repository

import com.example.weatherapp.data.model.City
import com.example.weatherapp.data.remote.RetrofitInstance

class CityRepository {

    suspend fun searchCities(query: String): List<City> {
        val response = RetrofitInstance.geocodingApi
            .searchCities(name = query.trim())

        return response.results.orEmpty()
    }
}
