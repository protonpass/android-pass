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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.asLoadingResult
import proton.android.pass.common.api.combineN
import proton.android.pass.common.api.getOrNull
import proton.android.pass.common.api.safeRunCatching
import proton.android.pass.common.api.some
import proton.android.pass.common.api.toOption
import proton.android.pass.commonpresentation.api.folders.FolderTreeBuilder
import proton.android.pass.commonui.api.require
import proton.android.pass.commonuimodels.api.FolderUiModel
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.data.api.repositories.BulkMoveToVaultEvent
import proton.android.pass.data.api.repositories.BulkMoveToVaultRepository
import proton.android.pass.data.api.repositories.MigrateItemsResult
import proton.android.pass.data.api.usecases.GetVaultWithItemCountById
import proton.android.pass.data.api.usecases.MigrateItems
import proton.android.pass.data.api.usecases.MigrateVault
import proton.android.pass.data.api.usecases.folders.MoveFolder
import proton.android.pass.data.api.usecases.folders.ObserveFolders
import proton.android.pass.data.api.usecases.securelink.ObserveHasAssociatedSecureLinks
import proton.android.pass.data.api.usecases.shares.ObserveShare
import proton.android.pass.domain.FolderId
import proton.android.pass.domain.ShareId
import proton.android.pass.features.migrate.MigrateNewParentFolderNavArgId
import proton.android.pass.features.migrate.MigrateModeArg
import proton.android.pass.features.migrate.MigrateModeValue
import proton.android.pass.features.migrate.MigrateSnackbarMessage
import proton.android.pass.navigation.api.CommonOptionalNavArgId
import proton.android.pass.log.api.PassLogger
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.navigation.api.DestinationShareNavArgId
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.android.pass.preferences.InternalSettingsRepository
import javax.inject.Inject

