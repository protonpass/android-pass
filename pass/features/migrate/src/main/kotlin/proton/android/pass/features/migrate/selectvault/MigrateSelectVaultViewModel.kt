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

package proton.android.pass.features.migrate.selectvault

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentList
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
import kotlinx.coroutines.launch
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.asLoadingResult
import proton.android.pass.common.api.toOption
import proton.android.pass.commonpresentation.api.folders.FolderTreeBuilder
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.require
import proton.android.pass.commonuimodels.api.FolderUiModel
import proton.android.pass.data.api.repositories.BulkMoveToVaultSelection
import proton.android.pass.data.api.repositories.BulkMoveToVaultRepository
import proton.android.pass.data.api.repositories.ParentContainer
import proton.android.pass.data.api.repositories.flattenByShare
import proton.android.pass.data.api.usecases.ObserveVaultsWithItemCount
import proton.android.pass.data.api.usecases.folders.ObserveFolders
import proton.android.pass.domain.Folder
import proton.android.pass.domain.FolderId
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.VaultWithItemCount
import proton.android.pass.domain.canCreate
import proton.android.pass.domain.toPermissions
import proton.android.pass.features.migrate.MigrateModeArg
import proton.android.pass.features.migrate.MigrateModeValue
import proton.android.pass.features.migrate.MigrateSnackbarMessage.CouldNotInit
import proton.android.pass.features.migrate.MigrateSnackbarMessage.FolderAlreadySameParent
import proton.android.pass.features.migrate.MigrateVaultFilter
import proton.android.pass.features.migrate.MigrateVaultFilterArg
import proton.android.pass.log.api.PassLogger
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.navigation.api.CommonOptionalNavArgId
import proton.android.pass.notifications.api.SnackbarDispatcher
import javax.inject.Inject

