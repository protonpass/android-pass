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

package proton.android.pass.composecomponents.impl.icon

import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemedBooleanPreviewProvider
import proton.android.pass.composecomponents.impl.R

@Composable
fun AllVaultsIcon(
    modifier: Modifier = Modifier,
    isItemSharingEnabled: Boolean,
    size: Int = 40,
    iconSize: Int = 20,
    onClick: (() -> Unit)? = null
) {
    val (backgroundColor, iconColor) = if (isItemSharingEnabled) {
        PassTheme.colors.interactionNormMinor1 to PassTheme.colors.interactionNormMajor2
    } else {
        PassTheme.colors.loginInteractionNormMinor1 to PassTheme.colors.loginInteractionNormMajor2
    }

    VaultIcon(
        modifier = modifier,
        backgroundColor = backgroundColor,
        iconColor = iconColor,
        icon = R.drawable.ic_brand_pass,
        size = size,
        iconSize = iconSize,
        onClick = onClick
    )
}

@[Preview Composable]
internal fun AllVaultsIconPreview(
    @PreviewParameter(ThemedBooleanPreviewProvider::class) input: Pair<Boolean, Boolean>
) {
    val (isDark, isItemSharingEnabled) = input

    PassTheme(isDark = isDark) {
        Surface {
            AllVaultsIcon(
                isItemSharingEnabled = isItemSharingEnabled
            )
        }
    }
}
