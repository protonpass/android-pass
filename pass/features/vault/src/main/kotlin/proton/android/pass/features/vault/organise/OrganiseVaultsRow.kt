/*
 * Copyright (c) 2025 Proton AG
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

package proton.android.pass.features.vault.organise

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Checkbox
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
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.extension.toColor
import proton.android.pass.composecomponents.impl.extension.toResource
import proton.android.pass.composecomponents.impl.icon.VaultIcon
import proton.android.pass.composecomponents.impl.text.Text
import proton.android.pass.domain.ShareColor
import proton.android.pass.domain.ShareIcon
import proton.android.pass.domain.ShareId
import proton.android.pass.features.vault.R

@Composable
internal fun OrganiseVaultsRow(
    modifier: Modifier = Modifier.Companion,
    shareId: ShareId,
    @DrawableRes shareIconRes: Int,
    iconColor: Color,
    iconBackgroundColor: Color,
    name: String,
    itemsCount: Int,
    isSelected: Boolean,
    onClick: (ShareId, Boolean) -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.mediumSmall),
        verticalAlignment = Alignment.Companion.CenterVertically
    ) {

        Checkbox(
            checked = isSelected,
            onCheckedChange = { onClick(shareId, it) }
        )

        VaultIcon(
            icon = shareIconRes,
            iconColor = iconColor,
            backgroundColor = iconBackgroundColor
        )

        Column(
            modifier = Modifier.Companion
                .weight(weight = 1f)
                .padding(start = Spacing.mediumSmall),
            verticalArrangement = Arrangement.spacedBy(space = Spacing.extraSmall)
        ) {
            Text.Body1Regular(
                text = name
            )

            Text.Body2Regular(
                text = pluralStringResource(
                    R.plurals.organise_vaults_item_count,
                    itemsCount,
                    itemsCount
                ),
                color = PassTheme.colors.textWeak
            )
        }
    }
}

@Preview
@Composable
fun OrganiseVaultsRowPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            OrganiseVaultsRow(
                shareId = ShareId(""),
                shareIconRes = ShareIcon.Icon1.toResource(),
                iconColor = ShareColor.Color1.toColor(),
                iconBackgroundColor = ShareColor.Color1.toColor(isBackground = true),
                name = "Personal",
                itemsCount = 7961,
                isSelected = false,
                onClick = { _, _ -> }
            )
        }
    }
}
