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

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import proton.android.pass.commonuimodels.api.masks.TextMask
import proton.android.pass.composecomponents.impl.toggles.PassVisibilityToggle
import proton.android.pass.composecomponents.impl.utils.PassItemColors
import proton.android.pass.domain.ItemDiffType

@Composable
internal fun PassItemDetailMaskedFieldRow(
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int?,
    showLeadingIcon: Boolean = true,
    title: String,
    maskedSubtitle: TextMask? = null,
    itemColors: PassItemColors,
    itemDiffType: ItemDiffType = ItemDiffType.None,
    isSelectable: Boolean = false,
    isToggleable: Boolean = false,
    onClick: (() -> Unit)? = null,
    contentInBetween: (@Composable () -> Unit)? = null
) {
    var isMasked by remember { mutableStateOf(true) }

    PassItemDetailFieldRow(
        modifier = modifier,
        icon = icon,
        showLeadingIcon = showLeadingIcon,
        title = title,
        subtitle = maskedSubtitle?.let {
            if (isMasked) {
                maskedSubtitle.masked
            } else {
                maskedSubtitle.unmasked
            }
        } ?: "",
        itemColors = itemColors,
        itemDiffType = itemDiffType,
        isSelectable = isSelectable,
        onClick = onClick
    ) {
        contentInBetween?.invoke()

        if (isToggleable) {
            PassVisibilityToggle(
                isVisible = !isMasked,
                onToggle = { isVisible -> isMasked = !isVisible },
                itemColors = itemColors
            )
        }
    }
}
