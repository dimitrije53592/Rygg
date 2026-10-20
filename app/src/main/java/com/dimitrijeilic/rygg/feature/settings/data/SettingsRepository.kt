package com.dimitrijeilic.rygg.feature.settings.data

import android.content.Context
import android.content.res.Resources
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.dimitrijeilic.rygg.core.locale.AppLocaleStore
import com.dimitrijeilic.rygg.core.ui.theme.ThemeMode
import com.dimitrijeilic.rygg.core.units.MeasurementSystem
import com.dimitrijeilic.rygg.feature.map.domain.OffRouteThreshold
import com.dimitrijeilic.rygg.feature.record.domain.RecordingResolution
import com.dimitrijeilic.rygg.feature.settings.domain.AppLanguage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

// Future account sync: units, language, theme, resolution are per account; the rest per device.
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

    // System locale: the in-app language override drops the region from Locale.getDefault().
    val measurementSystem: Flow<MeasurementSystem> = dataStore.data.map { prefs ->
        prefs[MEASUREMENT_SYSTEM_KEY]?.let { name -> MeasurementSystem.entries.firstOrNull { it.name == name } }
            ?: MeasurementSystem.defaultFor(Resources.getSystem().configuration.locales[0])
    }

    suspend fun setMeasurementSystem(system: MeasurementSystem) {
        dataStore.edit { it[MEASUREMENT_SYSTEM_KEY] = system.name }
    }

    suspend fun currentMeasurementSystem(): MeasurementSystem = measurementSystem.first()

    // How densely recordings store track points; GPS polling is unaffected.
    val recordingResolution: Flow<RecordingResolution> = dataStore.data.map { prefs ->
        prefs[RECORDING_RESOLUTION_KEY]?.let { name -> RecordingResolution.entries.firstOrNull { it.name == name } }
            ?: RecordingResolution.BALANCED
    }

    suspend fun setRecordingResolution(resolution: RecordingResolution) {
        dataStore.edit { it[RECORDING_RESOLUTION_KEY] = resolution.name }
    }

    val batteryPromptSeen: Flow<Boolean> = dataStore.data.map { it[BATTERY_PROMPT_SEEN_KEY] ?: false }

    suspend fun setBatteryPromptSeen() {
        dataStore.edit { it[BATTERY_PROMPT_SEEN_KEY] = true }
    }

    val offRouteAlertsEnabled: Flow<Boolean> = dataStore.data.map { it[OFF_ROUTE_ALERTS_KEY] ?: true }

    val offRouteThreshold: Flow<OffRouteThreshold> = dataStore.data.map { prefs ->
        prefs[OFF_ROUTE_THRESHOLD_KEY]?.let { name -> OffRouteThreshold.entries.firstOrNull { it.name == name } }
            ?: OffRouteThreshold.DEFAULT
    }

    suspend fun setOffRouteAlertsEnabled(enabled: Boolean) {
        dataStore.edit { it[OFF_ROUTE_ALERTS_KEY] = enabled }
    }

    suspend fun setOffRouteThreshold(threshold: OffRouteThreshold) {
        dataStore.edit { it[OFF_ROUTE_THRESHOLD_KEY] = threshold.name }
    }

    // Cloud sync preferences. Sync is on by default; files transfer on Wi-Fi by default.
    val syncEnabled: Flow<Boolean> = dataStore.data.map { it[SYNC_ENABLED_KEY] ?: true }
    val syncWifiOnly: Flow<Boolean> = dataStore.data.map { it[SYNC_WIFI_ONLY_KEY] ?: true }
    val syncPhotos: Flow<Boolean> = dataStore.data.map { it[SYNC_PHOTOS_KEY] ?: true }

    suspend fun setSyncEnabled(enabled: Boolean) {
        dataStore.edit { it[SYNC_ENABLED_KEY] = enabled }
    }

    suspend fun setSyncWifiOnly(wifiOnly: Boolean) {
        dataStore.edit { it[SYNC_WIFI_ONLY_KEY] = wifiOnly }
    }

    suspend fun setSyncPhotos(enabled: Boolean) {
        dataStore.edit { it[SYNC_PHOTOS_KEY] = enabled }
    }

    suspend fun currentSyncEnabled(): Boolean = syncEnabled.first()

    suspend fun currentSyncWifiOnly(): Boolean = syncWifiOnly.first()

    suspend fun currentSyncPhotos(): Boolean = syncPhotos.first()

    fun currentLanguage(): AppLanguage {
        val tag = AppLocaleStore.getLanguageTag(context)
        return AppLanguage.entries.firstOrNull { it.tag == tag } ?: AppLanguage.ENGLISH
    }

    fun setLanguage(language: AppLanguage) {
        AppLocaleStore.setLanguageTag(context, language.tag)
    }

    fun appVersionName(): String =
        context.packageManager.getPackageInfo(context.packageName, 0).versionName.orEmpty()
}

private val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
private val MEASUREMENT_SYSTEM_KEY = stringPreferencesKey("measurement_system")
private val RECORDING_RESOLUTION_KEY = stringPreferencesKey("recording_resolution")
private val SYNC_ENABLED_KEY = booleanPreferencesKey("sync_enabled")
private val SYNC_WIFI_ONLY_KEY = booleanPreferencesKey("sync_wifi_only")
private val SYNC_PHOTOS_KEY = booleanPreferencesKey("sync_photos")
private val OFF_ROUTE_ALERTS_KEY = booleanPreferencesKey("off_route_alerts_enabled")
private val OFF_ROUTE_THRESHOLD_KEY = stringPreferencesKey("off_route_threshold")
private val BATTERY_PROMPT_SEEN_KEY = booleanPreferencesKey("battery_prompt_seen")
