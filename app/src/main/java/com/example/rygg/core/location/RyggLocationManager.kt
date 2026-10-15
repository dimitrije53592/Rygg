package com.example.rygg.core.location

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import androidx.core.location.LocationManagerCompat
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

// One wrapper around FusedLocationProviderClient shared by the map UI and the recording
// session, so GPS acquisition and the permission check live in a single place.
@Singleton
class RyggLocationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val fusedClient = LocationServices.getFusedLocationProviderClient(context)
    private val settingsClient = LocationServices.getSettingsClient(context)
    private val systemLocationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    // Cold stream of fixes: requests updates on collect, removes them on cancellation. Closes
    // immediately without permission so callers observe an empty stream rather than a crash.
    @SuppressLint("MissingPermission")
    fun locationUpdates(
        minDistanceMeters: Float = DEFAULT_MIN_DISTANCE_M,
        intervalMs: Long = DEFAULT_INTERVAL_MS
    ): Flow<Location> = callbackFlow {
        if (!hasLocationPermission()) {
            close()
            return@callbackFlow
        }
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, intervalMs)
            .setMinUpdateDistanceMeters(minDistanceMeters)
            .build()
        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { trySend(it) }
            }
        }
        fusedClient.lastLocation.addOnSuccessListener { fix -> fix?.let { trySend(it) } }
        fusedClient.requestLocationUpdates(request, callback, Looper.getMainLooper())
        awaitClose { fusedClient.removeLocationUpdates(callback) }
    }

    fun hasLocationPermission(): Boolean = context.locationPermission() != LocationPermission.NONE

    fun isLocationEnabled(): Boolean = LocationManagerCompat.isLocationEnabled(systemLocationManager)

    fun observeLocationEnabled(): Flow<Boolean> = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                trySend(isLocationEnabled())
            }
        }
        trySend(isLocationEnabled())
        context.registerReceiver(receiver, IntentFilter(LocationManager.MODE_CHANGED_ACTION))
        awaitClose { context.unregisterReceiver(receiver) }
    }.distinctUntilChanged()

    suspend fun checkLocationSettings(): LocationSettingsCheck = try {
        val request = LocationSettingsRequest.Builder()
            .addLocationRequest(LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, DEFAULT_INTERVAL_MS).build())
            .build()
        settingsClient.checkLocationSettings(request).await()
        LocationSettingsCheck.Satisfied
    } catch (e: ResolvableApiException) {
        LocationSettingsCheck.Resolvable(e.resolution)
    } catch (e: ApiException) {
        LocationSettingsCheck.Unresolvable
    }

    private companion object {
        const val DEFAULT_INTERVAL_MS = 1_000L
        const val DEFAULT_MIN_DISTANCE_M = 1f
    }
}

sealed interface LocationSettingsCheck {
    data object Satisfied : LocationSettingsCheck

    data class Resolvable(val resolution: PendingIntent) : LocationSettingsCheck

    data object Unresolvable : LocationSettingsCheck
}
