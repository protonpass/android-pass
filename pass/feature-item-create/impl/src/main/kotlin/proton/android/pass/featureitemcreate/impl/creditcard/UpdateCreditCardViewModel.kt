package proton.android.pass.featureitemcreate.impl.creditcard

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.proton.core.accountmanager.domain.AccountManager
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.some
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.require
import proton.android.pass.commonui.api.toItemContents
import proton.android.pass.commonui.api.toUiModel
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.errors.InvalidContentFormatVersionError
import proton.android.pass.data.api.repositories.DraftAttachmentRepository
import proton.android.pass.data.api.repositories.MetadataResolver
import proton.android.pass.data.api.usecases.CanPerformPaidAction
import proton.android.pass.data.api.usecases.ObserveItemById
import proton.android.pass.data.api.usecases.UpdateItem
import proton.android.pass.data.api.usecases.attachments.ClearAttachments
import proton.android.pass.data.api.usecases.attachments.UploadAttachment
import proton.android.pass.domain.Item
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.featureitemcreate.impl.ItemSavedState
import proton.android.pass.featureitemcreate.impl.ItemUpdate
import proton.android.pass.featureitemcreate.impl.creditcard.CreditCardSnackbarMessage.InitError
import proton.android.pass.featureitemcreate.impl.creditcard.CreditCardSnackbarMessage.ItemUpdateError
import proton.android.pass.featureitemcreate.impl.creditcard.CreditCardSnackbarMessage.UpdateAppToUpdateItemError
import proton.android.pass.log.api.PassLogger
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.android.pass.preferences.FeatureFlagsPreferencesRepository
import proton.android.pass.telemetry.api.EventItemType
import proton.android.pass.telemetry.api.TelemetryManager
import javax.inject.Inject

@HiltViewModel
class UpdateCreditCardViewModel @Inject constructor(
    private val getItemById: ObserveItemById,
    private val updateItem: UpdateItem,
    private val encryptionContextProvider: EncryptionContextProvider,
    private val snackbarDispatcher: SnackbarDispatcher,
    private val accountManager: AccountManager,
    private val telemetryManager: TelemetryManager,
    clearAttachments: ClearAttachments,
    uploadAttachment: UploadAttachment,
    draftAttachmentRepository: DraftAttachmentRepository,
    metadataResolver: MetadataResolver,
    canPerformPaidAction: CanPerformPaidAction,
    featureFlagsRepository: FeatureFlagsPreferencesRepository,
    savedStateHandleProvider: SavedStateHandleProvider
) : BaseCreditCardViewModel(
    clearAttachments = clearAttachments,
    uploadAttachment = uploadAttachment,
    draftAttachmentRepository = draftAttachmentRepository,
    metadataResolver = metadataResolver,
    encryptionContextProvider = encryptionContextProvider,
    canPerformPaidAction = canPerformPaidAction,
    featureFlagsRepository = featureFlagsRepository,
    savedStateHandleProvider = savedStateHandleProvider
) {
    private val navShareId: ShareId =
        ShareId(savedStateHandleProvider.get().require(CommonNavArgId.ShareId.key))
    private val navItemId: ItemId = ItemId(
        savedStateHandleProvider.get().require(CommonNavArgId.ItemId.key)
    )
    private var itemOption: Option<Item> = None

    init {
        viewModelScope.launch {
            setupInitialState()
        }
    }

    val state: StateFlow<UpdateCreditCardUiState> = combine(
        flowOf(navShareId),
        baseState,
        UpdateCreditCardUiState::Success
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = UpdateCreditCardUiState.NotInitialised
    )

    private suspend fun setupInitialState() {
        isLoadingState.update { IsLoadingState.Loading }
        runCatching { getItemById(navShareId, navItemId).first() }
            .onSuccess(::onCreditCardItemReceived)
            .onFailure {
                PassLogger.w(TAG, "Error getting item by id")
                PassLogger.w(TAG, it)
                snackbarDispatcher(InitError)
            }
        isLoadingState.update { IsLoadingState.NotLoading }
    }

    private fun onCreditCardItemReceived(item: Item) {
        itemOption = item.some()
        encryptionContextProvider.withEncryptionContext {
            val default = CreditCardItemFormState.default(this)
            if (creditCardItemFormState.compare(default, this)) {
                val itemContents = toItemContents(
                    itemType = item.itemType,
                    encryptionContext = this,
                    title = item.title,
                    note = item.note,
                    flags = item.flags
                )as ItemContents.CreditCard
                val expirationDate =
                    ExpirationDateProtoMapper.fromProto(itemContents.expirationDate)
                creditCardItemFormMutableState =
                    CreditCardItemFormState(itemContents.copy(expirationDate = expirationDate))
            }
        }
    }

    fun update() = viewModelScope.launch {
        val canUpdate = validateItem()
        if (!canUpdate) {
            PassLogger.i(TAG, "Cannot update credit card")
            return@launch
        }
        isLoadingState.update { IsLoadingState.Loading }
        runCatching {
            val userId = accountManager.getPrimaryUserId().first()
                ?: throw IllegalStateException("User id is null")
            val item = itemOption.value() ?: throw IllegalStateException("Item is null")
            val sanitisedItemFormState = creditCardItemFormState.sanitise()
            updateItem(
                userId = userId,
                shareId = navShareId,
                item = item,
                contents = sanitisedItemFormState.toItemContents()
            )
        }.onSuccess { item ->
            PassLogger.i(TAG, "Credit card successfully updated")
            isItemSavedState.update {
                ItemSavedState.Success(
                    itemId = item.id,
                    item = encryptionContextProvider.withEncryptionContext { item.toUiModel(this) }
                )
            }
            snackbarDispatcher(CreditCardSnackbarMessage.ItemUpdated)
            telemetryManager.sendEvent(ItemUpdate(EventItemType.CreditCard))
        }.onFailure {
            PassLogger.w(TAG, "Update credit card error")
            PassLogger.w(TAG, it)
            val message = if (it is InvalidContentFormatVersionError) {
                UpdateAppToUpdateItemError
            } else {
                ItemUpdateError
            }
            snackbarDispatcher(message)
        }
        isLoadingState.update { IsLoadingState.NotLoading }
    }

    companion object {
        private const val TAG = "UpdateCreditCardViewModel"
    }
}
