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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.asLoadingResult
import proton.android.pass.common.api.toOption
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.require
import proton.android.pass.data.api.repositories.BulkMoveToVaultRepository
import proton.android.pass.data.api.usecases.ObserveVaultsWithItemCount
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.VaultWithItemCount
import proton.android.pass.domain.canCreate
import proton.android.pass.domain.toPermissions
import proton.android.pass.features.migrate.MigrateModeArg
import proton.android.pass.features.migrate.MigrateModeValue
import proton.android.pass.features.migrate.MigrateSnackbarMessage.CouldNotInit
import proton.android.pass.features.migrate.MigrateVaultFilter
import proton.android.pass.features.migrate.MigrateVaultFilterArg
import proton.android.pass.log.api.PassLogger
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.notifications.api.SnackbarDispatcher
import javax.inject.Inject

@HiltViewModel
class MigrateSelectVaultViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandleProvider,
    bulkMoveToVaultRepository: BulkMoveToVaultRepository,
    observeVaults: ObserveVaultsWithItemCount,
    snackbarDispatcher: SnackbarDispatcher
) : ViewModel() {

    private val mode: Mode = getMode()

    private val eventFlow: MutableStateFlow<Option<SelectVaultEvent>> = MutableStateFlow(None)

    private val selectedItemsFlow = bulkMoveToVaultRepository.observe()
        .distinctUntilChanged()

    internal val state: StateFlow<MigrateSelectVaultUiState> = combine(
        observeVaults().asLoadingResult(),
        eventFlow,
        selectedItemsFlow
    ) { vaultResult, event, selectedItems ->
        when (vaultResult) {
            LoadingResult.Loading -> MigrateSelectVaultUiState.Loading
            is LoadingResult.Error -> {
                snackbarDispatcher(CouldNotInit)
                PassLogger.w(TAG, "Error observing active vaults")
                PassLogger.w(TAG, vaultResult.exception)
                MigrateSelectVaultUiState.Error
            }

            is LoadingResult.Success -> MigrateSelectVaultUiState.Success(
                vaultList = prepareVaults(vaultResult.data, selectedItems),
                event = event,
                mode = mode.migrateMode()
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = MigrateSelectVaultUiState.Uninitialised
    )

    internal fun onVaultSelected(shareId: ShareId) {
        val event = when (mode) {
            is Mode.MigrateSelectedItems -> SelectVaultEvent.VaultSelectedForMigrateItem(
                destinationShareId = shareId
            )

            is Mode.MigrateAllItems -> SelectVaultEvent.VaultSelectedForMigrateAll(
                sourceShareId = mode.shareId,
                destinationShareId = shareId
            )
        }

        eventFlow.update { event.toOption() }
    }

    internal fun clearEvent() {
        eventFlow.update { None }
    }

    private fun prepareVaults(
        vaults: List<VaultWithItemCount>,
        selectedItems: Option<Map<ShareId, List<ItemId>>>
    ): ImmutableList<VaultEnabledPair> = vaults.filter {
        if (mode is Mode.MigrateSelectedItems && mode.filter == MigrateVaultFilter.Shared) {
            it.vault.shared
        } else {
            true
        }
    }.map { prepareVault(it, selectedItems) }.toImmutableList()

    private fun prepareVault(
        vault: VaultWithItemCount,
        selectedItems: Option<Map<ShareId, List<ItemId>>>
    ): VaultEnabledPair {
        val canCreate = vault.vault.role.toPermissions().canCreate()
        return when (mode) {
            is Mode.MigrateSelectedItems -> {
                when (selectedItems) {
                    None -> VaultEnabledPair(
                        vault = vault,
                        status = VaultStatus.Disabled(
                            reason = VaultStatus.DisabledReason.NoPermission
                        )
                    )

                    is Some -> {
                        val selectedItemsMap = selectedItems.value
                        val state = if (selectedItemsMap.size == 1) {
                            // We only have 1 vault. Disable that one
                            val shareToBeMoved = selectedItemsMap.entries.first()
                            val isNotCurrentOne = vault.vault.shareId != shareToBeMoved.key
                            when {
                                isNotCurrentOne && canCreate -> VaultStatus.Enabled
                                isNotCurrentOne && !canCreate -> VaultStatus.Disabled(
                                    reason = VaultStatus.DisabledReason.NoPermission
                                )

                                else -> VaultStatus.Disabled(
                                    reason = VaultStatus.DisabledReason.SameVault
                                )
                            }
                        } else {
                            // We have many vaults. Enable only if permission matches
                            if (canCreate) {
                                VaultStatus.Enabled
                            } else {
                                VaultStatus.Disabled(VaultStatus.DisabledReason.NoPermission)
                            }
                        }

                        VaultEnabledPair(
                            vault = vault,
                            status = state
                        )
                    }
                }
            }

            is Mode.MigrateAllItems -> {
                val isNotCurrentOne = vault.vault.shareId != mode.shareId
                VaultEnabledPair(
                    vault = vault,
                    status = if (isNotCurrentOne) VaultStatus.Enabled else VaultStatus.Disabled(
                        reason = VaultStatus.DisabledReason.SameVault
                    )
                )
            }
        }
    }

    private fun getMode(): Mode {
        val savedState = savedStateHandle.get()
        return when (MigrateModeValue.valueOf(savedState.require(MigrateModeArg.key))) {
            MigrateModeValue.SelectedItems -> {
                Mode.MigrateSelectedItems(
                    filter = MigrateVaultFilter.valueOf(
                        savedState.require(MigrateVaultFilterArg.key)
                    )
                )
            }

            MigrateModeValue.AllVaultItems -> {
                val sourceShareId = ShareId(savedState.require(CommonNavArgId.ShareId.key))
                Mode.MigrateAllItems(sourceShareId)
            }
        }
    }

    internal sealed interface Mode {

        data class MigrateSelectedItems(
            val filter: MigrateVaultFilter
        ) : Mode

        data class MigrateAllItems(val shareId: ShareId) : Mode

        fun migrateMode(): MigrateMode = when (this) {
            is MigrateSelectedItems -> MigrateMode.MigrateItem
            is MigrateAllItems -> MigrateMode.MigrateAll
        }
    }

    private companion object {

        private const val TAG = "MigrateSelectVaultViewModel"

    }

}
