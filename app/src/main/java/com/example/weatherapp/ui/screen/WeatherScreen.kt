
package com.example.weatherapp.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.weatherapp.data.model.City
import com.example.weatherapp.ui.viewmodel.*
import java.util.Locale

@Composable
fun WeatherScreen(
    weatherViewModel: WeatherViewModel = viewModel()
) {
    val uiState by weatherViewModel.uiState
        .collectAsStateWithLifecycle()

    val searchState by weatherViewModel.searchState
        .collectAsStateWithLifecycle()

    val selectedCity by weatherViewModel.selectedCity
        .collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var showSearch by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "WeatherApp",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(20.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { query ->
                searchQuery = query
                showSearch = true
                weatherViewModel.searchCities(query)
            },
            label = { Text("Buscar ciudad") },
            placeholder = { Text("Ej: Santiago") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        if (showSearch) {
            CitySearchResults(
                state = searchState,
                onCitySelected = { city ->
                    weatherViewModel.selectCity(city)
                    searchQuery = ""
                    showSearch = false
                }
            )
        }

        Spacer(Modifier.height(24.dp))

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
                val weather = state.weather.current

                Text(
                    text = "${selectedCity.name}, ${selectedCity.country}",
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    text = "${weather.temperature.format(1)} °C",
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(24.dp))

                WeatherDetail(
                    "Sensación térmica",
                    "${weather.apparentTemperature.format(1)} °C"
                )

                WeatherDetail(
                    "Humedad",
                    "${weather.humidity}%"
                )

                WeatherDetail(
                    "Viento",
                    "${weather.windSpeed.format(1)} km/h"
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
private fun CitySearchResults(
    state: CitySearchState,
    onCitySelected: (City) -> Unit
) {
    when (state) {

        CitySearchState.Idle -> Unit

        CitySearchState.Loading -> {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth()
            )
        }

        is CitySearchState.Error -> {
            Text(
                text = state.message,
                color = MaterialTheme.colorScheme.error
            )
        }

        is CitySearchState.Success -> {
            if (state.cities.isEmpty()) {
                Text("No se encontraron ciudades.")
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 240.dp)
                ) {
                    items(
                        items = state.cities,
                        key = { it.id }
                    ) { city ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onCitySelected(city)
                                }
                                .padding(12.dp)
                        ) {
                            Text(
                                text = city.name,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = listOfNotNull(
                                    city.region,
                                    city.country
                                ).joinToString(", "),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        HorizontalDivider()
                    }
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
