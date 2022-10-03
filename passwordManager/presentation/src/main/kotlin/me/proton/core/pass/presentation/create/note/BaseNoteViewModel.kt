package me.proton.core.pass.presentation.create.note

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.proton.core.pass.presentation.uievents.IsLoadingState
import me.proton.core.pass.presentation.uievents.ItemSavedState

abstract class BaseNoteViewModel : ViewModel() {

    protected val noteItemState: MutableStateFlow<NoteItem> = MutableStateFlow(NoteItem.Empty)
    protected val isLoadingState: MutableStateFlow<IsLoadingState> =
        MutableStateFlow(IsLoadingState.NotLoading)
    protected val isItemSavedState: MutableStateFlow<ItemSavedState> =
        MutableStateFlow(ItemSavedState.Unknown)
    protected val noteItemValidationErrorsState: MutableStateFlow<Set<NoteItemValidationErrors>> =
        MutableStateFlow(emptySet())

    val noteUiState: StateFlow<CreateUpdateNoteUiState> = combine(
        noteItemState,
        isLoadingState,
        isItemSavedState,
        noteItemValidationErrorsState
    ) { noteItem, isLoading, isItemSaved, noteItemValidationErrors ->
        CreateUpdateNoteUiState(
            noteItem = noteItem,
            errorList = noteItemValidationErrors,
            isLoadingState = isLoading,
            isItemSaved = isItemSaved
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = CreateUpdateNoteUiState.Initial
        )

    fun onTitleChange(value: String) = viewModelScope.launch {
        noteItemState.value = noteItemState.value.copy(title = value)
        noteItemValidationErrorsState.value = noteItemValidationErrorsState.value.toMutableSet()
            .apply { remove(NoteItemValidationErrors.BlankTitle) }
    }

    fun onNoteChange(value: String) = viewModelScope.launch {
        noteItemState.value = noteItemState.value.copy(note = value)
    }
}
