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

package proton.android.pass.features.itemdetail.note

import android.content.Context
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
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import proton.android.pass.biometry.AuthOverrideState
import proton.android.pass.clipboard.api.ClipboardManager
import proton.android.pass.common.api.FlowUtils.oneShot
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.None
import proton.android.pass.common.api.asLoadingResult
import proton.android.pass.common.api.combineN
import proton.android.pass.common.api.getOrNull
import proton.android.pass.commonui.api.ClassHolder
import proton.android.pass.commonui.api.FileHandler
import proton.android.pass.commonui.api.require
import proton.android.pass.commonui.api.toUiModel
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.commonuimodels.api.attachments.AttachmentsState
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.composecomponents.impl.uievents.IsPermanentlyDeletedState
import proton.android.pass.composecomponents.impl.uievents.IsRestoredFromTrashState
import proton.android.pass.composecomponents.impl.uievents.IsSentToTrashState
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.errors.ItemNotFoundError
import proton.android.pass.data.api.repositories.BulkMoveToVaultRepository
import proton.android.pass.data.api.usecases.CanPerformPaidAction
import proton.android.pass.data.api.usecases.DeleteItems
import proton.android.pass.data.api.usecases.GetItemActions
import proton.android.pass.data.api.usecases.GetUserPlan
import proton.android.pass.data.api.usecases.ItemActions
import proton.android.pass.data.api.usecases.ObserveItemByIdWithVault
import proton.android.pass.data.api.usecases.PinItem
import proton.android.pass.data.api.usecases.RestoreItems
import proton.android.pass.data.api.usecases.TrashItems
import proton.android.pass.data.api.usecases.UnpinItem
import proton.android.pass.data.api.usecases.attachments.DownloadAttachment
import proton.android.pass.data.api.usecases.attachments.ObserveDetailItemAttachments
import proton.android.pass.data.api.usecases.capabilities.CanShareShare
import proton.android.pass.data.api.usecases.shares.ObserveShare
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.attachments.Attachment
import proton.android.pass.domain.attachments.AttachmentId
import proton.android.pass.features.itemdetail.DetailSnackbarMessages
import proton.android.pass.features.itemdetail.DetailSnackbarMessages.InitError
import proton.android.pass.features.itemdetail.DetailSnackbarMessages.ItemMovedToTrash
import proton.android.pass.features.itemdetail.DetailSnackbarMessages.ItemNotMovedToTrash
import proton.android.pass.features.itemdetail.DetailSnackbarMessages.ItemNotRestored
import proton.android.pass.features.itemdetail.DetailSnackbarMessages.ItemPermanentlyDeleted
import proton.android.pass.features.itemdetail.DetailSnackbarMessages.ItemRestored
import proton.android.pass.features.itemdetail.DetailSnackbarMessages.NoteCopiedToClipboard
import proton.android.pass.features.itemdetail.ItemDelete
import proton.android.pass.features.itemdetail.R
import proton.android.pass.features.itemdetail.common.ItemDetailEvent
import proton.android.pass.features.itemdetail.common.NoteItemFeatures
import proton.android.pass.features.itemdetail.common.ShareClickAction
import proton.android.pass.log.api.PassLogger
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.android.pass.preferences.FeatureFlag
import proton.android.pass.preferences.FeatureFlagsPreferencesRepository
import proton.android.pass.telemetry.api.EventItemType
import proton.android.pass.telemetry.api.TelemetryManager
import javax.inject.Inject

