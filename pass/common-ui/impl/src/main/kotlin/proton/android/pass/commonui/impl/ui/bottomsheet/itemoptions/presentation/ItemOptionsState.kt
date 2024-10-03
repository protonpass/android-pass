/*
 * Copyright (c) 2023-2024 Proton AG
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

package proton.android.pass.commonui.impl.ui.bottomsheet.itemoptions.presentation

import androidx.compose.runtime.Stable
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.domain.ItemType

@Stable
internal data class ItemOptionsState(
    internal val event: ItemOptionsEvent,
    internal val canModify: Boolean,
    private val loginItemTypeOption: Option<ItemType.Login>,
    private val isLoadingState: IsLoadingState
) {

    internal val email: String = when (loginItemTypeOption) {
        None -> ""
        is Some -> loginItemTypeOption.value.itemEmail
    }

    internal val hasEmail: Boolean = email.isNotEmpty()

    internal val username: String = when (loginItemTypeOption) {
        None -> ""
        is Some -> loginItemTypeOption.value.itemUsername
    }

    internal val hasUsername: Boolean = username.isNotEmpty()

    internal val password: String = when (loginItemTypeOption) {
        None -> ""
        is Some -> loginItemTypeOption.value.password
    }

    internal val hasPassword: Boolean = password.isNotEmpty()

    internal val isLoading: Boolean = isLoadingState.value()

    internal companion object {

        internal val Initial = ItemOptionsState(
            isLoadingState = IsLoadingState.NotLoading,
            event = ItemOptionsEvent.Idle,
            canModify = false,
            loginItemTypeOption = None
        )

    }

}
