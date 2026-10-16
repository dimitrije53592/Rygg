package com.dimitrijeilic.rygg.core.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

enum class LocationPermission { NONE, APPROXIMATE, PRECISE }

fun Context.locationPermission(): LocationPermission = when {
    isGranted(Manifest.permission.ACCESS_FINE_LOCATION) -> LocationPermission.PRECISE
    isGranted(Manifest.permission.ACCESS_COARSE_LOCATION) -> LocationPermission.APPROXIMATE
    else -> LocationPermission.NONE
}

private fun Context.isGranted(permission: String): Boolean =
    ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
