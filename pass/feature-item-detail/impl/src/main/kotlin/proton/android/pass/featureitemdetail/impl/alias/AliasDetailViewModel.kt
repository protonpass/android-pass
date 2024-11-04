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

package proton.android.pass.featureitemdetail.impl.alias

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import proton.android.pass.clipboard.api.ClipboardManager
import proton.android.pass.common.api.FlowUtils.oneShot
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.asLoadingResult
import proton.android.pass.common.api.combineN
import proton.android.pass.common.api.getOrNull
import proton.android.pass.common.api.toOption
import proton.android.pass.commonui.api.require
import proton.android.pass.commonui.api.toUiModel
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.composecomponents.impl.uievents.IsPermanentlyDeletedState
import proton.android.pass.composecomponents.impl.uievents.IsRestoredFromTrashState
import proton.android.pass.composecomponents.impl.uievents.IsSentToTrashState
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.errors.ItemNotFoundError
import proton.android.pass.data.api.repositories.BulkMoveToVaultRepository
import proton.android.pass.data.api.usecases.CanPerformPaidAction
import proton.android.pass.data.api.usecases.ChangeAliasStatus
import proton.android.pass.data.api.usecases.DeleteItems
import proton.android.pass.data.api.usecases.GetItemActions
import proton.android.pass.data.api.usecases.GetItemByIdWithVault
import proton.android.pass.data.api.usecases.GetUserPlan
import proton.android.pass.data.api.usecases.ItemActions
import proton.android.pass.data.api.usecases.ObserveAliasDetails
import proton.android.pass.data.api.usecases.PinItem
import proton.android.pass.data.api.usecases.RestoreItems
import proton.android.pass.data.api.usecases.TrashItems
import proton.android.pass.data.api.usecases.UnpinItem
import proton.android.pass.data.api.usecases.aliascontact.ObserveAliasContacts
import proton.android.pass.data.api.usecases.capabilities.CanShareVault
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.canUpdate
import proton.android.pass.domain.toPermissions
import proton.android.pass.featureitemdetail.impl.DetailSnackbarMessages
import proton.android.pass.featureitemdetail.impl.DetailSnackbarMessages.AliasChangeStatusError
import proton.android.pass.featureitemdetail.impl.DetailSnackbarMessages.AliasCopiedToClipboard
import proton.android.pass.featureitemdetail.impl.DetailSnackbarMessages.InitError
import proton.android.pass.featureitemdetail.impl.DetailSnackbarMessages.ItemMovedToTrash
import proton.android.pass.featureitemdetail.impl.DetailSnackbarMessages.ItemNotMovedToTrash
import proton.android.pass.featureitemdetail.impl.DetailSnackbarMessages.ItemNotRestored
import proton.android.pass.featureitemdetail.impl.DetailSnackbarMessages.ItemRestored
import proton.android.pass.featureitemdetail.impl.ItemDelete
import proton.android.pass.featureitemdetail.impl.common.AliasItemFeatures
import proton.android.pass.featureitemdetail.impl.common.ItemDetailEvent
import proton.android.pass.featureitemdetail.impl.common.ShareClickAction
import proton.android.pass.log.api.PassLogger
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.android.pass.preferences.FeatureFlag
import proton.android.pass.preferences.FeatureFlagsPreferencesRepository
import proton.android.pass.preferences.UserPreferencesRepository
import proton.android.pass.telemetry.api.EventItemType
import proton.android.pass.telemetry.api.TelemetryManager
import javax.inject.Inject

