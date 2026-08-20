package com.example.rygg.core.navigation

import android.net.Uri
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import com.example.rygg.core.ui.components.RyggBottomAppBar
import com.example.rygg.core.ui.theme.RyggTheme
import com.example.rygg.core.ui.utils.RouteShareLinks
import com.example.rygg.feature.auth.ui.components.SkipSignInDialog
import com.example.rygg.feature.auth.ui.viewmodel.AuthViewModel
import com.example.rygg.feature.auth.ui.wrapper.ForgotPasswordWrapper
import com.example.rygg.feature.auth.ui.wrapper.LoginWrapper
import com.example.rygg.feature.auth.ui.wrapper.RegisterWrapper
import com.example.rygg.feature.details.ui.wrapper.DetailsWrapper
import com.example.rygg.feature.details.ui.wrapper.ImportPreviewWrapper
import com.example.rygg.feature.details.ui.wrapper.RecordingPreviewWrapper
import com.example.rygg.feature.details.ui.wrapper.SharedRouteWrapper
import com.example.rygg.feature.library.ui.wrapper.LibraryWrapper
import com.example.rygg.feature.map.ui.wrapper.MapWrapper
import com.example.rygg.feature.map.ui.wrapper.RouteFollowingWrapper
import com.example.rygg.feature.profile.ui.wrapper.ProfileWrapper
import com.example.rygg.feature.record.ui.wrapper.RecordWrapper
import com.example.rygg.feature.settings.ui.wrapper.SettingsWrapper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation() {
    val authViewModel: AuthViewModel = hiltViewModel()
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    val startDestination: Any = remember { if (authViewModel.isLoggedIn()) Library else Login }

    // Enter the app at Library, clearing the auth back stack behind it.
    fun enterLibrary() {
        navController.navigate(Library) {
            popUpTo(navController.graph.id) { inclusive = true }
        }
    }

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
                var showSkipDialog by remember { mutableStateOf(false) }
                LoginWrapper(
                    onAuthSkipped = { showSkipDialog = true },
                    onLoggedIn = { enterLibrary() },
                    onNavigateToRegister = { navController.navigate(Register) },
                    onNavigateToForgotPassword = { navController.navigate(ForgotPassword) }
                )
                if (showSkipDialog) {
                    SkipSignInDialog(
                        onContinueAsGuest = {
                            showSkipDialog = false
                            enterLibrary()
                        },
                        onSignIn = { showSkipDialog = false }
                    )
                }
            }
            composable<Register> {
                var showSkipDialog by remember { mutableStateOf(false) }
                RegisterWrapper(
                    onAuthSkip = { showSkipDialog = true },
                    onRegistered = { enterLibrary() },
                    onNavigateBack = { navController.navigateUp() }
                )
                if (showSkipDialog) {
                    SkipSignInDialog(
                        onContinueAsGuest = {
                            showSkipDialog = false
                            enterLibrary()
                        },
                        onSignIn = { showSkipDialog = false }
                    )
                }
            }
            composable<ForgotPassword> {
                ForgotPasswordWrapper(onNavigateBack = { navController.navigateUp() })
            }
            composable<Library> {
                LibraryWrapper(
                    onEntryClick = { entryId ->
                        navController.navigate(Details(entryId = entryId))
                    },
                    onImport = { uri, discipline ->
                        navController.navigate(
                            // Encode the SAF content:// URI so its /, %, # don't mangle the route.
                            ImportPreview(uri = Uri.encode(uri.toString()), discipline = discipline.name)
                        )
                    },
                    onOpenProfile = { navController.navigate(Profile) }
                )
            }
            composable<Details> {
                DetailsWrapper(
                    onNavigateBack = { navController.navigateUp() },
                    onViewOnMap = { entryId ->
                        navController.navigate(Map(entryId = entryId))
                    }
                )
            }
            // Deep link "<RouteShareLinks.BASE>/s/{token}" opens a shared route for any recipient.
            composable<SharedRoutePreview>(
                deepLinks = listOf(navDeepLink<SharedRoutePreview>(basePath = "${RouteShareLinks.BASE}/s"))
            ) {
                SharedRouteWrapper(
                    onNavigateBack = { navController.navigateUp() },
                    onSaved = { entryId ->
                        navController.navigate(Details(entryId = entryId)) {
                            popUpTo<SharedRoutePreview> { inclusive = true }
                        }
                    }
                )
            }
            composable<ImportPreview> {
                ImportPreviewWrapper(onDone = { navController.popBackStack() })
            }
            composable<Record> {
                RecordWrapper(
                    onRecordingStopped = { navController.navigate(RecordingPreview) }
                )
            }
            composable<RecordingPreview> {
                RecordingPreviewWrapper(onDone = { navController.popBackStack() })
            }
            composable<Map> {
                MapWrapper(
                    onStartFollow = { entryId ->
                        navController.navigate(FollowRoute(entryId = entryId))
                    }
                )
            }
            composable<FollowRoute> {
                RouteFollowingWrapper(onExit = { navController.navigateUp() })
            }
            composable<Profile> {
                ProfileWrapper(
                    onAuthEntry = {
                        navController.navigate(Login) {
                            popUpTo(navController.graph.id) { inclusive = true }
                        }
                    },
                    onOpenSettings = { navController.navigate(Settings) }
                )
            }
            composable<Settings> {
                SettingsWrapper()
            }
        }
    }
}
