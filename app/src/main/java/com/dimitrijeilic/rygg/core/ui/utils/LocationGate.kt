package com.dimitrijeilic.rygg.core.ui.utils

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.location.Location
import android.provider.Settings
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.dimitrijeilic.rygg.core.location.LocationPermission
import com.dimitrijeilic.rygg.core.location.LocationPermissionStore
import com.dimitrijeilic.rygg.core.location.LocationPurpose
import com.dimitrijeilic.rygg.core.location.LocationReadiness
import com.dimitrijeilic.rygg.core.location.LocationSettingsCheck
import com.dimitrijeilic.rygg.core.location.RyggLocationManager
import com.dimitrijeilic.rygg.core.location.locationPermission
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.launch

@Stable
class LocationGate internal constructor(
    location: State<Location?>,
    permission: State<LocationPermission>,
    servicesOn: State<Boolean>,
    prompt: State<LocationPrompt?>,
    private val evaluate: (LocationPurpose) -> LocationReadiness,
    private val onRequestAccess: (LocationPurpose, () -> Unit) -> Unit,
    private val onResolvePrompt: () -> Unit,
    private val onDismissPrompt: () -> Unit
) {
    val location: Location? by location
    val prompt: LocationPrompt? by prompt
    private val permission: LocationPermission by permission
    private val servicesOn: Boolean by servicesOn

    val hasSignal: Boolean get() = permission != LocationPermission.NONE && servicesOn

    fun readinessFor(purpose: LocationPurpose): LocationReadiness = evaluate(purpose)

    fun requestAccess(purpose: LocationPurpose, onReady: () -> Unit = {}) = onRequestAccess(purpose, onReady)

    fun resolvePrompt() = onResolvePrompt()

    fun dismissPrompt() = onDismissPrompt()
}

// Bridges the (non-injectable) Composable to the shared, Hilt-provided RyggLocationManager.
@EntryPoint
@InstallIn(SingletonComponent::class)
private interface LocationManagerEntryPoint {
    fun ryggLocationManager(): RyggLocationManager
}

@Composable
fun rememberLocationGate(): LocationGate {
    val context = LocalContext.current
    val activity = LocalActivity.current
    val scope = rememberCoroutineScope()
    val locationManager = remember {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            LocationManagerEntryPoint::class.java
        ).ryggLocationManager()
    }

    val location = remember { mutableStateOf<Location?>(null) }
    val permission = remember { mutableStateOf(context.locationPermission()) }
    val servicesOn = remember { mutableStateOf(locationManager.isLocationEnabled()) }
    val prompt = remember { mutableStateOf<LocationPrompt?>(null) }
    val pending = remember { mutableStateOf<PendingRequest?>(null) }

    LaunchedEffect(Unit) {
        locationManager.observeLocationEnabled().collect { servicesOn.value = it }
    }

    LifecycleResumeEffect(Unit) {
        permission.value = context.locationPermission()
        servicesOn.value = locationManager.isLocationEnabled()
        onPauseOrDispose { }
    }

    val canStream = permission.value != LocationPermission.NONE && servicesOn.value
    LaunchedEffect(canStream) {
        if (canStream) {
            locationManager.locationUpdates().collect { location.value = it }
        }
    }

    val evaluate: (LocationPurpose) -> LocationReadiness = { purpose ->
        readiness(purpose, permission.value, servicesOn.value) { isPermissionBlocked(activity) }
    }

    lateinit var advance: (auto: Boolean) -> Unit

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        permission.value = context.locationPermission()
        advance(permission.value != LocationPermission.NONE)
    }

    val resolutionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) servicesOn.value = locationManager.isLocationEnabled()
        advance(false)
    }

    val launchPermissionDialog = {
        LocationPermissionStore.markRequested(context)
        permissionLauncher.launch(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        )
    }

    val launchTurnOnDialog: () -> Unit = {
        scope.launch {
            when (val check = locationManager.checkLocationSettings()) {
                LocationSettingsCheck.Satisfied -> {
                    servicesOn.value = locationManager.isLocationEnabled()
                    advance(false)
                }

                is LocationSettingsCheck.Resolvable -> resolutionLauncher.launch(
                    IntentSenderRequest.Builder(check.resolution).build()
                )

                LocationSettingsCheck.Unresolvable -> context.startActivity(
                    Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            }
        }
    }

    advance = { auto ->
        val request = pending.value
        if (request != null) {
            when (val readiness = evaluate(request.purpose)) {
                LocationReadiness.Ready -> {
                    prompt.value = null
                    pending.value = null
                    request.onReady()
                }

                LocationReadiness.PermissionNeeded -> if (auto) {
                    prompt.value = null
                    launchPermissionDialog()
                } else {
                    prompt.value = LocationPrompt(request.purpose, readiness)
                }

                LocationReadiness.ServicesOff -> if (auto) {
                    prompt.value = null
                    launchTurnOnDialog()
                } else {
                    prompt.value = LocationPrompt(request.purpose, readiness)
                }

                LocationReadiness.PermissionBlocked,
                LocationReadiness.ApproximateOnly -> prompt.value = LocationPrompt(request.purpose, readiness)
            }
        }
    }

    LaunchedEffect(permission.value, servicesOn.value) {
        if (pending.value != null && prompt.value != null) advance(false)
    }

    return remember {
        LocationGate(
            location = location,
            permission = permission,
            servicesOn = servicesOn,
            prompt = prompt,
            evaluate = { evaluate(it) },
            onRequestAccess = { purpose, onReady ->
                pending.value = PendingRequest(purpose, onReady)
                advance(true)
            },
            onResolvePrompt = {
                when (prompt.value?.readiness) {
                    LocationReadiness.PermissionNeeded,
                    LocationReadiness.ApproximateOnly -> {
                        prompt.value = null
                        launchPermissionDialog()
                    }

                    LocationReadiness.PermissionBlocked -> context.openAppSettings()

                    LocationReadiness.ServicesOff -> {
                        prompt.value = null
                        launchTurnOnDialog()
                    }

                    LocationReadiness.Ready, null -> advance(false)
                }
            },
            onDismissPrompt = {
                prompt.value = null
                pending.value = null
            }
        )
    }
}

private fun readiness(
    purpose: LocationPurpose,
    permission: LocationPermission,
    servicesOn: Boolean,
    isBlocked: () -> Boolean
): LocationReadiness = when {
    permission == LocationPermission.NONE ->
        if (isBlocked()) LocationReadiness.PermissionBlocked else LocationReadiness.PermissionNeeded

    !servicesOn -> LocationReadiness.ServicesOff

    purpose.requiresPrecise && permission == LocationPermission.APPROXIMATE ->
        if (isBlocked()) LocationReadiness.PermissionBlocked else LocationReadiness.ApproximateOnly

    else -> LocationReadiness.Ready
}

private fun isPermissionBlocked(activity: Activity?): Boolean {
    activity ?: return false
    return LocationPermissionStore.wasRequested(activity) &&
        !ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_FINE_LOCATION)
}

data class LocationPrompt(
    val purpose: LocationPurpose,
    val readiness: LocationReadiness
)

private class PendingRequest(val purpose: LocationPurpose, val onReady: () -> Unit)
