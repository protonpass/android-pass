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
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.asLoadingResult
import proton.android.pass.common.api.toOption
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonpresentation.api.folders.FolderTreeBuilder
import proton.android.pass.commonui.api.require
import proton.android.pass.commonuimodels.api.FolderUiModel
import proton.android.pass.data.api.repositories.BulkMoveToVaultRepository
import proton.android.pass.data.api.usecases.ObserveVaultsWithItemCount
import proton.android.pass.data.api.usecases.folders.ObserveFolders
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
    private val observeFolders: ObserveFolders,
    snackbarDispatcher: SnackbarDispatcher
) : ViewModel() {

    private data class VaultShareKey(
        val userId: UserId,
        val shareId: ShareId
    )

    private data class VaultsWithFolders(
        val vaultShares: List<VaultWithItemCount>,
        val vaultFolders: Map<ShareId, PersistentList<FolderUiModel>>
    )

    private val mode: Mode = getMode()

    private val eventFlow: MutableStateFlow<Option<SelectVaultEvent>> = MutableStateFlow(None)

    private val selectedItemsFlow = bulkMoveToVaultRepository.observe()
        .distinctUntilChanged()

    private val vaultSharesFlow: Flow<List<VaultWithItemCount>> =
        observeVaults(includeHidden = true)

    private val vaultShareKeysFlow: Flow<List<VaultShareKey>> = vaultSharesFlow
        .map(::toVaultShareKeys)
        .distinctUntilChanged()

    private val vaultFoldersFlow: Flow<Map<ShareId, PersistentList<FolderUiModel>>> =
        vaultShareKeysFlow.flatMapLatest(::observeVaultFolders)

    private val vaultsWithFoldersFlow: Flow<VaultsWithFolders> = combine(
        vaultSharesFlow,
        vaultFoldersFlow,
        ::VaultsWithFolders
    )

    internal val state: StateFlow<MigrateSelectVaultUiState> = combine(
        vaultsWithFoldersFlow.asLoadingResult(),
        eventFlow,
        selectedItemsFlow
    ) { vaultsWithFoldersResult, event, selectedItems ->
        when (vaultsWithFoldersResult) {
            LoadingResult.Loading -> MigrateSelectVaultUiState.Loading
            is LoadingResult.Error -> {
                snackbarDispatcher(CouldNotInit)
                PassLogger.w(TAG, "Error observing active vaults")
                PassLogger.w(TAG, vaultsWithFoldersResult.exception)
                MigrateSelectVaultUiState.Error
            }

            is LoadingResult.Success -> MigrateSelectVaultUiState.Success(
                vaultList = prepareVaults(
                    vaultsWithFoldersResult.data.vaultShares,
                    vaultsWithFoldersResult.data.vaultFolders,
                    selectedItems
                ),
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
        vaultFolders: Map<ShareId, PersistentList<FolderUiModel>>,
        selectedItems: Option<Map<ShareId, List<ItemId>>>
    ): ImmutableList<VaultEnabledPair> = vaults
        .filter {
            if (mode is Mode.MigrateSelectedItems && mode.filter == MigrateVaultFilter.Shared) {
                it.vault.shared
            } else {
                true
            }
        }
        .map { prepareVault(it, vaultFolders, selectedItems) }
        .toImmutableList()

    private fun prepareVault(
        vault: VaultWithItemCount,
        vaultFolders: Map<ShareId, PersistentList<FolderUiModel>>,
        selectedItems: Option<Map<ShareId, List<ItemId>>>
    ): VaultEnabledPair {
        val canCreate = vault.vault.role.toPermissions().canCreate()
        val folderTree = vaultFolders[vault.vault.shareId] ?: persistentListOf()
        return when (mode) {
            is Mode.MigrateSelectedItems -> {
                when (selectedItems) {
                    None -> VaultEnabledPair(
                        vaultWithItemCount = vault,
                        status = VaultStatus.Disabled(
                            reason = VaultStatus.DisabledReason.NoPermission
                        ),
                        folderTree = folderTree
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
                            vaultWithItemCount = vault,
                            status = state,
                            folderTree = folderTree
                        )
                    }
                }
            }

            is Mode.MigrateAllItems -> {
                val isNotCurrentOne = vault.vault.shareId != mode.shareId
                VaultEnabledPair(
                    vaultWithItemCount = vault,
                    status = if (isNotCurrentOne) VaultStatus.Enabled else VaultStatus.Disabled(
                        reason = VaultStatus.DisabledReason.SameVault
                    ),
                    folderTree = folderTree
                )
            }
        }
    }

    private fun toVaultShareKeys(vaults: List<VaultWithItemCount>): List<VaultShareKey> = vaults
        .asSequence()
        .map { vault ->
            VaultShareKey(
                userId = vault.vault.userId,
                shareId = vault.vault.shareId
            )
        }
        .distinct()
        .sortedWith(
            compareBy<VaultShareKey> { it.userId.id }
                .thenBy { it.shareId.id }
        )
        .toList()

    private fun observeVaultFolders(shareKeys: List<VaultShareKey>): Flow<Map<ShareId, PersistentList<FolderUiModel>>> {
        if (shareKeys.isEmpty()) {
            return flowOf(emptyMap())
        }

        val folderFlows = shareKeys.map(::observeFolderTreeForShare)
        return combine(folderFlows) { shareFolderPairs -> shareFolderPairs.toMap() }
            .distinctUntilChanged()
    }

    private fun observeFolderTreeForShare(shareKey: VaultShareKey): Flow<Pair<ShareId, PersistentList<FolderUiModel>>> =
        observeFolders(shareKey.userId, shareKey.shareId)
            .distinctUntilChanged()
            .map { folderList ->
                shareKey.shareId to FolderTreeBuilder.build(folderList)
            }
            .onStart {
                emit(shareKey.shareId to persistentListOf())
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
