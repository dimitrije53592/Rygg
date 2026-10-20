package com.dimitrijeilic.rygg.feature.profile.data

import com.dimitrijeilic.rygg.core.common.Outcome
import com.dimitrijeilic.rygg.core.common.outcomeCatching
import com.dimitrijeilic.rygg.feature.profile.domain.ReauthCredential
import com.dimitrijeilic.rygg.feature.sync.data.RouteSyncManager
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

// Deletes the signed-in account and everything it owns in the cloud. Order matters: re-auth
// first (a stale session can't delete the user), then the data while the uid can still pass the
// rules, and the auth user last — SyncInitializer then sees the sign-out like any other.
class AccountDeletionRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val routeSyncManager: RouteSyncManager
) {
    suspend fun deleteAccount(credential: ReauthCredential): Outcome<Unit> = outcomeCatching {
        val user = firebaseAuth.currentUser?.takeUnless { it.isAnonymous }
            ?: throw IllegalStateException(ERR_NOT_SIGNED_IN)
        user.reauthenticate(credential.toAuthCredential(user)).await()
        routeSyncManager.deleteAccountData(user.uid)
        user.delete().await()
        Unit
    }

    private fun ReauthCredential.toAuthCredential(user: FirebaseUser): AuthCredential = when (this) {
        is ReauthCredential.Password ->
            EmailAuthProvider.getCredential(user.email ?: throw IllegalStateException(ERR_NO_EMAIL), password)

        is ReauthCredential.Google -> GoogleAuthProvider.getCredential(idToken, null)
    }

    companion object {
        const val ERR_NOT_SIGNED_IN = "NOT_SIGNED_IN"
        const val ERR_NO_EMAIL = "NO_EMAIL"
    }
}
