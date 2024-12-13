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

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi
import androidx.lifecycle.viewmodel.compose.saveable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import proton.android.pass.common.api.combineN
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.featureitemcreate.impl.ItemSavedState
import proton.android.pass.featureitemcreate.impl.common.attachments.AttachmentsHandler
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.android.pass.preferences.FeatureFlag
import proton.android.pass.preferences.FeatureFlagsPreferencesRepository
import java.net.URI

abstract class BaseNoteViewModel(
    private val snackbarDispatcher: SnackbarDispatcher,
    private val attachmentsHandler: AttachmentsHandler,
    featureFlagsRepository: FeatureFlagsPreferencesRepository,
    savedStateHandleProvider: SavedStateHandleProvider
) : ViewModel() {

    private val hasUserEditedContentFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)

    init {
        attachmentsHandler.observeNewAttachments(viewModelScope) { newUris ->
            if (newUris.isNotEmpty()) {
                onUserEditedContent()
                newUris.forEach { uri ->
                    attachmentsHandler.uploadNewAttachment(uri, viewModelScope)
                }
            }
        }
    }

    @OptIn(SavedStateHandleSaveableApi::class)
    protected var noteItemFormMutableState: NoteItemFormState by savedStateHandleProvider.get()
        .saveable { mutableStateOf(NoteItemFormState.Empty) }
    val noteItemFormState: NoteItemFormState get() = noteItemFormMutableState

    protected val isLoadingState: MutableStateFlow<IsLoadingState> =
        MutableStateFlow(IsLoadingState.NotLoading)
    protected val isItemSavedState: MutableStateFlow<ItemSavedState> =
        MutableStateFlow(ItemSavedState.Unknown)
    protected val noteItemValidationErrorsState: MutableStateFlow<Set<NoteItemValidationErrors>> =
        MutableStateFlow(emptySet())

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    val baseNoteUiState: StateFlow<BaseNoteUiState> = combineN(
        noteItemValidationErrorsState,
        isLoadingState,
        isItemSavedState,
        hasUserEditedContentFlow,
        attachmentsHandler.attachmentsFlow,
        featureFlagsRepository.get<Boolean>(FeatureFlag.FILE_ATTACHMENTS_V1)
    ) { noteItemValidationErrors, isLoading, isItemSaved, hasUserEditedContent, attachmentsState,
        isFileAttachmentsEnabled ->
        BaseNoteUiState(
            errorList = noteItemValidationErrors,
            isLoadingState = isLoading,
            itemSavedState = isItemSaved,
            hasUserEditedContent = hasUserEditedContent,
            attachmentsState = attachmentsState,
            isFileAttachmentsEnabled = isFileAttachmentsEnabled
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = BaseNoteUiState.Initial
        )

    fun onTitleChange(value: String) {
        onUserEditedContent()
        noteItemFormMutableState = noteItemFormMutableState.copy(title = value)
        noteItemValidationErrorsState.update {
            it.toMutableSet().apply { remove(NoteItemValidationErrors.BlankTitle) }
        }
    }

    fun onNoteChange(value: String) {
        onUserEditedContent()
        noteItemFormMutableState = noteItemFormMutableState.copy(note = value)
    }

    protected fun onUserEditedContent() {
        if (hasUserEditedContentFlow.value) return
        hasUserEditedContentFlow.update { true }
    }

    fun onEmitSnackbarMessage(snackbarMessage: NoteSnackbarMessage) = viewModelScope.launch {
        snackbarDispatcher(snackbarMessage)
    }

    fun openDraftAttachment(
        context: Context,
        uri: URI,
        mimetype: String
    ) {
        attachmentsHandler.openDraftAttachment(context, uri, mimetype)
    }

    override fun onCleared() {
        attachmentsHandler.onClearAttachments()
        super.onCleared()
    }
}
