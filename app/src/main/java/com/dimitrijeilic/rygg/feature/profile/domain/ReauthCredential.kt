package com.dimitrijeilic.rygg.feature.profile.domain

// Proof of a fresh login. Firebase only deletes a user who signed in recently, so deletion always
// re-authenticates first rather than failing halfway through on an old session.
sealed interface ReauthCredential {
    data class Password(val password: String) : ReauthCredential

    data class Google(val idToken: String) : ReauthCredential
}
