package com.dimitrijeilic.rygg.feature.auth.data

import com.dimitrijeilic.rygg.core.common.Outcome
import com.dimitrijeilic.rygg.core.common.outcomeCatching
import com.dimitrijeilic.rygg.feature.auth.domain.SignInMethod
import com.dimitrijeilic.rygg.feature.auth.domain.User
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) {
    val authState: Flow<User?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.signedInUser()?.toUser())
        }

        firebaseAuth.addAuthStateListener(listener)
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }

    fun isLoggedIn(): Boolean = firebaseAuth.signedInUser() != null

    fun currentUser(): User? = firebaseAuth.signedInUser()?.toUser()

    // Google wins when both are linked: re-authenticating is then one tap instead of typing.
    fun currentSignInMethod(): SignInMethod? {
        val providers = firebaseAuth.signedInUser()?.providerData?.map { it.providerId } ?: return null
        return when {
            GoogleAuthProvider.PROVIDER_ID in providers -> SignInMethod.GOOGLE
            EmailAuthProvider.PROVIDER_ID in providers -> SignInMethod.PASSWORD
            else -> null
        }
    }

    suspend fun login(email: String, password: String): Outcome<Unit> = outcomeCatching {
        firebaseAuth.signInWithEmailAndPassword(email, password).await()
        Unit
    }

    suspend fun register(name: String, email: String, password: String): Outcome<Unit> = outcomeCatching {
        val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()

        val update = UserProfileChangeRequest.Builder().setDisplayName(name).build()
        result.user?.updateProfile(update)?.await()
        Unit
    }

    suspend fun signInWithGoogle(idToken: String): Outcome<Unit> = outcomeCatching {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential).await()
        Unit
    }

    suspend fun sendPasswordReset(email: String): Outcome<Unit> = outcomeCatching {
        firebaseAuth.sendPasswordResetEmail(email).await()
        Unit
    }

    fun signOut() {
        firebaseAuth.signOut()
    }

    // Mints a uid for writes a guest is allowed to make (feedback), signing in anonymously if there
    // is no session yet. Needs the network only on that first call; afterwards the session is cached.
    suspend fun ensureAnyUid(): String =
        firebaseAuth.currentUser?.uid
            ?: firebaseAuth.signInAnonymously().await().user?.uid
            ?: error("Anonymous sign-in returned no user")
}

// An anonymous session is a write credential, not a signed-in account — the rest of the app must
// keep seeing a guest, or sync would switch on and adopt routes the user never asked to upload.
// A later real sign-in replaces (rather than links) the anonymous account, orphaning its uid; that
// is harmless, since the only documents written under it are feedback the user already sent.
private fun FirebaseAuth.signedInUser(): FirebaseUser? = currentUser?.takeUnless { it.isAnonymous }

private fun FirebaseUser.toUser(): User = User(
    uid = uid,
    email = email.orEmpty(),
    displayName = displayName.orEmpty()
)
