package com.example.rygg.core.navigation

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.rygg.R
import com.example.rygg.feature.auth.ui.viewmodel.AuthViewModel
import com.example.rygg.feature.auth.ui.wrapper.ForgotPasswordWrapper
import com.example.rygg.feature.auth.ui.wrapper.LoginWrapper
import com.example.rygg.feature.auth.ui.wrapper.RegisterWrapper
import com.example.rygg.feature.library.ui.wrapper.LibraryWrapper
import com.example.rygg.feature.profile.ui.screen.ProfileScreen
import com.example.rygg.feature.profile.ui.screen.ProfileScreenParams
import com.example.rygg.feature.profile.ui.screen.ProfileUiState
import com.example.rygg.feature.settings.ui.wrapper.SettingsWrapper
import kotlin.reflect.KClass

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation() {
    val authViewModel: AuthViewModel = hiltViewModel()
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    val startDestination: Any = remember { if (authViewModel.isLoggedIn()) Library else Login }

    val currentTopLevel = TopLevelDestination.entries.firstOrNull { currentDestination.isOn(it) }
    val isTopLevel = currentTopLevel != null
    val isAuthScreen = currentDestination.isAnyOf(Login::class, Register::class, ForgotPassword::class)

    Scaffold(
        topBar = {
            if (!isAuthScreen) {
                CenterAlignedTopAppBar(
                    title = { Text(stringResource(currentTitleRes(currentTopLevel))) },
                    navigationIcon = {
                        if (!isTopLevel) {
                            IconButton(onClick = { navController.navigateUp() }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = stringResource(R.string.nav_back)
                                )
                            }
                        }
                    }
                )
            }
        },
        bottomBar = {
            if (isTopLevel) {
                NavigationBar {
                    TopLevelDestination.entries.forEach { destination ->
                        NavigationBarItem(
                            selected = currentDestination.isOn(destination),
                            onClick = { navController.navigateToTab(destination.route) },
                            icon = { Icon(destination.icon, contentDescription = null) },
                            label = { Text(stringResource(destination.labelRes)) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable<Login> {
                LoginWrapper(
                    onAuthSkipped = {
                        navController.navigate(Library) {
                            popUpTo(navController.graph.id) { inclusive = true }
                        }
                    },
                    onLoggedIn = {
                        navController.navigate(Library) {
                            popUpTo(navController.graph.id) { inclusive = true }
                        }
                    },
                    onNavigateToRegister = { navController.navigate(Register) },
                    onNavigateToForgotPassword = { navController.navigate(ForgotPassword) }
                )
            }
            composable<Register> {
                RegisterWrapper(
                    onAuthSkip = {
                        navController.navigate(Library) {
                            popUpTo(navController.graph.id) { inclusive = true }
                        }
                    },
                    onRegistered = {
                        navController.navigate(Library) {
                            popUpTo(navController.graph.id) { inclusive = true }
                        }
                    },
                    onNavigateBack = { navController.navigateUp() }
                )
            }
            composable<ForgotPassword> {
                ForgotPasswordWrapper(onNavigateBack = { navController.navigateUp() })
            }
            composable<Library> {
                LibraryWrapper()
            }
            composable<Profile> {
                val user by authViewModel.currentUser.collectAsStateWithLifecycle()

                ProfileScreen(
                    params = ProfileScreenParams(
                        uiState = ProfileUiState(
                            displayName = user?.displayName.orEmpty(),
                            email = user?.email.orEmpty()
                        ),
                        onSignOut = {
                            authViewModel.signOut()
                            navController.navigate(Login) {
                                popUpTo(navController.graph.id) { inclusive = true }
                            }
                        }
                    )
                )
            }
            composable<Settings> {
                SettingsWrapper()
            }
        }
    }
}

private fun NavDestination?.isOn(destination: TopLevelDestination): Boolean =
    this?.hierarchy?.any { it.hasRoute(destination.route::class) } == true

private fun NavDestination?.isAnyOf(vararg routes: KClass<*>): Boolean =
    this?.hierarchy?.any { dest -> routes.any { dest.hasRoute(it) } } == true

@StringRes
private fun currentTitleRes(
    topLevel: TopLevelDestination?
): Int = when {
    topLevel != null -> topLevel.labelRes
    else -> R.string.app_name
}
