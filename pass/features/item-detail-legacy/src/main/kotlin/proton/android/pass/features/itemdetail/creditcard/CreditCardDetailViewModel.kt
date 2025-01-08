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

package proton.android.pass.features.itemdetail.creditcard

import android.content.Context
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
import proton.android.pass.clipboard.api.ClipboardManager
import proton.android.pass.common.api.FlowUtils.oneShot
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.None
import proton.android.pass.common.api.asLoadingResult
import proton.android.pass.common.api.combineN
import proton.android.pass.common.api.getOrNull
import proton.android.pass.common.api.map
import proton.android.pass.commonui.api.ClassHolder
import proton.android.pass.commonui.api.FileHandler
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.StringUtils.maskCreditCardNumber
import proton.android.pass.commonui.api.require
import proton.android.pass.commonui.api.toUiModel
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.commonuimodels.api.attachments.AttachmentsState
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.composecomponents.impl.uievents.IsPermanentlyDeletedState
import proton.android.pass.composecomponents.impl.uievents.IsRestoredFromTrashState
import proton.android.pass.composecomponents.impl.uievents.IsSentToTrashState
import proton.android.pass.crypto.api.context.EncryptionContext
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.crypto.api.toEncryptedByteArray
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
import proton.android.pass.data.api.usecases.capabilities.CanShareVault
import proton.android.pass.data.api.usecases.shares.ObserveShare
import proton.android.pass.domain.HiddenState
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.Share
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.attachments.Attachment
import proton.android.pass.domain.attachments.AttachmentId
import proton.android.pass.features.itemdetail.DetailSnackbarMessages
import proton.android.pass.features.itemdetail.ItemDelete
import proton.android.pass.features.itemdetail.R
import proton.android.pass.features.itemdetail.common.CreditCardItemFeatures
import proton.android.pass.features.itemdetail.common.ItemDetailEvent
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
class CreditCardDetailViewModel @Inject constructor(
    private val snackbarDispatcher: SnackbarDispatcher,
    private val clipboardManager: ClipboardManager,
    private val encryptionContextProvider: EncryptionContextProvider,
    private val trashItem: TrashItems,
    private val deleteItem: DeleteItems,
    private val restoreItem: RestoreItems,
    private val telemetryManager: TelemetryManager,
    private val canShareVault: CanShareVault,
    private val bulkMoveToVaultRepository: BulkMoveToVaultRepository,
    private val pinItem: PinItem,
    private val unpinItem: UnpinItem,
    private val downloadAttachment: DownloadAttachment,
    private val fileHandler: FileHandler,
    featureFlagsRepository: FeatureFlagsPreferencesRepository,
    observeItemAttachments: ObserveDetailItemAttachments,
    canPerformPaidAction: CanPerformPaidAction,
    observeItemByIdWithVault: ObserveItemByIdWithVault,
    savedStateHandle: SavedStateHandleProvider,
    getItemActions: GetItemActions,
    getUserPlan: GetUserPlan,
    observeShare: ObserveShare
) : ViewModel() {

    private val shareId: ShareId =
        ShareId(savedStateHandle.get().require(CommonNavArgId.ShareId.key))
    private val itemId: ItemId =
        ItemId(savedStateHandle.get().require(CommonNavArgId.ItemId.key))

    private val isLoadingState: MutableStateFlow<IsLoadingState> =
        MutableStateFlow(IsLoadingState.NotLoading)
    private val isItemSentToTrashState: MutableStateFlow<IsSentToTrashState> =
        MutableStateFlow(IsSentToTrashState.NotSent)
    private val isPermanentlyDeletedState: MutableStateFlow<IsPermanentlyDeletedState> =
        MutableStateFlow(IsPermanentlyDeletedState.NotDeleted)
    private val isRestoredFromTrashState: MutableStateFlow<IsRestoredFromTrashState> =
        MutableStateFlow(IsRestoredFromTrashState.NotRestored)
    private val loadingAttachmentsState: MutableStateFlow<Set<AttachmentId>> =
        MutableStateFlow(emptySet())
    private val eventState: MutableStateFlow<ItemDetailEvent> =
        MutableStateFlow(ItemDetailEvent.Unknown)

    private val fieldVisibilityFlow: MutableStateFlow<FieldVisibility> =
        MutableStateFlow(FieldVisibility())

    private val canPerformPaidActionFlow = canPerformPaidAction().asLoadingResult()

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

    private data class FieldVisibility(
        val cardNumber: Boolean = false,
        val cvv: Boolean = false,
        val pin: Boolean = false
    )

    private data class CreditCardItemInfo(
        val itemUiModel: ItemUiModel,
        val cardNumberState: CardNumberState,
        val share: Share,
        val hasMoreThanOneVault: Boolean,
        val canPerformItemActions: Boolean,
        val attachments: List<Attachment>
    )

    private var hasItemBeenFetchedAtLeastOnce = false
    private val creditCardItemDetailsResultFlow = combine(
        observeItemByIdWithVault(shareId, itemId),
        observeShare(shareId),
        ::Pair
    ).flatMapLatest { (itemByIdWithVault, share) ->
        if (itemByIdWithVault.item.hasAttachments) {
            observeItemAttachments(shareId, itemId)
                .catch { error ->
                    PassLogger.w(TAG, "Error fetching attachments")
                    PassLogger.w(TAG, error)
                    throw error
                }
        } else {
            flowOf(emptyList())
        }.map { attachments ->
            Triple(itemByIdWithVault, attachments, share)
        }
    }
        .catch { if (!(hasItemBeenFetchedAtLeastOnce && it is ItemNotFoundError)) throw it }
        .onEach { hasItemBeenFetchedAtLeastOnce = true }
        .asLoadingResult()

    private val itemInfoFlow: Flow<LoadingResult<CreditCardItemInfo>> = combine(
        creditCardItemDetailsResultFlow,
        fieldVisibilityFlow
    ) { detailsResult, fieldVisibility ->
        detailsResult.map { (details, attachments, share) ->
            val (itemUiModel, cardNumber) = encryptionContextProvider.withEncryptionContext {
                val model = details.item.toUiModel(this)
                var contents = model.contents as ItemContents.CreditCard

                val cardNumber = if (fieldVisibility.cardNumber) {
                    val withSpaces = contents.number.chunked(4).joinToString(" ")
                    CardNumberState.Visible(withSpaces)
                } else {
                    CardNumberState.Masked(maskCreditCardNumber(contents.number))
                }

                contents = contents.copy(
                    expirationDate = adaptExpirationDate(contents.expirationDate),
                    cvv = fieldHiddenStateValue(contents.cvv, fieldVisibility.cvv, this),
                    pin = fieldHiddenStateValue(contents.pin, fieldVisibility.pin, this)
                )

                model.copy(contents = contents) to cardNumber
            }

            CreditCardItemInfo(
                itemUiModel = itemUiModel,
                share = share,
                cardNumberState = cardNumber,
                hasMoreThanOneVault = details.hasMoreThanOneVault,
                canPerformItemActions = details.canPerformItemActions,
                attachments = attachments
            )
        }

    }.distinctUntilChanged()

    private val itemFeaturesFlow: Flow<CreditCardItemFeatures> = combine(
        getUserPlan(),
        featureFlagsRepository.get<Boolean>(FeatureFlag.FILE_ATTACHMENTS_V1),
        featureFlagsRepository.get<Boolean>(FeatureFlag.ITEM_SHARING_V1)
    ) { userPlan, isFileAttachmentsEnabled, isItemSharingEnabled ->
        CreditCardItemFeatures(
            isHistoryEnabled = userPlan.isPaidPlan,
            isFileAttachmentsEnabled = isFileAttachmentsEnabled,
            isItemSharingEnabled = isItemSharingEnabled
        )
    }

    private val loadingStates = combine(
        isLoadingState,
        loadingAttachmentsState,
        ::Pair
    )

    internal val uiState: StateFlow<CreditCardDetailUiState> = combineN(
        itemInfoFlow,
        loadingStates,
        isItemSentToTrashState,
        isPermanentlyDeletedState,
        isRestoredFromTrashState,
        canPerformPaidActionFlow,
        shareActionFlow,
        oneShot { getItemActions(shareId = shareId, itemId = itemId) }.asLoadingResult(),
        eventState,
        itemFeaturesFlow
    ) { itemDetails,
        (isLoading, loadingAttachments),
        isItemSentToTrash,
        isPermanentlyDeleted,
        isRestoredFromTrash,
        canPerformPaidActionResult,
        shareAction,
        itemActions,
        event,
        itemFeatures ->
        when (itemDetails) {
            is LoadingResult.Error -> {
                if (!isPermanentlyDeleted.value()) {
                    snackbarDispatcher(DetailSnackbarMessages.InitError)
                    CreditCardDetailUiState.Error
                } else {
                    CreditCardDetailUiState.Pending
                }
            }

            LoadingResult.Loading -> CreditCardDetailUiState.NotInitialised
            is LoadingResult.Success -> {
                val details = itemDetails.data
                val isPaid = canPerformPaidActionResult.getOrNull() == true
                val actions = itemActions.getOrNull() ?: ItemActions.Disabled

                CreditCardDetailUiState.Success(
                    itemContent = CreditCardDetailUiState.ItemContent(
                        model = details.itemUiModel,
                        cardNumber = details.cardNumberState
                    ),
                    share = details.share,
                    isLoading = isLoading.value(),
                    isItemSentToTrash = isItemSentToTrash.value(),
                    isPermanentlyDeleted = isPermanentlyDeleted.value(),
                    isRestoredFromTrash = isRestoredFromTrash.value(),
                    isDowngradedMode = !isPaid,
                    canPerformActions = details.canPerformItemActions,
                    shareClickAction = shareAction,
                    itemActions = actions,
                    event = event,
                    itemFeatures = itemFeatures,
                    attachmentsState = AttachmentsState(
                        draftAttachmentsList = emptyList(), // no drafts in detail
                        attachmentsList = details.attachments,
                        loadingAttachments = loadingAttachments,
                        needsUpgrade = None
                    ),
                    hasMoreThanOneVault = details.hasMoreThanOneVault
                )
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = CreditCardDetailUiState.NotInitialised
    )

    fun onMoveToTrash(shareId: ShareId, itemId: ItemId) = viewModelScope.launch {
        isLoadingState.update { IsLoadingState.Loading }
        runCatching { trashItem(items = mapOf(shareId to listOf(itemId))) }
            .onSuccess {
                isItemSentToTrashState.update { IsSentToTrashState.Sent }
                snackbarDispatcher(DetailSnackbarMessages.ItemMovedToTrash)
            }
            .onFailure {
                snackbarDispatcher(DetailSnackbarMessages.ItemNotMovedToTrash)
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
            snackbarDispatcher(DetailSnackbarMessages.ItemPermanentlyDeleted)
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
            snackbarDispatcher(DetailSnackbarMessages.ItemRestored)
        }.onFailure {
            PassLogger.i(TAG, it, "Error restoring item")
            snackbarDispatcher(DetailSnackbarMessages.ItemNotRestored)
        }
        isLoadingState.update { IsLoadingState.NotLoading }
    }

    fun copyCardHolderName() = viewModelScope.launch {
        modelFromState()?.let {
            clipboardManager.copyToClipboard(it.cardHolder)
            snackbarDispatcher(DetailSnackbarMessages.CardHolderCopiedToClipboard)
        }
    }

    fun copyCvv() = viewModelScope.launch {
        modelFromState()?.let {
            val cvv = when (val content = it.cvv) {
                is HiddenState.Empty -> return@let
                is HiddenState.Concealed -> {
                    encryptionContextProvider.withEncryptionContext {
                        decrypt(content.encrypted)
                    }
                }

                is HiddenState.Revealed -> content.clearText
            }
            clipboardManager.copyToClipboard(cvv, isSecure = true)
            snackbarDispatcher(DetailSnackbarMessages.CardVerificationNumberCopiedToClipboard)
        }
    }

    fun copyNumber() = viewModelScope.launch {
        modelFromState()?.let {
            clipboardManager.copyToClipboard(it.number)
            snackbarDispatcher(DetailSnackbarMessages.CardNumberCopiedToClipboard)
        }
    }

    fun toggleCvv() = viewModelScope.launch {
        fieldVisibilityFlow.update { it.copy(cvv = !it.cvv) }
    }

    fun toggleNumber() = viewModelScope.launch {
        fieldVisibilityFlow.update { it.copy(cardNumber = !it.cardNumber) }
    }

    fun togglePin() = viewModelScope.launch {
        fieldVisibilityFlow.update { it.copy(pin = !it.pin) }
    }

    fun clearEvent() = viewModelScope.launch {
        eventState.update { ItemDetailEvent.Unknown }
    }

    fun onMigrate() = viewModelScope.launch {
        bulkMoveToVaultRepository.save(mapOf(shareId to listOf(itemId)))
        eventState.update { ItemDetailEvent.MoveToVault }
    }

    internal fun pinItem(shareId: ShareId, itemId: ItemId) = viewModelScope.launch {
        isLoadingState.update { IsLoadingState.Loading }

        runCatching { pinItem.invoke(shareId, itemId) }
            .onSuccess { snackbarDispatcher(DetailSnackbarMessages.ItemPinnedSuccess) }
            .onFailure { error ->
                PassLogger.w(TAG, error, "An error occurred pinning Credit Card item")
                snackbarDispatcher(DetailSnackbarMessages.ItemPinnedError)
            }

        isLoadingState.update { IsLoadingState.NotLoading }
    }

    internal fun unpinItem(shareId: ShareId, itemId: ItemId) = viewModelScope.launch {
        isLoadingState.update { IsLoadingState.Loading }

        runCatching { unpinItem.invoke(shareId, itemId) }
            .onSuccess { snackbarDispatcher(DetailSnackbarMessages.ItemUnpinnedSuccess) }
            .onFailure { error ->
                PassLogger.w(TAG, error, "An error occurred unpinning Credit Card item")
                snackbarDispatcher(DetailSnackbarMessages.ItemUnpinnedError)
            }

        isLoadingState.update { IsLoadingState.NotLoading }
    }


    private fun modelFromState(): ItemContents.CreditCard? {
        val state = uiState.value
        return if (state is CreditCardDetailUiState.Success) {
            state.itemContent.model.contents as? ItemContents.CreditCard
        } else {
            null
        }
    }

    private fun fieldHiddenStateValue(
        state: HiddenState,
        isVisible: Boolean,
        context: EncryptionContext
    ): HiddenState = if (isVisible) {
        HiddenState.Revealed(
            encrypted = state.encrypted,
            clearText = context.decrypt(state.encrypted)
        )
    } else {
        encryptedOrEmpty(state, context)
    }


    private fun encryptedOrEmpty(state: HiddenState, context: EncryptionContext): HiddenState {
        val isEmpty = context.decrypt(state.encrypted.toEncryptedByteArray()).isEmpty()
        return if (isEmpty) {
            HiddenState.Empty(context.encrypt(""))
        } else {
            state
        }
    }

    private fun adaptExpirationDate(input: String): String {
        val parts = input.split("-")
        return if (parts.size == 2) {
            val year = parts[0].substring(2)
            val month = parts[1].padStart(2, '0')
            "$month / $year"
        } else {
            input
        }
    }

    fun onAttachmentOpen(contextHolder: ClassHolder<Context>, attachment: Attachment) {
        viewModelScope.launch {
            loadingAttachmentsState.update { it + attachment.id }
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
        private const val TAG = "CreditCardDetailViewModel"
    }

}
