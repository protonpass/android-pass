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

package proton.android.pass.composecomponents.impl.item.details.modifiers

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Radius
import proton.android.pass.commonui.api.applyIf
import proton.android.pass.domain.ItemDiffType

internal fun Modifier.contentDiff(itemDiffType: ItemDiffType): Modifier = composed {
    applyIf(
        condition = itemDiffType == ItemDiffType.Field,
        ifTrue = {
            RoundedCornerShape(size = Radius.mediumSmall.plus(2.dp)).let { shape ->
                padding(all = 1.dp)
                    .clip(shape = shape)
                    .border(
                        width = 1.dp,
                        color = PassTheme.colors.signalWarning,
                        shape = shape
                    )
            }
        }
    )
}
