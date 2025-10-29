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

package proton.android.pass.composecomponents.impl.topbar

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemedBooleanPreviewProvider
import proton.android.pass.composecomponents.impl.topbar.iconbutton.BackArrowCircleIconButton

@Composable
fun BackArrowTopAppBar(
    modifier: Modifier = Modifier,
    title: String? = null,
    backgroundColor: Color = PassTheme.colors.backgroundStrong,
    arrowColor: Color = PassTheme.colors.interactionNormMajor2,
    backgroundArrowColor: Color = PassTheme.colors.interactionNormMinor1,
    actions: (@Composable RowScope.() -> Unit)? = null,
    onUpClick: () -> Unit
) {
    IconTopAppBar(
        modifier = modifier,
        title = title,
        backgroundColor = backgroundColor,
        actions = actions,
        navigationIcon = {
            BackArrowCircleIconButton(
                modifier = Modifier.padding(
                    horizontal = Spacing.medium - Spacing.extraSmall,
                    vertical = Spacing.extraSmall
                ),
                color = arrowColor,
                backgroundColor = backgroundArrowColor,
                onUpClick = onUpClick
            )
        }
    )
}

@Preview
@Composable
fun BackArrowTopAppBarPreview(@PreviewParameter(ThemedBooleanPreviewProvider::class) input: Pair<Boolean, Boolean>) {
    val title = if (input.first) "Title" else null
    PassTheme(isDark = input.first) {
        Surface {
            BackArrowTopAppBar(
                title = title,
                onUpClick = { }
            )
        }
    }
}
