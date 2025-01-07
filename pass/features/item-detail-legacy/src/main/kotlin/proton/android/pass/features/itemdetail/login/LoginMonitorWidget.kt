/*
 * Copyright (c) 2024 Proton AG
 * This file is part of Proton AG and Proton Pass.
 *
 * Proton Pass is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Pass is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Pass.  If not, see <https://www.gnu.org/licenses/>.
 */

package proton.android.pass.features.itemdetail.login

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Radius
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.body3Norm
import proton.android.pass.composecomponents.impl.R
import proton.android.pass.composecomponents.impl.container.roundedContainer
import proton.android.pass.composecomponents.impl.utils.PassItemColors
import proton.android.pass.composecomponents.impl.utils.passItemColors
import proton.android.pass.domain.items.ItemCategory

@Composable
internal fun LoginMonitorWidget(
    modifier: Modifier = Modifier,
    title: String,
    @StringRes subtitleResId: Int? = null,
    itemColors: PassItemColors = passItemColors(itemCategory = ItemCategory.Note),
    additionalContent: (@Composable ColumnScope.() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .roundedContainer(
                backgroundColor = itemColors.minorSecondary,
                borderColor = itemColors.minorSecondary
            )
            .padding(all = Spacing.medium),
        horizontalArrangement = Arrangement.spacedBy(space = Spacing.small)
    ) {
        Icon(
            modifier = Modifier
                .size(size = 24.dp)
                .clip(shape = RoundedCornerShape(Radius.small))
                .background(color = itemColors.minorPrimary)
                .padding(all = Spacing.extraSmall),
            painter = painterResource(id = R.drawable.ic_exclamation_mark),
            contentDescription = null,
            tint = itemColors.majorSecondary
        )

        Column(
            modifier = Modifier.weight(weight = 1f),
            verticalArrangement = Arrangement.spacedBy(space = Spacing.small)
        ) {
            Text(
                text = title,
                color = itemColors.majorSecondary,
                style = ProtonTheme.typography.body1Medium
            )

            additionalContent?.let { content -> content() }

            subtitleResId?.let { id ->
                Text(
                    text = stringResource(id = id),
                    color = itemColors.majorSecondary,
                    style = PassTheme.typography.body3Norm()
                )
            }
        }
    }
}
