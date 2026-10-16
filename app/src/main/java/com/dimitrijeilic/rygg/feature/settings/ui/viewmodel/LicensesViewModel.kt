package com.dimitrijeilic.rygg.feature.settings.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dimitrijeilic.rygg.core.common.Outcome
import com.dimitrijeilic.rygg.core.common.asResult
import com.dimitrijeilic.rygg.feature.settings.data.LicensesRepository
import com.dimitrijeilic.rygg.feature.settings.domain.OpenSourceLicenses
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class LicensesViewModel @Inject constructor(
    licensesRepository: LicensesRepository
) : ViewModel() {
    private val expandedId = MutableStateFlow<String?>(null)

    val uiState: StateFlow<LicensesUiState> = combine(
        licensesRepository.observeLicenses().asResult(),
        expandedId
    ) { outcome, expanded ->
        LicensesUiState(
            loadingState = when (outcome) {
                Outcome.Loading -> LicensesLoadingState.Loading
                is Outcome.Success -> LicensesLoadingState.Loaded(outcome.data)
                is Outcome.Error -> LicensesLoadingState.Error(outcome.cause.message)
            },
            expandedId = expanded
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LicensesUiState())

    // One licence text open at a time keeps the long list scannable.
    fun onToggleExpanded(id: String) {
        expandedId.update { if (it == id) null else id }
    }
}

data class LicensesUiState(
    val loadingState: LicensesLoadingState = LicensesLoadingState.Loading,
    val expandedId: String? = null
)

sealed interface LicensesLoadingState {
    data object Loading : LicensesLoadingState

    data class Loaded(val licenses: OpenSourceLicenses) : LicensesLoadingState

    data class Error(val message: String?) : LicensesLoadingState
}
