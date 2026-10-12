package com.example.weatherapp.data.remote

import com.example.weatherapp.data.model.CityResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface GeocodingApi {

    @GET("v1/search")
    suspend fun searchCities(
        @Query("name") name: String,
        @Query("count") count: Int = 10,
        @Query("language") language: String = "es",
        @Query("format") format: String = "json"
    ): CityResponse
}
