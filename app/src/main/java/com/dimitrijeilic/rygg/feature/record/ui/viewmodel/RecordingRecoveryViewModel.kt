package com.dimitrijeilic.rygg.feature.record.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dimitrijeilic.rygg.core.gpx.haversineMeters
import com.dimitrijeilic.rygg.feature.auth.domain.Discipline
import com.dimitrijeilic.rygg.feature.record.data.RecordingController
import com.dimitrijeilic.rygg.feature.record.domain.RecordingState
import com.dimitrijeilic.rygg.feature.record.domain.RecoveredRecording
import com.dimitrijeilic.rygg.feature.record.service.RecordingService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

// Offers to recover a recording whose process was killed before the user saved it.
@HiltViewModel
class RecordingRecoveryViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val recordingController: RecordingController
) : ViewModel() {
    private val pending = MutableStateFlow<RecoveredRecording?>(null)

    private val _openPreview = MutableSharedFlow<Unit>()
    val openPreview: SharedFlow<Unit> = _openPreview.asSharedFlow()

    // Hidden as soon as a recording is live, e.g. when the restarted service recovered it first.
    val uiState: StateFlow<RecordingRecoveryUiState> =
        combine(pending, recordingController.snapshot) { recovered, snapshot ->
            RecordingRecoveryUiState(
                pending = recovered
                    ?.takeIf { snapshot.state == RecordingState.IDLE }
                    ?.toSummary()
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = RecordingRecoveryUiState()
        )

    init {
        viewModelScope.launch { pending.value = recordingController.pendingRecovery() }
    }

    fun onResume() {
        pending.value = null
        RecordingService.recover(context)
    }

    fun onSave() {
        pending.value = null
        viewModelScope.launch {
            if (recordingController.recover() != null) _openPreview.emit(Unit)
        }
    }

    fun onDiscard() {
        pending.value = null
        recordingController.reset()
    }

    fun onDismiss() {
        pending.value = null
    }
}

private fun RecoveredRecording.toSummary() = RecoverySummary(
    discipline = discipline,
    elapsedMillis = elapsedMillis,
    distanceMeters = segments.sumOf { segment ->
        segment.zipWithNext().sumOf { (a, b) -> haversineMeters(a.lat, a.lon, b.lat, b.lon) }
    }
)

data class RecordingRecoveryUiState(
    val pending: RecoverySummary? = null
)

data class RecoverySummary(
    val discipline: Discipline,
    val elapsedMillis: Long,
    val distanceMeters: Double
)
