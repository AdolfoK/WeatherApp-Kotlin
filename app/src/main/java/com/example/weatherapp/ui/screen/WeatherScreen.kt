
package com.example.weatherapp.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.weatherapp.data.model.*
import com.example.weatherapp.ui.viewmodel.*
import com.example.weatherapp.util.getWeatherCondition
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
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
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
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
                val response = state.weather

                CurrentWeatherSection(
                    city = "${selectedCity.name}, ${selectedCity.country}",
                    weather = response.current
                )

                Spacer(Modifier.height(24.dp))

                HorizontalDivider()

                Spacer(Modifier.height(24.dp))

                HourlyForecastSection(
                    hourly = response.hourly,
                    currentTime = response.current.time
                )

                Spacer(Modifier.height(24.dp))

                DailyForecastSection(
                    daily = response.daily
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
private fun CurrentWeatherSection(
    city: String,
    weather: CurrentWeather
) {
    val condition = getWeatherCondition(
        weather.weatherCode,
        weather.isDay == 1
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = city,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = condition.icon,
            style = MaterialTheme.typography.displayLarge
        )

        Text(
            text = "${weather.temperature.format(1)} °C",
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = condition.description,
            style = MaterialTheme.typography.titleMedium
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
    }
}

@Composable
private fun HourlyForecastSection(
    hourly: HourlyWeather,
    currentTime: String
) {
    val currentHour = LocalDateTime
        .parse(currentTime)
        .withMinute(0)
        .withSecond(0)
        .withNano(0)

    val hours = hourly.time.indices.filter { index ->
        val dateTime = LocalDateTime.parse(hourly.time[index])

        !dateTime.isBefore(currentHour) &&
                dateTime.isBefore(currentHour.plusHours(24)) &&
                index < hourly.temperatures.size &&
                index < hourly.weatherCodes.size
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Próximas 24 horas",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(16.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(hours) { index ->
                val dateTime =
                    LocalDateTime.parse(hourly.time[index])

                val condition = getWeatherCondition(
                    hourly.weatherCodes[index]
                )

                ElevatedCard(
                    modifier = Modifier.width(100.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = dateTime.format(
                                DateTimeFormatter.ofPattern("HH:mm")
                            ),
                            style = MaterialTheme.typography.bodySmall
                        )

                        Spacer(Modifier.height(10.dp))

                        Text(
                            text = condition.icon,
                            style = MaterialTheme.typography.headlineMedium
                        )

                        Spacer(Modifier.height(10.dp))

                        Text(
                            text = "${hourly.temperatures[index].format(0)}°",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DailyForecastSection(
    daily: DailyWeather
) {
    val count = minOf(
        daily.time.size,
        daily.maxTemperatures.size,
        daily.minTemperatures.size,
        daily.weatherCodes.size
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Pronóstico de 7 días",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(16.dp))

        repeat(count) { index ->
            val date = LocalDate.parse(daily.time[index])

            val dayName = date.format(
                DateTimeFormatter.ofPattern(
                    "EEEE",
                    Locale("es", "CL")
                )
            ).replaceFirstChar { it.uppercase() }

            val condition = getWeatherCondition(
                daily.weatherCodes[index]
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (index == 0) "Hoy" else dayName,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = condition.icon,
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(Modifier.width(16.dp))

                Text(
                    text = "${daily.minTemperatures[index].format(0)}°",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.width(12.dp))

                Text(
                    text = "${daily.maxTemperatures[index].format(0)}°",
                    fontWeight = FontWeight.Bold
                )
            }

            if (index < count - 1) {
                HorizontalDivider()
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
    String.format(
        Locale.getDefault(),
        "%.${decimals}f",
        this
    )
