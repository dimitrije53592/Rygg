package com.example.rygg.core.locale

import android.content.Context
import android.content.res.Configuration
import androidx.core.content.edit
import java.util.Locale

object AppLocaleStore {
    private const val PREFS_NAME = "app_locale"
    private const val KEY_LANGUAGE_TAG = "language_tag"

    fun getLanguageTag(context: Context): String? =
        prefs(context).getString(KEY_LANGUAGE_TAG, null)

    fun setLanguageTag(context: Context, tag: String) {
        prefs(context).edit { putString(KEY_LANGUAGE_TAG, tag) }
    }

    // Wraps a base context with the saved locale; call from Activity.attachBaseContext.
    fun wrap(context: Context): Context {
        val tag = getLanguageTag(context) ?: return context

        val locale = Locale.forLanguageTag(tag)
        Locale.setDefault(locale)

        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)

        return context.createConfigurationContext(configuration)
    }

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}
