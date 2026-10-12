
package com.example.weatherapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

data class WeatherData(
    val temperature: Double,
    val humidity: Int,
    val apparentTemperature: Double,
    val windSpeed: Double
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                WeatherScreen()
            }
        }
    }
}

@Composable
fun WeatherScreen() {
    var weather by remember { mutableStateOf<WeatherData?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var reloadKey by remember { mutableIntStateOf(0) }

    LaunchedEffect(reloadKey) {
        isLoading = true
        error = null

        try {
            weather = fetchWeather()
        } catch (e: Exception) {
            error = "No se pudo consultar el clima. Revisa tu conexión."
        } finally {
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "WeatherApp",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(32.dp))

        when {
            isLoading -> {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(12.dp))
                Text("Consultando el clima...")
            }

            error != null -> {
                Text(
                    text = error ?: "",
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { reloadKey++ }) {
                    Text("Reintentar")
                }
            }

            weather != null -> {
                val data = weather!!

                Text(
                    text = "Temuco, Chile",
                    style = MaterialTheme.typography.headlineSmall
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "${data.temperature.format(1)} °C",
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(24.dp))

                WeatherDetail(
                    "Sensación térmica",
                    "${data.apparentTemperature.format(1)} °C"
                )
                WeatherDetail("Humedad", "${data.humidity}%")
                WeatherDetail(
                    "Viento",
                    "${data.windSpeed.format(1)} km/h"
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(onClick = { reloadKey++ }) {
                    Text("Actualizar clima")
                }
            }
        }
    }
}

@Composable
fun WeatherDetail(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label)
        Text(value, fontWeight = FontWeight.SemiBold)
    }
}

fun Double.format(decimals: Int): String =
    String.format(Locale.getDefault(), "%.${decimals}f", this)

suspend fun fetchWeather(): WeatherData =
    withContext(Dispatchers.IO) {
        val endpoint =
            "https://api.open-meteo.com/v1/forecast" +
                    "?latitude=-38.7359" +
                    "&longitude=-72.5904" +
                    "&current=temperature_2m,relative_humidity_2m," +
                    "apparent_temperature,wind_speed_10m" +
                    "&timezone=America%2FSantiago"

        val connection =
            URL(endpoint).openConnection() as HttpURLConnection

        try {
            connection.requestMethod = "GET"
            connection.connectTimeout = 10000
            connection.readTimeout = 10000

            if (connection.responseCode !in 200..299) {
                throw Exception("HTTP ${connection.responseCode}")
            }

            val response = connection.inputStream
                .bufferedReader()
                .use { it.readText() }

            val current = JSONObject(response)
                .getJSONObject("current")

            WeatherData(
                temperature = current.getDouble("temperature_2m"),
                humidity = current.getInt("relative_humidity_2m"),
                apparentTemperature = current.getDouble("apparent_temperature"),
                windSpeed = current.getDouble("wind_speed_10m")
            )
        } finally {
            connection.disconnect()
        }
    }
