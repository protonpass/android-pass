/*
 * Copyright (c) 2023 Proton AG
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

package proton.android.pass.features.itemdetail.creditcard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.badge.CircledBadge
import proton.android.pass.composecomponents.impl.badge.OverlayBadge
import proton.android.pass.composecomponents.impl.item.icon.CreditCardIcon
import proton.android.pass.domain.Share
import proton.android.pass.features.itemdetail.common.ItemTitleInput
import proton.android.pass.features.itemdetail.common.ItemTitleText
import proton.android.pass.features.itemdetail.common.ThemeItemTitleProvider
import proton.android.pass.features.itemdetail.common.VaultNameSubtitle

@Composable
fun CreditCardTitle(
    modifier: Modifier = Modifier,
    title: String,
    share: Share,
    onShareClick: () -> Unit,
    isPinned: Boolean,
    hasMoreThanOneVaultShare: Boolean
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = Spacing.medium)
    ) {
        OverlayBadge(
            isShown = isPinned,
            badge = {
                CircledBadge(
                    backgroundColor = PassTheme.colors.cardInteractionNormMajor1
                )
            },
            content = {
                CreditCardIcon(size = 60, shape = PassTheme.shapes.squircleMediumLargeShape)
            }
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(space = Spacing.small)
        ) {
            ItemTitleText(text = title)

            VaultNameSubtitle(
                share = share,
                onClick = onShareClick,
                hasMoreThanOneVaultShare = hasMoreThanOneVaultShare
            )
        }
    }
}

@Preview
@Composable
fun AliasTitlePreview(@PreviewParameter(ThemeItemTitleProvider::class) input: Pair<Boolean, ItemTitleInput>) {
    val (isDark, params) = input

    PassTheme(isDark = isDark) {
        Surface {
            CreditCardTitle(
                title = params.itemUiModel.contents.title,
                share = params.share,
                onShareClick = {},
                isPinned = params.itemUiModel.isPinned,
                hasMoreThanOneVaultShare = true
            )
        }
    }
}
