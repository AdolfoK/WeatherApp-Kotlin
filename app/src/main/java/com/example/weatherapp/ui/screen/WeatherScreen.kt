
package com.example.weatherapp.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.weatherapp.data.model.City
import com.example.weatherapp.data.model.CurrentWeather
import com.example.weatherapp.ui.components.*
import com.example.weatherapp.ui.viewmodel.*
import com.example.weatherapp.util.getWeatherCondition
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

    val searchQuery by weatherViewModel.searchQuery
        .collectAsStateWithLifecycle()

    val keyboardController = LocalSoftwareKeyboardController.current

    var showSearch by remember { mutableStateOf(false) }

    val gradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF234B83),
            Color(0xFF142D53),
            Color(0xFF09162D)
        )
    )

    Scaffold(
        containerColor = Color.Transparent
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(24.dp))

                Text(
                    text = "WeatherApp",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(Modifier.height(24.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { query ->
                        showSearch = true
                        weatherViewModel.onSearchQueryChange(query)
                    },
                    label = { Text("Buscar ciudad") },
                    placeholder = { Text("Ej: Santiago, Tokio...") },
                    singleLine = true,
                    shape = RoundedCornerShape(18.dp),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Search
                    ),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            keyboardController?.hide()
                        }
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFB9D9FF),
                        unfocusedBorderColor =
                            Color.White.copy(alpha = 0.4f),
                        focusedLabelColor = Color.White,
                        unfocusedLabelColor =
                            Color.White.copy(alpha = 0.8f),
                        cursorColor = Color.White
                    )
                )

                if (showSearch && searchQuery.isNotBlank()) {
                    Spacer(Modifier.height(12.dp))

                    CitySearchResults(
                        state = searchState,
                        onCitySelected = { city ->
                            weatherViewModel.selectCity(city)
                            showSearch = false
                            keyboardController?.hide()
                        }
                    )
                }

                Spacer(Modifier.height(32.dp))

                when (val state = uiState) {
                    WeatherUiState.Loading -> {
                        Spacer(Modifier.height(60.dp))
                        CircularProgressIndicator()
                        Spacer(Modifier.height(16.dp))
                        Text("Consultando el clima...")
                    }

                    is WeatherUiState.Error -> {
                        Spacer(Modifier.height(40.dp))

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

                        WeatherHero(
                            city = "${selectedCity.name}, ${selectedCity.country}",
                            weather = response.current
                        )

                        Spacer(Modifier.height(32.dp))

                        WeatherDetailsGrid(
                            weather = response.current
                        )

                        Spacer(Modifier.height(32.dp))

                        HourlyForecastCard(
                            hourly = response.hourly,
                            currentTime = response.current.time
                        )

                        Spacer(Modifier.height(32.dp))

                        DailyForecastCard(
                            daily = response.daily
                        )

                        Spacer(Modifier.height(28.dp))

                        OutlinedButton(
                            onClick = weatherViewModel::loadWeather,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Actualizar clima")
                        }

                        Spacer(Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun WeatherHero(
    city: String,
    weather: CurrentWeather
) {
    val condition = getWeatherCondition(
        weather.weatherCode,
        weather.isDay == 1
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = city,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = condition.icon,
            style = MaterialTheme.typography.displayLarge
        )

        Text(
            text = "${weather.temperature.format(0)}°",
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Light
        )

        Text(
            text = condition.description,
            style = MaterialTheme.typography.titleLarge
        )
    }
}

@Composable
private fun CitySearchResults(
    state: CitySearchState,
    onCitySelected: (City) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF243E63)
        )
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
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.error
                )
            }

            is CitySearchState.Success -> {
                if (state.cities.isEmpty()) {
                    Text(
                        text = "No se encontraron ciudades.",
                        modifier = Modifier.padding(16.dp)
                    )
                } else {
                    // Usamos Column porque la lista está limitada
                    // a 10 resultados y vive dentro de un scroll.
                    Column {
                        state.cities.forEach { city ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onCitySelected(city)
                                    }
                                    .padding(14.dp)
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
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(
                                        alpha = 0.7f
                                    )
                                )
                            }

                            HorizontalDivider(
                                color = Color.White.copy(alpha = 0.1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun Double.format(decimals: Int): String =
    String.format(
        Locale.getDefault(),
        "%.${decimals}f",
        this
    )
