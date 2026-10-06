package com.example.rygg.feature.feedback.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.rygg.R
import com.example.rygg.core.ui.components.RyggCard
import com.example.rygg.core.ui.components.RyggPrimaryButton
import com.example.rygg.core.ui.components.RyggTextField
import com.example.rygg.core.ui.components.RyggTopAppBar
import com.example.rygg.core.ui.theme.RyggColor
import com.example.rygg.core.ui.theme.RyggTheme
import com.example.rygg.feature.feedback.domain.FeedbackCategory
import com.example.rygg.feature.feedback.ui.viewmodel.FeedbackUiState

@Composable
fun FeedbackScreen(params: FeedbackScreenParams) {
    Scaffold(
        topBar = {
            RyggTopAppBar(title = stringResource(R.string.feedback_title), actions = {})
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(RyggTheme.getColor(RyggColor.SurfaceDim))
                .padding(innerPadding)
                // Edge-to-edge is on, so the keyboard would otherwise sit over the message field.
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(RyggTheme.dimens.commonContentPadding16),
            verticalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing24)
        ) {
            FeedbackSection(title = stringResource(R.string.feedback_category_label)) {
                FeedbackCategory.entries.forEach { category ->
                    CategoryRow(
                        label = stringResource(category.labelRes),
                        selected = params.uiState.category == category,
                        onSelect = { params.onCategorySelected(category) }
                    )
                }
            }

            FeedbackSection(title = stringResource(R.string.feedback_message_label)) {
                RyggTextField(
                    value = params.uiState.message,
                    onValueChange = params.onMessageChanged,
                    modifier = Modifier.fillMaxWidth(),
                    isEnabled = !params.uiState.isSending,
                    singleLine = false,
                    minLines = 5,
                    placeholderText = stringResource(R.string.feedback_message_placeholder)
                )
                Text(
                    text = stringResource(R.string.feedback_context_disclosure),
                    style = RyggTheme.typography.bodySmall,
                    color = RyggTheme.getColor(RyggColor.TextSecondary),
                    modifier = Modifier.padding(top = RyggTheme.dimens.commonContentPadding8)
                )
            }

            RyggPrimaryButton(
                text = stringResource(R.string.feedback_send),
                onClick = params.onSend,
                modifier = Modifier.fillMaxWidth(),
                isEnabled = params.uiState.canSend,
                isLoading = params.uiState.isSending
            )
        }
    }
}

@Composable
private fun FeedbackSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing8)) {
        Text(
            text = title,
            style = RyggTheme.typography.titleMedium,
            color = RyggTheme.getColor(RyggColor.TextPrimary),
            modifier = Modifier.padding(start = RyggTheme.dimens.commonContentPadding4)
        )
        RyggCard(content = content)
    }
}

@Composable
private fun CategoryRow(
    label: String,
    selected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, onClick = onSelect)
            .padding(vertical = RyggTheme.dimens.commonContentPadding8),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing8)
    ) {
        RadioButton(selected = selected, onClick = onSelect)
        Text(text = label, style = RyggTheme.typography.bodyLarge)
    }
}

@Preview
@Composable
private fun FeedbackScreenPreview() {
    RyggTheme {
        FeedbackScreen(
            params = FeedbackScreenParams(
                uiState = FeedbackUiState(
                    message = "The elevation profile looks flat on routes I record above the tree line.",
                    category = FeedbackCategory.BUG
                ),
                onMessageChanged = {},
                onCategorySelected = {},
                onSend = {}
            )
        )
    }
}

data class FeedbackScreenParams(
    val uiState: FeedbackUiState,
    val onMessageChanged: (String) -> Unit,
    val onCategorySelected: (FeedbackCategory) -> Unit,
    val onSend: () -> Unit
)
