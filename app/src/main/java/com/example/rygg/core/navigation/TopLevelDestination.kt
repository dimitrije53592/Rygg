package com.example.rygg.core.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.example.rygg.R

enum class TopLevelDestination(
    val route: Any,
    @DrawableRes val icon: Int,
    @StringRes val labelRes: Int
) {
    LIBRARY(route = Library, icon = R.drawable.ic_library, labelRes = R.string.nav_library),
    MAP(route = Map, icon = R.drawable.ic_map, labelRes = R.string.nav_map)
}
