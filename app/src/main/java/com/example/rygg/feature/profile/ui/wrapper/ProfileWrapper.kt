package com.example.rygg.feature.profile.ui.wrapper

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.rygg.feature.profile.ui.screen.ProfileScreen
import com.example.rygg.feature.profile.ui.screen.ProfileScreenParams
import com.example.rygg.feature.profile.ui.viewmodel.ProfileViewModel

@Composable
fun ProfileWrapper(
    onAuthEntry: () -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ProfileScreen(
        params = ProfileScreenParams(
            uiState = uiState,
            onSignOut = {
                viewModel.signOut()
                onAuthEntry()
            },
            onSignIn = onAuthEntry,
            onOpenSettings = onOpenSettings
        )
    )
}
