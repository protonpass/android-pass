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

package proton.android.pass.composecomponents.impl.toggles

import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import proton.android.pass.composecomponents.impl.R
import proton.android.pass.composecomponents.impl.container.Circle
import proton.android.pass.composecomponents.impl.utils.PassItemColors
import me.proton.core.presentation.R as CoreR

@Composable
fun PassVisibilityToggle(
    modifier: Modifier = Modifier,
    isVisible: Boolean,
    onToggle: (Boolean) -> Unit,
    itemColors: PassItemColors
) {
    Circle(
        modifier = modifier,
        backgroundColor = itemColors.minorPrimary,
        onClick = { onToggle(!isVisible) }
    ) {
        Icon(
            painter = if (isVisible) {
                painterResource(CoreR.drawable.ic_proton_eye_slash)
            } else {
                painterResource(CoreR.drawable.ic_proton_eye)
            },
            contentDescription = if (isVisible) {
                stringResource(R.string.action_conceal)
            } else {
                stringResource(R.string.action_reveal)
            },
            tint = itemColors.majorSecondary
        )
    }

}
