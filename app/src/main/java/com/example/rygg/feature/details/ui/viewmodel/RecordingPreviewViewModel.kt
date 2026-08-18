package com.example.rygg.feature.details.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rygg.core.common.Outcome
import com.example.rygg.core.gpx.model.ElevationSample
import com.example.rygg.core.ui.utils.capitalize
import com.example.rygg.core.ui.utils.formatDate
import com.example.rygg.feature.library.data.GpxFileEntryRepository
import com.example.rygg.feature.library.domain.GpxFileEntry
import com.example.rygg.feature.record.data.RecordingController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecordingPreviewViewModel @Inject constructor(
    private val gpxFileEntryRepository: GpxFileEntryRepository,
    private val recordingController: RecordingController
) : ViewModel() {
    private var stagedEntry: GpxFileEntry? = null
    private var elevationProfile: List<ElevationSample> = emptyList()

    private val _uiState = MutableStateFlow(DetailsUiState(DetailsLoadingState.Loading))
    val uiState = _uiState.asStateFlow()

    private val _name = MutableStateFlow("")
    val name = _name.asStateFlow()

    private val _finished = MutableSharedFlow<Unit>()
    val finished = _finished.asSharedFlow()

    // One-shot signal that a save failed; the screen stays put and surfaces the error.
    private val _error = MutableSharedFlow<Unit>()
    val error = _error.asSharedFlow()

    init {
        viewModelScope.launch {
            val document = recordingController.buildDocument()
            if (document == null) {
                // Nothing meaningful was recorded (< 2 points) — show the graceful empty state
                // with a discard confirmation rather than a generic error.
                _uiState.value = DetailsUiState(DetailsLoadingState.Empty)
                return@launch
            }
            val discipline = recordingController.currentDiscipline()
            _name.value = "${discipline.name.capitalize()} · ${formatDate(System.currentTimeMillis())}"

            when (val staged = gpxFileEntryRepository.stageRecordedTrack(document, discipline)) {
                is Outcome.Success -> {
                    stagedEntry = staged.data
                    elevationProfile = gpxFileEntryRepository.loadElevationProfile(staged.data)
                    emitLoaded()
                }

                is Outcome.Error -> _uiState.value = DetailsUiState(DetailsLoadingState.Error(staged.cause.message))
                Outcome.Loading -> Unit
            }
        }
    }

    fun onNameChange(value: String) {
        _name.value = value
        emitLoaded()
    }

    fun onSave() {
        viewModelScope.launch {
            val entry = stagedEntry ?: return@launch
            when (gpxFileEntryRepository.persistRecordedTrack(entry, _name.value)) {
                is Outcome.Success -> {
                    recordingController.reset()
                    _finished.emit(Unit)
                }

                is Outcome.Error -> _error.emit(Unit)
                Outcome.Loading -> Unit
            }
        }
    }

    fun onDiscard() {
        viewModelScope.launch {
            stagedEntry?.let { gpxFileEntryRepository.discardStagedFile(it) }
            recordingController.reset()
            _finished.emit(Unit)
        }
    }

    private fun emitLoaded() {
        val entry = stagedEntry ?: return
        _uiState.value = DetailsUiState(
            DetailsLoadingState.Loaded(
                entry = entry.copy(name = _name.value),
                elevationProfile = elevationProfile
            )
        )
    }
}
