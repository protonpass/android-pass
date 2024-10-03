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
import proton.android.pass.domain.HiddenState
import proton.android.pass.domain.ItemContents

@Stable
internal data class ItemOptionsState(
    internal val event: ItemOptionsEvent,
    internal val canModify: Boolean,
    private val loginItemContentsOption: Option<ItemContents.Login>,
    private val isLoadingState: IsLoadingState
) {

    internal val email: String = when (loginItemContentsOption) {
        None -> ""
        is Some -> loginItemContentsOption.value.itemEmail
    }

    internal val hasEmail: Boolean = email.isNotEmpty()

    internal val username: String = when (loginItemContentsOption) {
        None -> ""
        is Some -> loginItemContentsOption.value.itemUsername
    }

    internal val hasUsername: Boolean = username.isNotEmpty()

    internal val hiddenStatePassword: HiddenState = when (loginItemContentsOption) {
        None -> HiddenState.Empty("")
        is Some -> loginItemContentsOption.value.password
    }

    internal val hasPassword: Boolean = hiddenStatePassword !is HiddenState.Empty

    internal val isLoading: Boolean = isLoadingState.value()

    internal companion object {

        internal val Initial = ItemOptionsState(
            isLoadingState = IsLoadingState.NotLoading,
            event = ItemOptionsEvent.Idle,
            canModify = false,
            loginItemContentsOption = None
        )

    }

}