@HiltViewModel
class MigrateSelectVaultViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandleProvider,
    bulkMoveToVaultRepository: BulkMoveToVaultRepository,
    observeVaults: ObserveVaultsWithItemCount,
    private val observeFolders: ObserveFolders,
    private val snackbarDispatcher: SnackbarDispatcher
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

    private val selectedItemsSelectionFlow: StateFlow<Option<BulkMoveToVaultSelection>> =
        bulkMoveToVaultRepository.observe()
            .distinctUntilChanged()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000L),
                initialValue = None
            )

    private val selectedItemsFlow: StateFlow<Option<Map<ShareId, List<ItemId>>>> =
        selectedItemsSelectionFlow
            .map { it.map { selection -> selection.flattenByShare() } }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000L),
                initialValue = None
            )

    private val selectedItemsAnalysisFlow: StateFlow<SelectedItemsAnalysis> =
        selectedItemsSelectionFlow
            .map { it.value()?.let(::analyzeSelectedItems) ?: SelectedItemsAnalysis.Empty }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000L),
                initialValue = SelectedItemsAnalysis.Empty
            )

    private val vaultSharesFlow: Flow<List<VaultWithItemCount>> =
        observeVaults(includeHidden = true).map { vaults ->
            when (val m = mode) {
                is Mode.MoveFolder -> vaults.filter { it.vault.shareId == m.sourceShareId }
                else -> vaults
            }
        }

    private val vaultShareKeysFlow: Flow<List<VaultShareKey>> = vaultSharesFlow
        .map(::toVaultShareKeys)
        .distinctUntilChanged()

    // In MoveFolder mode there is exactly one share — share the raw folder list so that
    // both the tree builder and the parent-ID lookup subscribe only once.
    private val sourceFoldersFlow: StateFlow<List<Folder>> = when (val m = mode) {
        is Mode.MoveFolder ->
            vaultSharesFlow
                .flatMapLatest { vaults ->
                    val userId = vaults.firstOrNull()?.vault?.userId
                        ?: return@flatMapLatest flowOf(emptyList())
                    observeFolders(userId, m.sourceShareId)
                }
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5_000L),
                    initialValue = emptyList()
                )
        else -> MutableStateFlow(emptyList())
    }

    private val currentParentFolderIdFlow: StateFlow<Option<FolderId>> = when (val m = mode) {
        is Mode.MoveFolder ->
            sourceFoldersFlow
                .map { folders -> folders.find { it.folderId == m.folderId }?.parentFolderId.toOption() }
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.Eagerly,
                    initialValue = None
                )
        else -> MutableStateFlow(None)
    }

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
        selectedItemsFlow,
        selectedItemsAnalysisFlow
    ) { vaultsWithFoldersResult, event, selectedItems, selectedItemsAnalysis ->
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
                    selectedItems,
                    selectedItemsAnalysis
                ),
                event = event,
                mode = mode.migrateMode(),
                folderIdToExpand = (mode as? Mode.MoveFolder)?.folderId.toOption(),
                disabledFolderId = selectedItemsAnalysis.disabledFolderId,
                disabledFolderItemCount = selectedItemsAnalysis.disabledFolderItemCount
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = MigrateSelectVaultUiState.Uninitialised
    )

    internal fun onVaultSelected(shareId: ShareId) {
        when (val currentMode = mode) {
            is Mode.MigrateSelectedItems -> eventFlow.update {
                SelectVaultEvent.VaultSelectedForMigrateItem(
                    destinationShareId = shareId
                ).toOption()
            }

            is Mode.MigrateAllItems -> eventFlow.update {
                SelectVaultEvent.VaultSelectedForMigrateAll(
                    sourceShareId = currentMode.shareId,
                    destinationShareId = shareId
                ).toOption()
            }

            is Mode.MoveFolder -> {
                if (currentParentFolderIdFlow.value is None) {
                    viewModelScope.launch { snackbarDispatcher(FolderAlreadySameParent) }
                    return
                }
                eventFlow.update {
                    SelectVaultEvent.VaultSelectedForMoveFolder(
                        shareId = currentMode.sourceShareId,
                        folderId = currentMode.folderId
                    ).toOption()
                }
            }
        }
    }

    internal fun onFolderSelected(shareId: ShareId, newParentFolderId: FolderId) {
        when (val currentMode = mode) {
            is Mode.MigrateSelectedItems -> {
                eventFlow.update {
                    SelectVaultEvent.VaultSelectedForMigrateItem(
                        destinationShareId = shareId,
                        destFolderId = newParentFolderId.toOption()
                    ).toOption()
                }
            }

            is Mode.MoveFolder -> {
                val currentParent = currentParentFolderIdFlow.value
                if (currentParent is Some && currentParent.value == newParentFolderId) {
                    viewModelScope.launch { snackbarDispatcher(FolderAlreadySameParent) }
                    return
                }
                eventFlow.update {
                    SelectVaultEvent.VaultSelectedForMoveFolder(
                        shareId = currentMode.sourceShareId,
                        folderId = currentMode.folderId,
                        newParentFolderId = newParentFolderId
                    ).toOption()
                }
            }

            else -> Unit
        }
    }

    internal fun clearEvent() {
        eventFlow.update { None }
    }

    private fun prepareVaults(
        vaults: List<VaultWithItemCount>,
        vaultFolders: Map<ShareId, PersistentList<FolderUiModel>>,
        selectedItems: Option<Map<ShareId, List<ItemId>>>,
        selectedItemsAnalysis: SelectedItemsAnalysis
    ): ImmutableList<MigrateVaultState> = vaults
        .filter {
            when (mode) {
                is Mode.MigrateSelectedItems ->
                    mode.filter != MigrateVaultFilter.Shared || it.vault.shared
                is Mode.MoveFolder -> it.vault.shareId == mode.sourceShareId
                is Mode.MigrateAllItems -> true
            }
        }
        .map { prepareVault(it, vaultFolders, selectedItems, selectedItemsAnalysis) }
        .toImmutableList()

    @Suppress("LongMethod")
    private fun prepareVault(
        vault: VaultWithItemCount,
        vaultFolders: Map<ShareId, PersistentList<FolderUiModel>>,
        selectedItems: Option<Map<ShareId, List<ItemId>>>,
        selectedItemsAnalysis: SelectedItemsAnalysis
    ): MigrateVaultState {
        val canCreate = vault.vault.role.toPermissions().canCreate()
        val folderTree = vaultFolders[vault.vault.shareId] ?: persistentListOf()
        return when (mode) {
            is Mode.MigrateSelectedItems -> {
                when (selectedItems) {
                    None -> MigrateVaultState(
                        vaultWithItemCount = vault,
                        status = VaultStatus.Disabled(
                            reason = VaultStatus.DisabledReason.NoPermission
                        ),
                        folderTree = folderTree
                    )

                    is Some -> {
                        val selectedItemsMap = selectedItems.value
                        val state = if (selectedItemsMap.size == 1) {
                            val shareToBeMoved = selectedItemsMap.entries.first()
                            val isSameVault = vault.vault.shareId == shareToBeMoved.key
                            when {
                                !isSameVault && canCreate -> VaultStatus.Enabled
                                !isSameVault && !canCreate -> VaultStatus.Disabled(
                                    reason = VaultStatus.DisabledReason.NoPermission
                                )
                                selectedItemsAnalysis.disableSourceVault && canCreate ->
                                    VaultStatus.Disabled(
                                        reason = VaultStatus.DisabledReason.SameVault
                                    )
                                isSameVault && canCreate -> VaultStatus.Enabled
                                else -> VaultStatus.Disabled(
                                    reason = VaultStatus.DisabledReason.NoPermission
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

                        MigrateVaultState(
                            vaultWithItemCount = vault,
                            status = state,
                            folderTree = folderTree
                        )
                    }
                }
            }

            is Mode.MigrateAllItems -> {
                val isNotCurrentOne = vault.vault.shareId != mode.shareId
                MigrateVaultState(
                    vaultWithItemCount = vault,
                    status = if (isNotCurrentOne) VaultStatus.Enabled else VaultStatus.Disabled(
                        reason = VaultStatus.DisabledReason.SameVault
                    ),
                    folderTree = folderTree
                )
            }

            is Mode.MoveFolder -> MigrateVaultState(
                vaultWithItemCount = vault,
                status = VaultStatus.Enabled,
                folderTree = folderTree
            )
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

    private fun observeFolderTreeForShare(shareKey: VaultShareKey): Flow<Pair<ShareId, PersistentList<FolderUiModel>>> {
        val foldersFlow: Flow<List<Folder>> = when (mode) {
            is Mode.MoveFolder -> sourceFoldersFlow
            else -> observeFolders(shareKey.userId, shareKey.shareId)
        }
        return foldersFlow
            .distinctUntilChanged()
            .map { folderList ->
                val tree = FolderTreeBuilder.build(folderList)
                val filteredTree = when (val m = mode) {
                    is Mode.MoveFolder -> removeFolderFromTree(tree, m.folderId)
                    else -> tree
                }
                shareKey.shareId to filteredTree
            }
            .onStart {
                emit(shareKey.shareId to persistentListOf())
            }
    }

    private fun removeFolderFromTree(folders: List<FolderUiModel>, folderId: FolderId): PersistentList<FolderUiModel> =
        folders
            .filter { it.id != folderId }
            .map { it.copy(folders = removeFolderFromTree(it.folders, folderId)) }
            .toPersistentList()

    private fun getMode(): Mode {
        return when (MigrateModeValue.valueOf(savedStateHandle.get().require(MigrateModeArg.key))) {
            MigrateModeValue.SelectedItems -> Mode.MigrateSelectedItems(
                filter = savedStateHandle.get()
                    .require<String>(MigrateVaultFilterArg.key)
                    .let(MigrateVaultFilter::valueOf),
                sourceFolderId = savedStateHandle.get()
                    .get<String>(CommonOptionalNavArgId.FolderId.key)
                    ?.let(::FolderId)
                    .toOption()
            )

            MigrateModeValue.AllVaultItems -> Mode.MigrateAllItems(
                shareId = savedStateHandle.get()
                    .require<String>(CommonNavArgId.ShareId.key)
                    .let(::ShareId)
            )

            MigrateModeValue.MoveFolder -> Mode.MoveFolder(
                sourceShareId = savedStateHandle.get()
                    .require<String>(CommonNavArgId.ShareId.key)
                    .let(::ShareId),
                folderId = savedStateHandle.get()
                    .require<String>(CommonOptionalNavArgId.FolderId.key)
                    .let(::FolderId)
            )
        }
    }

    internal sealed interface Mode {

        data class MigrateSelectedItems(
            val filter: MigrateVaultFilter,
            val sourceFolderId: Option<FolderId> = None
        ) : Mode

        data class MigrateAllItems(val shareId: ShareId) : Mode

        data class MoveFolder(
            val sourceShareId: ShareId,
            val folderId: FolderId
        ) : Mode

        fun migrateMode(): MigrateMode = when (this) {
            is MigrateSelectedItems -> MigrateMode.MigrateItem
            is MigrateAllItems -> MigrateMode.MigrateAll
            is MoveFolder -> MigrateMode.MoveFolder
        }
    }

    private companion object {

        private const val TAG = "MigrateSelectVaultViewModel"

    }

}

internal data class SelectedItemsAnalysis(
    val sourceShareId: ShareId?,
    val disableSourceVault: Boolean,
    val disabledFolderId: Option<FolderId>,
    val disabledFolderItemCount: Int
) {
    companion object {
        val Empty = SelectedItemsAnalysis(
            sourceShareId = null,
            disableSourceVault = false,
            disabledFolderId = None,
            disabledFolderItemCount = 0
        )
    }
}

internal fun analyzeSelectedItems(selection: BulkMoveToVaultSelection): SelectedItemsAnalysis {
    if (selection.size != 1) return SelectedItemsAnalysis.Empty

    val sourceShareId = selection.keys.firstOrNull() ?: return SelectedItemsAnalysis.Empty
    val containers = selection[sourceShareId].orEmpty().filterValues { it.isNotEmpty() }

    val hasRootItems = containers.keys.any { it is ParentContainer.Share }
    val folderContainers = containers.keys
        .mapNotNull { it as? ParentContainer.Folder }
    val folderIds = folderContainers.map { it.folderId }.toSet()

    val disableSourceVault = hasRootItems && folderIds.isEmpty()
    val disabledFolderId = when {
        !hasRootItems && folderIds.size == 1 -> folderIds.first().toOption()
        else -> None
    }
    val disabledFolderItemCount = if (disabledFolderId is Some) {
        containers.entries
            .firstOrNull { (container, _) ->
                container is ParentContainer.Folder && container.folderId == disabledFolderId.value
            }
            ?.value
            ?.size
            ?: 0
    } else {
        0
    }

    return SelectedItemsAnalysis(
        sourceShareId = sourceShareId,
        disableSourceVault = disableSourceVault,
        disabledFolderId = disabledFolderId,
        disabledFolderItemCount = disabledFolderItemCount
    )
}
