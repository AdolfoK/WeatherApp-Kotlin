package com.example.weatherapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherapp.data.model.City
import com.example.weatherapp.data.model.WeatherResponse
import com.example.weatherapp.data.repository.CityRepository
import com.example.weatherapp.data.repository.WeatherRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

class WeatherViewModel : ViewModel() {

    private val weatherRepository = WeatherRepository()
    private val cityRepository = CityRepository()

    private val _uiState =
        MutableStateFlow<WeatherUiState>(
            WeatherUiState.Loading
        )

    val uiState: StateFlow<WeatherUiState> =
        _uiState.asStateFlow()

    private val _searchState =
        MutableStateFlow<CitySearchState>(
            CitySearchState.Idle
        )

    val searchState: StateFlow<CitySearchState> =
        _searchState.asStateFlow()

    private val _selectedCity = MutableStateFlow(
        City(
            id = 0,
            name = "Temuco",
            latitude = -38.7359,
            longitude = -72.5904,
            country = "Chile"
        )
    )

    val selectedCity: StateFlow<City> =
        _selectedCity.asStateFlow()

    private var searchJob: Job? = null
    private var weatherJob: Job? = null

    init {
        loadWeather()
    }

    fun searchCities(query: String) {
        searchJob?.cancel()

        if (query.trim().length < 2) {
            _searchState.value = CitySearchState.Idle
            return
        }

        searchJob = viewModelScope.launch {
            _searchState.value = CitySearchState.Loading

            try {
                val cities = cityRepository.searchCities(query)

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

    fun selectCity(city: City) {
        searchJob?.cancel()
        _selectedCity.value = city
        _searchState.value = CitySearchState.Idle
        loadWeather()
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

                _uiState.value =
                    WeatherUiState.Success(result)

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
