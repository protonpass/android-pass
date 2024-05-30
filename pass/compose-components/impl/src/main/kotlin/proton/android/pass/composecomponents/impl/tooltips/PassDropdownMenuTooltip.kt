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

package proton.android.pass.composecomponents.impl.tooltips

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DropdownMenu
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.DpOffset
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Radius
import proton.android.pass.commonui.api.Spacing

@Composable
fun PassDropdownMenuTooltip(
    modifier: Modifier = Modifier,
    title: String,
    description: String,
    shouldDisplayTooltip: Boolean,
    onForget: () -> Unit,
    container: @Composable () -> Unit
) {
    var expanded by remember { mutableStateOf(value = shouldDisplayTooltip) }

    Box(modifier = modifier) {
        container()

        DropdownMenu(
            modifier = Modifier
                .background(color = PassTheme.colors.searchBarBackground)
                .clip(shape = RoundedCornerShape(size = Radius.small)),
            offset = DpOffset(
                x = Spacing.none,
                y = Spacing.extraSmall
            ),
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            PassTooltip(
                title = title,
                description = description,
                onDismiss = {
                    expanded = false
                    onForget()
                }
            )
        }
    }
}
