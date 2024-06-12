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

package proton.android.pass.features.secure.links.list.ui.grid

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Radius
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.item.icon.AliasIcon
import me.proton.core.presentation.R as CoreR

@Composable
internal fun SecureLinksListGridCell(
    modifier: Modifier = Modifier,
    onCellClick: () -> Unit,
    onCellOptionsClick: () -> Unit,
    index: Int
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = PassTheme.colors.interactionNormMinor2,
                shape = RoundedCornerShape(size = Radius.small)
            )
            .clickable { onCellClick() }
    ) {
        Column(
            modifier = Modifier
                .padding(all = Spacing.medium)
                .align(alignment = Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(space = Spacing.small)
        ) {
            SecureLinksListGridCellIcon()

            SecureLinksListGridCellInfo(
                title = "Title $index",
                expiration = "Expiration $index",
                views = "Views $index"
            )
        }

        SecureLinksGridCellMenu(
            onClick = onCellOptionsClick
        )
    }
}

@Composable
private fun SecureLinksListGridCellIcon(
    modifier: Modifier = Modifier
) {
    AliasIcon(modifier = modifier)
}

@Composable
private fun SecureLinksListGridCellInfo(
    modifier: Modifier = Modifier,
    title: String,
    expiration: String,
    views: String
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(space = Spacing.extraSmall)
    ) {
        Text(
            text = title,
            style = ProtonTheme.typography.body2Medium,
            color = PassTheme.colors.textNorm
        )

        Text(
            text = expiration,
            style = ProtonTheme.typography.captionRegular,
            color = PassTheme.colors.textWeak
        )

        Text(
            text = views,
            style = ProtonTheme.typography.captionRegular,
            color = PassTheme.colors.textWeak
        )
    }
}

@Composable
private fun BoxScope.SecureLinksGridCellMenu(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    IconButton(
        modifier = modifier.align(alignment = Alignment.TopEnd),
        onClick = onClick
    ) {
        Icon(
            painter = painterResource(id = CoreR.drawable.ic_proton_three_dots_vertical),
            contentDescription = null,
            tint = ProtonTheme.colors.iconWeak
        )
    }
}

@[Preview Composable]
internal fun SecureLinksListGridCellPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    PassTheme(isDark = isDark) {
        Surface {
            SecureLinksListGridCell(
                index = 1,
                onCellClick = {},
                onCellOptionsClick = {}
            )
        }
    }
}
