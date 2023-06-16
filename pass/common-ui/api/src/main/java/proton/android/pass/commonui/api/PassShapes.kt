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

package proton.android.pass.commonui.api

import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class PassShapes(
    val bottomsheetShape: Shape,
    val containerInputShape: Shape,
    val squircleMediumShape: Shape,
    val squircleMediumLargeShape: Shape
) {
    companion object {
        val Default: PassShapes = PassShapes(
            bottomsheetShape = RoundedCornerShape(
                topStart = Radius.medium,
                topEnd = Radius.medium,
                bottomStart = 0.dp,
                bottomEnd = 0.dp
            ),
            containerInputShape = RoundedCornerShape(Radius.medium),
            squircleMediumShape = RoundedCornerShape(Radius.medium),
            squircleMediumLargeShape = RoundedCornerShape(Radius.medium + Radius.small)
        )
    }
}

val LocalPassShapes = staticCompositionLocalOf {
    PassShapes(
        bottomsheetShape = CutCornerShape(0.dp),
        containerInputShape = CutCornerShape(0.dp),
        squircleMediumShape = CutCornerShape(0.dp),
        squircleMediumLargeShape = CutCornerShape(0.dp)
    )
}

object Radius {
    val extraSmall: Dp = 4.dp
    val small: Dp = 8.dp
    val medium: Dp = 16.dp
    val large: Dp = 32.dp
    val extraLarge: Dp = 64.dp
}
