package com.dimitrijeilic.rygg.feature.auth.domain

data class User(
    val uid: String,
    val email: String,
    val displayName: String
)
