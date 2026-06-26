package com.example.rygg.feature.settings.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.rygg.core.locale.AppLocaleStore
import com.example.rygg.core.ui.theme.ThemeMode
import com.example.rygg.feature.settings.domain.AppLanguage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dataStore: DataStore<Preferences>
) {
    val themeMode: Flow<ThemeMode> = dataStore.data.map { prefs ->
        prefs[THEME_MODE_KEY]?.let { name -> ThemeMode.entries.firstOrNull { it.name == name } } ?: ThemeMode.SYSTEM
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { it[THEME_MODE_KEY] = mode.name }
    }

    fun currentLanguage(): AppLanguage {
        val tag = AppLocaleStore.getLanguageTag(context)
        return AppLanguage.entries.firstOrNull { it.tag == tag } ?: AppLanguage.ENGLISH
    }

    fun setLanguage(language: AppLanguage) {
        AppLocaleStore.setLanguageTag(context, language.tag)
    }
}

private val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
