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

package proton.android.pass.features.vault.bottomsheet.options

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.features.vault.VaultNavigation

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun VaultOptionsBottomSheet(
    modifier: Modifier = Modifier,
    onNavigate: (VaultNavigation) -> Unit,
    viewModel: VaultOptionsViewModel = hiltViewModel()
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.event) {
        when (val event = uiState.event) {
            VaultOptionsEvent.Idle -> Unit
            is VaultOptionsEvent.OnMigrateVaultItems -> {
                VaultNavigation.VaultMigrate(
                    shareId = event.shareId
                ).also(onNavigate)
            }

            is VaultOptionsEvent.OnMigrateVaultItemsSharedWarning -> {
                VaultNavigation.VaultMigrateSharedWarning(
                    shareId = event.shareId
                ).also(onNavigate)
            }
        }

        viewModel.onEventConsumed(uiState.event)
    }

    when (val state = uiState) {
        is VaultOptionsUiState.Success -> VaultOptionsBottomSheetContents(
            modifier = modifier,
            state = state,
            onEvent = {
                when (it) {
                    VaultOptionsUserEvent.OnEdit -> {
                        onNavigate(VaultNavigation.VaultEdit(state.shareId))
                    }

                    VaultOptionsUserEvent.OnLeave -> {
                        onNavigate(VaultNavigation.VaultLeave(state.shareId))
                    }

                    VaultOptionsUserEvent.OnMigrate -> {
                        viewModel.onMigrateVault()
                    }

                    VaultOptionsUserEvent.OnRemove -> {
                        onNavigate(VaultNavigation.VaultRemove(state.shareId, state.isLastVault))
                    }

                    VaultOptionsUserEvent.OnShare -> {
                        onNavigate(
                            VaultNavigation.VaultShare(
                                shareId = state.shareId,
                                showEditVault = false
                            )
                        )
                    }

                    VaultOptionsUserEvent.OnVaultAccess -> {
                        onNavigate(VaultNavigation.VaultAccess(state.shareId))
                    }
                }
            }
        )

        VaultOptionsUiState.Loading,
        VaultOptionsUiState.Uninitialised -> Unit

        VaultOptionsUiState.Error -> {
            LaunchedEffect(Unit) {
                onNavigate(VaultNavigation.DismissBottomsheet)
            }
        }
    }
}
