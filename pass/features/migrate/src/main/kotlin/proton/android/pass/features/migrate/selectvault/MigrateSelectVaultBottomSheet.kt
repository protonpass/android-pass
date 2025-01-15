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

package proton.android.pass.features.migrate.selectvault

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.common.api.Some
import proton.android.pass.commonui.api.bottomSheet
import proton.android.pass.features.migrate.MigrateNavigation

@Composable
fun MigrateSelectVaultBottomSheet(
    modifier: Modifier = Modifier,
    onNavigate: (MigrateNavigation) -> Unit,
    viewModel: MigrateSelectVaultViewModel = hiltViewModel()
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()

    when (val state = uiState) {
        MigrateSelectVaultUiState.Error -> onNavigate(MigrateNavigation.DismissBottomsheet)
        MigrateSelectVaultUiState.Loading,
        MigrateSelectVaultUiState.Uninitialised -> Unit

        is MigrateSelectVaultUiState.Success -> {
            LaunchedEffect(state.event) {
                val event = state.event
                if (event is Some) {
                    when (val value = event.value) {
                        SelectVaultEvent.Close -> onNavigate(MigrateNavigation.DismissBottomsheet)
                        is SelectVaultEvent.VaultSelectedForMigrateItem -> {
                            onNavigate(
                                MigrateNavigation.VaultSelectedForMigrateItem(
                                    destShareId = value.destinationShareId
                                )
                            )
                        }

                        is SelectVaultEvent.VaultSelectedForMigrateAll -> {
                            onNavigate(
                                MigrateNavigation.VaultSelectedForMigrateAll(
                                    sourceShareId = value.sourceShareId,
                                    destShareId = value.destinationShareId
                                )
                            )
                        }
                    }
                    viewModel.clearEvent()
                }
            }

            MigrateSelectVaultContents(
                modifier = modifier.bottomSheet(),
                vaults = state.vaultList,
                onVaultSelected = { viewModel.onVaultSelected(it) }
            )
        }
    }
}
