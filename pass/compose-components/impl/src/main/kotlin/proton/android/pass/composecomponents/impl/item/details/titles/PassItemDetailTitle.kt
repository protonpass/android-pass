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

package proton.android.pass.composecomponents.impl.item.details.titles

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.domain.ItemDiffType

@Composable
internal fun PassItemDetailTitle(
    modifier: Modifier = Modifier,
    text: String,
    maxLines: Int = 2,
    itemDiffType: ItemDiffType = ItemDiffType.None
) {
    Text(
        modifier = modifier,
        text = text,
        fontSize = 28.sp,
        fontWeight = FontWeight.W700,
        letterSpacing = 0.03.sp,
        lineHeight = 34.sp,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        color = if (itemDiffType == ItemDiffType.Content) {
            PassTheme.colors.signalWarning
        } else {
            ProtonTheme.colors.textNorm
        }
    )
}