@[HiltViewModel Suppress("LongParameterList")]
class AliasDetailViewModel @Inject constructor(
    private val clipboardManager: ClipboardManager,
    private val snackbarDispatcher: SnackbarDispatcher,
    private val encryptionContextProvider: EncryptionContextProvider,
    private val trashItem: TrashItems,
    private val deleteItem: DeleteItems,
    private val restoreItem: RestoreItems,
    private val telemetryManager: TelemetryManager,
    private val canShareVault: CanShareVault,
    private val bulkMoveToVaultRepository: BulkMoveToVaultRepository,
    private val pinItem: PinItem,
    private val unpinItem: UnpinItem,
    private val changeAliasStatus: ChangeAliasStatus,
    observeAliasContacts: ObserveAliasContacts,
    userPreferencesRepository: UserPreferencesRepository,
    canPerformPaidAction: CanPerformPaidAction,
    getItemByIdWithVault: GetItemByIdWithVault,
    observeAliasDetails: ObserveAliasDetails,
    savedStateHandle: SavedStateHandle,
    getItemActions: GetItemActions,
    featureFlagsRepository: FeatureFlagsPreferencesRepository,
    getUserPlan: GetUserPlan
) : ViewModel() {

    private val shareId: ShareId = savedStateHandle
        .require<String>(CommonNavArgId.ShareId.key)
        .let(::ShareId)

    private val itemId: ItemId = savedStateHandle
        .require<String>(CommonNavArgId.ItemId.key)
        .let(::ItemId)

    private val loadingStates: Map<LoadingStateKey, MutableStateFlow<IsLoadingState>> = mapOf(
        LoadingStateKey.MovingToTrash to MutableStateFlow(IsLoadingState.NotLoading),
        LoadingStateKey.PermanentlyDeleting to MutableStateFlow(IsLoadingState.NotLoading),
        LoadingStateKey.RestoringFromTrash to MutableStateFlow(IsLoadingState.NotLoading),
        LoadingStateKey.Pinning to MutableStateFlow(IsLoadingState.NotLoading),
        LoadingStateKey.Unpinning to MutableStateFlow(IsLoadingState.NotLoading),
        LoadingStateKey.AliasStateToggling to MutableStateFlow(IsLoadingState.NotLoading)
    )
    private val allLoadingStates =
        combine<Pair<LoadingStateKey, IsLoadingState>, Map<LoadingStateKey, IsLoadingState>>(
            loadingStates.map { (key, flow) ->
                flow.map { key to it }
            },
            Array<Pair<LoadingStateKey, IsLoadingState>>::toMap
        )
    private val isItemSentToTrashState: MutableStateFlow<IsSentToTrashState> =
        MutableStateFlow(IsSentToTrashState.NotSent)
    private val isPermanentlyDeletedState: MutableStateFlow<IsPermanentlyDeletedState> =
        MutableStateFlow(IsPermanentlyDeletedState.NotDeleted)
    private val isRestoredFromTrashState: MutableStateFlow<IsRestoredFromTrashState> =
        MutableStateFlow(IsRestoredFromTrashState.NotRestored)
    private val eventState: MutableStateFlow<ItemDetailEvent> =
        MutableStateFlow(ItemDetailEvent.Unknown)

    private val canPerformPaidActionFlow: Flow<LoadingResult<Boolean>> =
        canPerformPaidAction().asLoadingResult()

    private val shareActionFlow: Flow<ShareClickAction> = canPerformPaidActionFlow
        .map { isPaidResult ->
            val isPaid = isPaidResult.getOrNull() ?: false
            val canShareVault = canShareVault(shareId).value()
            when {
                isPaid && canShareVault -> ShareClickAction.Share
                else -> ShareClickAction.Upgrade
            }
        }
        .distinctUntilChanged()

    private var hasItemBeenFetchedAtLeastOnce = false
    private val aliasItemDetailsResultFlow = getItemByIdWithVault(shareId, itemId)
        .catch { if (!(hasItemBeenFetchedAtLeastOnce && it is ItemNotFoundError)) throw it }
        .onEach { hasItemBeenFetchedAtLeastOnce = true }
        .asLoadingResult()

    private val itemFeaturesFlow: Flow<AliasItemFeatures> = combine(
        getUserPlan().map { it.isPaidPlan },
        featureFlagsRepository[FeatureFlag.SL_ALIASES_SYNC],
        userPreferencesRepository.observeAliasTrashDialogStatusPreference().map { it.value },
        featureFlagsRepository[FeatureFlag.ADVANCED_ALIAS_MANAGEMENT_V1],
        ::AliasItemFeatures
    )

    private val aliasDetailsAndContactsFlow = combine(
        observeAliasDetails(shareId, itemId).asLoadingResult(),
        observeAliasContacts(shareId, itemId).asLoadingResult(),
        ::Pair
    )

    internal val uiState: StateFlow<AliasDetailUiState> = combineN(
        aliasItemDetailsResultFlow,
        aliasDetailsAndContactsFlow,
        allLoadingStates,
        isItemSentToTrashState,
        isPermanentlyDeletedState,
        isRestoredFromTrashState,
        shareActionFlow,
        oneShot { getItemActions(shareId = shareId, itemId = itemId) }.asLoadingResult(),
        eventState,
        itemFeaturesFlow
    ) { itemLoadingResult,
        (aliasDetailsResult, aliasContactsResult),
        allLoadingStates,
        isItemSentToTrash,
        isPermanentlyDeleted,
        isRestoredFromTrash,
        shareAction,
        itemActions,
        event,
        itemFeatures ->
        when (itemLoadingResult) {
            is LoadingResult.Error -> {
                if (!isPermanentlyDeleted.value()) {
                    snackbarDispatcher(InitError)
                    AliasDetailUiState.Error
                } else {
                    AliasDetailUiState.Pending
                }
            }

            LoadingResult.Loading -> AliasDetailUiState.NotInitialised
            is LoadingResult.Success -> {
                val details = itemLoadingResult.data
                val vault = details.vault.takeIf { details.hasMoreThanOneVault }

                val permissions = details.vault.role.toPermissions()
                val canPerformItemActions = permissions.canUpdate()
                val actions = itemActions.getOrNull() ?: ItemActions.Disabled
                val aliasDetails = aliasDetailsResult.getOrNull()
                val isAliasDetailsLoading = aliasDetailsResult is LoadingResult.Loading
                AliasDetailUiState.Success(
                    itemUiModel = encryptionContextProvider.withEncryptionContext {
                        details.item.toUiModel(this)
                    },
                    vault = vault,
                    mailboxes = aliasDetails?.mailboxes?.toPersistentList() ?: persistentListOf(),
                    slNote = aliasDetails?.slNote.orEmpty(),
                    displayName = aliasDetails?.name.orEmpty(),
                    stats = aliasDetails?.stats.toOption(),
                    contactsCount = aliasContactsResult.getOrNull()?.total ?: 0,
                    isLoadingMap = allLoadingStates,
                    isLoadingMailboxes = isAliasDetailsLoading,
                    isItemSentToTrash = isItemSentToTrash.value(),
                    isPermanentlyDeleted = isPermanentlyDeleted.value(),
                    isRestoredFromTrash = isRestoredFromTrash.value(),
                    canPerformActions = canPerformItemActions,
                    shareClickAction = shareAction,
                    itemActions = actions,
                    event = event,
                    isHistoryFeatureEnabled = itemFeatures.isHistoryEnabled,
                    isSLAliasSyncEnabled = itemFeatures.slAliasSyncEnabled,
                    isAliasManagementEnabled = itemFeatures.isAliasManagementEnabled,
                    isAliasTrashDialogChecked = itemFeatures.isAliasTrashDialogChecked
                )
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = AliasDetailUiState.NotInitialised
    )

    internal fun onCopyAlias(alias: String) {
        clipboardManager.copyToClipboard(alias)

        viewModelScope.launch {
            snackbarDispatcher(AliasCopiedToClipboard)
        }
    }

    internal fun onMoveToTrash(shareId: ShareId, itemId: ItemId) {
        viewModelScope.launch {
            setLoadingState(LoadingStateKey.MovingToTrash, IsLoadingState.Loading)

            runCatching { trashItem(items = mapOf(shareId to listOf(itemId))) }
                .onSuccess {
                    isItemSentToTrashState.update { IsSentToTrashState.Sent }
                    snackbarDispatcher(ItemMovedToTrash)
                }
                .onFailure {
                    snackbarDispatcher(ItemNotMovedToTrash)
                    PassLogger.d(TAG, it, "Could not delete item")
                }
            setLoadingState(LoadingStateKey.MovingToTrash, IsLoadingState.NotLoading)
        }
    }

    internal fun onPermanentlyDelete(itemUiModel: ItemUiModel) {
        viewModelScope.launch {
            setLoadingState(LoadingStateKey.PermanentlyDeleting, IsLoadingState.Loading)
            runCatching {
                deleteItem(items = mapOf(itemUiModel.shareId to listOf(itemUiModel.id)))
            }.onSuccess {
                telemetryManager.sendEvent(ItemDelete(EventItemType.from(itemUiModel.contents)))
                isPermanentlyDeletedState.update { IsPermanentlyDeletedState.Deleted }
                snackbarDispatcher(DetailSnackbarMessages.ItemPermanentlyDeleted)
                PassLogger.i(TAG, "Item deleted successfully")
            }.onFailure {
                snackbarDispatcher(DetailSnackbarMessages.ItemNotPermanentlyDeleted)
                PassLogger.i(TAG, it, "Could not delete item")
            }
            setLoadingState(LoadingStateKey.PermanentlyDeleting, IsLoadingState.NotLoading)
        }
    }

    internal fun onItemRestore(shareId: ShareId, itemId: ItemId) {
        viewModelScope.launch {
            setLoadingState(LoadingStateKey.RestoringFromTrash, IsLoadingState.Loading)
            runCatching {
                restoreItem(items = mapOf(shareId to listOf(itemId)))
            }.onSuccess {
                isRestoredFromTrashState.update { IsRestoredFromTrashState.Restored }
                PassLogger.i(TAG, "Item restored successfully")
                snackbarDispatcher(ItemRestored)
            }.onFailure {
                PassLogger.i(TAG, it, "Error restoring item")
                snackbarDispatcher(ItemNotRestored)
            }
            setLoadingState(LoadingStateKey.RestoringFromTrash, IsLoadingState.NotLoading)
        }
    }

    internal fun onConsumeEvent(event: ItemDetailEvent) {
        eventState.compareAndSet(event, ItemDetailEvent.Unknown)
    }

    internal fun onMigrate() {
        viewModelScope.launch {
            bulkMoveToVaultRepository.save(mapOf(shareId to listOf(itemId)))
            eventState.update { ItemDetailEvent.MoveToVault }
        }
    }

    internal fun pinItem(shareId: ShareId, itemId: ItemId) {
        viewModelScope.launch {
            setLoadingState(LoadingStateKey.Pinning, IsLoadingState.Loading)
            runCatching { pinItem.invoke(shareId, itemId) }
                .onSuccess { snackbarDispatcher(DetailSnackbarMessages.ItemPinnedSuccess) }
                .onFailure { error ->
                    PassLogger.w(TAG, error, "An error occurred pinning Alias item")
                    snackbarDispatcher(DetailSnackbarMessages.ItemPinnedError)
                }
            setLoadingState(LoadingStateKey.Unpinning, IsLoadingState.NotLoading)
        }
    }

    internal fun unpinItem(shareId: ShareId, itemId: ItemId) {
        viewModelScope.launch {
            setLoadingState(LoadingStateKey.Unpinning, IsLoadingState.Loading)
            runCatching { unpinItem.invoke(shareId, itemId) }
                .onSuccess { snackbarDispatcher(DetailSnackbarMessages.ItemUnpinnedSuccess) }
                .onFailure { error ->
                    PassLogger.w(TAG, error, "An error occurred unpinning Alias item")
                    snackbarDispatcher(DetailSnackbarMessages.ItemUnpinnedError)
                }
            setLoadingState(LoadingStateKey.Unpinning, IsLoadingState.NotLoading)
        }
    }

    internal fun toggleAliasState(
        shareId: ShareId,
        itemId: ItemId,
        state: Boolean
    ) {
        viewModelScope.launch {
            setLoadingState(LoadingStateKey.AliasStateToggling, IsLoadingState.Loading)
            runCatching { changeAliasStatus(shareId, itemId, state) }
                .onSuccess {
                    PassLogger.i(TAG, "Alias status changed successfully")
                }
                .onFailure {
                    snackbarDispatcher(AliasChangeStatusError)
                    PassLogger.w(TAG, "Error changing alias status")
                    PassLogger.w(TAG, it)
                }
            setLoadingState(LoadingStateKey.AliasStateToggling, IsLoadingState.NotLoading)
        }
    }

    private fun setLoadingState(key: LoadingStateKey, isLoading: IsLoadingState) {
        loadingStates[key]?.update { isLoading }
    }

    private companion object {

        private const val TAG = "AliasDetailViewModel"

    }

}

enum class LoadingStateKey {
    MovingToTrash,
    PermanentlyDeleting,
    RestoringFromTrash,
    Pinning,
    Unpinning,
    AliasStateToggling
}
