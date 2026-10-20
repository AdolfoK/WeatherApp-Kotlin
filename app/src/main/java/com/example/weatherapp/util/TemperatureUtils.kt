package com.example.weatherapp.util

import java.util.Locale

fun formatTemperature(
    celsius: Double,
    useFahrenheit: Boolean,
    decimals: Int = 0
): String {
    val value = if (useFahrenheit) {
        celsius * 1.8 + 32.0
    } else {
        celsius
    }

    val unit = if (useFahrenheit) "°F" else "°C"

    return String.format(
        Locale.getDefault(),
        "%.${decimals}f",
        value
    ) + unit
}
