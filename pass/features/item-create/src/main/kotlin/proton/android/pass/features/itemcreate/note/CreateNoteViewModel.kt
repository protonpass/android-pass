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

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi
import androidx.lifecycle.viewmodel.compose.saveable
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
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
import proton.android.pass.common.api.asLoadingResult
import proton.android.pass.common.api.toOption
import proton.android.pass.commonpresentation.api.attachments.AttachmentsHandler
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.toUiModel
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.repositories.ItemRepository
import proton.android.pass.data.api.usecases.CanPerformPaidAction
import proton.android.pass.data.api.usecases.GetItemById
import proton.android.pass.data.api.usecases.GetShareById
import proton.android.pass.data.api.usecases.ObserveVaultsWithItemCount
import proton.android.pass.data.api.usecases.attachments.LinkAttachmentsToItem
import proton.android.pass.data.api.usecases.defaultvault.ObserveDefaultVault
import proton.android.pass.data.api.usecases.shares.ObserveShare
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.VaultWithItemCount
import proton.android.pass.domain.toItemContents
import proton.android.pass.features.itemcreate.ItemCreate
import proton.android.pass.features.itemcreate.ItemSavedState
import proton.android.pass.features.itemcreate.R
import proton.android.pass.features.itemcreate.common.CustomFieldDraftRepository
import proton.android.pass.features.itemcreate.common.OptionShareIdSaver
import proton.android.pass.features.itemcreate.common.ShareUiState
import proton.android.pass.features.itemcreate.common.UICustomFieldContent
import proton.android.pass.features.itemcreate.common.canDisplayWarningMessageForCreationFlow
import proton.android.pass.features.itemcreate.common.customfields.CustomFieldHandler
import proton.android.pass.features.itemcreate.common.formprocessor.NoteItemFormProcessor
import proton.android.pass.features.itemcreate.common.getShareUiStateFlow
import proton.android.pass.features.itemcreate.note.NoteSnackbarMessage.ItemCreationError
import proton.android.pass.features.itemcreate.note.NoteSnackbarMessage.ItemLinkAttachmentsError
import proton.android.pass.features.itemcreate.note.NoteSnackbarMessage.NoteCreated
import proton.android.pass.inappreview.api.InAppReviewTriggerMetrics
import proton.android.pass.log.api.PassLogger
import proton.android.pass.navigation.api.CommonOptionalNavArgId
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.android.pass.preferences.InternalSettingsRepository
import proton.android.pass.preferences.UserPreferencesRepository
import proton.android.pass.telemetry.api.EventItemType
import proton.android.pass.telemetry.api.TelemetryManager
import javax.inject.Inject

