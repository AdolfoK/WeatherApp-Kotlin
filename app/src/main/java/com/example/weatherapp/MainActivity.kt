
package com.example.weatherapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.weatherapp.ui.screen.WeatherScreen
import com.example.weatherapp.ui.theme.WeatherTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            WeatherTheme {
                WeatherScreen()
            }
        }
    }
}
