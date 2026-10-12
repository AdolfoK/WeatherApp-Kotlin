
package com.example.weatherapp.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    private const val WEATHER_URL =
        "https://api.open-meteo.com/"

    private const val GEOCODING_URL =
        "https://geocoding-api.open-meteo.com/"

    private fun createRetrofit(baseUrl: String): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(
                GsonConverterFactory.create()
            )
            .build()
    }

    val api: WeatherApi by lazy {
        createRetrofit(WEATHER_URL)
            .create(WeatherApi::class.java)
    }

    val geocodingApi: GeocodingApi by lazy {
        createRetrofit(GEOCODING_URL)
            .create(GeocodingApi::class.java)
    }
}
