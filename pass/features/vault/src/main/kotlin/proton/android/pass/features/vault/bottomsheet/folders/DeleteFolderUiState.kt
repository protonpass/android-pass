/*
 * Copyright (c) 2025-2026 Proton AG
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

package proton.android.pass.features.vault.bottomsheet.folders

import androidx.compose.runtime.Stable
import proton.android.pass.composecomponents.impl.uievents.IsButtonEnabled
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState

sealed interface DeleteFolderEvent {

    data object Unknown : DeleteFolderEvent

    data object Deleted : DeleteFolderEvent

}

@Stable
data class DeleteFolderUiState(
    internal val folderName: String,
    internal val folderText: String,
    internal val event: DeleteFolderEvent,
    internal val isButtonEnabled: IsButtonEnabled,
    private val isLoadingState: IsLoadingState
) {

    internal val isLoading: Boolean = isLoadingState.value()

    internal companion object {

        internal val Initial = DeleteFolderUiState(
            folderName = "",
            folderText = "",
            event = DeleteFolderEvent.Unknown,
            isButtonEnabled = IsButtonEnabled.Disabled,
            isLoadingState = IsLoadingState.NotLoading
        )

    }

}
