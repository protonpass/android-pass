package proton.android.pass.featureitemdetail.impl.note

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import proton.android.pass.clipboard.api.ClipboardManager
import proton.android.pass.common.api.onError
import proton.android.pass.common.api.onSuccess
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.composecomponents.impl.uievents.IsSentToTrashState
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.usecases.TrashItem
import proton.android.pass.featureitemdetail.impl.DetailSnackbarMessages.ItemMovedToTrash
import proton.android.pass.featureitemdetail.impl.DetailSnackbarMessages.ItemNotMovedToTrash
import proton.android.pass.featureitemdetail.impl.DetailSnackbarMessages.NoteCopiedToClipboard
import proton.android.pass.log.api.PassLogger
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.pass.domain.Item
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId
import javax.inject.Inject

@HiltViewModel
class NoteDetailViewModel @Inject constructor(
    private val clipboardManager: ClipboardManager,
    private val snackbarDispatcher: SnackbarDispatcher,
    private val encryptionContextProvider: EncryptionContextProvider,
    private val trashItem: TrashItem
) : ViewModel() {

    private val itemFlow: MutableStateFlow<Item?> = MutableStateFlow(null)
    private val isLoadingState: MutableStateFlow<IsLoadingState> =
        MutableStateFlow(IsLoadingState.NotLoading)
    private val isItemSentToTrashState: MutableStateFlow<IsSentToTrashState> =
        MutableStateFlow(IsSentToTrashState.NotSent)

    val viewState: StateFlow<NoteDetailUiState> = combine(
        itemFlow,
        isLoadingState,
        isItemSentToTrashState
    ) { item, isLoading, isItemSentToTrash ->
        item ?: return@combine NoteDetailUiState.Initial
        encryptionContextProvider.withEncryptionContext {
            NoteDetailUiState(
                title = decrypt(item.title),
                note = decrypt(item.note),
                isLoading = isLoading.value(),
                isItemSentToTrash = isItemSentToTrash.value()
            )
        }
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = NoteDetailUiState.Initial
        )

    fun setItem(item: Item) {
        itemFlow.update { item }
    }

    fun onCopyToClipboard() = viewModelScope.launch {
        withContext(Dispatchers.IO) {
            val decrypted = encryptionContextProvider.withEncryptionContext {
                val note = itemFlow.value?.note ?: return@withEncryptionContext ""
                decrypt(note)
            }
            clipboardManager.copyToClipboard(decrypted)
        }
        snackbarDispatcher(NoteCopiedToClipboard)
    }

    fun onDelete(shareId: ShareId, itemId: ItemId) = viewModelScope.launch {
        isLoadingState.update { IsLoadingState.Loading }
        trashItem(shareId = shareId, itemId = itemId)
            .onSuccess {
                isItemSentToTrashState.update { IsSentToTrashState.Sent }
                snackbarDispatcher(ItemMovedToTrash)
            }
            .onError {
                snackbarDispatcher(ItemNotMovedToTrash)
                PassLogger.d(TAG, it, "Could not delete item")
            }
        isLoadingState.update { IsLoadingState.NotLoading }
    }

    companion object {
        private const val TAG = "NoteDetailViewModel"
    }
}
