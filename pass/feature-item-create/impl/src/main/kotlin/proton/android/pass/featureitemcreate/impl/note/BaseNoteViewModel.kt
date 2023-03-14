package proton.android.pass.featureitemcreate.impl.note

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.toOption
import proton.android.pass.commonuimodels.api.ShareUiModel
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.data.api.usecases.ObserveVaults
import proton.android.pass.featureitemcreate.impl.ItemSavedState
import proton.android.pass.log.api.PassLogger
import proton.android.pass.navigation.api.CommonNavArgId
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId

abstract class BaseNoteViewModel(
    observeVaults: ObserveVaults,
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

    private val observeAllVaultsFlow = observeVaults()
        .map { shares ->
            when (shares) {
                LoadingResult.Loading -> emptyList()
                is LoadingResult.Error -> {
                    PassLogger.e(TAG, shares.exception, "Cannot retrieve all shares")
                    emptyList()
                }
                is LoadingResult.Success -> shares.data.map { ShareUiModel(it.shareId, it.name, it.color, it.icon) }
            }
        }
        .distinctUntilChanged()

    private val sharesWrapperState = combine(
        navShareIdState,
        selectedShareIdState,
        observeAllVaultsFlow
    ) { navShareId, selectedShareId, allShares ->
        val selectedShare = allShares
            .firstOrNull { it.id == selectedShareId.value() }
            ?: allShares.firstOrNull { it.id == navShareId.value() }
            ?: allShares.first()
        SharesWrapper(allShares, selectedShare)
    }

    private data class SharesWrapper(
        val shareList: List<ShareUiModel>,
        val currentShare: ShareUiModel
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
        isItemSavedState
    ) { shareWrapper, noteItemWrapper, isLoading, isItemSaved ->
        CreateUpdateNoteUiState(
            shareList = shareWrapper.shareList,
            selectedShareId = shareWrapper.currentShare,
            noteItem = noteItemWrapper.noteItem,
            errorList = noteItemWrapper.noteItemValidationErrors,
            isLoadingState = isLoading,
            isItemSaved = isItemSaved,
            showVaultSelector = shareWrapper.shareList.size > 1
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

    fun changeVault(shareId: ShareId) = viewModelScope.launch {
        selectedShareIdState.update { shareId.toOption() }
    }

    companion object {
        private const val TAG = "BaseNoteViewModel"
    }
}
