package com.example.weatherapp.data.repository

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.weatherapp.data.model.City
import com.example.weatherapp.data.model.WeatherPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.weatherDataStore by preferencesDataStore(
    name = "weather_preferences"
)

class PreferencesRepository(
    context: Context
) {
    private val dataStore = context.applicationContext.weatherDataStore

    private object Keys {
        val CITY_ID = intPreferencesKey("city_id")
        val CITY_NAME = stringPreferencesKey("city_name")
        val CITY_LATITUDE = doublePreferencesKey("city_latitude")
        val CITY_LONGITUDE = doublePreferencesKey("city_longitude")
        val CITY_COUNTRY = stringPreferencesKey("city_country")
        val CITY_REGION = stringPreferencesKey("city_region")
        val USE_FAHRENHEIT = booleanPreferencesKey("use_fahrenheit")
    }

    val preferences: Flow<WeatherPreferences> =
        dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { values ->
                val defaults = WeatherPreferences()

                WeatherPreferences(
                    city = City(
                        id = values[Keys.CITY_ID]
                            ?: defaults.city.id,
                        name = values[Keys.CITY_NAME]
                            ?: defaults.city.name,
                        latitude = values[Keys.CITY_LATITUDE]
                            ?: defaults.city.latitude,
                        longitude = values[Keys.CITY_LONGITUDE]
                            ?: defaults.city.longitude,
                        country = values[Keys.CITY_COUNTRY]
                            ?: defaults.city.country,
                        region = values[Keys.CITY_REGION]
                    ),
                    useFahrenheit =
                        values[Keys.USE_FAHRENHEIT] ?: false
                )
            }

    suspend fun saveCity(city: City) {
        dataStore.edit { values ->
            values[Keys.CITY_ID] = city.id
            values[Keys.CITY_NAME] = city.name
            values[Keys.CITY_LATITUDE] = city.latitude
            values[Keys.CITY_LONGITUDE] = city.longitude
            values[Keys.CITY_COUNTRY] = city.country

            if (city.region != null) {
                values[Keys.CITY_REGION] = city.region
            } else {
                values.remove(Keys.CITY_REGION)
            }
        }
    }

    suspend fun saveTemperatureUnit(useFahrenheit: Boolean) {
        dataStore.edit { values ->
            values[Keys.USE_FAHRENHEIT] = useFahrenheit
        }
    }
}
