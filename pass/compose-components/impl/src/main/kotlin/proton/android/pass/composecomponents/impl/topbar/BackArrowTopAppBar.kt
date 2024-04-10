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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.heroNorm
import proton.android.pass.composecomponents.impl.topbar.iconbutton.BackArrowCircleIconButton

@Composable
fun BackArrowTopAppBar(
    modifier: Modifier = Modifier,
    title: String? = null,
    backgroundColor: Color = PassTheme.colors.backgroundStrong,
    actions: (@Composable RowScope.() -> Unit)? = null,
    onUpClick: () -> Unit
) {
    ProtonTopAppBar(
        modifier = modifier,
        backgroundColor = backgroundColor,
        title = {
            title?.let {
                Text(
                    text = title,
                    style = PassTheme.typography.heroNorm()
                )
            }
        },
        navigationIcon = {
            BackArrowCircleIconButton(
                modifier = Modifier.padding(
                    horizontal = Spacing.medium - Spacing.extraSmall,
                    vertical = Spacing.extraSmall
                ),
                color = PassTheme.colors.interactionNormMajor2,
                backgroundColor = PassTheme.colors.interactionNormMinor1,
                onUpClick = onUpClick
            )
        },
        actions = {
            actions?.let {
                actions()
                Spacer(modifier = Modifier.width(Spacing.medium - Spacing.extraSmall))
            }
        }
    )
}
