package com.example.rygg.core.location

import android.content.Context
import androidx.core.content.edit

// Android can't tell "never asked" from "permanently denied", so remember asking.
object LocationPermissionStore {
    fun wasRequested(context: Context): Boolean = prefs(context).getBoolean(KEY_REQUESTED, false)

    fun markRequested(context: Context) {
        prefs(context).edit { putBoolean(KEY_REQUESTED, true) }
    }

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}

private const val PREFS_NAME = "location_permission"
private const val KEY_REQUESTED = "requested"
