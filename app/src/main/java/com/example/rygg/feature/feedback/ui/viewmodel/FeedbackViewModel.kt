package com.example.rygg.feature.feedback.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rygg.core.common.Outcome
import com.example.rygg.feature.feedback.data.FeedbackRepository
import com.example.rygg.feature.feedback.domain.FeedbackCategory
import com.example.rygg.feature.settings.data.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FeedbackViewModel @Inject constructor(
    private val feedbackRepository: FeedbackRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    private val state = MutableStateFlow(FeedbackUiState())

    // One-shot outcomes handed to the wrapper, which owns the Context to toast and navigate on.
    private val _events = Channel<FeedbackEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    // Nothing to combine — the form is driven entirely by the user, so the backing flow is the state.
    val uiState: StateFlow<FeedbackUiState> = state.asStateFlow()

    fun onMessageChanged(message: String) {
        // Hard cap rather than a validation error: the rule rejects longer messages, and the
        // unawaited write would fail out of sight after the user was told it sent.
        if (message.length > FeedbackRepository.MAX_MESSAGE_LENGTH) return
        state.update { it.copy(message = message) }
    }

    fun onCategorySelected(category: FeedbackCategory) {
        state.update { it.copy(category = category) }
    }

    fun onSend() {
        val current = state.value
        if (!current.canSend) return

        viewModelScope.launch {
            val now = System.currentTimeMillis()
            if (now - settingsRepository.lastFeedbackSentAt() < THROTTLE_WINDOW_MS) {
                _events.send(FeedbackEvent.Throttled)
                return@launch
            }

            state.update { it.copy(isSending = true) }
            val outcome = feedbackRepository.submit(current.category, current.message)
            state.update { it.copy(isSending = false) }

            when (outcome) {
                is Outcome.Success -> {
                    settingsRepository.setLastFeedbackSentAt(now)
                    _events.send(FeedbackEvent.Sent)
                }

                is Outcome.Error -> _events.send(FeedbackEvent.Failed)
                Outcome.Loading -> Unit
            }
        }
    }

    private companion object {
        const val THROTTLE_WINDOW_MS = 30_000L
    }
}

// One-shot events the wrapper turns into a toast plus a trip back to where the user came from.
sealed interface FeedbackEvent {
    data object Sent : FeedbackEvent

    data object Failed : FeedbackEvent

    data object Throttled : FeedbackEvent
}

data class FeedbackUiState(
    val message: String = "",
    val category: FeedbackCategory = FeedbackCategory.BUG,
    val isSending: Boolean = false
) {
    val canSend: Boolean get() = message.isNotBlank() && !isSending
}
