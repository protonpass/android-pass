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

package proton.android.pass.commonui.api

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

object Gradients {
    val RadialPurple = Brush.radialGradient(
        center = Offset(Float.POSITIVE_INFINITY, 0.0f),
        radius = 800f,
        colors = listOf(
            PassPalette.VividViolet,
            PassPalette.IndigoViolet
        )
    )
    val VerticalApricot = Brush.verticalGradient(
        colors = listOf(
            PassPalette.PaleApricot,
            Color.Transparent
        ),
        endY = Float.POSITIVE_INFINITY / 2
    )
}
