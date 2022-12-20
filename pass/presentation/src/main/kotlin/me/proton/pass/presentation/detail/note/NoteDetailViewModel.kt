package me.proton.pass.presentation.detail.note

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.proton.android.pass.clipboard.api.ClipboardManager
import me.proton.android.pass.data.api.crypto.EncryptionContextProvider
import me.proton.android.pass.notifications.api.SnackbarMessageRepository
import me.proton.pass.domain.Item
import me.proton.pass.presentation.detail.DetailSnackbarMessages
import javax.inject.Inject

@HiltViewModel
class NoteDetailViewModel @Inject constructor(
    private val clipboardManager: ClipboardManager,
    private val snackbarMessageRepository: SnackbarMessageRepository,
    private val encryptionContextProvider: EncryptionContextProvider
) : ViewModel() {

    private val itemFlow: MutableStateFlow<Item?> = MutableStateFlow(null)

    val viewState: StateFlow<NoteDetailUiState> = itemFlow.mapLatest { getUiModel(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = NoteDetailUiState.Initial
        )

    fun setItem(item: Item) {
        itemFlow.update { item }
    }

    fun onCopyToClipboard() = viewModelScope.launch {
        val decrypted = encryptionContextProvider.withEncryptionContext {
            val note = itemFlow.value?.note ?: return@withEncryptionContext ""
            decrypt(note)
        }
        clipboardManager.copyToClipboard(decrypted)
        snackbarMessageRepository.emitSnackbarMessage(DetailSnackbarMessages.NoteCopiedToClipboard)
    }

    private fun getUiModel(item: Item?): NoteDetailUiState {
        if (item == null) return NoteDetailUiState.Initial

        return encryptionContextProvider.withEncryptionContext {
            NoteDetailUiState(
                title = decrypt(item.title),
                note = decrypt(item.note)
            )
        }
    }

}
