package com.dimitrijeilic.rygg.feature.offline.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dimitrijeilic.rygg.core.common.Outcome
import com.dimitrijeilic.rygg.core.common.asResult
import com.dimitrijeilic.rygg.feature.offline.data.OfflineRegionRepository
import com.dimitrijeilic.rygg.feature.offline.domain.OfflineRegion
import com.dimitrijeilic.rygg.feature.offline.domain.RegionStatus
import com.dimitrijeilic.rygg.feature.offline.service.OfflineDownloadService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OfflineMapsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: OfflineRegionRepository
) : ViewModel() {
    private val _error = MutableSharedFlow<Unit>()
    val error = _error.asSharedFlow()

    val uiState: StateFlow<OfflineMapsUiState> = repository.observeRegions()
        .asResult()
        .map { outcome ->
            OfflineMapsUiState(
                loadingState = when (outcome) {
                    Outcome.Loading -> RegionsLoadingState.Loading
                    is Outcome.Success -> RegionsLoadingState.Loaded(
                        regions = outcome.data,
                        totalBytes = outcome.data.sumOf { it.sizeBytes },
                        freeBytes = repository.freeBytes()
                    )
                    is Outcome.Error -> RegionsLoadingState.Error(outcome.cause.message)
                }
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), OfflineMapsUiState())

    fun onResume(id: Long) = launchAction(startsDownload = id) { repository.resume(id) }

    fun onPause(id: Long) = launchAction { repository.pause(id) }

    fun onUpdate(id: Long) = launchAction(startsDownload = id) { repository.update(id) }

    fun onRename(id: Long, name: String) = launchAction { repository.rename(id, name) }

    fun onDelete(id: Long) = launchAction { repository.delete(id) }

    private fun launchAction(startsDownload: Long? = null, action: suspend () -> Outcome<*>) {
        viewModelScope.launch {
            when (action()) {
                is Outcome.Success -> startsDownload?.let { OfflineDownloadService.start(context, it) }
                is Outcome.Error -> _error.emit(Unit)
                Outcome.Loading -> Unit
            }
        }
    }
}

data class OfflineMapsUiState(
    val loadingState: RegionsLoadingState = RegionsLoadingState.Loading
)

sealed interface RegionsLoadingState {
    data object Loading : RegionsLoadingState

    data class Loaded(
        val regions: List<OfflineRegion>,
        val totalBytes: Long,
        val freeBytes: Long
    ) : RegionsLoadingState {
        val hasActiveDownload: Boolean get() = regions.any { it.status == RegionStatus.Downloading }
    }

    data class Error(val message: String?) : RegionsLoadingState
}
