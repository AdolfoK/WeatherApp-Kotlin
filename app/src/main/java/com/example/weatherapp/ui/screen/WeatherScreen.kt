
package com.example.weatherapp.ui.screen

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.weatherapp.data.model.City
import com.example.weatherapp.data.model.CurrentWeather
import com.example.weatherapp.ui.components.DailyForecastCard
import com.example.weatherapp.ui.components.HourlyForecastCard
import com.example.weatherapp.ui.components.WeatherDetailsGrid
import com.example.weatherapp.ui.viewmodel.CitySearchState
import com.example.weatherapp.ui.viewmodel.WeatherUiState
import com.example.weatherapp.ui.viewmodel.WeatherViewModel
import com.example.weatherapp.util.formatTemperature
import com.example.weatherapp.util.getWeatherCondition

@Composable
fun WeatherScreen(
    weatherViewModel: WeatherViewModel = viewModel()
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val uiState by weatherViewModel.uiState
        .collectAsStateWithLifecycle()

    val searchState by weatherViewModel.searchState
        .collectAsStateWithLifecycle()

    val searchQuery by weatherViewModel.searchQuery
        .collectAsStateWithLifecycle()

    val useFahrenheit by weatherViewModel.useFahrenheit
        .collectAsStateWithLifecycle()

    val isLocating by weatherViewModel.isLocating
        .collectAsStateWithLifecycle()

    val locationError by weatherViewModel.locationError
        .collectAsStateWithLifecycle()

    var showSearch by remember {
        mutableStateOf(false)
    }

    // Solicitar permisos de ubicación a Android
    val locationPermissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->

            val fineGranted =
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true

            val coarseGranted =
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

            if (fineGranted || coarseGranted) {
                weatherViewModel.useCurrentLocation()
            } else {
                weatherViewModel.onLocationPermissionDenied()
            }
        }

    // Comprobar si los permisos ya fueron concedidos
    fun requestCurrentLocation() {
        val fineGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (fineGranted || coarseGranted) {
            weatherViewModel.useCurrentLocation()
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

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

                // Buscador de ciudades
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { query ->
                        showSearch = true
                        weatherViewModel.onSearchQueryChange(query)
                    },
                    label = {
                        Text("Buscar ciudad")
                    },
                    placeholder = {
                        Text("Ej: Santiago, Tokio...")
                    },
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

                // Resultados de búsqueda
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

                Spacer(Modifier.height(16.dp))

                // Botón GPS
                OutlinedButton(
                    onClick = {
                        keyboardController?.hide()
                        showSearch = false
                        weatherViewModel.clearLocationError()
                        requestCurrentLocation()
                    },
                    enabled = !isLocating,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isLocating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )

                        Spacer(Modifier.width(10.dp))

                        Text("Obteniendo ubicación...")
                    } else {
                        Text("📍 Usar mi ubicación")
                    }
                }

                // Mensaje de error GPS
                if (locationError != null) {
                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = locationError.orEmpty(),
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(Modifier.height(12.dp))

                // Selector Celsius / Fahrenheit
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "°C",
                        color = if (!useFahrenheit) {
                            Color.White
                        } else {
                            Color.White.copy(alpha = 0.5f)
                        },
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(Modifier.width(8.dp))

                    Switch(
                        checked = useFahrenheit,
                        onCheckedChange = { fahrenheit ->
                            weatherViewModel.setTemperatureUnit(
                                fahrenheit
                            )
                        }
                    )

                    Spacer(Modifier.width(8.dp))

                    Text(
                        text = "°F",
                        color = if (useFahrenheit) {
                            Color.White
                        } else {
                            Color.White.copy(alpha = 0.5f)
                        },
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(Modifier.height(24.dp))

                // Estado del clima
                when (val state = uiState) {

                    WeatherUiState.Loading -> {
                        Spacer(Modifier.height(60.dp))

                        CircularProgressIndicator()

                        Spacer(Modifier.height(16.dp))

                        Text(
                            text = "Consultando el clima...",
                            color = Color.White
                        )
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

                        // Ciudad y pronóstico pertenecen
                        // a la misma respuesta del ViewModel
                        val city = state.city
                        val response = state.weather

                        val cityLabel =
                            if (city.country.isBlank()) {
                                city.name
                            } else {
                                "${city.name}, ${city.country}"
                            }

                        WeatherHero(
                            city = cityLabel,
                            weather = response.current,
                            useFahrenheit = useFahrenheit
                        )

                        Spacer(Modifier.height(32.dp))

                        WeatherDetailsGrid(
                            weather = response.current,
                            useFahrenheit = useFahrenheit
                        )

                        Spacer(Modifier.height(32.dp))

                        HourlyForecastCard(
                            hourly = response.hourly,
                            currentTime = response.current.time,
                            useFahrenheit = useFahrenheit
                        )

                        Spacer(Modifier.height(32.dp))

                        DailyForecastCard(
                            daily = response.daily,
                            useFahrenheit = useFahrenheit
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

// Tarjeta principal del clima
@Composable
private fun WeatherHero(
    city: String,
    weather: CurrentWeather,
    useFahrenheit: Boolean
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
            textAlign = TextAlign.Center,
            color = Color.White
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = condition.icon,
            style = MaterialTheme.typography.displayLarge
        )

        Text(
            text = formatTemperature(
                celsius = weather.temperature,
                useFahrenheit = useFahrenheit
            ),
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Light,
            color = Color.White
        )

        Text(
            text = condition.description,
            style = MaterialTheme.typography.titleLarge,
            color = Color.White
        )
    }
}

// Resultados del buscador de ciudades
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
                        modifier = Modifier.padding(16.dp),
                        color = Color.White
                    )
                } else {
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
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
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
