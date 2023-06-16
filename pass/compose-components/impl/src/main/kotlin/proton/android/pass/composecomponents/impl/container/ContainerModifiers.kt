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

package proton.android.pass.composecomponents.impl.container

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import proton.android.pass.commonui.api.PassTheme

@Stable
fun Modifier.roundedContainerNorm() = composed {
    roundedContainer(
        backgroundColor = PassTheme.colors.inputBackgroundNorm,
        borderColor = PassTheme.colors.inputBorderNorm
    )
}

@Stable
fun Modifier.roundedContainerStrong() = composed {
    roundedContainer(
        backgroundColor = PassTheme.colors.inputBackgroundStrong,
        borderColor = PassTheme.colors.inputBorderStrong
    )
}

@Stable
fun Modifier.roundedContainer(backgroundColor: Color, borderColor: Color) = composed {
    clip(PassTheme.shapes.containerInputShape)
        .background(backgroundColor)
        .border(
            width = 1.dp,
            color = borderColor,
            shape = PassTheme.shapes.containerInputShape
        )
}

