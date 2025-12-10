package proton.android.pass.features.itemcreate.creditcard

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
import proton.android.pass.clipboard.api.ClipboardManager
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.safeRunCatching
import proton.android.pass.common.api.some
import proton.android.pass.commonpresentation.api.attachments.AttachmentsHandler
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.require
import proton.android.pass.commonui.api.toUiModel
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.errors.InvalidContentFormatVersionError
import proton.android.pass.data.api.repositories.PendingAttachmentLinkRepository
import proton.android.pass.data.api.usecases.CanPerformPaidAction
import proton.android.pass.data.api.usecases.GetItemById
import proton.android.pass.data.api.usecases.ObserveItemById
import proton.android.pass.data.api.usecases.UpdateItem
import proton.android.pass.data.api.usecases.attachments.LinkAttachmentsToItem
import proton.android.pass.data.api.usecases.attachments.RenameAttachments
import proton.android.pass.data.api.usecases.shares.ObserveShare
import proton.android.pass.domain.Item
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.areItemContentsEqual
import proton.android.pass.domain.toItemContents
import proton.android.pass.features.itemcreate.ItemSavedState
import proton.android.pass.features.itemcreate.ItemUpdate
import proton.android.pass.features.itemcreate.common.CustomFieldDraftRepository
import proton.android.pass.features.itemcreate.common.UICustomFieldContent
import proton.android.pass.features.itemcreate.common.canDisplaySharedItemWarningDialogFlow
import proton.android.pass.features.itemcreate.common.canDisplayVaultSharedWarningDialogFlow
import proton.android.pass.features.itemcreate.common.customfields.CustomFieldHandler
import proton.android.pass.features.itemcreate.common.formprocessor.CreditCardFormProcessorType
import proton.android.pass.features.itemcreate.creditcard.CreditCardSnackbarMessage.AttachmentsInitError
import proton.android.pass.features.itemcreate.creditcard.CreditCardSnackbarMessage.InitError
import proton.android.pass.features.itemcreate.creditcard.CreditCardSnackbarMessage.ItemLinkAttachmentsError
import proton.android.pass.features.itemcreate.creditcard.CreditCardSnackbarMessage.ItemRenameAttachmentsError
import proton.android.pass.features.itemcreate.creditcard.CreditCardSnackbarMessage.ItemUpdateError
import proton.android.pass.features.itemcreate.creditcard.CreditCardSnackbarMessage.UpdateAppToUpdateItemError
import proton.android.pass.log.api.PassLogger
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.android.pass.preferences.FeatureFlagsPreferencesRepository
import proton.android.pass.preferences.InternalSettingsRepository
import proton.android.pass.preferences.UserPreferencesRepository
import proton.android.pass.telemetry.api.EventItemType
import proton.android.pass.telemetry.api.TelemetryManager
import javax.inject.Inject

