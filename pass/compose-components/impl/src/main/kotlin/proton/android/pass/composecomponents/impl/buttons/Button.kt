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

package proton.android.pass.composecomponents.impl.buttons

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.ButtonElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

object Button {
    @Composable
    fun Circular(
        modifier: Modifier = Modifier,
        color: Color,
        contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
        elevation: ButtonElevation = ButtonDefaults.elevation(),
        enabled: Boolean = true,
        onClick: () -> Unit,
        content: @Composable RowScope.() -> Unit
    ) {
        Button(
            modifier = modifier,
            contentPadding = contentPadding,
            enabled = enabled,
            colors = ButtonDefaults.buttonColors(backgroundColor = color),
            shape = CircleShape,
            elevation = elevation,
            onClick = onClick
        ) {
            content()
        }
    }
}
