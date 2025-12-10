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

package proton.android.pass.features.itemcreate.note

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
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
import proton.android.pass.common.api.Some
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
import proton.android.pass.features.itemcreate.common.formprocessor.NoteItemFormProcessor
import proton.android.pass.features.itemcreate.note.NoteSnackbarMessage.AttachmentsInitError
import proton.android.pass.features.itemcreate.note.NoteSnackbarMessage.InitError
import proton.android.pass.features.itemcreate.note.NoteSnackbarMessage.ItemLinkAttachmentsError
import proton.android.pass.features.itemcreate.note.NoteSnackbarMessage.ItemRenameAttachmentsError
import proton.android.pass.features.itemcreate.note.NoteSnackbarMessage.ItemUpdateError
import proton.android.pass.features.itemcreate.note.NoteSnackbarMessage.NoteUpdated
import proton.android.pass.features.itemcreate.note.NoteSnackbarMessage.UpdateAppToUpdateItemError
import proton.android.pass.log.api.PassLogger
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.android.pass.preferences.InternalSettingsRepository
import proton.android.pass.preferences.UserPreferencesRepository
import proton.android.pass.telemetry.api.EventItemType
import proton.android.pass.telemetry.api.TelemetryManager
import javax.inject.Inject

@Suppress("LongParameterList")
@HiltViewModel
class UpdateNoteViewModel @Inject constructor(
    private val accountManager: AccountManager,
    private val getItemById: GetItemById,
    private val updateItem: UpdateItem,
    private val snackbarDispatcher: SnackbarDispatcher,
    private val encryptionContextProvider: EncryptionContextProvider,
    private val telemetryManager: TelemetryManager,
    private val attachmentsHandler: AttachmentsHandler,
    private val linkAttachmentsToItem: LinkAttachmentsToItem,
    private val renameAttachments: RenameAttachments,
    private val pendingAttachmentLinkRepository: PendingAttachmentLinkRepository,
    clipboardManager: ClipboardManager,
    canPerformPaidAction: CanPerformPaidAction,
    customFieldHandler: CustomFieldHandler,
    customFieldDraftRepository: CustomFieldDraftRepository,
    noteItemFormProcessor: NoteItemFormProcessor,
    userPreferencesRepository: UserPreferencesRepository,
    savedStateHandleProvider: SavedStateHandleProvider,
    observeShare: ObserveShare,
    observeItemById: ObserveItemById,
    private val settingsRepository: InternalSettingsRepository
) : BaseNoteViewModel(
    clipboardManager = clipboardManager,
    canPerformPaidAction = canPerformPaidAction,
    userPreferencesRepository = userPreferencesRepository,
    snackbarDispatcher = snackbarDispatcher,
    attachmentsHandler = attachmentsHandler,
    customFieldHandler = customFieldHandler,
    customFieldDraftRepository = customFieldDraftRepository,
    noteItemFormProcessor = noteItemFormProcessor,
    encryptionContextProvider = encryptionContextProvider,
    savedStateHandleProvider = savedStateHandleProvider
) {

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        PassLogger.w(TAG, throwable)
    }

    private val navShareId: ShareId =
        ShareId(savedStateHandleProvider.get().require(CommonNavArgId.ShareId.key))
    private val navItemId: ItemId =
        ItemId(savedStateHandleProvider.get().require(CommonNavArgId.ItemId.key))

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
        viewModelScope.launch(coroutineExceptionHandler) {
            setupInitialState()
        }
    }


    internal val updateNoteUiState: StateFlow<UpdateNoteUiState> = combine(
        flowOf(navShareId),
        baseNoteUiState,
        canDisplayVaultSharedWarningDialogFlow,
        canDisplaySharedItemWarningDialogFlow,
        ::UpdateNoteUiState
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = UpdateNoteUiState.Initial
    )

    private suspend fun setupInitialState() {
        if (itemOption != None) return
        isLoadingState.update { IsLoadingState.Loading }
        runCatching { getItemById(shareId = navShareId, itemId = navItemId) }
            .onFailure {
                PassLogger.w(TAG, it)
                PassLogger.w(TAG, "Get item error")
                snackbarDispatcher(InitError)
            }
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
                onNoteItemReceived(item)
            }

        isLoadingState.update { IsLoadingState.NotLoading }
    }

    private suspend fun onNoteItemReceived(item: Item) {
        itemOption = item.some()
        if (noteItemFormState == NoteItemFormState.Empty) {
            val formState = encryptionContextProvider.withEncryptionContextSuspendable {
                NoteItemFormState(item.toItemContents { decrypt(it) })
            }
            originalCustomFields = formState.customFields
            noteItemFormMutableState = formState.copy(
                customFields = customFieldHandler.sanitiseForEditingCustomFields(formState.customFields)
            )
        }
    }

    internal fun doNotDisplayWarningDialog() {
        settingsRepository.setHasShownItemInSharedVaultWarning(true)
    }

    @Suppress("LongMethod")
    fun updateItem(shareId: ShareId) = viewModelScope.launch(coroutineExceptionHandler) {
        val initialItem = itemOption
        if (initialItem == None) return@launch
        if (!isFormStateValid(originalCustomFields)) return@launch

        isLoadingState.update { IsLoadingState.Loading }
        val userId = accountManager.getPrimaryUserId()
            .first { userId -> userId != null }
        if (userId != null && initialItem is Some) {
            val contents = noteItemFormMutableState.toItemContents()
            runCatching {
                val hasContentsChanged =
                    encryptionContextProvider.withEncryptionContextSuspendable {
                        !areItemContentsEqual(
                            a = initialItem.value.toItemContents { decrypt(it) },
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
                        shareId = shareId,
                        item = initialItem.value,
                        contents = contents
                    )
                } else {
                    initialItem.value
                }
            }.onSuccess { item ->
                snackbarDispatcher(NoteUpdated)
                runCatching {
                    renameAttachments(item.shareId, item.id)
                }.onFailure {
                    PassLogger.w(TAG, "Error renaming attachments")
                    PassLogger.w(TAG, it)
                    snackbarDispatcher(ItemRenameAttachmentsError)
                }
                runCatching {
                    linkAttachmentsToItem(item.shareId, item.id, item.revision)
                }.onFailure {
                    PassLogger.w(TAG, "Link attachment error")
                    PassLogger.w(TAG, it)
                    snackbarDispatcher(ItemLinkAttachmentsError)
                }
                isItemSavedState.update {
                    encryptionContextProvider.withEncryptionContext {
                        ItemSavedState.Success(
                            item.id,
                            item.toUiModel(this@withEncryptionContext)
                        )
                    }
                }
                telemetryManager.sendEvent(ItemUpdate(EventItemType.Note))
            }.onFailure {
                val message = if (it is InvalidContentFormatVersionError) {
                    UpdateAppToUpdateItemError
                } else {
                    ItemUpdateError
                }
                PassLogger.w(TAG, "Update item error")
                PassLogger.w(TAG, it)
                snackbarDispatcher(message)
            }
        } else {
            PassLogger.i(TAG, "Empty User Id")
            snackbarDispatcher(ItemUpdateError)
        }
        isLoadingState.update { IsLoadingState.NotLoading }
    }

    companion object {
        private const val TAG = "UpdateNoteViewModel"
    }
}
