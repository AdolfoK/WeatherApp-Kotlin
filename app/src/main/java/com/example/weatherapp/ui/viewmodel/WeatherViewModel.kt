package com.example.weatherapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherapp.data.model.WeatherResponse
import com.example.weatherapp.data.repository.WeatherRepository
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

class WeatherViewModel : ViewModel() {

    private val repository = WeatherRepository()

    private val _uiState =
        MutableStateFlow<WeatherUiState>(
            WeatherUiState.Loading
        )

    val uiState: StateFlow<WeatherUiState> =
        _uiState.asStateFlow()

    init {
        loadWeather()
    }

    fun loadWeather() {
        viewModelScope.launch {
            _uiState.value = WeatherUiState.Loading

            try {
                val result = repository.getWeather(
                    latitude = -38.7359,
                    longitude = -72.5904
                )

                _uiState.value =
                    WeatherUiState.Success(result)

            } catch (e: Exception) {
                _uiState.value = WeatherUiState.Error(
                    "No se pudo obtener el clima. " +
                            "Comprueba tu conexión a internet."
                )
            }
        }
    }
}
