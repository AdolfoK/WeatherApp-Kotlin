
package com.example.weatherapp.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherapp.data.model.City
import com.example.weatherapp.data.model.WeatherResponse
import com.example.weatherapp.data.repository.CityRepository
import com.example.weatherapp.data.repository.LocationRepository
import com.example.weatherapp.data.repository.PreferencesRepository
import com.example.weatherapp.data.repository.WeatherRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed interface WeatherUiState {
    data object Loading : WeatherUiState

    data class Success(
        val weather: WeatherResponse
    ) : WeatherUiState

    data class Error(
        val message: String
    ) : WeatherUiState
}

sealed interface CitySearchState {
    data object Idle : CitySearchState
    data object Loading : CitySearchState

    data class Success(
        val cities: List<City>
    ) : CitySearchState

    data class Error(
        val message: String
    ) : CitySearchState
}

@OptIn(FlowPreview::class)
class WeatherViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val weatherRepository = WeatherRepository()
    private val cityRepository = CityRepository()
    private val locationRepository = LocationRepository(application)
    private val preferencesRepository = PreferencesRepository(application)

    private val _uiState =
        MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _searchState =
        MutableStateFlow<CitySearchState>(CitySearchState.Idle)
    val searchState = _searchState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedCity = MutableStateFlow(
        City(
            id = 0,
            name = "Temuco",
            latitude = -38.7359,
            longitude = -72.5904,
            country = "Chile"
        )
    )
    val selectedCity = _selectedCity.asStateFlow()

    private val _useFahrenheit = MutableStateFlow(false)
    val useFahrenheit = _useFahrenheit.asStateFlow()

    private val _isLocating = MutableStateFlow(false)
    val isLocating = _isLocating.asStateFlow()

    private val _locationError = MutableStateFlow<String?>(null)
    val locationError = _locationError.asStateFlow()

    private var weatherJob: Job? = null
    private var locationJob: Job? = null

    init {
        observeSearch()
        loadPreferences()
    }

    private fun loadPreferences() {
        viewModelScope.launch {
            try {
                val preferences =
                    preferencesRepository.preferences.first()

                _selectedCity.value = preferences.city
                _useFahrenheit.value = preferences.useFahrenheit

                loadWeather()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = WeatherUiState.Error(
                    "No se pudieron recuperar las preferencias."
                )
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query

        _searchState.value =
            if (query.trim().length < 2) {
                CitySearchState.Idle
            } else {
                CitySearchState.Loading
            }
    }

    private fun observeSearch() {
        viewModelScope.launch {
            _searchQuery
                .debounce(400)
                .distinctUntilChanged()
                .collectLatest { query ->

                    if (query.trim().length < 2) {
                        _searchState.value = CitySearchState.Idle
                        return@collectLatest
                    }

                    _searchState.value = CitySearchState.Loading

                    try {
                        val cities =
                            cityRepository.searchCities(query)

                        _searchState.value =
                            CitySearchState.Success(cities)
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        _searchState.value = CitySearchState.Error(
                            "No se pudieron buscar las ciudades."
                        )
                    }
                }
        }
    }

    fun selectCity(city: City) {
        locationJob?.cancel()
        _isLocating.value = false
        _locationError.value = null

        _searchQuery.value = ""
        _searchState.value = CitySearchState.Idle
        _selectedCity.value = city

        loadWeather()
        saveCity(city)
    }

    private fun saveCity(city: City) {
        viewModelScope.launch {
            try {
                preferencesRepository.saveCity(city)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("WeatherApp", "Error al guardar ciudad", e)
            }
        }
    }

    fun setTemperatureUnit(fahrenheit: Boolean) {
        _useFahrenheit.value = fahrenheit

        viewModelScope.launch {
            try {
                preferencesRepository.saveTemperatureUnit(fahrenheit)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("WeatherApp", "Error al guardar unidad", e)
            }
        }
    }

    fun onLocationPermissionDenied() {
        _locationError.value =
            "Permiso de ubicación denegado. Puedes seguir buscando ciudades."
    }

    fun clearLocationError() {
        _locationError.value = null
    }

    // Llamar únicamente después de comprobar los permisos.
    fun useCurrentLocation() {
        if (_isLocating.value) return

        locationJob?.cancel()

        locationJob = viewModelScope.launch {
            _isLocating.value = true
            _locationError.value = null

            try {
                val location = locationRepository.getCurrentLocation()

                // No suponemos que las coordenadas corresponden
                // a una ciudad determinada.
                val city = City(
                    id = -1,
                    name = "Mi ubicación",
                    latitude = location.latitude,
                    longitude = location.longitude,
                    country = ""
                )

                _selectedCity.value = city
                _searchQuery.value = ""
                _searchState.value = CitySearchState.Idle

                loadWeather()
                saveCity(city)

            } catch (e: CancellationException) {
                throw e
            } catch (e: SecurityException) {
                _locationError.value =
                    "No hay permisos para acceder a la ubicación."
            } catch (e: Exception) {
                Log.e("WeatherApp", "Error de ubicación", e)
                _locationError.value =
                    "No se pudo obtener tu ubicación. " +
                            "Comprueba que la ubicación del dispositivo esté activada."
            } finally {
                _isLocating.value = false
            }
        }
    }

    fun loadWeather() {
        weatherJob?.cancel()

        weatherJob = viewModelScope.launch {
            _uiState.value = WeatherUiState.Loading

            try {
                val city = _selectedCity.value

                val result = weatherRepository.getWeather(
                    latitude = city.latitude,
                    longitude = city.longitude
                )

                _uiState.value = WeatherUiState.Success(result)

            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = WeatherUiState.Error(
                    "No se pudo obtener el clima. " +
                            "Comprueba tu conexión a internet."
                )
            }
        }
    }
}
