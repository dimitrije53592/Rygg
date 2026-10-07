package com.example.rygg.feature.library.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.example.rygg.R
import com.example.rygg.core.ui.components.pressScale
import com.example.rygg.core.ui.theme.RyggColor
import com.example.rygg.core.ui.theme.RyggMotion
import com.example.rygg.core.ui.theme.RyggShapes
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
            .background(RyggTheme.getColor(RyggColor.SurfaceDim))
            .horizontalScroll(rememberScrollState())
            .padding(
                horizontal = RyggTheme.dimens.commonContentPadding16,
                vertical = RyggTheme.dimens.commonContentPadding12
            ),
        horizontalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing8)
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

// Selected reads as a filled brand chip; unselected is an outline on the page ground rather than a
// grey fill, so the bar does not compete with the cards below it.
@Composable
private fun DisciplineChip(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    @DrawableRes iconRes: Int? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val container by animateColorAsState(
        targetValue = if (selected) {
            RyggTheme.getColor(RyggColor.BrandGreen)
        } else {
            RyggTheme.getColor(RyggColor.Surface)
        },
        animationSpec = RyggMotion.effects(),
        label = "chipContainer"
    )
    val content by animateColorAsState(
        targetValue = if (selected) {
            RyggTheme.getColor(RyggColor.OnBrand)
        } else {
            RyggTheme.getColor(RyggColor.TextSecondary)
        },
        animationSpec = RyggMotion.effects(),
        label = "chipContent"
    )

    Row(
        modifier = Modifier
            .pressScale(interactionSource)
            .clip(RyggShapes.chip)
            .background(container)
            .then(
                if (selected) {
                    Modifier
                } else {
                    Modifier.border(
                        width = RyggTheme.dimens.border1,
                        color = RyggTheme.getColor(RyggColor.Outline),
                        shape = RyggShapes.chip
                    )
                }
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .defaultMinSize(minHeight = RyggTheme.dimens.buttonSize40)
            .padding(horizontal = RyggTheme.dimens.commonContentPadding16),
        verticalAlignment = Alignment.CenterVertically
    ) {
        iconRes?.let {
            Icon(
                painter = painterResource(iconRes),
                tint = content,
                contentDescription = null,
                modifier = Modifier.size(RyggTheme.dimens.iconSize16)
            )
            Spacer(Modifier.size(RyggTheme.dimens.commonSpacing8))
        }
        Text(
            text = title.capitalize(),
            color = content,
            style = RyggTheme.typography.labelLarge
        )
    }
}
