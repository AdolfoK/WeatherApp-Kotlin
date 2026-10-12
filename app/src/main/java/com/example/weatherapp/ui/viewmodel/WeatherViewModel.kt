
package com.example.weatherapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherapp.data.model.City
import com.example.weatherapp.data.model.WeatherResponse
import com.example.weatherapp.data.repository.CityRepository
import com.example.weatherapp.data.repository.WeatherRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
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
class WeatherViewModel : ViewModel() {

    private val weatherRepository = WeatherRepository()
    private val cityRepository = CityRepository()

    private val _uiState =
        MutableStateFlow<WeatherUiState>(
            WeatherUiState.Loading
        )

    val uiState = _uiState.asStateFlow()

    private val _searchState =
        MutableStateFlow<CitySearchState>(
            CitySearchState.Idle
        )

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

    private var weatherJob: Job? = null

    init {
        observeSearch()
        loadWeather()
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query

        _searchState.value = if (query.trim().length < 2) {
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
        _searchQuery.value = ""
        _searchState.value = CitySearchState.Idle
        _selectedCity.value = city
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