@Suppress("LongParameterList")
@HiltViewModel
class UpdateCreditCardViewModel @Inject constructor(
    private val getItemById: GetItemById,
    private val updateItem: UpdateItem,
    private val encryptionContextProvider: EncryptionContextProvider,
    private val snackbarDispatcher: SnackbarDispatcher,
    private val accountManager: AccountManager,
    private val telemetryManager: TelemetryManager,
    private val linkAttachmentsToItem: LinkAttachmentsToItem,
    private val renameAttachments: RenameAttachments,
    private val pendingAttachmentLinkRepository: PendingAttachmentLinkRepository,
    userPreferencesRepository: UserPreferencesRepository,
    attachmentsHandler: AttachmentsHandler,
    canPerformPaidAction: CanPerformPaidAction,
    customFieldHandler: CustomFieldHandler,
    customFieldDraftRepository: CustomFieldDraftRepository,
    creditCardItemFormProcessor: CreditCardFormProcessorType,
    clipboardManager: ClipboardManager,
    savedStateHandleProvider: SavedStateHandleProvider,
    observeShare: ObserveShare,
    observeItemById: ObserveItemById,
    private val settingsRepository: InternalSettingsRepository,
    featureFlagsPreferencesRepository: FeatureFlagsPreferencesRepository
) : BaseCreditCardViewModel(
    userPreferencesRepository = userPreferencesRepository,
    attachmentsHandler = attachmentsHandler,
    encryptionContextProvider = encryptionContextProvider,
    canPerformPaidAction = canPerformPaidAction,
    customFieldHandler = customFieldHandler,
    customFieldDraftRepository = customFieldDraftRepository,
    creditCardItemFormProcessor = creditCardItemFormProcessor,
    clipboardManager = clipboardManager,
    savedStateHandleProvider = savedStateHandleProvider,
    featureFlagsPreferencesRepository = featureFlagsPreferencesRepository
) {
    private val navShareId: ShareId =
        ShareId(savedStateHandleProvider.get().require(CommonNavArgId.ShareId.key))
    private val navItemId: ItemId = ItemId(
        savedStateHandleProvider.get().require(CommonNavArgId.ItemId.key)
    )
    private var itemOption: Option<Item> = None
    private var originalCustomFields: List<UICustomFieldContent> = emptyList()

    private val canDisplayVaultSharedWarningDialogFlow =
        canDisplayVaultSharedWarningDialogFlow(
            settingsRepository = settingsRepository,
            observeShare = observeShare,
            shareId = navShareId
        )

    private val canDisplaySharedItemWarningDialogFlow =
        canDisplaySharedItemWarningDialogFlow(
            settingsRepository = settingsRepository,
            observeItemById = observeItemById,
            shareId = navShareId,
            itemId = navItemId
        )

    init {
        viewModelScope.launch {
            isLoadingState.update { IsLoadingState.Loading }
            runCatching { getItemById(shareId = navShareId, itemId = navItemId) }
                .onSuccess { item ->
                    runCatching {
                        if (item.hasAttachments) {
                            attachmentsHandler.getAttachmentsForItem(item.shareId, item.id)
                        }
                        item
                    }.onFailure {
                        PassLogger.w(TAG, it)
                        PassLogger.w(TAG, "Get attachments error")
                        snackbarDispatcher(AttachmentsInitError)
                    }
                    onCreditCardItemReceived(item)
                }
                .onFailure {
                    PassLogger.w(TAG, "Error getting item by id")
                    PassLogger.w(TAG, it)
                    snackbarDispatcher(InitError)
                }
            isLoadingState.update { IsLoadingState.NotLoading }
        }
    }

    internal val state: StateFlow<UpdateCreditCardUiState> = combine(
        flowOf(navShareId),
        baseState,
        canDisplayVaultSharedWarningDialogFlow,
        canDisplaySharedItemWarningDialogFlow,
        UpdateCreditCardUiState::Success
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = UpdateCreditCardUiState.NotInitialised
    )

    private suspend fun onCreditCardItemReceived(item: Item) {
        itemOption = item.some()
        encryptionContextProvider.withEncryptionContextSuspendable {
            val default = CreditCardItemFormState.default(this)
            if (creditCardItemFormState.compare(default, this)) {
                val formState = CreditCardItemFormState(item.toItemContents { decrypt(it) })
                originalCustomFields = formState.customFields
                val expirationDate = ExpirationDateProtoMapper.fromProto(formState.expirationDate)
                creditCardItemFormMutableState = formState.copy(
                    expirationDate = expirationDate,
                    customFields = customFieldHandler.sanitiseForEditingCustomFields(formState.customFields)
                )
            }
        }
    }

    internal fun doNotDisplayWarningDialog() {
        settingsRepository.setHasShownItemInSharedVaultWarning(true)
    }

    @Suppress("LongMethod")
    fun update() = viewModelScope.launch {
        if (!isFormStateValid(originalCustomFields)) {
            PassLogger.i(TAG, "Cannot update credit card")
            return@launch
        }
        isLoadingState.update { IsLoadingState.Loading }
        runCatching {
            val userId = accountManager.getPrimaryUserId().first()
                ?: throw IllegalStateException("User id is null")
            val initialItem = itemOption.value() ?: throw IllegalStateException("Item is null")
            val contents = creditCardItemFormState.sanitise().toItemContents()
            val hasContentsChanged = encryptionContextProvider.withEncryptionContextSuspendable {
                !areItemContentsEqual(
                    a = initialItem.toItemContents { decrypt(it) },
                    b = contents,
                    decrypt = { decrypt(it) }
                )
            }
            val hasPendingAttachments =
                pendingAttachmentLinkRepository.getAllToLink().isNotEmpty() ||
                    pendingAttachmentLinkRepository.getAllToUnLink().isNotEmpty()
            if (hasContentsChanged || hasPendingAttachments) {
                updateItem(
                    userId = userId,
                    shareId = navShareId,
                    item = initialItem,
                    contents = contents
                )
            } else {
                initialItem
            }
        }.onSuccess { item ->
            snackbarDispatcher(CreditCardSnackbarMessage.ItemUpdated)
            safeRunCatching {
                renameAttachments(item.shareId, item.id)
            }.onFailure {
                PassLogger.w(TAG, "Error renaming attachments")
                PassLogger.w(TAG, it)
                snackbarDispatcher(ItemRenameAttachmentsError)
            }
            safeRunCatching {
                linkAttachmentsToItem(item.shareId, item.id, item.revision)
            }.onFailure {
                PassLogger.w(TAG, "Link attachment error")
                PassLogger.w(TAG, it)
                snackbarDispatcher(ItemLinkAttachmentsError)
            }
            PassLogger.i(TAG, "Credit card successfully updated")
            isItemSavedState.update {
                ItemSavedState.Success(
                    itemId = item.id,
                    item = encryptionContextProvider.withEncryptionContext { item.toUiModel(this) }
                )
            }
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
