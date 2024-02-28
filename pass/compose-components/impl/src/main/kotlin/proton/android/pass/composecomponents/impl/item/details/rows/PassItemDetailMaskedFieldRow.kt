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

package proton.android.pass.composecomponents.impl.item.details.rows

import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import me.proton.core.presentation.R
import proton.android.pass.commonuimodels.api.masks.TextMask
import proton.android.pass.composecomponents.impl.container.Circle
import proton.android.pass.composecomponents.impl.utils.ProtonItemColors

@Composable
internal fun PassItemDetailMaskedFieldRow(
    modifier: Modifier = Modifier,
    icon: Painter,
    title: String,
    maskedSubtitle: TextMask,
    itemColors: ProtonItemColors,
    isSelectable: Boolean = false,
    isToggleable: Boolean = false,
    onClick: (() -> Unit)? = null,
) {

    var subtitle by remember { mutableStateOf(maskedSubtitle.masked) }

    PassItemDetailFieldRow(
        modifier = modifier,
        icon = icon,
        title = title,
        subtitle = subtitle,
        itemColors = itemColors,
        isSelectable = isSelectable,
        onClick = onClick,
    ) {
        if (isToggleable) {
            var isVisible by remember { mutableStateOf(false) }

            subtitle = if (isVisible) maskedSubtitle.unmasked else maskedSubtitle.masked

            Circle(
                backgroundColor = itemColors.minorPrimary,
                onClick = { isVisible = !isVisible },
            ) {
                Icon(
                    painter = if (isVisible) {
                        painterResource(R.drawable.ic_proton_eye_slash)
                    } else {
                        painterResource(R.drawable.ic_proton_eye)
                    },
                    contentDescription = null,
                    tint = itemColors.majorSecondary,
                )
            }
        }
    }

}
