/*
 * Copyright (c) 2023-2026 Proton AG
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

package proton.android.pass.features.vault.folders

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import proton.android.pass.composecomponents.impl.uievents.IsButtonEnabled
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState

internal class AddFolderToVaultDialogPreviewProvider : PreviewParameterProvider<AddFolderToVaultUiState> {

    override val values: Sequence<AddFolderToVaultUiState> = sequence {
        for (button in listOf(IsButtonEnabled.Enabled, IsButtonEnabled.Disabled)) {
            for (showSameFolderExist in listOf(false, true)) {
                for (text in listOf("", "a folder name")) {
                    for (isEditMode in listOf(false, true)) {
                        yield(
                            AddFolderToVaultUiState(
                                folderName = text,
                                event = AddFolderToVaultEvent.Unknown,
                                isButtonEnabled = button,
                                isLoadingState = IsLoadingState.NotLoading,
                                showSameFolderExist = showSameFolderExist,
                                isEditMode = isEditMode
                            )
                        )
                    }
                }
            }
        }
    }

}
