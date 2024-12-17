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

package proton.android.pass.featurehome.impl.shares.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemedBooleanPreviewProvider
import proton.android.pass.composecomponents.impl.badge.CircledBadge
import proton.android.pass.composecomponents.impl.badge.OverlayBadge
import proton.android.pass.composecomponents.impl.buttons.PassSharingShareIcon
import proton.android.pass.composecomponents.impl.icon.VaultIcon
import proton.android.pass.composecomponents.impl.item.icon.ThreeDotsMenuButton
import proton.android.pass.composecomponents.impl.text.Text
import proton.android.pass.domain.items.ItemCategory
import proton.android.pass.featurehome.impl.R
import me.proton.core.presentation.R as CoreR
import proton.android.pass.composecomponents.impl.R as CompR

@Composable
internal fun SharesDrawerShareRow(
    modifier: Modifier = Modifier,
    @DrawableRes shareIconRes: Int,
    iconColor: Color,
    iconBackgroundColor: Color,
    name: String,
    itemsCount: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    membersCount: Int = 0,
    onShareClick: (() -> Unit)? = null,
    onMenuOptionsClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(
                start = Spacing.medium,
                top = Spacing.mediumSmall,
                bottom = Spacing.mediumSmall
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OverlayBadge(
            isShown = isSelected,
            content = {
                VaultIcon(
                    icon = shareIconRes,
                    iconColor = iconColor,
                    backgroundColor = iconBackgroundColor
                )
            },
            badge = {
                CircledBadge(
                    ratio = 0.75F,
                    icon = CoreR.drawable.ic_proton_checkmark,
                    iconColor = PassTheme.colors.interactionNormMinor1,
                    backgroundColor = PassTheme.colors.interactionNormMajor2
                )
            }
        )

        Column(
            modifier = Modifier
                .weight(weight = 1f)
                .padding(start = Spacing.mediumSmall),
            verticalArrangement = Arrangement.spacedBy(space = Spacing.extraSmall)
        ) {
            Text.Body1Regular(
                text = name
            )

            Text.Body2Regular(
                text = pluralStringResource(
                    R.plurals.vault_drawer_vaults_item_count,
                    itemsCount,
                    itemsCount
                ),
                color = PassTheme.colors.textWeak
            )
        }

        onShareClick?.let { onClick ->
            PassSharingShareIcon(
                itemCategory = ItemCategory.Unknown,
                shareSharedCount = membersCount,
                onClick = onClick
            )
        }

        onMenuOptionsClick?.let { onClick ->
            ThreeDotsMenuButton(
                onClick = onClick
            )
        }
    }
}

@[Preview Composable]
internal fun ShareDrawerVaultRowPreview(
    @PreviewParameter(ThemedBooleanPreviewProvider::class) input: Pair<Boolean, Boolean>
) {
    val (isDark, isSelected) = input

    PassTheme(isDark = isDark) {
        Surface {
            SharesDrawerShareRow(
                shareIconRes = CompR.drawable.ic_brand_pass,
                iconColor = PassTheme.colors.interactionNormMajor2,
                iconBackgroundColor = PassTheme.colors.interactionNormMinor1,
                name = "Share name",
                itemsCount = 16,
                membersCount = 5,
                isSelected = isSelected,
                onClick = {},
                onShareClick = {},
                onMenuOptionsClick = {}
            )
        }
    }
}
