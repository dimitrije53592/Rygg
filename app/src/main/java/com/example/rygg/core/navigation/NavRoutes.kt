package com.example.rygg.core.navigation

import kotlinx.serialization.Serializable

@Serializable
data object Login

@Serializable
data object Register

@Serializable
data object ForgotPassword

@Serializable
data object Library

@Serializable
data object Record

@Serializable
data class Details(val entryId: Long)

@Serializable
data class Map(val entryId: Long? = null)

@Serializable
data class FollowRoute(val entryId: Long)

@Serializable
data object Profile

@Serializable
data object Settings
