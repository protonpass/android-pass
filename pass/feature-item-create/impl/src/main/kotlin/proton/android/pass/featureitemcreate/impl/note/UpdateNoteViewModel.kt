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

package proton.android.pass.featureitemcreate.impl.note

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
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.some
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.require
import proton.android.pass.commonui.api.toUiModel
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.errors.InvalidContentFormatVersionError
import proton.android.pass.data.api.repositories.DraftAttachmentRepository
import proton.android.pass.data.api.repositories.ItemRepository
import proton.android.pass.data.api.repositories.MetadataResolver
import proton.android.pass.data.api.usecases.GetShareById
import proton.android.pass.data.api.usecases.attachments.ClearAttachments
import proton.android.pass.data.api.usecases.attachments.UploadAttachment
import proton.android.pass.domain.Item
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.featureitemcreate.impl.ItemSavedState
import proton.android.pass.featureitemcreate.impl.ItemUpdate
import proton.android.pass.featureitemcreate.impl.note.NoteSnackbarMessage.InitError
import proton.android.pass.featureitemcreate.impl.note.NoteSnackbarMessage.ItemUpdateError
import proton.android.pass.featureitemcreate.impl.note.NoteSnackbarMessage.NoteUpdated
import proton.android.pass.featureitemcreate.impl.note.NoteSnackbarMessage.UpdateAppToUpdateItemError
import proton.android.pass.log.api.PassLogger
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.android.pass.preferences.FeatureFlagsPreferencesRepository
import proton.android.pass.telemetry.api.EventItemType
import proton.android.pass.telemetry.api.TelemetryManager
import javax.inject.Inject

@HiltViewModel
class UpdateNoteViewModel @Inject constructor(
    private val accountManager: AccountManager,
    private val itemRepository: ItemRepository,
    private val getShare: GetShareById,
    private val snackbarDispatcher: SnackbarDispatcher,
    private val encryptionContextProvider: EncryptionContextProvider,
    private val telemetryManager: TelemetryManager,
    metadataResolver: MetadataResolver,
    uploadAttachment: UploadAttachment,
    clearAttachments: ClearAttachments,
    draftAttachmentRepository: DraftAttachmentRepository,
    featureFlagsRepository: FeatureFlagsPreferencesRepository,
    savedStateHandleProvider: SavedStateHandleProvider
) : BaseNoteViewModel(
    snackbarDispatcher = snackbarDispatcher,
    metadataResolver = metadataResolver,
    uploadAttachment = uploadAttachment,
    clearAttachments = clearAttachments,
    draftAttachmentRepository = draftAttachmentRepository,
    featureFlagsRepository = featureFlagsRepository,
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

    init {
        viewModelScope.launch(coroutineExceptionHandler) {
            setupInitialState()
        }
    }

    val updateNoteUiState: StateFlow<UpdateNoteUiState> = combine(
        flowOf(navShareId),
        baseNoteUiState,
        ::UpdateNoteUiState
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = UpdateNoteUiState.Initial
    )

    private suspend fun setupInitialState() {
        if (itemOption != None) return
        isLoadingState.update { IsLoadingState.Loading }
        runCatching { itemRepository.getById(navShareId, navItemId) }
            .onSuccess(::onNoteItemReceived)
            .onFailure {
                PassLogger.i(TAG, it, "Get by id error")
                snackbarDispatcher(InitError)
            }

        isLoadingState.update { IsLoadingState.NotLoading }
    }

    private fun onNoteItemReceived(item: Item) {
        itemOption = item.some()
        if (noteItemFormState == NoteItemFormState.Empty) {
            noteItemFormMutableState = encryptionContextProvider.withEncryptionContext {
                NoteItemFormState(
                    title = decrypt(item.title),
                    note = decrypt(item.note)
                )
            }
        }
    }

    fun updateItem(shareId: ShareId) = viewModelScope.launch(coroutineExceptionHandler) {
        val item = itemOption
        if (item == None) return@launch
        isLoadingState.update { IsLoadingState.Loading }
        val noteItem = noteItemFormMutableState
        val userId = accountManager.getPrimaryUserId()
            .first { userId -> userId != null }
        if (userId != null && item is Some) {
            val itemContents = noteItem.toItemContents()
            runCatching { getShare(userId, shareId) }
                .onFailure {
                    PassLogger.e(TAG, it, "Get share error")
                    snackbarDispatcher(ItemUpdateError)
                }
                .mapCatching { share ->
                    itemRepository.updateItem(userId, share, item.value, itemContents)
                }
                .onSuccess { newItem ->
                    isItemSavedState.update {
                        encryptionContextProvider.withEncryptionContext {
                            ItemSavedState.Success(
                                newItem.id,
                                newItem.toUiModel(this@withEncryptionContext)
                            )
                        }
                    }
                    snackbarDispatcher(NoteUpdated)
                    telemetryManager.sendEvent(ItemUpdate(EventItemType.Note))
                }
                .onFailure {
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
