package com.dimitrijeilic.rygg.core.ui.utils

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.NoCredentialException
import com.dimitrijeilic.rygg.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

// Shared by Google sign-in and the re-auth before account deletion: both need a fresh ID token
// from Credential Manager. The returned function throws GetCredentialException when the user
// cancels or no account is available, and yields null for an unexpected credential type.
// With [reauthenticate] it confirms the account already used with Rygg instead of offering a
// fresh sign-in: a single authorized account is picked silently, and the full chooser is only
// the fallback when none is found.
@Composable
fun rememberGoogleIdTokenRequest(reauthenticate: Boolean = false): suspend () -> String? {
    val context = LocalContext.current
    val credentialManager = remember { CredentialManager.create(context) }
    return remember(context, credentialManager, reauthenticate) {
        {
            if (reauthenticate) {
                try {
                    credentialManager.requestIdToken(context, authorizedOnly = true)
                } catch (e: NoCredentialException) {
                    credentialManager.requestIdToken(context, authorizedOnly = false)
                }
            } else {
                credentialManager.requestIdToken(context, authorizedOnly = false)
            }
        }
    }
}

private suspend fun CredentialManager.requestIdToken(context: Context, authorizedOnly: Boolean): String? {
    val googleIdOption = GetGoogleIdOption.Builder()
        .setServerClientId(context.getString(R.string.default_web_client_id))
        .setFilterByAuthorizedAccounts(authorizedOnly)
        .setAutoSelectEnabled(authorizedOnly)
        .build()
    val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()
    val credential = getCredential(context, request).credential
    return if (credential is CustomCredential &&
        credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
    ) {
        GoogleIdTokenCredential.createFrom(credential.data).idToken
    } else {
        null
    }
}
