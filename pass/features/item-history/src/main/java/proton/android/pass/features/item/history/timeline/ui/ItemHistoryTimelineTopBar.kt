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

package proton.android.pass.features.item.history.timeline.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.item.icon.ThreeDotsMenuButton
import proton.android.pass.composecomponents.impl.topbar.iconbutton.BackArrowCircleIconButton
import proton.android.pass.composecomponents.impl.utils.PassItemColors

@Composable
internal fun ItemHistoryTimelineTopBar(
    modifier: Modifier = Modifier,
    colors: PassItemColors,
    onUpClick: () -> Unit,
    onOptions: () -> Unit
) {
    ProtonTopAppBar(
        modifier = modifier,
        backgroundColor = PassTheme.colors.itemDetailBackground,
        title = {},
        navigationIcon = {
            BackArrowCircleIconButton(
                modifier = Modifier.padding(Spacing.mediumSmall, Spacing.small),
                color = colors.majorSecondary,
                backgroundColor = colors.minorPrimary,
                onUpClick = onUpClick
            )
        },
        actions = {
            ThreeDotsMenuButton(
                size = 40.dp,
                dotsColor = colors.majorSecondary,
                backgroundColor = colors.minorPrimary,
                onClick = onOptions
            )
            Spacer(Modifier.width(Spacing.small))
        }
    )
}
