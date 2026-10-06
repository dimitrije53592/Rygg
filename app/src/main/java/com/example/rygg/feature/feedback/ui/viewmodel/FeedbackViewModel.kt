package com.example.rygg.feature.feedback.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rygg.core.common.Outcome
import com.example.rygg.feature.feedback.data.FeedbackRepository
import com.example.rygg.feature.feedback.domain.FeedbackCategory
import com.example.rygg.feature.feedback.domain.FeedbackDelivery
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FeedbackViewModel @Inject constructor(
    private val feedbackRepository: FeedbackRepository
) : ViewModel() {
    private val state = MutableStateFlow(FeedbackUiState())

    // Nothing to combine — the form is driven entirely by the user, so the backing flow is the state.
    val uiState: StateFlow<FeedbackUiState> = state.asStateFlow()

    fun onMessageChanged(message: String) {
        // Hard cap rather than a validation error: the rule rejects anything longer.
        if (message.length > FeedbackRepository.MAX_MESSAGE_LENGTH) return
        state.update { it.copy(message = message, sendState = FeedbackSendState.Editing) }
    }

    fun onCategorySelected(category: FeedbackCategory) {
        state.update { it.copy(category = category) }
    }

    fun onSend() {
        val current = state.value
        if (!current.canSend) return

        viewModelScope.launch {
            state.update { it.copy(sendState = FeedbackSendState.Sending) }

            when (val outcome = feedbackRepository.submit(current.category, current.message)) {
                is Outcome.Success ->
                    state.update { it.copy(sendState = FeedbackSendState.Sent(outcome.data)) }

                is Outcome.Error ->
                    state.update {
                        it.copy(sendState = FeedbackSendState.Failed(outcome.cause.message))
                    }

                Outcome.Loading -> Unit
            }
        }
    }
}

data class FeedbackUiState(
    val message: String = "",
    val category: FeedbackCategory = FeedbackCategory.BUG,
    val sendState: FeedbackSendState = FeedbackSendState.Editing
) {
    val canSend: Boolean get() = message.isNotBlank() && sendState !is FeedbackSendState.Sending
}

sealed interface FeedbackSendState {
    data object Editing : FeedbackSendState

    data object Sending : FeedbackSendState

    data class Sent(val delivery: FeedbackDelivery) : FeedbackSendState

    // The cause is shown only in debug builds — it is the one place a send failure is visible,
    // since the app has no logging of its own.
    data class Failed(val cause: String?) : FeedbackSendState
}
