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

package proton.android.pass.features.migrate.confirmvault

import androidx.lifecycle.SavedStateHandle
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
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.asLoadingResult
import proton.android.pass.common.api.combineN
import proton.android.pass.common.api.toOption
import proton.android.pass.commonpresentation.api.folders.FolderTreeBuilder
import proton.android.pass.commonui.api.require
import proton.android.pass.commonuimodels.api.FolderUiModel
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.data.api.repositories.BulkMoveToVaultRepository
import proton.android.pass.data.api.repositories.BulkMoveToVaultSelection
import proton.android.pass.data.api.repositories.flattenByShare
import proton.android.pass.data.api.usecases.ObserveVaultsWithItemCount
import proton.android.pass.data.api.usecases.folders.ObserveFoldersByParentId
import proton.android.pass.data.api.usecases.items.GetMigrationItemsSelection
import proton.android.pass.domain.items.MigrationItemsSelection
import proton.android.pass.data.api.usecases.securelink.ObserveHasAssociatedSecureLinks
import proton.android.pass.data.api.usecases.shares.ObserveShare
import proton.android.pass.domain.Folder
import proton.android.pass.domain.FolderId
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.VaultWithItemCount
import proton.android.pass.domain.canCreate
import proton.android.pass.domain.toPermissions
import proton.android.pass.features.migrate.MigrateModeArg
import proton.android.pass.features.migrate.MigrateModeValue
import proton.android.pass.features.migrate.MigrateSnackbarMessage
import proton.android.pass.features.migrate.MigrateVaultFilter
import proton.android.pass.features.migrate.MigrateVaultFilterArg
import proton.android.pass.log.api.PassLogger
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.navigation.api.CommonOptionalNavArgId
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.android.pass.preferences.InternalSettingsRepository
import javax.inject.Inject

