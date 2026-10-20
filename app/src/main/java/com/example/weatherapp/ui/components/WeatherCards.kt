
package com.example.weatherapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.weatherapp.data.model.CurrentWeather
import com.example.weatherapp.data.model.DailyWeather
import com.example.weatherapp.data.model.HourlyWeather
import com.example.weatherapp.util.formatTemperature
import com.example.weatherapp.util.getWeatherCondition
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

// Tarjeta reutilizable para mostrar un dato del clima
@Composable
fun WeatherInfoCard(
    title: String,
    value: String,
    icon: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.10f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = icon,
                style = MaterialTheme.typography.headlineMedium
            )

            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.75f)
            )

            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

// Tarjetas de sensación térmica, humedad y viento
@Composable
fun WeatherDetailsGrid(
    weather: CurrentWeather,
    useFahrenheit: Boolean
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            WeatherInfoCard(
                title = "Sensación térmica",
                value = formatTemperature(
                    celsius = weather.apparentTemperature,
                    useFahrenheit = useFahrenheit,
                    decimals = 1
                ),
                icon = "🌡️",
                modifier = Modifier.weight(1f)
            )

            WeatherInfoCard(
                title = "Humedad",
                value = "${weather.humidity}%",
                icon = "💧",
                modifier = Modifier.weight(1f)
            )
        }

        WeatherInfoCard(
            title = "Velocidad del viento",
            value = "${String.format(Locale.getDefault(), "%.1f", weather.windSpeed)} km/h",
            icon = "💨",
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// Pronóstico meteorológico de las próximas 24 horas
@Composable
fun HourlyForecastCard(
    hourly: HourlyWeather,
    currentTime: String,
    useFahrenheit: Boolean
) {
    val currentHour = LocalDateTime.parse(currentTime)
        .withMinute(0)
        .withSecond(0)
        .withNano(0)

    // Seleccionamos únicamente las próximas 24 horas
    val hours = hourly.time.indices.filter { index ->
        val hour = LocalDateTime.parse(hourly.time[index])

        !hour.isBefore(currentHour) &&
                hour.isBefore(currentHour.plusHours(24)) &&
                index < hourly.temperatures.size &&
                index < hourly.weatherCodes.size
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Próximas 24 horas",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(hours) { index ->

                val hour = LocalDateTime.parse(
                    hourly.time[index]
                )

                val condition = getWeatherCondition(
                    code = hourly.weatherCodes[index],
                    isDay = hour.hour in 7..18
                )

                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(
                            alpha = 0.10f
                        )
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .width(100.dp)
                            .padding(vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = hour.format(
                                DateTimeFormatter.ofPattern("HH:mm")
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )

                        Text(
                            text = condition.icon,
                            style = MaterialTheme.typography.headlineMedium
                        )

                        Text(
                            text = formatTemperature(
                                celsius = hourly.temperatures[index],
                                useFahrenheit = useFahrenheit
                            ),
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

// Pronóstico meteorológico de los próximos siete días
@Composable
fun DailyForecastCard(
    daily: DailyWeather,
    useFahrenheit: Boolean
) {
    // Evita acceder a posiciones que no existan
    val count = minOf(
        daily.time.size,
        daily.maxTemperatures.size,
        daily.minTemperatures.size,
        daily.weatherCodes.size
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Pronóstico de 7 días",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.10f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                repeat(count) { index ->

                    val date = LocalDate.parse(
                        daily.time[index]
                    )

                    val dayName = date.format(
                        DateTimeFormatter.ofPattern(
                            "EEE",
                            Locale.forLanguageTag("es-CL")
                        )
                    ).replaceFirstChar {
                        it.uppercase()
                    }

                    val condition = getWeatherCondition(
                        daily.weatherCodes[index]
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Día de la semana
                        Text(
                            text = if (index == 0) {
                                "Hoy"
                            } else {
                                dayName
                            },
                            modifier = Modifier.weight(1f),
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )

                        // Icono del clima
                        Text(
                            text = condition.icon,
                            style = MaterialTheme.typography.titleLarge
                        )

                        Spacer(Modifier.width(16.dp))

                        // Temperatura mínima
                        Text(
                            text = formatTemperature(
                                celsius = daily.minTemperatures[index],
                                useFahrenheit = useFahrenheit
                            ),
                            color = Color.White.copy(alpha = 0.65f)
                        )

                        Spacer(Modifier.width(12.dp))

                        // Temperatura máxima
                        Text(
                            text = formatTemperature(
                                celsius = daily.maxTemperatures[index],
                                useFahrenheit = useFahrenheit
                            ),
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    if (index < count - 1) {
                        HorizontalDivider(
                            color = Color.White.copy(alpha = 0.12f)
                        )
                    }
                }
            }
        }
    }
}
