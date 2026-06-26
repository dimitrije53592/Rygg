package com.example.rygg.core.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.rygg.R

enum class TopLevelDestination(
    val route: Any,
    val icon: ImageVector,
    @StringRes val labelRes: Int
) {
    HOME(route = Home, icon = Icons.Filled.Home, labelRes = R.string.nav_home),
    PROFILE(route = Profile, icon = Icons.Filled.Person, labelRes = R.string.nav_profile),
    SETTINGS(route = Settings, icon = Icons.Filled.Settings, labelRes = R.string.nav_settings)
}
