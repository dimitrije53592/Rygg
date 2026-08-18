package com.example.rygg.feature.record.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rygg.feature.auth.domain.Discipline
import com.example.rygg.feature.record.data.RecordingController
import com.example.rygg.feature.record.domain.RecordingState
import com.example.rygg.feature.record.service.RecordingService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class RecordViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val recordingController: RecordingController
) : ViewModel() {
    private val selectedDiscipline = MutableStateFlow(Discipline.HIKE)

    val uiState: StateFlow<RecordUiState> =
        combine(recordingController.snapshot, selectedDiscipline) { snapshot, discipline ->
            RecordUiState(
                state = snapshot.state,
                discipline = if (snapshot.state == RecordingState.IDLE) discipline else snapshot.discipline,
                elapsedMillis = snapshot.elapsedMillis,
                distanceMeters = snapshot.distanceMeters,
                currentSpeedMps = snapshot.currentSpeedMps,
                ascentMeters = snapshot.ascentMeters,
                elevationMeters = snapshot.elevationMeters,
                pointCount = snapshot.pointCount,
                waypointCount = snapshot.waypointCount,
                gpsReady = snapshot.gpsReady
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = RecordUiState()
        )

    fun onSelectDiscipline(discipline: Discipline) {
        selectedDiscipline.value = discipline
    }

    fun onStart(discipline: Discipline) {
        RecordingService.start(context, discipline)
    }

    fun onPause() {
        RecordingService.pause(context)
    }

    fun onResume() {
        RecordingService.resume(context)
    }

    fun onStop() {
        RecordingService.stop(context)
    }

    fun onAddWaypoint(name: String) {
        recordingController.addWaypoint(name)
    }
}

data class RecordUiState(
    val state: RecordingState = RecordingState.IDLE,
    val discipline: Discipline = Discipline.HIKE,
    val elapsedMillis: Long = 0L,
    val distanceMeters: Double = 0.0,
    val currentSpeedMps: Double = 0.0,
    val ascentMeters: Double = 0.0,
    val elevationMeters: Double? = null,
    val pointCount: Int = 0,
    val waypointCount: Int = 0,
    val gpsReady: Boolean = false
)
