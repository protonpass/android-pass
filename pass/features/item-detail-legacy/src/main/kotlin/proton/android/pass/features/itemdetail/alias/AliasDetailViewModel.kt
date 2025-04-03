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

package proton.android.pass.features.itemdetail.alias

import android.content.Context
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
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
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
import proton.android.pass.commonpresentation.api.attachments.AttachmentsHandler
import proton.android.pass.commonui.api.ClassHolder
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
import proton.android.pass.data.api.usecases.GetUserPlan
import proton.android.pass.data.api.usecases.ItemActions
import proton.android.pass.data.api.usecases.ObserveAliasDetails
import proton.android.pass.data.api.usecases.ObserveItemByIdWithVault
import proton.android.pass.data.api.usecases.PinItem
import proton.android.pass.data.api.usecases.RestoreItems
import proton.android.pass.data.api.usecases.TrashItems
import proton.android.pass.data.api.usecases.UnpinItem
import proton.android.pass.data.api.usecases.aliascontact.ObserveAliasContacts
import proton.android.pass.data.api.usecases.attachments.ObserveDetailItemAttachments
import proton.android.pass.data.api.usecases.capabilities.CanShareShare
import proton.android.pass.data.api.usecases.shares.ObserveShare
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.attachments.Attachment
import proton.android.pass.features.itemdetail.DetailSnackbarMessages
import proton.android.pass.features.itemdetail.DetailSnackbarMessages.AliasChangeStatusError
import proton.android.pass.features.itemdetail.DetailSnackbarMessages.AliasCopiedToClipboard
import proton.android.pass.features.itemdetail.DetailSnackbarMessages.InitError
import proton.android.pass.features.itemdetail.DetailSnackbarMessages.ItemMovedToTrash
import proton.android.pass.features.itemdetail.DetailSnackbarMessages.ItemNotMovedToTrash
import proton.android.pass.features.itemdetail.DetailSnackbarMessages.ItemNotRestored
import proton.android.pass.features.itemdetail.DetailSnackbarMessages.ItemRestored
import proton.android.pass.features.itemdetail.ItemDelete
import proton.android.pass.features.itemdetail.common.AliasItemFeatures
import proton.android.pass.features.itemdetail.common.ItemDetailEvent
import proton.android.pass.features.itemdetail.common.ShareClickAction
import proton.android.pass.log.api.PassLogger
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.android.pass.preferences.FeatureFlag
import proton.android.pass.preferences.FeatureFlagsPreferencesRepository
import proton.android.pass.preferences.UserPreferencesRepository
import proton.android.pass.preferences.featurediscovery.FeatureDiscoveryBannerPreference
import proton.android.pass.preferences.featurediscovery.FeatureDiscoveryFeature.AliasManagementContacts
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
    private val canShareShare: CanShareShare,
    private val bulkMoveToVaultRepository: BulkMoveToVaultRepository,
    private val pinItem: PinItem,
    private val unpinItem: UnpinItem,
    private val changeAliasStatus: ChangeAliasStatus,
    private val attachmentsHandler: AttachmentsHandler,
    private val userPreferencesRepository: UserPreferencesRepository,
    observeAliasContacts: ObserveAliasContacts,
    observeItemAttachments: ObserveDetailItemAttachments,
    canPerformPaidAction: CanPerformPaidAction,
    observeItemByIdWithVault: ObserveItemByIdWithVault,
    observeAliasDetails: ObserveAliasDetails,
    savedStateHandle: SavedStateHandle,
    getItemActions: GetItemActions,
    featureFlagsRepository: FeatureFlagsPreferencesRepository,
    getUserPlan: GetUserPlan,
    observeShare: ObserveShare
) : ViewModel() {

    private val shareId: ShareId = savedStateHandle
        .require<String>(CommonNavArgId.ShareId.key)
        .let(::ShareId)

    private val itemId: ItemId = savedStateHandle
        .require<String>(CommonNavArgId.ItemId.key)
        .let(::ItemId)

    private val allLoadingStates =
        MutableStateFlow<Map<LoadingStateKey, IsLoadingState>>(emptyMap())
    private val isItemSentToTrashState: MutableStateFlow<IsSentToTrashState> =
        MutableStateFlow(IsSentToTrashState.NotSent)
    private val isPermanentlyDeletedState: MutableStateFlow<IsPermanentlyDeletedState> =
        MutableStateFlow(IsPermanentlyDeletedState.NotDeleted)
    private val isRestoredFromTrashState: MutableStateFlow<IsRestoredFromTrashState> =
        MutableStateFlow(IsRestoredFromTrashState.NotRestored)
    private val actionsFlow: Flow<Triple<IsSentToTrashState, IsPermanentlyDeletedState, IsRestoredFromTrashState>> =
        combine(
            isItemSentToTrashState,
            isPermanentlyDeletedState,
            isRestoredFromTrashState
        ) { sent, deleted, restored ->
            Triple(sent, deleted, restored)
        }
    private val eventState: MutableStateFlow<ItemDetailEvent> =
        MutableStateFlow(ItemDetailEvent.Unknown)

    private val canPerformPaidActionFlow: Flow<LoadingResult<Boolean>> =
        canPerformPaidAction().asLoadingResult()

    private val shareActionFlow: Flow<ShareClickAction> = canPerformPaidActionFlow
        .map { isPaidResult ->
            val isPaid = isPaidResult.getOrNull() ?: false
            val canShareVault = canShareShare(shareId).value
            when {
                isPaid && canShareVault -> ShareClickAction.Share
                else -> ShareClickAction.Upgrade
            }
        }
        .distinctUntilChanged()

    private var hasItemBeenFetchedAtLeastOnce = false

    private val aliasItemDetailsResultFlow = combine(
        observeItemByIdWithVault(shareId, itemId),
        observeShare(shareId),
        ::Pair
    )
        .flatMapLatest { (itemWithVault, share) ->
            val attachmentsFlow = if (itemWithVault.item.hasAttachments) {
                observeItemAttachments(shareId, itemId)
                    .catch { error ->
                        PassLogger.w(TAG, "Error fetching attachments: ${error.message}")
                        emit(emptyList())
                    }
            } else {
                flowOf(emptyList())
            }
            attachmentsFlow.map { attachments ->
                Triple(itemWithVault, attachments, share)
            }
        }
        .catch { if (!(hasItemBeenFetchedAtLeastOnce && it is ItemNotFoundError)) throw it }
        .onEach { hasItemBeenFetchedAtLeastOnce = true }
        .asLoadingResult()

    private val itemFeaturesFlow: Flow<AliasItemFeatures> = combine(
        getUserPlan().map { it.isPaidPlan },
        featureFlagsRepository.get<Boolean>(FeatureFlag.FILE_ATTACHMENTS_V1),
        userPreferencesRepository.observeAliasTrashDialogStatusPreference().map { it.value }
    ) { isHistoryEnabled, isFileAttachmentsEnabled, isAliasTrashDialogChecked ->
        AliasItemFeatures(
            isHistoryEnabled = isHistoryEnabled,
            isFileAttachmentsEnabled = isFileAttachmentsEnabled,
            isAliasTrashDialogChecked = isAliasTrashDialogChecked
        )
    }

    private val aliasDetailsAndContactsFlow = combine(
        observeAliasDetails(shareId, itemId).asLoadingResult(),
        observeAliasContacts(shareId, itemId).asLoadingResult(),
        ::Pair
    )

    internal val uiState: StateFlow<AliasDetailUiState> = combineN(
        aliasItemDetailsResultFlow,
        aliasDetailsAndContactsFlow,
        allLoadingStates,
        actionsFlow,
        shareActionFlow,
        oneShot { getItemActions(shareId = shareId, itemId = itemId) }.asLoadingResult(),
        eventState,
        itemFeaturesFlow,
        userPreferencesRepository.observeDisplayFeatureDiscoverBanner(AliasManagementContacts),
        attachmentsHandler.attachmentState
    ) { itemLoadingResult,
        (aliasDetailsResult, aliasContactsResult),
        allLoadingStates,
        (isItemSentToTrash, isPermanentlyDeleted, isRestoredFromTrash),
        shareAction,
        itemActions,
        event,
        itemFeatures,
        displayContactsBanner,
        attachmentState ->
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
                val (details, attachments, share) = itemLoadingResult.data
                val actions = itemActions.getOrNull() ?: ItemActions.Disabled
                val aliasDetails = aliasDetailsResult.getOrNull()
                val isAliasDetailsLoading = aliasDetailsResult is LoadingResult.Loading
                AliasDetailUiState.Success(
                    itemUiModel = encryptionContextProvider.withEncryptionContext {
                        details.item.toUiModel(this)
                    },
                    share = share,
                    mailboxes = aliasDetails?.mailboxes?.toPersistentList() ?: persistentListOf(),
                    isAliasCreatedByUser = aliasDetails?.canModify ?: false,
                    slNote = aliasDetails?.slNote.orEmpty(),
                    displayName = aliasDetails?.name.orEmpty(),
                    stats = aliasDetails?.stats.toOption(),
                    contactsCount = aliasContactsResult.getOrNull()?.total ?: 0,
                    isLoadingMap = allLoadingStates,
                    isLoadingMailboxes = isAliasDetailsLoading,
                    isItemSentToTrash = isItemSentToTrash.value(),
                    isPermanentlyDeleted = isPermanentlyDeleted.value(),
                    isRestoredFromTrash = isRestoredFromTrash.value(),
                    canPerformActions = details.canPerformItemActions,
                    shareClickAction = shareAction,
                    itemActions = actions,
                    event = event,
                    itemFeatures = itemFeatures,
                    attachmentsState = attachmentState.copy(attachmentsList = attachments),
                    hasMoreThanOneVault = details.hasMoreThanOneVault,
                    displayContactsBanner = displayContactsBanner.value
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
            setLoadingState(LoadingStateKey.Pinning, IsLoadingState.NotLoading)
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
        allLoadingStates.update { it + (key to isLoading) }
    }

    fun onAttachmentOpen(contextHolder: ClassHolder<Context>, attachment: Attachment) {
        viewModelScope.launch {
            attachmentsHandler.openAttachment(contextHolder, attachment)
        }
    }

    fun dismissContactsBanner() {
        viewModelScope.launch {
            userPreferencesRepository.setDisplayFeatureDiscoverBanner(
                AliasManagementContacts,
                FeatureDiscoveryBannerPreference.NotDisplay
            )
        }
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