@Suppress("LongParameterList")
@HiltViewModel
class CreateNoteViewModel @Inject constructor(
    private val accountManager: AccountManager,
    private val getShare: GetShareById,
    private val itemRepository: ItemRepository,
    private val snackbarDispatcher: SnackbarDispatcher,
    private val encryptionContextProvider: EncryptionContextProvider,
    private val telemetryManager: TelemetryManager,
    private val inAppReviewTriggerMetrics: InAppReviewTriggerMetrics,
    private val linkAttachmentsToItem: LinkAttachmentsToItem,
    private val getItemById: GetItemById,
    clipboardManager: ClipboardManager,
    canPerformPaidAction: CanPerformPaidAction,
    userPreferencesRepository: UserPreferencesRepository,
    observeVaults: ObserveVaultsWithItemCount,
    observeDefaultVault: ObserveDefaultVault,
    attachmentsHandler: AttachmentsHandler,
    customFieldHandler: CustomFieldHandler,
    customFieldDraftRepository: CustomFieldDraftRepository,
    noteItemFormProcessor: NoteItemFormProcessor,
    savedStateHandleProvider: SavedStateHandleProvider,
    observeShare: ObserveShare,
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

    private val navShareId: Option<ShareId> =
        savedStateHandleProvider.get().get<String>(CommonOptionalNavArgId.ShareId.key)
            .toOption()
            .map(::ShareId)

    private val navItemId: Option<ItemId> =
        savedStateHandleProvider.get().get<String>(CommonOptionalNavArgId.ItemId.key)
            .toOption()
            .map(::ItemId)

    @OptIn(SavedStateHandleSaveableApi::class)
    private var selectedShareIdMutableState: Option<ShareId> by savedStateHandleProvider.get()
        .saveable(stateSaver = OptionShareIdSaver) { mutableStateOf(None) }
    private val selectedShareIdState: StateFlow<Option<ShareId>> =
        snapshotFlow { selectedShareIdMutableState }
            .filterNotNull()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = None
            )

    private val observeAllVaultsFlow: Flow<List<VaultWithItemCount>> =
        observeVaults(includeHidden = true).distinctUntilChanged()

    private val canDisplayWarningVaultSharedDialogFlow =
        canDisplayWarningMessageForCreationFlow(
            selectedShareIdMutableState = selectedShareIdMutableState,
            observeShare = observeShare,
            navShareId = navShareId,
            settingsRepository = settingsRepository
        )

    private val shareUiState: StateFlow<ShareUiState> = getShareUiStateFlow(
        navShareIdState = flowOf(navShareId),
        selectedShareIdState = selectedShareIdState,
        observeAllVaultsFlow = observeAllVaultsFlow.asLoadingResult(),
        observeDefaultVaultFlow = observeDefaultVault().asLoadingResult(),
        viewModelScope = viewModelScope,
        tag = TAG
    )

    internal val createNoteUiState: StateFlow<CreateNoteUiState> = combine(
        shareUiState,
        baseNoteUiState,
        canDisplayWarningVaultSharedDialogFlow,
        ::CreateNoteUiState
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CreateNoteUiState.Initial
    )

    suspend fun duplicateContents(context: Context) {
        val shareId = navShareId.value() ?: return
        val itemId = navItemId.value() ?: return
        val item = getItemById(shareId = shareId, itemId = itemId)

        val currentValue = noteItemFormState
        encryptionContextProvider.withEncryptionContextSuspendable {
            val itemContents = item.toItemContents<ItemContents.Note> { decrypt(it) }
            val customFields = itemContents.customFields.map(UICustomFieldContent.Companion::from)
            noteItemFormMutableState = currentValue.copy(
                title = context.getString(R.string.title_duplicate, decrypt(item.title)),
                note = decrypt(item.note),
                customFields = customFieldHandler.sanitiseForEditingCustomFields(customFields)
            )
        }
    }

    internal fun doNotDisplayWarningDialog() {
        settingsRepository.setHasShownItemInSharedVaultWarning(true)
    }

    fun createNote(shareId: ShareId) = viewModelScope.launch(coroutineExceptionHandler) {
        if (!isFormStateValid()) return@launch
        isLoadingState.update { IsLoadingState.Loading }
        val userId = accountManager.getPrimaryUserId()
            .first { userId -> userId != null }
        if (userId != null) {
            runCatching { getShare(userId, shareId) }
                .onFailure { PassLogger.e(TAG, it, "Error getting share") }
                .mapCatching { share ->
                    val itemContents = noteItemFormState.toItemContents()
                    itemRepository.createItem(userId, share, itemContents)
                }
                .onFailure {
                    PassLogger.w(TAG, "Create item error")
                    PassLogger.w(TAG, it)
                    snackbarDispatcher(ItemCreationError)
                }
                .onSuccess { item ->
                    snackbarDispatcher(NoteCreated)
                    runCatching {
                        linkAttachmentsToItem(item.shareId, item.id, item.revision)
                    }.onFailure {
                        PassLogger.w(TAG, "Link attachment error")
                        PassLogger.w(TAG, it)
                        snackbarDispatcher(ItemLinkAttachmentsError)
                    }
                    inAppReviewTriggerMetrics.incrementItemCreatedCount()
                    isItemSavedState.update {
                        encryptionContextProvider.withEncryptionContext {
                            ItemSavedState.Success(
                                item.id,
                                item.toUiModel(this@withEncryptionContext)
                            )
                        }
                    }
                    telemetryManager.sendEvent(ItemCreate(EventItemType.Note))
                }
        } else {
            PassLogger.i(TAG, "Empty User Id")
            snackbarDispatcher(ItemCreationError)
        }
        isLoadingState.update { IsLoadingState.NotLoading }
    }

    fun changeVault(shareId: ShareId) = viewModelScope.launch {
        onUserEditedContent()
        selectedShareIdMutableState = Some(shareId)
    }

    companion object {
        private const val TAG = "CreateNoteViewModel"
    }
}