@HiltViewModel
class MigrateConfirmVaultViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val migrator: MigrateConfirmVaultMigrator,
    private val snackbarDispatcher: SnackbarDispatcher,
    private val observeHasAssociatedSecureLinks: ObserveHasAssociatedSecureLinks,
    private val observeFolders: ObserveFoldersByParentId,
    private val observeShare: ObserveShare,
    private val settingsRepository: InternalSettingsRepository,
    private val getMigrationItemsSelection: GetMigrationItemsSelection,
    bulkMoveToVaultRepository: BulkMoveToVaultRepository,
    observeVaults: ObserveVaultsWithItemCount
) : ViewModel() {

    private data class VaultShareKey(val userId: UserId, val shareId: ShareId)
    private data class VaultsWithFolders(
        val vaultShares: List<VaultWithItemCount>,
        val vaultFolders: Map<ShareId, PersistentList<FolderUiModel>?>
    ) {
        val isFoldersLoading: Boolean get() = vaultFolders.values.any { it == null }
    }

    private val mode: Mode = getMode()

    private val selectedDestinationFlow = MutableStateFlow<Option<SelectedDestination>>(None)
    private val isLoadingFlow = MutableStateFlow<IsLoadingState>(IsLoadingState.NotLoading)
    private val eventFlow = MutableStateFlow<Option<ConfirmMigrateEvent>>(None)
    private val showDissolveFolderDialogFlow = MutableStateFlow(false)

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
            .map { it.map { sel -> sel.flattenByShare() } }
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
        .map { vaults ->
            vaults.asSequence()
                .map { VaultShareKey(userId = it.vault.userId, shareId = it.vault.shareId) }
                .distinct()
                .sortedWith(compareBy<VaultShareKey> { it.userId.id }.thenBy { it.shareId.id })
                .toList()
        }
        .distinctUntilChanged()

    private val sourceFoldersFlow: StateFlow<List<Folder>> = when (val m = mode) {
        is Mode.MoveFolder ->
            vaultSharesFlow
                .flatMapLatest { vaults ->
                    val userId = vaults.firstOrNull()?.vault?.userId
                        ?: return@flatMapLatest flowOf(emptyList())
                    observeFolders(userId, m.sourceShareId).take(1)
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

    private val descendantFolderIdsFlow: StateFlow<Set<FolderId>> = sourceFoldersFlow
        .map { folders ->
            if (mode is Mode.MoveFolder) findDescendantIds(folders, mode.folderId) else emptySet()
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = emptySet()
        )

    private val vaultFoldersFlow: Flow<Map<ShareId, PersistentList<FolderUiModel>?>> =
        vaultShareKeysFlow.flatMapLatest { shareKeys ->
            if (shareKeys.isEmpty()) return@flatMapLatest flowOf(emptyMap())
            combine(shareKeys.map(::observeFolderTreeForShare)) { pairs -> pairs.toMap() }
                .distinctUntilChanged()
        }

    private val vaultsWithFoldersFlow: Flow<VaultsWithFolders> = combine(
        vaultSharesFlow,
        vaultFoldersFlow,
        ::VaultsWithFolders
    ).catch { e ->
        PassLogger.w(TAG, "Error observing vaults")
        PassLogger.w(TAG, e)
        snackbarDispatcher(MigrateSnackbarMessage.CouldNotInit)
        emit(VaultsWithFolders(vaultShares = emptyList(), vaultFolders = emptyMap()))
    }

    private val hasAssociatedSecureLinksFlow = selectedItemsFlow
        .flatMapLatest { selectedItemsOption ->
            when (selectedItemsOption) {
                None -> flowOf(false)
                is Some -> observeHasAssociatedSecureLinks(selectedItemsOption.value)
            }
        }

    private val hasItemsWithHighRevisionCountFlow: Flow<Boolean> = when (val m = mode) {
        is Mode.MigrateSelectedItems ->
            selectedItemsFlow
                .mapLatest { selectedItemsOption ->
                    when (selectedItemsOption) {
                        None -> false
                        is Some -> getMigrationItemsSelection(selectedItemsOption.value).hasItemsExceedingRevisionLimit
                    }
                }
                .catch { emit(false) }
                .onStart { emit(false) }
        is Mode.MoveAllItemsInFolder ->
            revisionLimitFlow { getMigrationItemsSelection(m.sourceShareId, m.folderId) }
        is Mode.MoveFolder ->
            revisionLimitFlow { getMigrationItemsSelection(m.sourceShareId, m.folderId) }
        is Mode.MigrateAllItems ->
            revisionLimitFlow { getMigrationItemsSelection(m.shareId) }
    }

    private val sourceHasNestedFolderItemsFlow: Flow<Boolean> = when (val m = mode) {
        is Mode.MoveAllItemsInFolder ->
            flow {
                emit(
                    getMigrationItemsSelection(m.sourceShareId, m.folderId)
                        .hasItemsInDescendantFolders(m.folderId)
                )
            }
                .catch { emit(false) }
                .onStart { emit(false) }
        is Mode.MigrateAllItems ->
            flow { emit(getMigrationItemsSelection(m.shareId).hasItemsInFolders) }
                .catch { emit(false) }
                .onStart { emit(false) }
        else -> flowOf(false)
    }

    private data class ItemSelectionAnalysis(
        val hasItemsWithHighRevisionCount: Boolean,
        val sourceHasNestedFolderItems: Boolean
    )

    private val itemSelectionAnalysisFlow: Flow<ItemSelectionAnalysis> = combine(
        hasItemsWithHighRevisionCountFlow,
        sourceHasNestedFolderItemsFlow
    ) { highRevision, nestedItems -> ItemSelectionAnalysis(highRevision, nestedItems) }

    private val canDisplayWarningVaultSharedDialogFlow = selectedDestinationFlow
        .flatMapLatest { destOpt ->
            when (destOpt) {
                None -> flowOf(false)
                is Some -> combine(
                    settingsRepository.hasShownItemInSharedVaultWarning(),
                    observeShare(shareId = destOpt.value.shareId)
                ) { hasShown, share -> !hasShown && share.shared }
            }
        }
        .onStart { emit(false) }

    internal val state: StateFlow<MigrateConfirmVaultUiState> = combineN(
        isLoadingFlow,
        eventFlow,
        selectedItemsFlow,
        selectedItemsAnalysisFlow,
        vaultsWithFoldersFlow.asLoadingResult(),
        hasAssociatedSecureLinksFlow,
        canDisplayWarningVaultSharedDialogFlow,
        selectedDestinationFlow,
        showDissolveFolderDialogFlow,
        itemSelectionAnalysisFlow,
        currentParentFolderIdFlow,
        descendantFolderIdsFlow
    ) { isLoading, event, selectedItems, selectedItemsAnalysis, vaultsResult,
        hasSecureLinks, canDisplayWarning, selectedDest, showDissolveDialog,
        itemSelectionAnalysis, currentParentFolderId, descendantFolderIds ->

        val hasItemsWithHighRevisionCount = itemSelectionAnalysis.hasItemsWithHighRevisionCount
        val sourceHasChildFolders = itemSelectionAnalysis.sourceHasNestedFolderItems

        val (vaultList, isLoadingVaults) = when (vaultsResult) {
            LoadingResult.Loading -> persistentListOf<MigrateVaultState>() to true
            is LoadingResult.Error -> persistentListOf<MigrateVaultState>() to false
            is LoadingResult.Success -> {
                val data = vaultsResult.data
                if (data.isFoldersLoading) {
                    persistentListOf<MigrateVaultState>() to true
                } else {
                    prepareVaults(
                        data.vaultShares,
                        data.vaultFolders.mapValues { it.value ?: persistentListOf() },
                        selectedItems,
                        selectedItemsAnalysis,
                        sourceHasChildFolders
                    ) to false
                }
            }
        }

        val sourceName = when (vaultsResult) {
            is LoadingResult.Success -> when (val m = mode) {
                is Mode.MigrateAllItems ->
                    vaultsResult.data.vaultShares.find { it.vault.shareId == m.shareId }?.vault?.name ?: ""
                is Mode.MoveAllItemsInFolder -> {
                    val folders = vaultsResult.data.vaultFolders[m.sourceShareId] ?: persistentListOf()
                    findFolderName(folders, m.folderId) ?: ""
                }
                else -> ""
            }
            else -> ""
        }

        val itemCount = selectedItems.map { entries -> entries.values.sumOf { it.size } }
        val isSameVaultMove = when (mode) {
            is Mode.MoveFolder -> true
            is Mode.MigrateSelectedItems ->
                selectedDest.value()?.shareId != null &&
                    selectedItems.value()?.keys?.singleOrNull() == selectedDest.value()?.shareId
            is Mode.MigrateAllItems -> selectedDest.value()?.shareId == mode.shareId
            is Mode.MoveAllItemsInFolder -> selectedDest.value()?.shareId == mode.sourceShareId
        }

        MigrateConfirmVaultUiState(
            isLoading = IsLoadingState.from(isLoading is IsLoadingState.Loading || isLoadingVaults),
            isLoadingVaults = isLoadingVaults,
            event = event,
            vaultList = vaultList,
            folderIdToExpand = if (mode is Mode.MoveFolder) currentParentFolderId else None,
            disabledFolderId = when (mode) {
                is Mode.MoveFolder -> currentParentFolderId
                is Mode.MoveAllItemsInFolder ->
                    if (sourceHasChildFolders) None else mode.folderId.toOption()
                else -> selectedItemsAnalysis.disabledFolderId
            },
            disabledFolderItemCount = selectedItemsAnalysis.disabledFolderItemCount,
            disabledDescendantFolderIds = descendantFolderIds,
            movingFolderId = if (mode is Mode.MoveFolder) mode.folderId.toOption() else None,
            selectedShareId = selectedDest.map { it.shareId },
            selectedFolderId = selectedDest.flatMap { it.folderId },
            mode = mode.migrateMode(itemCount),
            hasAssociatedSecureLinks = hasSecureLinks,
            canDisplayWarningVaultSharedDialog = canDisplayWarning,
            isSameVaultMove = isSameVaultMove,
            showDissolveFolderDialog = showDissolveDialog,
            hasItemsWithHighRevisionCount = hasItemsWithHighRevisionCount,
            sourceHasChildFolders = sourceHasChildFolders,
            sourceName = sourceName
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = MigrateConfirmVaultUiState.initial(mode.migrateMode(None))
    )

    internal fun onVaultSelected(shareId: ShareId) {
        when (val currentMode = mode) {
            is Mode.MoveFolder -> {
                if (currentParentFolderIdFlow.value is None) {
                    showDissolveFolderDialogFlow.update { true }
                    return
                }
                selectedDestinationFlow.update {
                    SelectedDestination(shareId = currentMode.sourceShareId).toOption()
                }
            }
            else -> selectedDestinationFlow.update {
                SelectedDestination(shareId = shareId).toOption()
            }
        }
    }

    internal fun onDismissDissolveFolderDialog() {
        showDissolveFolderDialogFlow.update { false }
    }

    internal fun onConfirmDissolveFolder() {
        showDissolveFolderDialogFlow.update { false }
        val currentMode = mode as? Mode.MoveFolder ?: return
        viewModelScope.launch {
            isLoadingFlow.update { IsLoadingState.Loading }
            val result = migrator.performFolderDissolve(
                shareId = currentMode.sourceShareId,
                folderId = currentMode.folderId
            )
            isLoadingFlow.update { IsLoadingState.NotLoading }
            result.value()?.let { event -> eventFlow.update { event.toOption() } }
        }
    }

    internal fun onFolderSelected(shareId: ShareId, folderId: FolderId) {
        when (val currentMode = mode) {
            is Mode.MigrateSelectedItems -> selectedDestinationFlow.update {
                SelectedDestination(shareId = shareId, folderId = folderId.toOption()).toOption()
            }
            is Mode.MoveFolder -> {
                if (folderId == currentMode.folderId || folderId in descendantFolderIdsFlow.value) return
                val currentParent = currentParentFolderIdFlow.value
                if (currentParent is Some && currentParent.value == folderId) {
                    viewModelScope.launch {
                        snackbarDispatcher(MigrateSnackbarMessage.FolderAlreadySameParent)
                    }
                    return
                }
                selectedDestinationFlow.update {
                    SelectedDestination(shareId = currentMode.sourceShareId, folderId = folderId.toOption()).toOption()
                }
            }
            is Mode.MoveAllItemsInFolder -> {
                if (folderId == currentMode.folderId && !state.value.sourceHasChildFolders) return
                selectedDestinationFlow.update {
                    SelectedDestination(shareId = shareId, folderId = folderId.toOption()).toOption()
                }
            }
            is Mode.MigrateAllItems -> selectedDestinationFlow.update {
                SelectedDestination(shareId = shareId, folderId = folderId.toOption()).toOption()
            }
        }
    }

    internal fun doNotDisplayWarningDialog() {
        settingsRepository.setHasShownItemInSharedVaultWarning(true)
    }

    internal fun onConfirm() {
        val destination = selectedDestinationFlow.value.value() ?: return
        viewModelScope.launch {
            isLoadingFlow.update { IsLoadingState.Loading }
            val result = when (mode) {
                is Mode.MigrateAllItems -> migrator.performAllItemsMigration(
                    sourceShareId = mode.shareId,
                    destShareId = destination.shareId,
                    destFolderId = destination.folderId.value()
                )
                is Mode.MigrateSelectedItems -> {
                    val itemsToMigrate = selectedItemsFlow.value.value() ?: run {
                        PassLogger.w(TAG, "Wanted to migrate selected items but none were selected")
                        isLoadingFlow.update { IsLoadingState.NotLoading }
                        return@launch
                    }
                    migrator.performItemMigration(
                        destShareId = destination.shareId,
                        destFolderId = destination.folderId,
                        itemsToMigrate = itemsToMigrate
                    )
                }
                is Mode.MoveFolder -> migrator.performFolderMove(
                    shareId = mode.sourceShareId,
                    folderId = mode.folderId,
                    newParentFolderId = destination.folderId.value()
                )
                is Mode.MoveAllItemsInFolder -> migrator.performMoveAllItemsInFolder(
                    sourceShareId = mode.sourceShareId,
                    sourceFolderId = mode.folderId,
                    destShareId = destination.shareId,
                    destFolderId = destination.folderId.value()
                )
            }
            isLoadingFlow.update { IsLoadingState.NotLoading }
            result.value()?.let { event -> eventFlow.update { event.toOption() } }
        }
    }

    internal fun onCancel() {
        eventFlow.update { ConfirmMigrateEvent.Close.toOption() }
    }

    private fun prepareVaults(
        vaults: List<VaultWithItemCount>,
        vaultFolders: Map<ShareId, PersistentList<FolderUiModel>>,
        selectedItems: Option<Map<ShareId, List<ItemId>>>,
        selectedItemsAnalysis: SelectedItemsAnalysis,
        sourceHasChildFolders: Boolean = false
    ): ImmutableList<MigrateVaultState> = vaults
        .filter {
            when (mode) {
                is Mode.MigrateSelectedItems ->
                    mode.filter != MigrateVaultFilter.Shared || it.vault.shared
                is Mode.MoveFolder -> it.vault.shareId == mode.sourceShareId
                is Mode.MigrateAllItems -> true
                is Mode.MoveAllItemsInFolder -> true
            }
        }
        .map { prepareVault(it, vaultFolders, selectedItems, selectedItemsAnalysis, sourceHasChildFolders) }
        .toImmutableList()

    @Suppress("LongMethod")
    private fun prepareVault(
        vault: VaultWithItemCount,
        vaultFolders: Map<ShareId, PersistentList<FolderUiModel>>,
        selectedItems: Option<Map<ShareId, List<ItemId>>>,
        selectedItemsAnalysis: SelectedItemsAnalysis,
        sourceHasChildFolders: Boolean = false
    ): MigrateVaultState {
        val canCreate = vault.vault.role.toPermissions().canCreate()
        val folderTree = vaultFolders[vault.vault.shareId] ?: persistentListOf()
        val state = when (mode) {
            is Mode.MigrateSelectedItems -> {
                when (selectedItems) {
                    None -> MigrateVaultState(
                        vaultWithItemCount = vault,
                        status = VaultStatus.Disabled(VaultStatus.DisabledReason.NoPermission),
                        folderTree = folderTree
                    )
                    is Some -> {
                        val selectedItemsMap = selectedItems.value
                        val status = if (selectedItemsMap.size == 1) {
                            val shareToBeMoved = selectedItemsMap.entries.first()
                            val isSameVault = vault.vault.shareId == shareToBeMoved.key
                            when {
                                !isSameVault && canCreate -> VaultStatus.Enabled
                                !isSameVault && !canCreate ->
                                    VaultStatus.Disabled(VaultStatus.DisabledReason.NoPermission)
                                selectedItemsAnalysis.disableSourceVault && canCreate ->
                                    VaultStatus.Disabled(VaultStatus.DisabledReason.SameVault)
                                isSameVault && canCreate -> VaultStatus.Enabled
                                else -> VaultStatus.Disabled(VaultStatus.DisabledReason.NoPermission)
                            }
                        } else {
                            if (canCreate) VaultStatus.Enabled
                            else VaultStatus.Disabled(VaultStatus.DisabledReason.NoPermission)
                        }
                        MigrateVaultState(vaultWithItemCount = vault, status = status, folderTree = folderTree)
                    }
                }
            }
            is Mode.MigrateAllItems -> MigrateVaultState(
                vaultWithItemCount = vault,
                status = when {
                    !canCreate -> VaultStatus.Disabled(VaultStatus.DisabledReason.NoPermission)
                    vault.vault.shareId != mode.shareId -> VaultStatus.Enabled
                    sourceHasChildFolders -> VaultStatus.Enabled
                    else -> VaultStatus.Disabled(VaultStatus.DisabledReason.SameVault)
                },
                folderTree = folderTree
            )
            is Mode.MoveFolder -> MigrateVaultState(
                vaultWithItemCount = vault,
                status = VaultStatus.Enabled,
                folderTree = folderTree
            )
            is Mode.MoveAllItemsInFolder -> MigrateVaultState(
                vaultWithItemCount = vault,
                status = if (canCreate) VaultStatus.Enabled
                else VaultStatus.Disabled(VaultStatus.DisabledReason.NoPermission),
                folderTree = folderTree
            )
        }
        return if (!canCreate) state.copy(folderTree = persistentListOf()) else state
    }

    private fun observeFolderTreeForShare(
        shareKey: VaultShareKey
    ): Flow<Pair<ShareId, PersistentList<FolderUiModel>?>> {
        val foldersFlow: Flow<List<Folder>> = when (mode) {
            is Mode.MoveFolder -> sourceFoldersFlow
            else -> observeFolders(shareKey.userId, shareKey.shareId)
        }
        return foldersFlow
            .distinctUntilChanged()
            .map { folderList ->
                val tree: PersistentList<FolderUiModel>? = FolderTreeBuilder.build(folderList)
                shareKey.shareId to tree
            }
            .onStart { emit(shareKey.shareId to null) }
    }

    private fun revisionLimitFlow(block: suspend () -> MigrationItemsSelection): Flow<Boolean> =
        flow { emit(block().hasItemsExceedingRevisionLimit) }
            .catch { emit(true) }
            .onStart { emit(false) }

    private fun findDescendantIds(folders: List<Folder>, rootId: FolderId): Set<FolderId> {
        val result = mutableSetOf<FolderId>()
        val queue = ArrayDeque<FolderId>()
        queue.add(rootId)
        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            folders.filter { it.parentFolderId == current }.forEach { child ->
                result.add(child.folderId)
                queue.add(child.folderId)
            }
        }
        return result
    }

    private fun getMode(): Mode = when (MigrateModeValue.valueOf(savedStateHandle.require(MigrateModeArg.key))) {
        MigrateModeValue.SelectedItems -> Mode.MigrateSelectedItems(
            filter = MigrateVaultFilter.valueOf(savedStateHandle.require(MigrateVaultFilterArg.key)),
            sourceFolderId = savedStateHandle.get<String>(CommonOptionalNavArgId.FolderId.key)
                ?.let(::FolderId)
                .toOption()
        )
        MigrateModeValue.AllVaultItems -> Mode.MigrateAllItems(
            shareId = ShareId(savedStateHandle.require(CommonNavArgId.ShareId.key))
        )
        MigrateModeValue.MoveFolder -> Mode.MoveFolder(
            sourceShareId = ShareId(savedStateHandle.require(CommonNavArgId.ShareId.key)),
            folderId = FolderId(savedStateHandle.require(CommonOptionalNavArgId.FolderId.key))
        )
        MigrateModeValue.MoveAllItemsInFolder -> Mode.MoveAllItemsInFolder(
            sourceShareId = ShareId(savedStateHandle.require(CommonNavArgId.ShareId.key)),
            folderId = FolderId(savedStateHandle.require(CommonOptionalNavArgId.FolderId.key))
        )
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

        data class MoveAllItemsInFolder(
            val sourceShareId: ShareId,
            val folderId: FolderId
        ) : Mode

        fun migrateMode(selectedItemCount: Option<Int>): MigrateMode = when (this) {
            is MigrateSelectedItems -> MigrateMode.MigrateSelectedItems(selectedItemCount.value() ?: 0)
            is MigrateAllItems -> MigrateMode.MigrateAll
            is MoveFolder -> MigrateMode.MoveFolder
            is MoveAllItemsInFolder -> MigrateMode.MoveAllItemsInFolder
        }
    }

    private fun findFolderName(folders: List<FolderUiModel>, folderId: FolderId): String? {
        for (folder in folders) {
            if (folder.id == folderId) return folder.name
            findFolderName(folder.folders, folderId)?.let { return it }
        }
        return null
    }

    private companion object {
        private const val TAG = "MigrateConfirmVaultViewModel"
    }
}

internal data class SelectedDestination(
    val shareId: ShareId,
    val folderId: Option<FolderId> = None
)
