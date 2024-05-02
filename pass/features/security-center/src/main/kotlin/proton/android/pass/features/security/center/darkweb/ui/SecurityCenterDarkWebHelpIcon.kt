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

package proton.android.pass.features.security.center.darkweb.ui

import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import me.proton.core.presentation.R
import proton.android.pass.commonui.api.applyIf

@Composable
internal fun SecurityCenterDarkWebHelpIcon(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    iconTint: Color,
    iconSize: Dp? = null
) {
    IconButton(
        modifier = modifier.applyIf(
            condition = iconSize != null,
            ifTrue = { size(size = iconSize!!) }
        ),
        onClick = onClick
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_proton_question_circle),
            contentDescription = null,
            tint = iconTint
        )
    }
}
