package me.proton.android.pass.ui.detail.note

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.keystore.decrypt
import me.proton.core.pass.domain.Item
import javax.inject.Inject

@HiltViewModel
class NoteDetailViewModel @Inject constructor(
    private val cryptoContext: CryptoContext
) : ViewModel() {

    private val itemFlow: MutableStateFlow<Item?> = MutableStateFlow(null)

    val viewState: StateFlow<NoteUiModel> = itemFlow.mapLatest { getUiModel(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = getInitialState()
        )

    fun setItem(item: Item) {
        if (itemFlow.value == null) {
            itemFlow.value = item
        }
    }

    private fun getUiModel(item: Item?): NoteUiModel {
        if (item == null) return getInitialState()

        return NoteUiModel(
            title = item.title.decrypt(cryptoContext.keyStoreCrypto),
            note = item.note.decrypt(cryptoContext.keyStoreCrypto)
        )
    }

    private fun getInitialState(): NoteUiModel =
        NoteUiModel(
            title = "",
            note = ""
        )

    data class NoteUiModel(
        val title: String,
        val note: String
    )
}
