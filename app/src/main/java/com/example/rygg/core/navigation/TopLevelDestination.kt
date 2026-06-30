package com.example.rygg.core.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CollectionsBookmark
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.rygg.R

enum class TopLevelDestination(
    val route: Any,
    val icon: ImageVector,
    @StringRes val labelRes: Int
) {
    LIBRARY(route = Library, icon = Icons.Filled.CollectionsBookmark, labelRes = R.string.nav_library)
}
