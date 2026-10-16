package com.dimitrijeilic.rygg.feature.feedback.data

import android.content.Context
import android.os.Build
import com.dimitrijeilic.rygg.BuildConfig
import com.dimitrijeilic.rygg.core.locale.AppLocaleStore
import com.dimitrijeilic.rygg.feature.auth.data.AuthRepository
import com.dimitrijeilic.rygg.feature.library.data.local.GpxFileEntryDao
import com.dimitrijeilic.rygg.feature.settings.data.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

// The triage facts a reporter would otherwise have to be asked for. Every field here is named to
// the user in feedback_context_disclosure before they send — keep the two honest about each other.
class FeedbackContextProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authRepository: AuthRepository,
    private val settingsRepository: SettingsRepository,
    private val dao: GpxFileEntryDao
) {
    suspend fun collect(): Map<String, Any> = withContext(Dispatchers.IO) {
        mapOf(
            "appVersion" to BuildConfig.VERSION_NAME,
            "versionCode" to BuildConfig.VERSION_CODE,
            "isDebug" to BuildConfig.DEBUG,
            "device" to Build.MANUFACTURER + " " + Build.MODEL,
            "androidVersion" to Build.VERSION.RELEASE.orEmpty(),
            "sdkInt" to Build.VERSION.SDK_INT,
            "localeTag" to AppLocaleStore.getLanguageTag(context).orEmpty(),
            "syncEnabled" to settingsRepository.currentSyncEnabled(),
            "routeCount" to dao.countActive()
        )
    }

    // A guest reports as signed-out even though an anonymous session carries the write.
    fun isSignedIn(): Boolean = authRepository.isLoggedIn()
}