@HiltViewModel
class MigrateConfirmVaultViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val migrateItems: MigrateItems,
    private val migrateVault: MigrateVault,
    private val moveFolder: MoveFolder,
    private val snackbarDispatcher: SnackbarDispatcher,
    private val bulkMoveToVaultRepository: BulkMoveToVaultRepository,
    private val observeHasAssociatedSecureLinks: ObserveHasAssociatedSecureLinks,
    private val observeFolders: ObserveFolders,
    getVaultById: GetVaultWithItemCountById,
    observeShare: ObserveShare,
    private val settingsRepository: InternalSettingsRepository
) : ViewModel() {

    private val mode = getMode()

    private val isLoadingFlow: MutableStateFlow<IsLoadingState> =
        MutableStateFlow(IsLoadingState.NotLoading)
    private val eventFlow: MutableStateFlow<Option<ConfirmMigrateEvent>> =
        MutableStateFlow(None)

    private val selectedItemsFlow = bulkMoveToVaultRepository.observe()
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = None
        )

    private val getVaultFlow = getVaultById(shareId = mode.destShareId)
        .catch {
            PassLogger.e(TAG, it, "Error getting Vault by id")
            eventFlow.update { ConfirmMigrateEvent.Close.toOption() }
        }
        .asLoadingResult()

    private val hasAssociatedSecureLinksFlow = selectedItemsFlow
        .flatMapLatest { selectedItemsOption ->
            when (selectedItemsOption) {
                None -> flowOf(false)
                is Some -> observeHasAssociatedSecureLinks(selectedItemsOption.value)
            }
        }

    private val shareId = ShareId(savedStateHandle.require(DestinationShareNavArgId.key))
    private val canDisplayWarningVaultSharedDialogFlow = combine(
        settingsRepository.hasShownItemInSharedVaultWarning(),
        observeShare(shareId = shareId)
    ) { hasShownItemInSharedVaultWarning, share ->
        !hasShownItemInSharedVaultWarning && share.shared
    }.onStart { emit(false) }

    private val folderTreeFlow: Flow<PersistentList<FolderUiModel>> =
        if (mode !is Mode.MoveFolder || mode.newParentFolderId == null) {
            flowOf(persistentListOf<FolderUiModel>())
        } else {
            getVaultById(shareId = mode.sourceShareId)
                .flatMapLatest { vault ->
                    observeFolders(vault.vault.userId, vault.vault.shareId)
                        .map { FolderTreeBuilder.build(it) }
                }
                .catch { emit(persistentListOf<FolderUiModel>()) }
                .onStart { emit(persistentListOf<FolderUiModel>()) }
        }

    internal val state: StateFlow<MigrateConfirmVaultUiState> = combineN(
        isLoadingFlow,
        getVaultFlow,
        eventFlow,
        selectedItemsFlow,
        hasAssociatedSecureLinksFlow,
        canDisplayWarningVaultSharedDialogFlow,
        folderTreeFlow
    ) { isLoading, vaultRes, event, selectedItems, hasAssociatedSecureLinks,
        canDisplayWarningVaultSharedDialog, folderTree ->
        val loading = isLoading is IsLoadingState.Loading || vaultRes is LoadingResult.Loading
        val vault = vaultRes.getOrNull().toOption()
        val itemCount = selectedItems.map { entries -> entries.values.sumOf { it.size } }
        MigrateConfirmVaultUiState(
            isLoading = IsLoadingState.from(loading),
            vault = vault,
            event = event,
            mode = mode.migrateMode(itemCount),
            hasAssociatedSecureLinks = hasAssociatedSecureLinks,
            canDisplayWarningVaultSharedDialog = canDisplayWarningVaultSharedDialog,
            folderTree = folderTree,
            newParentFolderId = (mode as? Mode.MoveFolder)?.newParentFolderId
        )

    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = MigrateConfirmVaultUiState.initial(mode.migrateMode(None))
    )

    internal fun doNotDisplayWarningDialog() {
        settingsRepository.setHasShownItemInSharedVaultWarning(true)
    }

    internal fun onConfirm() {
        viewModelScope.launch {
            when (mode) {
                is Mode.MigrateAllItems -> performAllItemsMigration(
                    sourceShareId = mode.sourceShareId,
                    destShareId = mode.destShareId
                )

                is Mode.MigrateSelectedItems -> performItemMigration(
                    destShareId = mode.destShareId
                )

                is Mode.MoveFolder -> performFolderMove(
                    shareId = mode.sourceShareId,
                    folderId = mode.folderId,
                    newParentFolderId = mode.newParentFolderId
                )
            }
        }
    }

    private suspend fun performFolderMove(
        shareId: ShareId,
        folderId: FolderId,
        newParentFolderId: FolderId?
    ) {
        isLoadingFlow.update { IsLoadingState.Loading }
        safeRunCatching {
            moveFolder(
                shareId = shareId,
                folderId = folderId,
                newParentFolderId = newParentFolderId
            )
        }.onSuccess {
            eventFlow.update { ConfirmMigrateEvent.FolderMoved.toOption() }
            snackbarDispatcher(MigrateSnackbarMessage.FolderMoved)
            isLoadingFlow.update { IsLoadingState.NotLoading }
        }.onFailure {
            isLoadingFlow.update { IsLoadingState.NotLoading }
            PassLogger.w(TAG, "Error moving folder")
            PassLogger.w(TAG, it)
            snackbarDispatcher(MigrateSnackbarMessage.FolderNotMoved)
        }
    }

    private suspend fun performAllItemsMigration(sourceShareId: ShareId, destShareId: ShareId) {
        isLoadingFlow.update { IsLoadingState.Loading }
        safeRunCatching {
            migrateVault(
                origin = sourceShareId,
                dest = destShareId
            )
        }.onSuccess {
            eventFlow.update { ConfirmMigrateEvent.AllItemsMigrated.some() }
            snackbarDispatcher(MigrateSnackbarMessage.VaultItemsMigrated)
            isLoadingFlow.update { IsLoadingState.NotLoading }
        }.onFailure {
            isLoadingFlow.update { IsLoadingState.NotLoading }
            PassLogger.w(TAG, "Error migrating all items")
            PassLogger.w(TAG, it)
            snackbarDispatcher(MigrateSnackbarMessage.VaultItemsNotMigrated)
        }
    }

    private suspend fun performItemMigration(destShareId: ShareId) {
        val itemsToMigrate = selectedItemsFlow.value.value() ?: run {
            PassLogger.w(TAG, "Wanted to migrate selected items but none were selected")
            return
        }

        isLoadingFlow.update { IsLoadingState.Loading }

        safeRunCatching {
            migrateItems(
                items = itemsToMigrate,
                destinationShare = destShareId
            )
        }.onSuccess { migrateResult ->
            when (migrateResult) {
                is MigrateItemsResult.AllMigrated -> {
                    val migratedItem = migrateResult.items.firstOrNull()
                    if (migratedItem == null) {
                        PassLogger.w(TAG, "No items were migrated")
                        snackbarDispatcher(MigrateSnackbarMessage.ItemNotMigrated)
                        return@onSuccess
                    }

                    eventFlow.update {
                        ConfirmMigrateEvent.ItemMigrated(
                            shareId = migratedItem.shareId,
                            itemId = migratedItem.id
                        ).toOption()
                    }
                    bulkMoveToVaultRepository.emitEvent(BulkMoveToVaultEvent.Completed)
                    bulkMoveToVaultRepository.delete()

                    snackbarDispatcher(MigrateSnackbarMessage.ItemMigrated)
                }

                is MigrateItemsResult.SomeMigrated -> {
                    val migratedItem = migrateResult.migratedItems.firstOrNull()
                    if (migratedItem == null) {
                        PassLogger.w(TAG, "No items were migrated")
                        snackbarDispatcher(MigrateSnackbarMessage.ItemNotMigrated)
                        return@onSuccess
                    }

                    eventFlow.update {
                        ConfirmMigrateEvent.ItemMigrated(
                            shareId = migratedItem.shareId,
                            itemId = migratedItem.id
                        ).toOption()
                    }

                    snackbarDispatcher(MigrateSnackbarMessage.SomeItemsNotMigrated)
                }

                is MigrateItemsResult.NoneMigrated -> {
                    PassLogger.w(TAG, "Error migrating items")
                    PassLogger.w(TAG, migrateResult.exception)
                    snackbarDispatcher(MigrateSnackbarMessage.ItemNotMigrated)
                }
            }
        }.onFailure {
            PassLogger.w(TAG, "Error migrating item")
            PassLogger.w(TAG, it)
            snackbarDispatcher(MigrateSnackbarMessage.ItemNotMigrated)
        }

        isLoadingFlow.update { IsLoadingState.NotLoading }
    }

    internal fun onCancel() {
        eventFlow.update { ConfirmMigrateEvent.Close.toOption() }
    }

    private fun getMode(): Mode {
        val destShareId = ShareId(savedStateHandle.require(DestinationShareNavArgId.key))
        return when (getNavMode()) {
            MigrateModeValue.SelectedItems -> Mode.MigrateSelectedItems(
                destShareId = destShareId
            )

            MigrateModeValue.AllVaultItems -> Mode.MigrateAllItems(
                sourceShareId = ShareId(savedStateHandle.require(CommonNavArgId.ShareId.key)),
                destShareId = destShareId
            )

            MigrateModeValue.MoveFolder -> Mode.MoveFolder(
                sourceShareId = destShareId,
                folderId = FolderId(savedStateHandle.require(CommonOptionalNavArgId.FolderId.key)),
                newParentFolderId = savedStateHandle.get<String?>(MigrateNewParentFolderNavArgId.key)
                    ?.let(::FolderId)
            )
        }
    }

    internal sealed interface Mode {
        val destShareId: ShareId

        data class MigrateSelectedItems(
            override val destShareId: ShareId
        ) : Mode

        data class MigrateAllItems(
            val sourceShareId: ShareId,
            override val destShareId: ShareId
        ) : Mode

        data class MoveFolder(
            val sourceShareId: ShareId,
            val folderId: FolderId,
            val newParentFolderId: FolderId? = null
        ) : Mode {
            override val destShareId: ShareId get() = sourceShareId
        }

        fun migrateMode(selectedItems: Option<Int>): MigrateMode = when (this) {
            is MigrateSelectedItems -> MigrateMode.MigrateSelectedItems(
                number = selectedItems.value() ?: 0
            )

            is MigrateAllItems -> MigrateMode.MigrateAll

            is MoveFolder -> MigrateMode.MoveFolder
        }
    }

    private fun getNavMode(): MigrateModeValue = MigrateModeValue.valueOf(
        savedStateHandle.require(MigrateModeArg.key)
    )

    private companion object {

        private const val TAG = "MigrateConfirmVaultViewModel"

    }

}
