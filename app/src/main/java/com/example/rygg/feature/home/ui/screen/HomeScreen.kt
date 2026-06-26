package com.example.rygg.feature.home.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.rygg.R
import com.example.rygg.core.ui.theme.RyggColor
import com.example.rygg.core.ui.theme.RyggTheme
import com.example.rygg.feature.home.domain.Item
import com.example.rygg.feature.home.ui.components.ItemRow

@Composable
fun HomeScreen(params: HomeScreenParams) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(RyggTheme.dimens.commonContentPadding16),
        verticalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing12)
    ) {
        var input by rememberSaveable { mutableStateOf("") }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing8),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.weight(1f),
                singleLine = true,
                label = { Text(stringResource(R.string.home_add_item_hint)) }
            )
            Button(
                onClick = {
                    params.onAddItem(input)
                    input = ""
                }
            ) {
                Text(stringResource(R.string.home_add_button))
            }
        }

        OutlinedButton(
            onClick = params.onSendNotification,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.home_send_notification))
        }

        val errorMessage = params.uiState.errorMessage
        when {
            params.uiState.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            errorMessage != null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = errorMessage,
                        style = RyggTheme.typography.bodyMedium,
                        color = RyggTheme.getColor(RyggColor.Error)
                    )
                }
            }

            params.uiState.items.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = stringResource(R.string.home_empty),
                        style = RyggTheme.typography.bodyMedium,
                        color = RyggTheme.getColor(RyggColor.TextSecondary)
                    )
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing4)
                ) {
                    items(items = params.uiState.items, key = { it.id }) { item ->
                        ItemRow(
                            item = item,
                            onClick = { params.onItemClick(item.id.toString()) },
                            onDelete = { params.onDeleteItem(item.id) }
                        )
                        HorizontalDivider(color = RyggTheme.getColor(RyggColor.Divider))
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    RyggTheme {
        HomeScreen(
            params = HomeScreenParams(
                uiState = HomeUiState(
                    items = listOf(
                        Item(id = 1, title = "First item", createdAt = 0),
                        Item(id = 2, title = "Second item", createdAt = 0)
                    ),
                    isLoading = false
                ),
                onAddItem = {},
                onDeleteItem = {},
                onItemClick = {},
                onSendNotification = {}
            )
        )
    }
}

data class HomeUiState(
    val items: List<Item> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

data class HomeScreenParams(
    val uiState: HomeUiState,
    val onAddItem: (String) -> Unit,
    val onDeleteItem: (Long) -> Unit,
    val onItemClick: (String) -> Unit,
    val onSendNotification: () -> Unit
)
