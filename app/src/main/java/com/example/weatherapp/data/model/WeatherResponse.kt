package com.example.weatherapp.data.model

import com.google.gson.annotations.SerializedName

data class WeatherResponse(
    val current: CurrentWeather,
    val hourly: HourlyWeather,
    val daily: DailyWeather
)

data class CurrentWeather(
    @SerializedName("temperature_2m")
    val temperature: Double,

    @SerializedName("relative_humidity_2m")
    val humidity: Int,

    @SerializedName("apparent_temperature")
    val apparentTemperature: Double,

    @SerializedName("wind_speed_10m")
    val windSpeed: Double,

    @SerializedName("weather_code")
    val weatherCode: Int,

    @SerializedName("is_day")
    val isDay: Int,

    val time: String
)

data class HourlyWeather(
    val time: List<String>,

    @SerializedName("temperature_2m")
    val temperatures: List<Double>,

    @SerializedName("weather_code")
    val weatherCodes: List<Int>
)

data class DailyWeather(
    val time: List<String>,

    @SerializedName("temperature_2m_max")
    val maxTemperatures: List<Double>,

    @SerializedName("temperature_2m_min")
    val minTemperatures: List<Double>,

    @SerializedName("weather_code")
    val weatherCodes: List<Int>
)
