package com.example.rygg.core.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import com.example.rygg.core.navigation.TopLevelDestination
import com.example.rygg.core.navigation.navigateToTab
import com.example.rygg.core.ui.theme.RyggColor
import com.example.rygg.core.ui.theme.RyggTheme

@Composable
fun RyggBottomAppBar(
    navController: NavController,
    currentDestination: NavDestination?
) {
    val currentTopLevel = TopLevelDestination.entries.firstOrNull { currentDestination.isOn(it) }
    val isTopLevel = currentTopLevel != null

    if (isTopLevel) {
        NavigationBar(
            containerColor = RyggTheme.getColor(RyggColor.SurfaceElevated)
        ) {
            TopLevelDestination.entries.forEach { destination ->
                val isSelected = currentDestination.isOn(destination)

                NavigationBarItem(
                    selected = isSelected,
                    onClick = { navController.navigateToTab(destination.route) },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = Color.Transparent
                    ),
                    label = {
                        Text(
                            text = stringResource(destination.labelRes),
                            color = if (isSelected) {
                                RyggTheme.getColor(RyggColor.BrandGreen)
                            } else {
                                RyggTheme.getColor(RyggColor.TextSecondary)
                            }
                        )
                    },
                    icon = {
                        Icon(
                            painter = painterResource(destination.icon),
                            tint = if (isSelected) {
                                RyggTheme.getColor(RyggColor.BrandGreen)
                            } else {
                                RyggTheme.getColor(RyggColor.TextSecondary)
                            },
                            modifier = Modifier.size(RyggTheme.dimens.iconSize32),
                            contentDescription = null
                        )
                    }
                )
            }
        }
    }
}

private fun NavDestination?.isOn(destination: TopLevelDestination): Boolean =
    this?.hierarchy?.any { it.hasRoute(destination.route::class) } == true
