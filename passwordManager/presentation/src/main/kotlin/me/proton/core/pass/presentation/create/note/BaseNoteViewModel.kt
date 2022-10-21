package me.proton.core.pass.presentation.create.note

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.proton.android.pass.notifications.api.SnackbarMessageRepository
import me.proton.core.pass.common.api.Option
import me.proton.core.pass.domain.ItemId
import me.proton.core.pass.domain.ShareId
import me.proton.core.pass.presentation.uievents.IsLoadingState
import me.proton.core.pass.presentation.uievents.ItemSavedState

abstract class BaseNoteViewModel(
    private val snackbarMessageRepository: SnackbarMessageRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    protected val shareId: Option<ShareId> =
        Option.fromNullable(savedStateHandle.get<String>("shareId")?.let { ShareId(it) })
    protected val itemId: Option<ItemId> =
        Option.fromNullable(savedStateHandle.get<String>("itemId")?.let { ItemId(it) })

    private val shareIdState: Flow<Option<ShareId>> = MutableStateFlow(shareId)
    protected val noteItemState: MutableStateFlow<NoteItem> = MutableStateFlow(NoteItem.Empty)
    protected val isLoadingState: MutableStateFlow<IsLoadingState> =
        MutableStateFlow(IsLoadingState.NotLoading)
    protected val isItemSavedState: MutableStateFlow<ItemSavedState> =
        MutableStateFlow(ItemSavedState.Unknown)
    protected val noteItemValidationErrorsState: MutableStateFlow<Set<NoteItemValidationErrors>> =
        MutableStateFlow(emptySet())

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

    val noteUiState: StateFlow<CreateUpdateNoteUiState> = combine(
        shareIdState,
        noteItemWrapperState,
        isLoadingState,
        isItemSavedState
    ) { shareId, noteItemWrapper, isLoading, isItemSaved ->
        CreateUpdateNoteUiState(
            shareId = shareId,
            noteItem = noteItemWrapper.noteItem,
            errorList = noteItemWrapper.noteItemValidationErrors,
            isLoadingState = isLoading,
            isItemSaved = isItemSaved
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = CreateUpdateNoteUiState.Initial
        )

    fun onTitleChange(value: String) {
        noteItemState.update { it.copy(title = value) }
        noteItemValidationErrorsState.update {
            it.toMutableSet().apply { remove(NoteItemValidationErrors.BlankTitle) }
        }
    }

    fun onNoteChange(value: String) {
        noteItemState.update { it.copy(note = value) }
    }

    fun onEmitSnackbarMessage(snackbarMessage: NoteSnackbarMessage) {
        viewModelScope.launch {
            snackbarMessageRepository.emitSnackbarMessage(snackbarMessage)
        }
    }
}
