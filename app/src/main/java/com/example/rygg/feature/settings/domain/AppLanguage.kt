package com.example.rygg.feature.settings.domain

import androidx.annotation.StringRes
import com.example.rygg.R

enum class AppLanguage(val tag: String, @StringRes val labelRes: Int) {
    ENGLISH("en", R.string.settings_language_english),
    SPANISH("es", R.string.settings_language_spanish)
}
