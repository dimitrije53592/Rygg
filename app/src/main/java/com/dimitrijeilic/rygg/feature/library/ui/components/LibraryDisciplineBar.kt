package com.dimitrijeilic.rygg.feature.library.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.dimitrijeilic.rygg.R
import com.dimitrijeilic.rygg.core.ui.theme.RyggColor
import com.dimitrijeilic.rygg.core.ui.theme.RyggTheme
import com.dimitrijeilic.rygg.feature.auth.domain.Discipline

@Composable
internal fun LibraryDisciplineBar(
    disciplines: List<Discipline>,
    selectedDiscipline: Discipline?,
    onDisciplineSelected: (Discipline?) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(RyggTheme.getColor(RyggColor.SurfaceDim))
            .horizontalScroll(rememberScrollState())
            .padding(
                horizontal = RyggTheme.dimens.commonContentPadding16,
                vertical = RyggTheme.dimens.commonContentPadding12
            ),
        horizontalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing8)
    ) {
        LibraryFilterChip(
            title = stringResource(R.string.library_filter_all),
            selected = selectedDiscipline == null,
            onClick = { onDisciplineSelected(null) }
        )
        disciplines.forEach { discipline ->
            LibraryFilterChip(
                title = stringResource(discipline.labelRes),
                iconRes = discipline.iconRes,
                selected = selectedDiscipline == discipline,
                onClick = { onDisciplineSelected(discipline) }
            )
        }
    }
}
