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

package proton.android.pass.features.itemcreate.alias

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.util.kotlin.takeIfNotBlank
import proton.android.pass.clipboard.api.ClipboardManager
import proton.android.pass.common.api.FlowUtils.oneShot
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.some
import proton.android.pass.common.api.toOption
import proton.android.pass.commonpresentation.api.attachments.AttachmentsHandler
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.require
import proton.android.pass.commonui.api.toUiModel
import proton.android.pass.composecomponents.impl.uievents.IsButtonEnabled
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.errors.InvalidContentFormatVersionError
import proton.android.pass.data.api.usecases.CanPerformPaidAction
import proton.android.pass.data.api.usecases.GetItemById
import proton.android.pass.data.api.usecases.ObserveAliasDetails
import proton.android.pass.data.api.usecases.UpdateAlias
import proton.android.pass.data.api.usecases.UpdateAliasContent
import proton.android.pass.data.api.usecases.attachments.LinkAttachmentsToItem
import proton.android.pass.data.api.usecases.attachments.RenameAttachments
import proton.android.pass.domain.AliasDetails
import proton.android.pass.domain.Item
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.toItemContents
import proton.android.pass.features.itemcreate.ItemSavedState
import proton.android.pass.features.itemcreate.ItemUpdate
import proton.android.pass.features.itemcreate.alias.AliasSnackbarMessage.AliasUpdated
import proton.android.pass.features.itemcreate.alias.AliasSnackbarMessage.InitAttachmentsError
import proton.android.pass.features.itemcreate.alias.AliasSnackbarMessage.InitError
import proton.android.pass.features.itemcreate.alias.AliasSnackbarMessage.ItemLinkAttachmentsError
import proton.android.pass.features.itemcreate.alias.AliasSnackbarMessage.ItemRenameAttachmentsError
import proton.android.pass.features.itemcreate.alias.AliasSnackbarMessage.ItemUpdateError
import proton.android.pass.features.itemcreate.alias.AliasSnackbarMessage.UpdateAppToUpdateItemError
import proton.android.pass.features.itemcreate.alias.draftrepositories.MailboxDraftRepository
import proton.android.pass.features.itemcreate.alias.draftrepositories.SuffixDraftRepository
import proton.android.pass.features.itemcreate.common.CommonFieldValidationError
import proton.android.pass.features.itemcreate.common.CustomFieldDraftRepository
import proton.android.pass.features.itemcreate.common.UICustomFieldContent
import proton.android.pass.features.itemcreate.common.customfields.CustomFieldHandler
import proton.android.pass.features.itemcreate.common.formprocessor.AliasItemFormProcessorType
import proton.android.pass.log.api.PassLogger
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.android.pass.preferences.UserPreferencesRepository
import proton.android.pass.telemetry.api.EventItemType
import proton.android.pass.telemetry.api.TelemetryManager
import javax.inject.Inject

