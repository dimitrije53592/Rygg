package com.example.rygg.feature.sync

import com.example.rygg.feature.auth.data.AuthRepository
import com.example.rygg.feature.settings.data.SettingsRepository
import com.example.rygg.feature.sync.data.RouteSyncManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

// App-scoped bridge from Firebase auth state + the sync-enabled preference to the sync engine.
// Started once from RyggApplication.onCreate(); drives sign-in/out and the sync on/off toggle.
@Singleton
class SyncInitializer @Inject constructor(
    private val authRepository: AuthRepository,
    private val settingsRepository: SettingsRepository,
    private val syncManager: RouteSyncManager
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var started = false
    private var currentUid: String? = null
    private var syncEnabled: Boolean? = null

    fun start() {
        if (started) return
        started = true
        scope.launch {
            combine(authRepository.authState, settingsRepository.syncEnabled) { user, enabled ->
                user?.uid to enabled
            }
                .distinctUntilChanged()
                .collect { (uid, enabled) ->
                    val previousUid = currentUid
                    val previousEnabled = syncEnabled
                    currentUid = uid
                    syncEnabled = enabled

                    // Auth moved away from a signed-in account → tear that account down first.
                    if (previousUid != null && previousUid != uid) {
                        syncManager.onSignedOut(previousUid)
                    }
                    when {
                        uid == null -> Unit // signed out; handled above
                        // Signed in and sync (re)enabled: a new account, or the toggle just flipped on.
                        enabled && (uid != previousUid || previousEnabled != true) ->
                            syncManager.onSignedIn(uid)
                        // Still the same signed-in account, but sync was just switched off.
                        !enabled && previousEnabled == true && uid == previousUid ->
                            syncManager.pauseSync()
                    }
                }
        }
    }
}
