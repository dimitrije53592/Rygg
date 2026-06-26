package com.example.rygg.feature.auth.data

import com.example.rygg.core.common.Outcome
import com.example.rygg.core.common.outcomeCatching
import com.example.rygg.feature.auth.domain.User
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
            trySend(auth.currentUser?.toUser())
        }

        firebaseAuth.addAuthStateListener(listener)
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }

    fun isLoggedIn(): Boolean = firebaseAuth.currentUser != null

    fun currentUser(): User? = firebaseAuth.currentUser?.toUser()

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
}

private fun FirebaseUser.toUser(): User = User(
    uid = uid,
    email = email.orEmpty(),
    displayName = displayName.orEmpty()
)
