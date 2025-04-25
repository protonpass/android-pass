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

package proton.android.pass.composecomponents.impl.item.icon

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemedBooleanPreviewProvider
import proton.android.pass.composecomponents.impl.container.BoxedIcon
import me.proton.core.presentation.R
import proton.android.pass.commonui.api.PassPalette

@Composable
fun CustomItemIcon(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    size: Int = 40,
    shape: Shape = PassTheme.shapes.squircleMediumShape,
    backgroundColor: Color = if (enabled) {
        PassPalette.SlateGray
    } else {
        PassPalette.MistGray
    },
    foregroundColor: Color = if (enabled) {
        ProtonTheme.colors.textNorm
    } else {
        ProtonTheme.colors.textDisabled
    }
) {
    BoxedIcon(
        modifier = modifier,
        backgroundColor = backgroundColor,
        size = size,
        shape = shape
    ) {
        Icon(
            modifier = Modifier.padding(Spacing.extraSmall),
            painter = painterResource(R.drawable.ic_proton_wrench),
            contentDescription = null,
            tint = foregroundColor
        )
    }
}

@Preview
@Composable
internal fun CustomIconPreview(@PreviewParameter(ThemedBooleanPreviewProvider::class) input: Pair<Boolean, Boolean>) {
    PassTheme(isDark = input.first) {
        Surface {
            CustomItemIcon(
                shape = PassTheme.shapes.squircleMediumShape,
                enabled = input.second
            )
        }
    }
}
