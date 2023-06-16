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

package proton.android.pass.featurevault.impl.bottomsheet

import androidx.compose.runtime.Stable
import proton.android.pass.composecomponents.impl.uievents.IsButtonEnabled
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.pass.domain.ShareColor
import proton.pass.domain.ShareIcon

@Stable
sealed interface IsVaultCreatedEvent {
    object Unknown : IsVaultCreatedEvent
    object Created : IsVaultCreatedEvent
}

@Stable
data class BaseVaultUiState(
    val name: String,
    val color: ShareColor,
    val icon: ShareIcon,
    val isLoading: IsLoadingState,
    val isTitleRequiredError: Boolean,
    val isCreateButtonEnabled: IsButtonEnabled,
    val isVaultCreatedEvent: IsVaultCreatedEvent
) {
    companion object {
        val Initial = BaseVaultUiState(
            name = "",
            color = ShareColor.Color1,
            icon = ShareIcon.Icon1,
            isLoading = IsLoadingState.NotLoading,
            isTitleRequiredError = false,
            isCreateButtonEnabled = IsButtonEnabled.Disabled,
            isVaultCreatedEvent = IsVaultCreatedEvent.Unknown
        )
    }
}

@Stable
data class CreateVaultUiState(
    val base: BaseVaultUiState,
    val displayNeedUpgrade: Boolean
) {
    companion object {
        val Initial = CreateVaultUiState(
            base = BaseVaultUiState.Initial,
            displayNeedUpgrade = false
        )
    }
}
