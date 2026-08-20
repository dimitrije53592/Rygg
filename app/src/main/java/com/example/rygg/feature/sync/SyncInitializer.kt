package com.example.rygg.feature.sync

import com.example.rygg.feature.auth.data.AuthRepository
import com.example.rygg.feature.sync.data.RouteSyncManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

// App-scoped bridge from Firebase auth state to the sync engine. Started once from
// RyggApplication.onCreate(); drives adoption/push/pull on sign-in and detaches on sign-out.
@Singleton
class SyncInitializer @Inject constructor(
    private val authRepository: AuthRepository,
    private val syncManager: RouteSyncManager
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var started = false
    private var currentUid: String? = null

    fun start() {
        if (started) return
        started = true
        scope.launch {
            authRepository.authState
                .distinctUntilChanged()
                .collect { user ->
                    val newUid = user?.uid
                    if (newUid == currentUid) return@collect
                    val previousUid = currentUid
                    currentUid = newUid
                    when {
                        newUid != null -> syncManager.onSignedIn(newUid)
                        previousUid != null -> syncManager.onSignedOut(previousUid)
                    }
                }
        }
    }
}