@[HiltViewModel Suppress("LongParameterList")]
class NoteDetailViewModel @Inject constructor(
    private val snackbarDispatcher: SnackbarDispatcher,
    private val encryptionContextProvider: EncryptionContextProvider,
    private val trashItem: TrashItems,
    private val deleteItem: DeleteItems,
    private val restoreItem: RestoreItems,
    private val telemetryManager: TelemetryManager,
    private val clipboardManager: ClipboardManager,
    private val canShareShare: CanShareShare,
    private val bulkMoveToVaultRepository: BulkMoveToVaultRepository,
    private val pinItem: PinItem,
    private val unpinItem: UnpinItem,
    private val downloadAttachment: DownloadAttachment,
    private val fileHandler: FileHandler,
    private val authOverrideState: AuthOverrideState,
    featureFlagsRepository: FeatureFlagsPreferencesRepository,
    canPerformPaidAction: CanPerformPaidAction,
    observeItemByIdWithVault: ObserveItemByIdWithVault,
    observeItemAttachments: ObserveDetailItemAttachments,
    savedStateHandle: SavedStateHandle,
    getItemActions: GetItemActions,
    getUserPlan: GetUserPlan,
    observeShare: ObserveShare
) : ViewModel() {

    private val shareId: ShareId = ShareId(savedStateHandle.require(CommonNavArgId.ShareId.key))
    private val itemId: ItemId = ItemId(savedStateHandle.require(CommonNavArgId.ItemId.key))

    private val isLoadingState: MutableStateFlow<IsLoadingState> =
        MutableStateFlow(IsLoadingState.NotLoading)
    private val isItemSentToTrashState: MutableStateFlow<IsSentToTrashState> =
        MutableStateFlow(IsSentToTrashState.NotSent)
    private val isPermanentlyDeletedState: MutableStateFlow<IsPermanentlyDeletedState> =
        MutableStateFlow(IsPermanentlyDeletedState.NotDeleted)
    private val isRestoredFromTrashState: MutableStateFlow<IsRestoredFromTrashState> =
        MutableStateFlow(IsRestoredFromTrashState.NotRestored)
    private val eventState: MutableStateFlow<ItemDetailEvent> =
        MutableStateFlow(ItemDetailEvent.Unknown)
    private val loadingAttachmentsState: MutableStateFlow<Set<AttachmentId>> =
        MutableStateFlow(emptySet())

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

    private val noteItemDetailsResultFlow = combine(
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

    private val itemFeaturesFlow: Flow<NoteItemFeatures> = combine(
        getUserPlan(),
        featureFlagsRepository.get<Boolean>(FeatureFlag.FILE_ATTACHMENTS_V1),
        featureFlagsRepository.get<Boolean>(FeatureFlag.ITEM_SHARING_V1)
    ) { userPlan, isFileAttachmentsEnabled, isItemSharingEnabled ->
        NoteItemFeatures(
            isHistoryEnabled = userPlan.isPaidPlan,
            isFileAttachmentsEnabled = isFileAttachmentsEnabled,
            isItemSharingEnabled = isItemSharingEnabled
        )
    }

    private val isSharedWithMeItemFlow = MutableStateFlow(false)

    internal val state: StateFlow<NoteDetailUiState> = combineN(
        noteItemDetailsResultFlow,
        isLoadingState,
        loadingAttachmentsState,
        isItemSentToTrashState,
        isPermanentlyDeletedState,
        isRestoredFromTrashState,
        shareActionFlow,
        oneShot { getItemActions(shareId = shareId, itemId = itemId) }.asLoadingResult(),
        eventState,
        itemFeaturesFlow
    ) { itemLoadingResult,
        isLoading,
        loadingAttachments,
        isItemSentToTrash,
        isPermanentlyDeleted,
        isRestoredFromTrash,
        shareAction,
        itemActions,
        event,
        itemFeatures ->
        when (itemLoadingResult) {
            is LoadingResult.Error -> {
                when {
                    isSharedWithMeItemFlow.value -> {
                        NoteDetailUiState.NotInitialised
                    }

                    !isPermanentlyDeleted.value() -> {
                        snackbarDispatcher(InitError)
                        NoteDetailUiState.Error
                    }

                    else -> {
                        NoteDetailUiState.Pending
                    }
                }
            }

            LoadingResult.Loading -> NoteDetailUiState.NotInitialised
            is LoadingResult.Success -> {
                val (details, attachments, share) = itemLoadingResult.data
                val actions = itemActions.getOrNull() ?: ItemActions.Disabled

                isSharedWithMeItemFlow.update { !details.item.isOwner && details.item.isShared }

                NoteDetailUiState.Success(
                    itemUiModel = encryptionContextProvider.withEncryptionContext {
                        details.item.toUiModel(this)
                    },
                    share = share,
                    isLoading = isLoading.value(),
                    isItemSentToTrash = isItemSentToTrash.value(),
                    isPermanentlyDeleted = isPermanentlyDeleted.value(),
                    isRestoredFromTrash = isRestoredFromTrash.value(),
                    canPerformActions = details.canPerformItemActions,
                    shareClickAction = shareAction,
                    attachmentsState = AttachmentsState(
                        draftAttachmentsList = emptyList(), // no drafts in detail
                        attachmentsList = attachments,
                        loadingAttachments = loadingAttachments,
                        needsUpgrade = None
                    ),
                    itemActions = actions,
                    event = event,
                    itemFeatures = itemFeatures,
                    hasMoreThanOneVault = details.hasMoreThanOneVault
                )
            }
        }
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = NoteDetailUiState.NotInitialised
        )

    fun onMoveToTrash(shareId: ShareId, itemId: ItemId) = viewModelScope.launch {
        isLoadingState.update { IsLoadingState.Loading }
        runCatching { trashItem(items = mapOf(shareId to listOf(itemId))) }
            .onSuccess {
                isItemSentToTrashState.update { IsSentToTrashState.Sent }
                snackbarDispatcher(ItemMovedToTrash)
            }
            .onFailure {
                snackbarDispatcher(ItemNotMovedToTrash)
                PassLogger.d(TAG, it, "Could not delete item")
            }
        isLoadingState.update { IsLoadingState.NotLoading }
    }

    fun onPermanentlyDelete(itemUiModel: ItemUiModel) = viewModelScope.launch {
        isLoadingState.update { IsLoadingState.Loading }
        runCatching {
            deleteItem(items = mapOf(itemUiModel.shareId to listOf(itemUiModel.id)))
        }.onSuccess {
            telemetryManager.sendEvent(ItemDelete(EventItemType.from(itemUiModel.contents)))
            isPermanentlyDeletedState.update { IsPermanentlyDeletedState.Deleted }
            snackbarDispatcher(ItemPermanentlyDeleted)
            PassLogger.i(TAG, "Item deleted successfully")
        }.onFailure {
            snackbarDispatcher(DetailSnackbarMessages.ItemNotPermanentlyDeleted)
            PassLogger.i(TAG, it, "Could not delete item")
        }
        isLoadingState.update { IsLoadingState.NotLoading }
    }

    fun onItemRestore(shareId: ShareId, itemId: ItemId) = viewModelScope.launch {
        isLoadingState.update { IsLoadingState.Loading }
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
        isLoadingState.update { IsLoadingState.NotLoading }
    }

    fun onCopyToClipboard(itemUiModel: ItemUiModel) = viewModelScope.launch {
        val contents = itemUiModel.contents as? ItemContents.Note ?: return@launch
        clipboardManager.copyToClipboard(contents.note, isSecure = false)
        snackbarDispatcher(NoteCopiedToClipboard)
    }

    internal fun clearEvent() {
        eventState.update { ItemDetailEvent.Unknown }
    }

    internal fun onMigrate() {
        val state = state.value as? NoteDetailUiState.Success ?: return

        viewModelScope.launch {
            bulkMoveToVaultRepository.save(mapOf(shareId to listOf(itemId)))

            if (state.itemUiModel.isShared) {
                ItemDetailEvent.MoveToVaultSharedWarning
            } else {
                ItemDetailEvent.MoveToVault
            }.also { event ->
                eventState.update { event }
            }
        }
    }

    internal fun pinItem(shareId: ShareId, itemId: ItemId) = viewModelScope.launch {
        isLoadingState.update { IsLoadingState.Loading }

        runCatching { pinItem.invoke(shareId, itemId) }
            .onSuccess { snackbarDispatcher(DetailSnackbarMessages.ItemPinnedSuccess) }
            .onFailure { error ->
                PassLogger.w(TAG, error, "An error occurred pinning Note item")
                snackbarDispatcher(DetailSnackbarMessages.ItemPinnedError)
            }

        isLoadingState.update { IsLoadingState.NotLoading }
    }

    internal fun unpinItem(shareId: ShareId, itemId: ItemId) = viewModelScope.launch {
        isLoadingState.update { IsLoadingState.Loading }

        runCatching { unpinItem.invoke(shareId, itemId) }
            .onSuccess { snackbarDispatcher(DetailSnackbarMessages.ItemUnpinnedSuccess) }
            .onFailure { error ->
                PassLogger.w(TAG, error, "An error occurred unpinning Note item")
                snackbarDispatcher(DetailSnackbarMessages.ItemUnpinnedError)
            }

        isLoadingState.update { IsLoadingState.NotLoading }
    }

    fun onAttachmentOpen(contextHolder: ClassHolder<Context>, attachment: Attachment) {
        viewModelScope.launch {
            loadingAttachmentsState.update { it + attachment.id }
            authOverrideState.setAuthOverride(true)
            runCatching {
                val uri = downloadAttachment(attachment)
                fileHandler.openFile(
                    contextHolder = contextHolder,
                    uri = uri,
                    mimeType = attachment.mimeType,
                    chooserTitle = contextHolder.get().value()?.getString(R.string.open_with) ?: ""
                )
            }.onSuccess {
                PassLogger.i(TAG, "Attachment opened: ${attachment.id}")
            }.onFailure {
                PassLogger.w(TAG, "Could not open attachment: ${attachment.id}")
                PassLogger.w(TAG, it)
                snackbarDispatcher(DetailSnackbarMessages.OpenAttachmentsError)
            }
            loadingAttachmentsState.update { it - attachment.id }
        }
    }

    companion object {
        private const val TAG = "NoteDetailViewModel"
    }

}
