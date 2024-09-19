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

package proton.android.pass.features.item.options.aliases.trash.dialogs.presentation

import androidx.compose.runtime.Stable
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.preferences.AliasTrashDialogStatusPreference

@Stable
internal data class ItemOptionsAliasTrashDialogState(
    internal val isLoadingState: IsLoadingState,
    internal val event: ItemOptionsAliasTrashDialogEvent,
    private val preference: AliasTrashDialogStatusPreference
) {

    internal val isLoading: Boolean = isLoadingState.value()

    internal val isRemindMeEnabled: Boolean = preference.value

    internal companion object {

        internal val Initial = ItemOptionsAliasTrashDialogState(
            isLoadingState = IsLoadingState.NotLoading,
            event = ItemOptionsAliasTrashDialogEvent.Idle,
            preference = AliasTrashDialogStatusPreference.Disabled
        )

    }

}
