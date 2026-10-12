package com.example.weatherapp.util

data class WeatherCondition(
    val description: String,
    val icon: String
)

fun getWeatherCondition(
    code: Int,
    isDay: Boolean = true
): WeatherCondition {
    return when (code) {
        0 -> WeatherCondition(
            "Despejado",
            if (isDay) "☀️" else "🌙"
        )

        1 -> WeatherCondition(
            "Mayormente despejado",
            if (isDay) "🌤️" else "🌙"
        )

        2 -> WeatherCondition(
            "Parcialmente nublado",
            "⛅"
        )

        3 -> WeatherCondition("Nublado", "☁️")

        45, 48 -> WeatherCondition("Niebla", "🌫️")

        51, 53, 55 -> WeatherCondition(
            "Llovizna", "🌦️"
        )

        56, 57 -> WeatherCondition(
            "Llovizna helada", "🌧️"
        )

        61, 63, 65 -> WeatherCondition(
            "Lluvia", "🌧️"
        )

        66, 67 -> WeatherCondition(
            "Lluvia helada", "🌧️"
        )

        71, 73, 75, 77 -> WeatherCondition(
            "Nieve", "❄️"
        )

        80, 81, 82 -> WeatherCondition(
            "Chubascos", "🌦️"
        )

        85, 86 -> WeatherCondition(
            "Chubascos de nieve", "🌨️"
        )

        95, 96, 99 -> WeatherCondition(
            "Tormenta", "⛈️"
        )

        else -> WeatherCondition(
            "Sin información", "🌡️"
        )
    }
}
