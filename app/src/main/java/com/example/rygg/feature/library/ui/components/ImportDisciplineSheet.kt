package com.example.rygg.feature.library.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.example.rygg.R
import com.example.rygg.core.ui.theme.RyggColor
import com.example.rygg.core.ui.theme.RyggShapes
import com.example.rygg.core.ui.theme.RyggTheme
import com.example.rygg.core.ui.utils.capitalize
import com.example.rygg.feature.auth.domain.Discipline

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ImportDisciplineSheet(
    onDisciplinePicked: (Discipline) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(),
        containerColor = RyggTheme.getColor(RyggColor.SurfaceElevated),
        shape = RyggShapes.sheet
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = RyggTheme.dimens.commonContentPadding16)
        ) {
            Text(
                text = stringResource(R.string.library_import),
                style = RyggTheme.typography.titleLarge,
                color = RyggTheme.getColor(RyggColor.TextPrimary),
                modifier = Modifier.padding(
                    horizontal = RyggTheme.dimens.commonContentPadding24,
                    vertical = RyggTheme.dimens.commonContentPadding12
                )
            )
            Discipline.entries.forEach { discipline ->
                DisciplineRow(
                    discipline = discipline,
                    onClick = { onDisciplinePicked(discipline) }
                )
            }
        }
    }
}

@Composable
private fun DisciplineRow(
    discipline: Discipline,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(
                horizontal = RyggTheme.dimens.commonContentPadding24,
                vertical = RyggTheme.dimens.commonContentPadding12
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing16)
    ) {
        Box(
            modifier = Modifier
                .size(RyggTheme.dimens.iconSize40)
                .clip(RyggShapes.chip)
                .background(RyggTheme.getColor(RyggColor.MossSurface)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(discipline.iconRes),
                contentDescription = null,
                tint = RyggTheme.getColor(RyggColor.BrandGreen),
                modifier = Modifier.size(RyggTheme.dimens.iconSize24)
            )
        }
        Text(
            text = discipline.name.capitalize(),
            style = RyggTheme.typography.bodyLarge,
            color = RyggTheme.getColor(RyggColor.TextPrimary)
        )
    }
}
