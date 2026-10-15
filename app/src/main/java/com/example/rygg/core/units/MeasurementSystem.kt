package com.example.rygg.core.units

import androidx.annotation.StringRes
import com.example.rygg.R
import java.util.Locale

enum class MeasurementSystem(@StringRes val labelRes: Int) {
    METRIC(R.string.settings_units_metric),
    IMPERIAL(R.string.settings_units_imperial);

    companion object {
        fun defaultFor(locale: Locale): MeasurementSystem =
            if (locale.country.uppercase() in IMPERIAL_REGIONS) IMPERIAL else METRIC
    }
}

private val IMPERIAL_REGIONS = setOf("US", "LR", "MM")
