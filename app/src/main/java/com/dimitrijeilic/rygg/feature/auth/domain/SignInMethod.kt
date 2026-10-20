package com.dimitrijeilic.rygg.feature.auth.domain

// How the signed-in account proves who it is — decides which re-authentication a sensitive
// action (account deletion) asks for.
enum class SignInMethod {
    PASSWORD,
    GOOGLE
}
