package com.example.weatherapp.data.model

import com.google.gson.annotations.SerializedName

data class CityResponse(
    val results: List<City>? = null
)

data class City(
    val id: Int,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val country: String,

    @SerializedName("admin1")
    val region: String? = null
)
