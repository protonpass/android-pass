package proton.android.pass.featurecreateitem.impl.note

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
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.featurecreateitem.impl.IsSentToTrashState
import proton.android.pass.featurecreateitem.impl.ItemSavedState
import proton.android.pass.notifications.api.SnackbarMessageRepository
import proton.android.pass.common.api.Option
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId

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
    protected val isSentToTrashState: MutableStateFlow<IsSentToTrashState> =
        MutableStateFlow(IsSentToTrashState.NotSent)

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
        isItemSavedState,
        isSentToTrashState
    ) { shareId, noteItemWrapper, isLoading, isItemSaved, isSentToTrash ->
        CreateUpdateNoteUiState(
            shareId = shareId,
            noteItem = noteItemWrapper.noteItem,
            errorList = noteItemWrapper.noteItemValidationErrors,
            isLoadingState = isLoading,
            isItemSaved = isItemSaved,
            isSentToTrash = isSentToTrash
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

    fun onEmitSnackbarMessage(snackbarMessage: NoteSnackbarMessage) =
        viewModelScope.launch {
            snackbarMessageRepository.emitSnackbarMessage(snackbarMessage)
        }
}
