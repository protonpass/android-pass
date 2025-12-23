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

package proton.android.pass.features.vault.bottomsheet.folders

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import proton.android.pass.features.vault.VaultNavigation

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun FolderOptionsBottomSheet(
    modifier: Modifier = Modifier,
    onNavigate: (VaultNavigation) -> Unit,
    viewModel: FolderOptionsViewModel = hiltViewModel()
) {
    FolderOptionsBottomSheetContents(
        modifier = modifier,
        onEvent = {
            when (it) {
                FolderOptionsUserEvent.OnCreateSubFolder -> {
                    onNavigate(
                        VaultNavigation.AddFolder(
                            viewModel.navShareId,
                            viewModel.navFolderId
                        )
                    )
                }

                FolderOptionsUserEvent.OnRenameFolder -> {
                    onNavigate(
                        VaultNavigation.RenameFolder(
                            viewModel.navShareId,
                            viewModel.navFolderId
                        )
                    )
                }

                FolderOptionsUserEvent.OnDeleteFolder -> {
                    onNavigate(
                        VaultNavigation.RemoveFolder(
                            viewModel.navShareId,
                            viewModel.navFolderId
                        )
                    )
                }
            }
        }
    )
}
