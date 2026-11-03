
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

private val AccentBlue = Color(0xFFB9D9FF)
private val BackgroundBlue = Color(0xFF09162D)
private val CardBlue = Color(0xFF243A59)
private val MutedText = Color(0xFFB7CAE3)

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
            Color(0xFF254D81),
            Color(0xFF142B4B),
            BackgroundBlue
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
                Spacer(Modifier.height(28.dp))

                // Encabezado
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "TU PRONÓSTICO",
                            style = MaterialTheme.typography.labelMedium,
                            color = AccentBlue,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(Modifier.height(4.dp))

                        Text(
                            text = "WeatherApp",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Text(
                        text = "🌤️",
                        style = MaterialTheme.typography.headlineLarge
                    )
                }

                Spacer(Modifier.height(24.dp))

                // Buscador
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { query ->
                        showSearch = true
                        weatherViewModel.onSearchQueryChange(query)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text("Buscar ciudad...")
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = AccentBlue
                        )
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
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = CardBlue,
                        unfocusedContainerColor = CardBlue,
                        focusedBorderColor = AccentBlue,
                        unfocusedBorderColor = Color.Transparent,
                        focusedPlaceholderColor = MutedText,
                        unfocusedPlaceholderColor = MutedText,
                        cursorColor = AccentBlue
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

                Spacer(Modifier.height(16.dp))

                // Ubicación GPS
                OutlinedButton(
                    onClick = {
                        keyboardController?.hide()
                        showSearch = false
                        weatherViewModel.clearLocationError()
                        requestCurrentLocation()
                    },
                    enabled = !isLocating,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = AccentBlue,
                        disabledContentColor = MutedText
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        Color(0xFF547AA5)
                    )
                ) {
                    if (isLocating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = AccentBlue
                        )

                        Spacer(Modifier.width(10.dp))

                        Text("Obteniendo ubicación...")
                    } else {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null
                        )

                        Spacer(Modifier.width(8.dp))

                        Text("Usar mi ubicación")
                    }
                }

                if (locationError != null) {
                    Spacer(Modifier.height(10.dp))

                    Text(
                        text = locationError.orEmpty(),
                        color = Color(0xFFFFB4AB),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(Modifier.height(22.dp))

                // Selector de unidades con colores estables
                TemperatureUnitSelector(
                    useFahrenheit = useFahrenheit,
                    onUnitChange = weatherViewModel::setTemperatureUnit
                )

                Spacer(Modifier.height(30.dp))

                when (val state = uiState) {

                    WeatherUiState.Loading -> {
                        Spacer(Modifier.height(50.dp))

                        CircularProgressIndicator(
                            color = AccentBlue
                        )

                        Spacer(Modifier.height(16.dp))

                        Text(
                            text = "Consultando el clima...",
                            color = MutedText
                        )
                    }

                    is WeatherUiState.Error -> {
                        Spacer(Modifier.height(35.dp))

                        Text(
                            text = state.message,
                            color = Color(0xFFFFB4AB),
                            textAlign = TextAlign.Center
                        )

                        Spacer(Modifier.height(16.dp))

                        Button(
                            onClick = weatherViewModel::loadWeather,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AccentBlue,
                                contentColor = BackgroundBlue
                            )
                        ) {
                            Text("Reintentar")
                        }
                    }

                    is WeatherUiState.Success -> {
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

                        Spacer(Modifier.height(30.dp))

                        WeatherDetailsGrid(
                            weather = response.current,
                            useFahrenheit = useFahrenheit
                        )

                        Spacer(Modifier.height(30.dp))

                        HourlyForecastCard(
                            hourly = response.hourly,
                            currentTime = response.current.time,
                            useFahrenheit = useFahrenheit
                        )

                        Spacer(Modifier.height(30.dp))

                        DailyForecastCard(
                            daily = response.daily,
                            useFahrenheit = useFahrenheit
                        )

                        Spacer(Modifier.height(24.dp))

                        OutlinedButton(
                            onClick = weatherViewModel::loadWeather,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = AccentBlue
                            )
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

// Selector personalizado: Celsius / Fahrenheit
@Composable
private fun TemperatureUnitSelector(
    useFahrenheit: Boolean,
    onUnitChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "Unidad de temperatura",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )

            Text(
                text = "Selecciona tu preferencia",
                style = MaterialTheme.typography.bodySmall,
                color = MutedText
            )
        }

        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(CardBlue)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            TemperatureUnitButton(
                text = "°C",
                selected = !useFahrenheit,
                onClick = { onUnitChange(false) }
            )

            TemperatureUnitButton(
                text = "°F",
                selected = useFahrenheit,
                onClick = { onUnitChange(true) }
            )
        }
    }
}

@Composable
private fun TemperatureUnitButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(
                if (selected) AccentBlue else Color.Transparent
            )
            .clickable(
                enabled = !selected,
                onClick = onClick
            )
            .padding(horizontal = 15.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.Bold,
            color = if (selected) {
                BackgroundBlue
            } else {
                MutedText
            }
        )
    }
}

// Clima principal
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
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = AccentBlue,
                modifier = Modifier.size(20.dp)
            )

            Spacer(Modifier.width(5.dp))

            Text(
                text = city,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                color = Color.White
            )
        }

        Spacer(Modifier.height(12.dp))

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
            style = MaterialTheme.typography.titleMedium,
            color = MutedText
        )
    }
}

// Resultados de búsqueda
@Composable
private fun CitySearchResults(
    state: CitySearchState,
    onCitySelected: (City) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardBlue
        )
    ) {
        when (state) {

            CitySearchState.Idle -> Unit

            CitySearchState.Loading -> {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = AccentBlue
                )
            }

            is CitySearchState.Error -> {
                Text(
                    text = state.message,
                    modifier = Modifier.padding(16.dp),
                    color = Color(0xFFFFB4AB)
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
                                    color = MutedText
                                )
                            }

                            HorizontalDivider(
                                color = Color.White.copy(alpha = 0.10f)
                            )
                        }
                    }
                }
            }
        }
    }
}
