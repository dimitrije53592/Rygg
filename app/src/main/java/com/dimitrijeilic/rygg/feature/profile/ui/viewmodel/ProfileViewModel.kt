package com.dimitrijeilic.rygg.feature.profile.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dimitrijeilic.rygg.feature.auth.data.AuthRepository
import com.dimitrijeilic.rygg.feature.library.data.GpxFileEntryRepository
import com.dimitrijeilic.rygg.feature.profile.ui.screen.ProfileUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    gpxFileEntryRepository: GpxFileEntryRepository
) : ViewModel() {
    val uiState: StateFlow<ProfileUiState> = combine(
        authRepository.authState,
        gpxFileEntryRepository.observeGpxFileEntries()
    ) { user, entries ->
        ProfileUiState(
            isSignedIn = user != null,
            displayName = user?.displayName.orEmpty(),
            email = user?.email.orEmpty(),
            activityCount = entries.size,
            totalDistanceMeters = entries.sumOf { it.distanceMeters },
            totalAscentMeters = entries.sumOf { it.ascentMeters }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = authRepository.currentUser().let { user ->
            ProfileUiState(
                isSignedIn = user != null,
                displayName = user?.displayName.orEmpty(),
                email = user?.email.orEmpty()
            )
        }
    )

    fun signOut() = authRepository.signOut()
}
