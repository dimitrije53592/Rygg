package com.example.rygg.feature.home.ui.viewmodel

import android.Manifest
import androidx.annotation.RequiresPermission
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rygg.core.common.Outcome
import com.example.rygg.core.common.asResult
import com.example.rygg.core.notification.NotificationHelper
import com.example.rygg.feature.home.data.ItemRepository
import com.example.rygg.feature.home.ui.screen.HomeUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val itemRepository: ItemRepository,
    private val notificationHelper: NotificationHelper
) : ViewModel() {
    val uiState: StateFlow<HomeUiState> = itemRepository.observeItems()
        .asResult()
        .map { outcome ->
            when (outcome) {
                Outcome.Loading -> HomeUiState(isLoading = true)
                is Outcome.Success -> HomeUiState(items = outcome.data, isLoading = false)
                is Outcome.Error -> HomeUiState(isLoading = false, errorMessage = outcome.cause.message)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState()
        )

    fun addItem(title: String) {
        val trimmed = title.trim()
        if (trimmed.isEmpty()) return

        viewModelScope.launch { itemRepository.addItem(trimmed) }
    }

    fun deleteItem(id: Long) {
        viewModelScope.launch { itemRepository.deleteItem(id) }
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun sendNotification(title: String, message: String) {
        notificationHelper.notify(title = title, message = message)
    }
}
