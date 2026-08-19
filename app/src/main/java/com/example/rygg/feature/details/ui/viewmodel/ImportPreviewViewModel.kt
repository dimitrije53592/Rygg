package com.example.rygg.feature.details.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.rygg.core.common.Outcome
import com.example.rygg.core.navigation.ImportPreview
import com.example.rygg.feature.auth.domain.Discipline
import com.example.rygg.feature.library.data.GpxFileEntryRepository
import com.example.rygg.feature.library.domain.GpxFileEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ImportPreviewViewModel @Inject constructor(
    private val gpxFileEntryRepository: GpxFileEntryRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val args = savedStateHandle.toRoute<ImportPreview>()
    private val uri = Uri.parse(Uri.decode(args.uri))
    private val discipline = runCatching { Discipline.valueOf(args.discipline) }
        .getOrDefault(Discipline.HIKE)

    private var stagedEntry: GpxFileEntry? = null

    private val _uiState = MutableStateFlow(DetailsUiState(DetailsLoadingState.Loading))
    val uiState = _uiState.asStateFlow()

    // One-shot signal that the preview is done (saved or discarded) and the screen can pop.
    private val _finished = MutableSharedFlow<Unit>()
    val finished = _finished.asSharedFlow()

    // One-shot signal that a save failed; the screen stays put and surfaces the error.
    private val _error = MutableSharedFlow<Unit>()
    val error = _error.asSharedFlow()

    init {
        viewModelScope.launch {
            _uiState.value = when (val staged = gpxFileEntryRepository.stageGpxFile(uri, discipline)) {
                is Outcome.Success -> {
                    stagedEntry = staged.data
                    DetailsUiState(
                        DetailsLoadingState.Loaded(
                            entry = staged.data,
                            elevationProfile = gpxFileEntryRepository.loadElevationProfile(staged.data)
                        )
                    )
                }

                is Outcome.Error -> DetailsUiState(DetailsLoadingState.Error(staged.cause.message))
                Outcome.Loading -> DetailsUiState(DetailsLoadingState.Loading)
            }
        }
    }

    fun onSave() {
        viewModelScope.launch {
            val entry = stagedEntry ?: return@launch
            when (gpxFileEntryRepository.persistGpxFile(entry)) {
                is Outcome.Success -> _finished.emit(Unit)
                is Outcome.Error -> _error.emit(Unit)
                Outcome.Loading -> Unit
            }
        }
    }

    fun onDiscard() {
        viewModelScope.launch {
            stagedEntry?.let { gpxFileEntryRepository.discardStagedFile(it) }
            _finished.emit(Unit)
        }
    }
}
