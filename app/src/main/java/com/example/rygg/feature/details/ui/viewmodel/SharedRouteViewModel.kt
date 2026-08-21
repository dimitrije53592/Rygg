package com.example.rygg.feature.details.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.rygg.core.common.Outcome
import com.example.rygg.core.navigation.SharedRoutePreview
import com.example.rygg.feature.sync.data.RouteSyncManager
import com.example.rygg.feature.sync.data.remote.SharedRoute
import com.example.rygg.feature.sync.data.remote.toPreviewEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SharedRouteViewModel @Inject constructor(
    private val routeSyncManager: RouteSyncManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val token = savedStateHandle.toRoute<SharedRoutePreview>().token
    private var shared: SharedRoute? = null

    private val _uiState = MutableStateFlow(SharedRouteUiState())
    val uiState: StateFlow<SharedRouteUiState> = _uiState.asStateFlow()

    init {
        resolve()
    }

    private fun resolve() {
        viewModelScope.launch {
            _uiState.update { it.copy(details = DetailsUiState(DetailsLoadingState.Loading)) }
            when (val outcome = routeSyncManager.resolveSharedRoute(token)) {
                is Outcome.Success -> {
                    shared = outcome.data
                    _uiState.update {
                        it.copy(
                            details = DetailsUiState(
                                DetailsLoadingState.Loaded(
                                    entry = outcome.data.route.toPreviewEntry(),
                                    elevationProfile = emptyList()
                                )
                            )
                        )
                    }
                }

                is Outcome.Error ->
                    _uiState.update {
                        it.copy(details = DetailsUiState(DetailsLoadingState.Error(outcome.cause.message)))
                    }

                Outcome.Loading -> Unit
            }
        }
    }

    fun onSaveCopy() {
        val toSave = shared ?: return
        if (_uiState.value.isSaving) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            when (val outcome = routeSyncManager.saveSharedCopy(toSave)) {
                is Outcome.Success ->
                    _uiState.update { it.copy(isSaving = false, savedEntryId = outcome.data) }

                is Outcome.Error ->
                    _uiState.update { it.copy(isSaving = false, error = outcome.cause.message) }

                Outcome.Loading -> Unit
            }
        }
    }
}

data class SharedRouteUiState(
    val details: DetailsUiState = DetailsUiState(DetailsLoadingState.Loading),
    val isSaving: Boolean = false,
    val savedEntryId: Long? = null,
    val error: String? = null
)
