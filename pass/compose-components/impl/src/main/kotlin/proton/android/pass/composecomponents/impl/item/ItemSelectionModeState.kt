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

package proton.android.pass.composecomponents.impl.item

import androidx.compose.runtime.Stable

@Stable
sealed interface ItemSelectionModeState {

    fun isSelected(): Boolean = this is InSelectionMode && state == ItemSelectionState.Selected
    fun isSelectable(): Boolean = !(this is InSelectionMode && state == ItemSelectionState.NotSelectable)

    @Stable
    object NotInSelectionMode : ItemSelectionModeState

    @Stable
    @JvmInline
    value class InSelectionMode(val state: ItemSelectionState) : ItemSelectionModeState

    @Stable
    enum class ItemSelectionState {
        Selected,
        Unselected,
        NotSelectable
    }

    companion object {
        fun fromValues(
            inSelectionMode: Boolean,
            isSelected: Boolean,
            isSelectable: Boolean
        ): ItemSelectionModeState = when (inSelectionMode) {
            true -> when {
                isSelected -> InSelectionMode(ItemSelectionState.Selected)
                isSelectable -> InSelectionMode(ItemSelectionState.Unselected)
                else -> InSelectionMode(ItemSelectionState.NotSelectable)
            }
            false -> NotInSelectionMode
        }
    }
}
