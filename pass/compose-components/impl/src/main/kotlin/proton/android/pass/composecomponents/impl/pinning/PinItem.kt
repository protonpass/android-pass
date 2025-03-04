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

package proton.android.pass.composecomponents.impl.pinning

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.captionStrongNorm
import proton.android.pass.common.api.ellipsize
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.composecomponents.impl.container.roundedContainer
import proton.android.pass.composecomponents.impl.item.icon.AliasIcon
import proton.android.pass.composecomponents.impl.item.icon.CreditCardIcon
import proton.android.pass.composecomponents.impl.item.icon.CustomIcon
import proton.android.pass.composecomponents.impl.item.icon.IdentityIcon
import proton.android.pass.composecomponents.impl.item.icon.LoginIcon
import proton.android.pass.composecomponents.impl.item.icon.NoteIcon
import proton.android.pass.domain.ItemContents

private const val ICON_SIZE = 24
private const val TEXT_MAX_LENGTH_BEFORE_ELLIPSE = 20

@Composable
fun PinItem(
    modifier: Modifier = Modifier,
    item: ItemUiModel,
    canLoadExternalImages: Boolean,
    onItemClick: (ItemUiModel) -> Unit
) {
    val pinBackgroundColor = when (item.contents) {
        is ItemContents.Note -> PassTheme.colors.noteInteractionNormMinor1
        is ItemContents.Login -> PassTheme.colors.loginInteractionNormMinor1
        is ItemContents.Alias -> PassTheme.colors.aliasInteractionNormMinor1
        is ItemContents.CreditCard -> PassTheme.colors.cardInteractionNormMinor1
        is ItemContents.Identity -> PassTheme.colors.interactionNormMinor1
        is ItemContents.WifiNetwork,
        is ItemContents.SSHKey,
        is ItemContents.Custom -> PassTheme.colors.loginInteractionNormMinor1
        is ItemContents.Unknown -> Color.Transparent
    }
    Row(
        modifier = modifier
            .roundedContainer(
                backgroundColor = pinBackgroundColor,
                borderColor = Color.Transparent
            )
            .clickable { onItemClick(item) }
            .padding(Spacing.small),
        horizontalArrangement = Arrangement.spacedBy(Spacing.small),
        verticalAlignment = Alignment.CenterVertically
    ) {
        when (val contents = item.contents) {
            is ItemContents.Note -> NoteIcon(
                modifier = Modifier.size(ICON_SIZE.dp),
                shape = PassTheme.shapes.squircleSmallShape,
                backgroundColor = PassTheme.colors.noteInteractionNormMinor2
            )

            is ItemContents.Login -> LoginIcon(
                modifier = Modifier.size(ICON_SIZE.dp),
                text = contents.title,
                shape = PassTheme.shapes.squircleSmallShape,
                canLoadExternalImages = canLoadExternalImages,
                size = ICON_SIZE,
                favIconPadding = 2.dp,
                website = contents.urls.firstOrNull(),
                packageName = contents.packageInfoSet.firstOrNull()?.packageName?.value,
                backgroundColor = PassTheme.colors.loginInteractionNormMinor2
            )

            is ItemContents.Alias -> AliasIcon(
                modifier = Modifier.size(ICON_SIZE.dp),
                shape = PassTheme.shapes.squircleSmallShape,
                backgroundColor = PassTheme.colors.aliasInteractionNormMinor2
            )

            is ItemContents.CreditCard -> CreditCardIcon(
                modifier = Modifier.size(ICON_SIZE.dp),
                shape = PassTheme.shapes.squircleSmallShape,
                backgroundColor = PassTheme.colors.cardInteractionNormMinor2
            )
            is ItemContents.Identity -> IdentityIcon(
                modifier = Modifier.size(ICON_SIZE.dp),
                shape = PassTheme.shapes.squircleSmallShape,
                backgroundColor = PassTheme.colors.cardInteractionNormMinor2
            )
            is ItemContents.WifiNetwork,
            is ItemContents.SSHKey,
            is ItemContents.Custom -> CustomIcon(
                modifier = Modifier.size(ICON_SIZE.dp),
                shape = PassTheme.shapes.squircleSmallShape,
                backgroundColor = PassTheme.colors.cardInteractionNormMinor2
            )

            is ItemContents.Unknown -> {}
        }
        Text(
            text = item.contents.title.ellipsize(TEXT_MAX_LENGTH_BEFORE_ELLIPSE),
            style = ProtonTheme.typography.captionStrongNorm
        )
    }
}

internal class ThemedPinItemPreviewProvider :
    ThemePairPreviewProvider<ItemUiModel>(PinItemPreviewProvider())

@Preview
@Composable
internal fun PinItemPreview(@PreviewParameter(ThemedPinItemPreviewProvider::class) input: Pair<Boolean, ItemUiModel>) {
    PassTheme(isDark = input.first) {
        Surface {
            PinItem(item = input.second, canLoadExternalImages = true, onItemClick = { _ -> })
        }
    }
}
