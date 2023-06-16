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

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.featureitemcreate.impl.ItemSavedState
import proton.android.pass.notifications.api.SnackbarDispatcher

abstract class BaseNoteViewModel(private val snackbarDispatcher: SnackbarDispatcher) : ViewModel() {

    protected val noteItemState: MutableStateFlow<NoteItem> = MutableStateFlow(NoteItem.Empty)
    protected val isLoadingState: MutableStateFlow<IsLoadingState> =
        MutableStateFlow(IsLoadingState.NotLoading)
    protected val isItemSavedState: MutableStateFlow<ItemSavedState> =
        MutableStateFlow(ItemSavedState.Unknown)
    protected val noteItemValidationErrorsState: MutableStateFlow<Set<NoteItemValidationErrors>> =
        MutableStateFlow(emptySet())
    private val hasUserEditedContentFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)

    private val noteItemWrapperState = combine(
        noteItemState,
        noteItemValidationErrorsState
    ) { noteItem, noteItemValidationErrors ->
        NoteItemWrapper(noteItem, noteItemValidationErrors)
    }

    private data class NoteItemWrapper(
        val noteItem: NoteItem,
        val noteItemValidationErrors: Set<NoteItemValidationErrors>
    )

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    val baseNoteUiState: StateFlow<BaseNoteUiState> = combine(
        noteItemWrapperState,
        isLoadingState,
        isItemSavedState,
        hasUserEditedContentFlow
    ) { noteItemWrapper, isLoading, isItemSaved, hasUserEditedContent ->
        BaseNoteUiState(
            noteItem = noteItemWrapper.noteItem,
            errorList = noteItemWrapper.noteItemValidationErrors,
            isLoadingState = isLoading,
            isItemSaved = isItemSaved,
            hasUserEditedContent = hasUserEditedContent
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = BaseNoteUiState.Initial
        )

    fun onTitleChange(value: String) {
        onUserEditedContent()
        noteItemState.update { it.copy(title = value) }
        noteItemValidationErrorsState.update {
            it.toMutableSet().apply { remove(NoteItemValidationErrors.BlankTitle) }
        }
    }

    fun onNoteChange(value: String) {
        onUserEditedContent()
        noteItemState.update { it.copy(note = value) }
    }

    protected fun onUserEditedContent() {
        if (hasUserEditedContentFlow.value) return
        hasUserEditedContentFlow.update { true }
    }

    fun onEmitSnackbarMessage(snackbarMessage: NoteSnackbarMessage) =
        viewModelScope.launch {
            snackbarDispatcher(snackbarMessage)
        }
}
