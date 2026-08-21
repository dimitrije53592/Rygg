package com.example.rygg.core.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination

// Switch to a bottom-nav tab. Pops any sub-screens back to the start destination so tapping a tab
// always lands on that tab's root.
//
// Note: this graph is flat (no per-tab nested graphs), so every sub-screen — Details, and the
// argument-carrying Map(entryId) opened from "View on map" — lives on the start destination's back
// stack. saveState/restoreState would then save that whole sub-stack under the start destination
// and immediately restore it, dumping the user back onto the buried Map(entryId) every time they
// tapped Library. Without them, each tab tap resolves to a clean tab root.
fun NavController.navigateToTab(route: Any) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) {
            inclusive = false
        }
        launchSingleTop = true
    }
}
