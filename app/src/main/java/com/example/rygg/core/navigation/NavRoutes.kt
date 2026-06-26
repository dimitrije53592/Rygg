package com.example.rygg.core.navigation

import kotlinx.serialization.Serializable

@Serializable
data object Login

@Serializable
data object Register

@Serializable
data object ForgotPassword

@Serializable
data object Home

@Serializable
data object Profile

@Serializable
data object Settings

@Serializable
data class Details(val id: String)
