package com.dimitrijeilic.rygg.core.navigation

import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.util.Consumer
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import com.dimitrijeilic.rygg.core.ui.components.LocalNavAnimatedVisibilityScope
import com.dimitrijeilic.rygg.core.ui.components.LocalSharedTransitionScope
import com.dimitrijeilic.rygg.core.ui.components.RyggBottomAppBar
import com.dimitrijeilic.rygg.feature.record.ui.viewmodel.LiveRecordingViewModel
import com.dimitrijeilic.rygg.core.ui.theme.RyggMotion
import com.dimitrijeilic.rygg.core.ui.theme.RyggTheme
import com.dimitrijeilic.rygg.core.ui.utils.RouteShareLinks
import com.dimitrijeilic.rygg.feature.auth.ui.components.SkipSignInDialog
import com.dimitrijeilic.rygg.feature.auth.ui.viewmodel.AuthViewModel
import com.dimitrijeilic.rygg.feature.auth.ui.wrapper.ForgotPasswordWrapper
import com.dimitrijeilic.rygg.feature.auth.ui.wrapper.LoginWrapper
import com.dimitrijeilic.rygg.feature.auth.ui.wrapper.RegisterWrapper
import com.dimitrijeilic.rygg.feature.details.ui.wrapper.DetailsWrapper
import com.dimitrijeilic.rygg.feature.details.ui.wrapper.ImportPreviewWrapper
import com.dimitrijeilic.rygg.feature.details.ui.wrapper.RecordingPreviewWrapper
import com.dimitrijeilic.rygg.feature.details.ui.wrapper.SharedRouteWrapper
import com.dimitrijeilic.rygg.feature.feedback.ui.wrapper.FeedbackWrapper
import com.dimitrijeilic.rygg.feature.library.ui.wrapper.LibraryWrapper
import com.dimitrijeilic.rygg.feature.map.ui.wrapper.MapWrapper
import com.dimitrijeilic.rygg.feature.map.ui.wrapper.RouteFollowingWrapper
import com.dimitrijeilic.rygg.feature.profile.ui.wrapper.ProfileWrapper
import com.dimitrijeilic.rygg.feature.record.service.RecordingService
import com.dimitrijeilic.rygg.feature.record.ui.wrapper.RecordWrapper
import com.dimitrijeilic.rygg.feature.settings.ui.wrapper.LicensesWrapper
import com.dimitrijeilic.rygg.feature.settings.ui.wrapper.SettingsWrapper

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun AppNavigation() {
    val authViewModel: AuthViewModel = hiltViewModel()
    val liveRecordingViewModel: LiveRecordingViewModel = hiltViewModel()
    val liveRecording by liveRecordingViewModel.liveRecording.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    val activity = LocalContext.current as ComponentActivity

    // NavHost does the navigating; this only covers side effects a deep link owes first.
    fun handleDeepLinkSideEffects(intent: Intent?) {
        if (intent?.data?.toString() == InternalDeepLinks.RECORDING_PREVIEW) {
            RecordingService.stop(activity)
        }
    }

    LaunchedEffect(Unit) { handleDeepLinkSideEffects(activity.intent) }

    // A singleTop activity gets later deep links via onNewIntent, which NavHost does not observe.
    DisposableEffect(navController) {
        val listener = Consumer<Intent> { intent ->
            handleDeepLinkSideEffects(intent)
            navController.handleDeepLink(intent)
        }
        activity.addOnNewIntentListener(listener)
        onDispose { activity.removeOnNewIntentListener(listener) }
    }

    val startDestination: Any = remember { if (authViewModel.isLoggedIn()) Library else Login }

    fun enterLibrary() {
        navController.navigate(Library) {
            popUpTo(navController.graph.id) { inclusive = true }
        }
    }

    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                visible = currentDestination.isTopLevel(),
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = RyggMotion.spatial()
                ),
                exit = slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = RyggMotion.spatialFast()
                )
            ) {
                RyggBottomAppBar(
                    navController = navController,
                    currentDestination = currentDestination,
                    recordingElapsedMillis = liveRecording?.elapsedMillis,
                    recordingPaused = liveRecording?.isPaused == true
                )
            }
        },
        contentWindowInsets = WindowInsets(RyggTheme.dimens.zeroPadding)
    ) { innerPadding ->
        SharedTransitionLayout {
            CompositionLocalProvider(LocalSharedTransitionScope provides this) {
                NavHost(
                    navController = navController,
                    startDestination = startDestination,
                    enterTransition = {
                        slideInHorizontally(
                            initialOffsetX = { it / SLIDE_FRACTION },
                            animationSpec = RyggMotion.spatial()
                        ) + fadeIn(animationSpec = RyggMotion.effects())
                    },
                    exitTransition = { fadeOut(animationSpec = RyggMotion.effectsFast()) },
                    popEnterTransition = { fadeIn(animationSpec = RyggMotion.effects()) },
                    popExitTransition = {
                        slideOutHorizontally(
                            targetOffsetX = { it / SLIDE_FRACTION },
                            animationSpec = RyggMotion.spatial()
                        ) + fadeOut(animationSpec = RyggMotion.effects())
                    },
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
                        CompositionLocalProvider(LocalNavAnimatedVisibilityScope provides this) {
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
                    }
                    composable<Details> {
                        CompositionLocalProvider(LocalNavAnimatedVisibilityScope provides this) {
                            DetailsWrapper(
                                onNavigateBack = { navController.navigateUp() },
                                onViewOnMap = { entryId ->
                                    navController.navigate(Map(entryId = entryId))
                                },
                                onOpenEntry = { entryId ->
                                    navController.navigate(Details(entryId = entryId)) {
                                        popUpTo<Details> { inclusive = true }
                                    }
                                }
                            )
                        }
                    }
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
                    composable<Record>(
                        deepLinks = listOf(navDeepLink<Record>(basePath = InternalDeepLinks.RECORD))
                    ) {
                        RecordWrapper(
                            onRecordingStopped = { navController.navigate(RecordingPreview) }
                        )
                    }
                    composable<RecordingPreview>(
                        deepLinks = listOf(navDeepLink<RecordingPreview>(basePath = InternalDeepLinks.RECORDING_PREVIEW))
                    ) {
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
                            onNavigateBack = { navController.navigateUp() },
                            onAuthEntry = {
                                navController.navigate(Login) {
                                    popUpTo(navController.graph.id) { inclusive = true }
                                }
                            },
                            onOpenSettings = { navController.navigate(Settings) },
                            onSendFeedback = { navController.navigate(Feedback) }
                        )
                    }
                    composable<Settings> {
                        SettingsWrapper(
                            onNavigateBack = { navController.navigateUp() },
                            onOpenLicenses = { navController.navigate(Licenses) }
                        )
                    }
                    composable<Licenses> {
                        LicensesWrapper(onNavigateBack = { navController.navigateUp() })
                    }
                    composable<Feedback> {
                        FeedbackWrapper(onNavigateBack = { navController.navigateUp() })
                    }
                }
            }
        }
    }
}

private const val SLIDE_FRACTION = 6
