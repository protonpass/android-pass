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

package proton.android.pass.features.vault.delete

import androidx.compose.runtime.Stable
import proton.android.pass.composecomponents.impl.uievents.IsButtonEnabled
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState

internal sealed interface DeleteVaultEvent {

    data object Unknown : DeleteVaultEvent

    data object Deleted : DeleteVaultEvent

}

@Stable
internal data class DeleteVaultUiState(
    internal val vaultName: String,
    internal val vaultText: String,
    internal val event: DeleteVaultEvent,
    internal val isButtonEnabled: IsButtonEnabled,
    internal val sharedItemsCount: Int,
    private val isLoadingState: IsLoadingState
) {

    internal val isLoading: Boolean = isLoadingState.value()

    internal val showSharedItemsWarning: Boolean = sharedItemsCount > 0

    internal companion object {

        internal val Initial = DeleteVaultUiState(
            vaultName = "",
            vaultText = "",
            event = DeleteVaultEvent.Unknown,
            isButtonEnabled = IsButtonEnabled.Disabled,
            isLoadingState = IsLoadingState.NotLoading,
            sharedItemsCount = 0
        )

    }

}
