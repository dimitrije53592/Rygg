package com.dimitrijeilic.rygg.feature.details.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.dimitrijeilic.rygg.core.common.Outcome
import com.dimitrijeilic.rygg.core.common.asResult
import com.dimitrijeilic.rygg.core.navigation.PhotoViewer
import com.dimitrijeilic.rygg.feature.library.data.RouteImageRepository
import com.dimitrijeilic.rygg.feature.library.domain.RouteImage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PhotoViewerViewModel @Inject constructor(
    private val routeImageRepository: RouteImageRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val route = savedStateHandle.toRoute<PhotoViewer>()

    val uiState: StateFlow<PhotoViewerUiState> =
        routeImageRepository.observeImages(route.entryId).asResult()
            .map { outcome ->
                PhotoViewerUiState(
                    startImageId = route.imageId,
                    loadingState = when (outcome) {
                        Outcome.Loading -> PhotoViewerLoadingState.Loading
                        is Outcome.Error -> PhotoViewerLoadingState.Error
                        is Outcome.Success -> PhotoViewerLoadingState.Loaded(outcome.data)
                    }
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = PhotoViewerUiState(startImageId = route.imageId)
            )

    fun onSetCover(imageId: String) {
        viewModelScope.launch { routeImageRepository.setCover(route.entryId, imageId) }
    }

    fun onDeletePhoto(imageId: String) {
        viewModelScope.launch { routeImageRepository.deleteImage(imageId) }
    }
}

data class PhotoViewerUiState(
    val startImageId: String,
    val loadingState: PhotoViewerLoadingState = PhotoViewerLoadingState.Loading
)

sealed interface PhotoViewerLoadingState {
    data object Loading : PhotoViewerLoadingState

    data class Loaded(val images: List<RouteImage>) : PhotoViewerLoadingState

    data object Error : PhotoViewerLoadingState
}
