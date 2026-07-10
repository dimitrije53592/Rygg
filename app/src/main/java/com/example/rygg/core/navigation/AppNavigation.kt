package com.example.rygg.core.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.rygg.core.ui.components.RyggBottomAppBar
import com.example.rygg.core.ui.theme.RyggTheme
import com.example.rygg.feature.auth.ui.viewmodel.AuthViewModel
import com.example.rygg.feature.auth.ui.wrapper.ForgotPasswordWrapper
import com.example.rygg.feature.auth.ui.wrapper.LoginWrapper
import com.example.rygg.feature.auth.ui.wrapper.RegisterWrapper
import com.example.rygg.feature.library.ui.wrapper.LibraryWrapper
import com.example.rygg.feature.profile.ui.screen.ProfileScreen
import com.example.rygg.feature.profile.ui.screen.ProfileScreenParams
import com.example.rygg.feature.profile.ui.screen.ProfileUiState
import com.example.rygg.feature.settings.ui.wrapper.SettingsWrapper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation() {
    val authViewModel: AuthViewModel = hiltViewModel()
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    val startDestination: Any = remember { if (authViewModel.isLoggedIn()) Library else Login }

    Scaffold(
        bottomBar = {
            RyggBottomAppBar(
                navController,
                currentDestination
            )
        },
        contentWindowInsets = WindowInsets(RyggTheme.dimens.zeroPadding)
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
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
