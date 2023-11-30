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

package proton.android.pass.composecomponents.impl.item

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.presentation.R
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemedBooleanPreviewProvider
import proton.android.pass.commonui.api.applyIf

@Composable
fun SelectModeIcon(
    modifier: Modifier = Modifier,
    isSelected: Boolean,
    size: Int = 40,
) {
    Box(
        modifier = modifier
            .size(size.dp)
            .border(
                width = 2.dp,
                color = PassTheme.colors.inputBorderNorm,
                shape = PassTheme.shapes.squircleMediumShape
            )
            .applyIf(
                condition = isSelected,
                ifTrue = {
                    background(
                        color = PassTheme.colors.interactionNorm,
                        shape = PassTheme.shapes.squircleMediumShape
                    )
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(
                painter = painterResource(R.drawable.ic_proton_checkmark),
                contentDescription = null,
                tint = PassTheme.colors.textNorm,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Preview
@Composable
fun SelectModeIconPreview(
    @PreviewParameter(ThemedBooleanPreviewProvider::class) input: Pair<Boolean, Boolean>
) {
    PassTheme(isDark = input.first) {
        Surface {
            SelectModeIcon(
                isSelected = input.second
            )
        }
    }
}
