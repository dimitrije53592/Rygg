package com.example.rygg.feature.record.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rygg.feature.record.data.RecordingController
import com.example.rygg.feature.record.domain.RecordingState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

// Feeds the app chrome (the Record FAB) so a running recording stays visible from every tab.
@HiltViewModel
class LiveRecordingViewModel @Inject constructor(
    recordingController: RecordingController
) : ViewModel() {
    val liveRecording: StateFlow<LiveRecording?> =
        recordingController.snapshot
            .map { snapshot ->
                if (snapshot.state == RecordingState.IDLE) {
                    null
                } else {
                    LiveRecording(
                        elapsedMillis = snapshot.elapsedMillis,
                        isPaused = snapshot.state != RecordingState.RECORDING
                    )
                }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}

data class LiveRecording(
    val elapsedMillis: Long,
    val isPaused: Boolean
)
