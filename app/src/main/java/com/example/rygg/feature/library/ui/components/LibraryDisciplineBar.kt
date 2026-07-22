package com.example.rygg.feature.library.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.example.rygg.R
import com.example.rygg.core.ui.theme.RyggColor
import com.example.rygg.core.ui.theme.RyggTheme
import com.example.rygg.core.ui.utils.capitalize
import com.example.rygg.feature.auth.domain.Discipline

@Composable
internal fun LibraryDisciplineBar(
    disciplines: List<Discipline>,
    selectedDiscipline: Discipline?,
    onDisciplineSelected: (Discipline?) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(RyggTheme.getColor(RyggColor.BrandGraphite))
            .padding(
                horizontal = RyggTheme.dimens.commonContentPadding12,
                vertical = RyggTheme.dimens.commonContentPadding12
            )
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(
            space = RyggTheme.dimens.commonSpacing4,
            alignment = Alignment.Start
        )
    ) {
        DisciplineChip(
            title = stringResource(R.string.library_filter_all),
            selected = selectedDiscipline == null,
            onClick = { onDisciplineSelected(null) }
        )
        disciplines.forEach { discipline ->
            DisciplineChip(
                title = discipline.name,
                iconRes = discipline.iconRes,
                selected = selectedDiscipline == discipline,
                onClick = { onDisciplineSelected(discipline) }
            )
        }
    }
}

@Composable
private fun DisciplineChip(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    @DrawableRes iconRes: Int? = null
) {
    val containerColor = if (selected) {
        RyggTheme.getColor(RyggColor.BrandGreen)
    } else {
        RyggTheme.getColor(RyggColor.TextSecondary).copy(alpha = 0.7f)
    }
    Button(
        colors = ButtonDefaults.buttonColors(containerColor = containerColor),
        modifier = Modifier.height(RyggTheme.dimens.buttonSize32),
        onClick = onClick
    ) {
        iconRes?.let {
            Icon(
                painter = painterResource(iconRes),
                tint = RyggTheme.getColor(RyggColor.SurfaceElevated),
                contentDescription = null,
                modifier = Modifier.size(RyggTheme.dimens.iconSize16)
            )
            Spacer(Modifier.size(RyggTheme.dimens.commonSpacing4))
        }
        Text(
            text = title.capitalize(),
            color = RyggTheme.getColor(RyggColor.SurfaceElevated),
            style = RyggTheme.typography.labelSmall
        )
    }
}
