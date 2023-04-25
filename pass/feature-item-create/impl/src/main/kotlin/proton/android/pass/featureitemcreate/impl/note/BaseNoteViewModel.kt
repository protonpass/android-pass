package proton.android.pass.featureitemcreate.impl.note

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.asLoadingResult
import proton.android.pass.common.api.getOrNull
import proton.android.pass.common.api.toOption
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.data.api.usecases.ObserveVaultsWithItemCount
import proton.android.pass.featureitemcreate.impl.ItemSavedState
import proton.android.pass.navigation.api.CommonNavArgId
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId
import proton.pass.domain.VaultWithItemCount

abstract class BaseNoteViewModel(
    observeVaults: ObserveVaultsWithItemCount,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    protected val navShareId = savedStateHandle.get<String>(CommonNavArgId.ShareId.key)
        .toOption()
        .map { ShareId(it) }
    private val navShareIdState = MutableStateFlow(navShareId)
    private val selectedShareIdState: MutableStateFlow<Option<ShareId>> = MutableStateFlow(None)

    protected val itemId: Option<ItemId> =
        savedStateHandle.get<String>(CommonNavArgId.ItemId.key)
            .toOption()
            .map { ItemId(it) }

    protected val noteItemState: MutableStateFlow<NoteItem> = MutableStateFlow(NoteItem.Empty)
    protected val isLoadingState: MutableStateFlow<IsLoadingState> =
        MutableStateFlow(IsLoadingState.NotLoading)
    protected val isItemSavedState: MutableStateFlow<ItemSavedState> =
        MutableStateFlow(ItemSavedState.Unknown)
    protected val noteItemValidationErrorsState: MutableStateFlow<Set<NoteItemValidationErrors>> =
        MutableStateFlow(emptySet())
    protected val hasUserEditedContentFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)

    private val observeAllVaultsFlow = observeVaults().distinctUntilChanged()

    private val sharesWrapperState = combine(
        navShareIdState,
        selectedShareIdState,
        observeAllVaultsFlow
    ) { navShareId, selectedShareId, allShares ->
        val selectedShare = allShares
            .firstOrNull { it.vault.shareId == selectedShareId.value() }
            ?: allShares.firstOrNull { it.vault.shareId == navShareId.value() }
            ?: allShares.first()
        SharesWrapper(allShares, selectedShare)
    }.asLoadingResult()

    private data class SharesWrapper(
        val vaultList: List<VaultWithItemCount>,
        val currentVault: VaultWithItemCount
    )

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
        sharesWrapperState,
        noteItemWrapperState,
        isLoadingState,
        isItemSavedState,
        hasUserEditedContentFlow
    ) { shareWrapper, noteItemWrapper, isLoading, isItemSaved, hasUserEditedContent ->
        val shares = shareWrapper.getOrNull()
        val showVaultSelector = shares?.let { it.vaultList.size > 1 } ?: false
        CreateUpdateNoteUiState(
            vaultList = shares?.vaultList ?: emptyList(),
            selectedVault = shares?.currentVault,
            noteItem = noteItemWrapper.noteItem,
            errorList = noteItemWrapper.noteItemValidationErrors,
            isLoadingState = isLoading,
            isItemSaved = isItemSaved,
            showVaultSelector = showVaultSelector,
            hasUserEditedContent = hasUserEditedContent
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = CreateUpdateNoteUiState.Initial
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

    fun changeVault(shareId: ShareId) = viewModelScope.launch {
        onUserEditedContent()
        selectedShareIdState.update { shareId.toOption() }
    }

    private fun onUserEditedContent() {
        if (hasUserEditedContentFlow.value) return
        hasUserEditedContentFlow.update { true }
    }
}
