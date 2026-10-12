package com.example.weatherapp.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.weatherapp.ui.viewmodel.WeatherUiState
import com.example.weatherapp.ui.viewmodel.WeatherViewModel
import java.util.Locale

@Composable
fun WeatherScreen(
    weatherViewModel: WeatherViewModel = viewModel()
) {
    val uiState by weatherViewModel.uiState
        .collectAsStateWithLifecycle()

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

        Spacer(Modifier.height(32.dp))

        when (val state = uiState) {

            WeatherUiState.Loading -> {
                CircularProgressIndicator()

                Spacer(Modifier.height(12.dp))

                Text("Consultando el clima...")
            }

            is WeatherUiState.Error -> {
                Text(
                    text = state.message,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = weatherViewModel::loadWeather
                ) {
                    Text("Reintentar")
                }
            }

            is WeatherUiState.Success -> {
                val data = state.weather.current

                Text(
                    text = "Temuco, Chile",
                    style = MaterialTheme.typography.headlineSmall
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    text = "${data.temperature.format(1)} °C",
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(24.dp))

                WeatherDetail(
                    "Sensación térmica",
                    "${data.apparentTemperature.format(1)} °C"
                )

                WeatherDetail(
                    "Humedad",
                    "${data.humidity}%"
                )

                WeatherDetail(
                    "Viento",
                    "${data.windSpeed.format(1)} km/h"
                )

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = weatherViewModel::loadWeather
                ) {
                    Text("Actualizar clima")
                }
            }
        }
    }
}

@Composable
private fun WeatherDetail(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label)
        Text(
            text = value,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private fun Double.format(decimals: Int): String =
    String.format(Locale.getDefault(), "%.${decimals}f", this)
