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

package proton.android.pass.features.security.center.shared.ui.counters

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import proton.android.pass.commonui.api.Radius
import proton.android.pass.commonui.api.Spacing

@Composable
internal fun SecurityCenterCounterIcon(
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int,
    iconColor: Color,
    iconBackgroundColor: Color,
    shape: Shape = RoundedCornerShape(Radius.small)
) {
    Icon(
        modifier = modifier
            .size(size = 24.dp)
            .clip(shape = shape)
            .background(color = iconBackgroundColor)
            .padding(all = Spacing.extraSmall),
        painter = painterResource(id = icon),
        contentDescription = null,
        tint = iconColor
    )
}
