package com.example.rygg.core.navigation

import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination

// Deliberately no saveState/restoreState: the graph is flat, so sub-screens like Map(entryId) sit
// on the start destination's stack and would be restored under it on every tab tap.
fun NavController.navigateToTab(route: Any) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) {
            inclusive = false
        }
        launchSingleTop = true
    }
}

fun NavDestination?.isOn(destination: TopLevelDestination): Boolean =
    this?.hierarchy?.any { it.hasRoute(destination.route::class) } == true

fun NavDestination?.isTopLevel(): Boolean =
    TopLevelDestination.entries.any { isOn(it) }