@Suppress("LongParameterList")
@HiltViewModel
class UpdateAliasViewModel @Inject constructor(
    private val accountManager: AccountManager,
    private val snackbarDispatcher: SnackbarDispatcher,
    private val updateAliasUseCase: UpdateAlias,
    private val encryptionContextProvider: EncryptionContextProvider,
    private val telemetryManager: TelemetryManager,
    private val getItemById: GetItemById,
    private val observeAliasDetails: ObserveAliasDetails,
    private val renameAttachments: RenameAttachments,
    private val linkAttachmentsToItem: LinkAttachmentsToItem,
    private val mailboxDraftRepository: MailboxDraftRepository,
    suffixDraftRepository: SuffixDraftRepository,
    userPreferencesRepository: UserPreferencesRepository,
    attachmentsHandler: AttachmentsHandler,
    customFieldHandler: CustomFieldHandler,
    customFieldDraftRepository: CustomFieldDraftRepository,
    canPerformPaidAction: CanPerformPaidAction,
    aliasItemFormProcessor: AliasItemFormProcessorType,
    clipboardManager: ClipboardManager,
    savedStateHandleProvider: SavedStateHandleProvider
) : BaseAliasViewModel(
    mailboxDraftRepository = mailboxDraftRepository,
    suffixDraftRepository = suffixDraftRepository,
    userPreferencesRepository = userPreferencesRepository,
    attachmentsHandler = attachmentsHandler,
    snackbarDispatcher = snackbarDispatcher,
    customFieldHandler = customFieldHandler,
    customFieldDraftRepository = customFieldDraftRepository,
    canPerformPaidAction = canPerformPaidAction,
    aliasItemFormProcessor = aliasItemFormProcessor,
    clipboardManager = clipboardManager,
    encryptionContextProvider = encryptionContextProvider,
    savedStateHandleProvider = savedStateHandleProvider
) {

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        PassLogger.w(TAG, throwable)
    }

    private val shareId: ShareId = savedStateHandleProvider.get()
        .require<String>(CommonNavArgId.ShareId.key)
        .let(::ShareId)

    private val itemId: ItemId = savedStateHandleProvider.get()
        .require<String>(CommonNavArgId.ItemId.key)
        .let(::ItemId)

    private var itemOption: Option<Item> = None
    private var originalCustomFields: List<UICustomFieldContent> = emptyList()

    private var mailboxesChanged = false
    private var isSLNoteChanged = false
    private var isDisplayNameChanged = false

    private val canModifyAliasStateFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)

    init {
        viewModelScope.launch(coroutineExceptionHandler) {
            isApplyButtonEnabledState.update { IsButtonEnabled.Disabled }
            if (itemOption != None) return@launch

            isLoadingState.update { IsLoadingState.Loading }

            runCatching {
                combine(
                    oneShot { getItemById(shareId, itemId) },
                    observeAliasDetails(shareId, itemId),
                    ::Pair
                ).first()
            }.onSuccess { (item, aliasDetails) ->
                runCatching {
                    if (item.hasAttachments) {
                        attachmentsHandler.getAttachmentsForItem(item.shareId, item.id)
                    }
                    item
                }.onFailure {
                    showError("Error getting attachments", InitAttachmentsError, it)
                }
                itemOption = item.some()
                onAliasDetails(aliasDetails, item)
            }.onFailure { error ->
                showError("Error setting the initial state", InitError, error)
            }

            isLoadingState.update { IsLoadingState.NotLoading }
        }

        mailboxDraftRepository.getSelectedMailboxFlow()
            .drop(1) // drop initial setup
            .take(1) // we don't need to listen for more
            .onEach {
                isApplyButtonEnabledState.update { IsButtonEnabled.Enabled }
                mailboxesChanged = true
            }
            .launchIn(viewModelScope)
    }

    internal val updateAliasUiState: StateFlow<UpdateAliasUiState> = combine(
        flowOf(shareId),
        baseAliasUiState,
        mailboxDraftRepository.getSelectedMailboxFlow().map { it.map(::AliasMailboxUiModel).toSet() },
        canModifyAliasStateFlow
    ) { shareId, aliasUiState, selectedMailboxes, canModify ->
        aliasItemFormMutableState = aliasItemFormState.copy(
            selectedMailboxes = selectedMailboxes
        )

        UpdateAliasUiState(
            selectedShareId = shareId,
            canModify = canModify,
            baseAliasUiState = aliasUiState
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = UpdateAliasUiState.Initial
    )

    override fun onNoteChange(value: String) {
        super.onNoteChange(value)
        isApplyButtonEnabledState.update { IsButtonEnabled.Enabled }
    }

    override fun onSLNoteChange(newSLNote: String) {
        super.onSLNoteChange(newSLNote)
        isApplyButtonEnabledState.update { IsButtonEnabled.Enabled }
        isSLNoteChanged = true
    }

    override fun onSenderNameChange(value: String) {
        super.onSenderNameChange(value)
        isApplyButtonEnabledState.update { IsButtonEnabled.Enabled }
        isDisplayNameChanged = true
    }

    override fun onTitleChange(value: String) {
        onUserEditedContent()
        aliasItemFormMutableState = aliasItemFormState.copy(title = value)
        aliasItemValidationErrorsState.update {
            it.toMutableSet()
                .apply { remove(CommonFieldValidationError.BlankTitle) }
        }
        isApplyButtonEnabledState.update { IsButtonEnabled.Enabled }
    }

    private suspend fun onAliasDetails(aliasDetails: AliasDetails, item: Item) {
        mailboxDraftRepository.addMailboxes(aliasDetails.availableMailboxes.toSet())
        aliasDetails.mailboxes.forEach {
            mailboxDraftRepository.toggleMailboxById(it.id)
        }
        val details = AliasDetailsUiModel(aliasDetails)
        val contents: ItemContents.Alias = encryptionContextProvider.withEncryptionContextSuspendable {
            item.toItemContents { decrypt(it) }
        }

        val (prefix, suffix) = AliasUtils.extractPrefixSuffix(contents.aliasEmail)
        val selectedSuffix = AliasSuffixUiModel(suffix, suffix, false, false, "")

        canModifyAliasStateFlow.update { aliasDetails.canModify }

        aliasItemFormMutableState = buildAliasFormState(
            current = aliasItemFormState,
            contents = contents,
            details = details,
            prefix = prefix,
            selectedSuffix = selectedSuffix
        )
        originalCustomFields = aliasItemFormState.customFields
    }

    private fun buildAliasFormState(
        current: AliasItemFormState,
        contents: ItemContents.Alias,
        details: AliasDetailsUiModel,
        prefix: String,
        selectedSuffix: AliasSuffixUiModel
    ): AliasItemFormState = current.copy(
        title = current.title.ifBlank { contents.title },
        note = current.note.ifBlank { contents.note },
        prefix = prefix,
        aliasOptions = AliasOptionsUiModel(emptyList(), details.availableMailboxes),
        selectedSuffix = selectedSuffix,
        selectedMailboxes = details.mailboxes.toSet(),
        aliasToBeCreated = contents.aliasEmail,
        slNote = details.slNote.takeIfNotBlank(),
        senderName = details.name?.takeIfNotBlank(),
        customFields = contents.customFields.map(UICustomFieldContent.Companion::from)
            .let { customFieldHandler.sanitiseForEditingCustomFields(it) }
    )

    private suspend fun showError(
        message: String,
        snackbarMessage: AliasSnackbarMessage,
        cause: Throwable? = null
    ) {
        PassLogger.w(TAG, message)
        cause?.let { PassLogger.w(TAG, it) }
        snackbarDispatcher(snackbarMessage)
        mutableCloseScreenEventFlow.update { CloseScreenEvent.Close }
    }

    @Suppress("LongMethod")
    internal fun updateAlias() {
        viewModelScope.launch(coroutineExceptionHandler) {
            if (!isFormStateValid(originalCustomFields)) {
                PassLogger.i(TAG, "Cannot update alias")
                return@launch
            }

            val body = createUpdateAliasBody()
            isLoadingState.update { IsLoadingState.Loading }

            val userId = accountManager.getPrimaryUserId().first { userId -> userId != null }
            val initialItem = itemOption
            if (userId != null && initialItem is Some) {
                runCatching {
                    updateAliasUseCase(
                        userId = userId,
                        item = initialItem.value,
                        content = body
                    )
                }.onSuccess { item ->
                    snackbarDispatcher(AliasUpdated)
                    runCatching {
                        renameAttachments(item.shareId, item.id)
                    }.onFailure {
                        PassLogger.w(TAG, "Error renaming attachments")
                        PassLogger.w(TAG, it)
                        snackbarDispatcher(ItemRenameAttachmentsError)
                    }
                    runCatching {
                        linkAttachmentsToItem(
                            shareId = item.shareId,
                            itemId = item.id,
                            revision = item.revision
                        )
                    }.onFailure {
                        PassLogger.w(TAG, "Error linking attachments to item")
                        PassLogger.w(TAG, it)
                        snackbarDispatcher(ItemLinkAttachmentsError)
                    }
                    isItemSavedState.update {
                        val itemUiModel = encryptionContextProvider.withEncryptionContext {
                            item.toUiModel(this)
                        }
                        ItemSavedState.Success(
                            itemId = item.id,
                            item = itemUiModel
                        )
                    }
                    isLoadingState.update { IsLoadingState.NotLoading }
                    telemetryManager.sendEvent(ItemUpdate(EventItemType.Alias))
                }.onFailure {
                    PassLogger.w(TAG, "Update alias error")
                    PassLogger.w(TAG, it)
                    val message = if (it is InvalidContentFormatVersionError) {
                        UpdateAppToUpdateItemError
                    } else {
                        ItemUpdateError
                    }
                    snackbarDispatcher(message)
                    isLoadingState.update { IsLoadingState.NotLoading }
                }
            } else {
                PassLogger.i(TAG, "Empty User Id")
                snackbarDispatcher(ItemUpdateError)
                isLoadingState.update { IsLoadingState.NotLoading }
            }
        }
    }

    private fun createUpdateAliasBody(): UpdateAliasContent {
        val mailboxes = if (mailboxesChanged) {
            val selectedMailboxes = aliasItemFormState
                .selectedMailboxes
                .map { it.toDomain() }
            Some(selectedMailboxes)
        } else None

        return UpdateAliasContent(
            mailboxes = mailboxes,
            itemData = aliasItemFormState.toItemContents(),
            slNoteOption = aliasItemFormState.slNote
                .takeIf { isSLNoteChanged }
                .toOption(),
            displayNameOption = aliasItemFormState.senderName
                .takeIf { isDisplayNameChanged }
                .toOption()
        )
    }

    private companion object {

        private const val TAG = "UpdateAliasViewModel"

    }

}
