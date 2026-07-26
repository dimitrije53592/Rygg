package com.example.rygg.core.ui.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

@Stable
class LocationState internal constructor(
    location: State<Location?>,
    private val onRequest: () -> Unit
) {
    val location: Location? by location
    fun request() = onRequest()
}

@Composable
fun rememberLocationState(): LocationState {
    val context = LocalContext.current
    val fusedClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val location = remember { mutableStateOf<Location?>(null) }

    val callback = remember {
        object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location.value = it }
            }
        }
    }

    val request = remember {
        LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, UPDATE_INTERVAL_MS)
            .setMinUpdateDistanceMeters(MIN_DISTANCE_M)
            .build()
    }

    @SuppressLint("MissingPermission")
    fun startUpdates() {
        if (!context.hasLocationPermission()) return
        fusedClient.lastLocation.addOnSuccessListener { fix -> fix?.let { location.value = it } }
        fusedClient.removeLocationUpdates(callback)
        fusedClient.requestLocationUpdates(request, callback, Looper.getMainLooper())
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        if (grants.values.any { it }) startUpdates()
    }

    DisposableEffect(Unit) {
        onDispose { fusedClient.removeLocationUpdates(callback) }
    }

    return remember {
        LocationState(
            location = location,
            onRequest = {
                if (context.hasLocationPermission()) {
                    startUpdates()
                } else {
                    permissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }
            }
        )
    }
}

private fun Context.hasLocationPermission(): Boolean =
    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED

private const val UPDATE_INTERVAL_MS = 1000L
private const val MIN_DISTANCE_M = 1f
