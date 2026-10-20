package com.example.weatherapp.data.repository

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class LocationRepository(context: Context) {

    private val locationClient =
        LocationServices.getFusedLocationProviderClient(
            context.applicationContext
        )

    // El llamador debe comprobar los permisos antes de usarla.
    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Location =
        suspendCancellableCoroutine { continuation ->

            val tokenSource = CancellationTokenSource()

            locationClient.getCurrentLocation(
                Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                tokenSource.token
            ).addOnSuccessListener { location ->

                if (continuation.isActive) {
                    if (location != null) {
                        continuation.resume(location)
                    } else {
                        continuation.resumeWithException(
                            IllegalStateException(
                                "No se pudo determinar la ubicación."
                            )
                        )
                    }
                }

            }.addOnFailureListener { exception ->

                if (continuation.isActive) {
                    continuation.resumeWithException(exception)
                }
            }

            continuation.invokeOnCancellation {
                tokenSource.cancel()
            }
        }
}
