package com.example.rygg.core.ui.utils

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Location
import android.location.LocationManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.location.LocationManagerCompat
import com.example.rygg.core.location.RyggLocationManager
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

@Stable
class LocationState internal constructor(
    location: State<Location?>,
    available: State<Boolean>,
    permissionDenied: State<Boolean>,
    private val onRequest: () -> Unit
) {
    val location: Location? by location
    val available: Boolean by available
    val permissionDenied: Boolean by permissionDenied

    val isUnavailable: Boolean get() = permissionDenied || !available

    fun request() = onRequest()
}

// Bridges the (non-injectable) Composable to the shared, Hilt-provided RyggLocationManager.
@EntryPoint
@InstallIn(SingletonComponent::class)
private interface LocationManagerEntryPoint {
    fun ryggLocationManager(): RyggLocationManager
}

@Composable
fun rememberLocationState(): LocationState {
    val context = LocalContext.current
    val locationManager = remember {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            LocationManagerEntryPoint::class.java
        ).ryggLocationManager()
    }

    val location = remember { mutableStateOf<Location?>(null) }
    val available = remember {
        val systemLocationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        mutableStateOf(LocationManagerCompat.isLocationEnabled(systemLocationManager))
    }
    val permissionDenied = remember { mutableStateOf(false) }
    var active by remember { mutableStateOf(false) }

    // Stream fixes from the shared manager only once active (permission granted + requested).
    LaunchedEffect(active) {
        if (active) {
            locationManager.locationUpdates().collect { location.value = it }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        if (grants.values.any { it }) {
            permissionDenied.value = false
            active = true
        } else {
            permissionDenied.value = true
        }
    }

    DisposableEffect(Unit) {
        val systemLocationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                available.value = LocationManagerCompat.isLocationEnabled(systemLocationManager)
            }
        }

        context.registerReceiver(receiver, IntentFilter(LocationManager.MODE_CHANGED_ACTION))

        onDispose {
            context.unregisterReceiver(receiver)
        }
    }

    return remember {
        LocationState(
            location = location,
            available = available,
            permissionDenied = permissionDenied,
            onRequest = {
                if (locationManager.hasLocationPermission()) {
                    permissionDenied.value = false
                    active = true
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
